/*
 * Copyright (C) 2012 Sony Mobile Communications AB
 *
 * This file is part of ApkAnalyser.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package analyser.gui.actions.lookup;

import gui.actions.AbstractCanceableAction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import analyser.gui.ClassTreeRenderer;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefMethod;

import mereflect.CollaborateClassContext;
import mereflect.CorruptBytecodeException;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEMethod;

public class FindNativeCallsAction extends AbstractCanceableAction {
    private static final long serialVersionUID = -858370226885686630L;
    protected static FindNativeCallsAction m_inst = null;
    protected List<ArrayList<MEMethod>> m_result;
    protected TraceDialog m_dialog = new TraceDialog();

    public static FindNativeCallsAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new FindNativeCallsAction("Find native calls", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected FindNativeCallsAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        m_result = new ArrayList<ArrayList<MEMethod>>();

        Object mRef = Selection.getSelectedObject();
        if (mRef == null || !(mRef instanceof RefMethod)) {
            return;
        }

        MEMethod mMethod = ((RefMethod) mRef).getMethod();

        CollaborateClassContext refCtx = MainFrame.getInstance().getResolver().getReferenceContext();
        CollaborateClassContext ctx = new CollaborateClassContext();
        ctx.addContext(refCtx);
        ctx.addContext(mMethod.getMEClass().getResource().getContext());
        recurseInvokations(ctx, new HashSet<MEMethod>(), new ArrayList<MEMethod>(), mMethod);
        if (isRunning()) {
            getMainFrame().actionFinished(this);
            showResult();
        }
    }

    protected void recurseInvokations(MEClassContext ctx, Set<MEMethod> resolved,
            List<MEMethod> callStack, MEMethod mMethod) throws IOException {
        if (resolved.contains(mMethod)) {
            return;
        }
        resolved.add(mMethod);
        callStack.add(mMethod);

        List<MEMethod.Invokation> invokations = null;
        Iterator<MEMethod.Invokation> iI = null;
        try {
            invokations = mMethod.getInvokations();
            iI = invokations.iterator();
        } catch (CorruptBytecodeException cbe) {
        }
        while (isRunning() && iI != null && iI.hasNext()) {
            MEMethod.Invokation invok = iI.next();
            // int count = ((Integer) invokations.get(invok)).intValue();
            try {
                //MEClass rClass = ctx.getMEClass(invok.invClassname);
                MEClass rClass = mMethod.getMEClass().getResource().getContext().getMEClass(invok.invClassname);
                MEMethod rMethod = rClass.getMethod(invok.invMethodname, invok.invDescriptor);
                if (rMethod == null) {
                    throw new ClassNotFoundException("Method " + invok.invMethodname + ":"
                            + invok.invDescriptor + " not found in class " + rClass.getName());
                }
                if (rMethod.isNative()) {
                    callStack.add(rMethod);
                    reportNative(rMethod, callStack);
                    callStack.remove(rMethod);
                } else {
                    recurseInvokations(ctx, resolved, callStack, rMethod);
                }
            } catch (ClassNotFoundException e2) {
            }
        }
        callStack.remove(mMethod);
    }

    protected void reportNative(MEMethod nativeMethod, List<MEMethod> callStack) {
        m_result.add(new ArrayList<MEMethod>(callStack));
    }

    protected void showResult() {
        MainFrame mainFrame = (MainFrame) getMainFrame();
        if (m_result.size() > 0) {
            m_dialog.setSize(700, 500);
            int x = mainFrame.getLocationOnScreen().x
                    + (mainFrame.getWidth() - m_dialog.getWidth()) / 2;
            int y = mainFrame.getLocationOnScreen().y
                    + (mainFrame.getHeight() - m_dialog.getHeight()) / 2;
            m_dialog.setLocation(x, y);
            m_dialog.init();
            m_dialog.setVisible(true);
            mainFrame.initBottomInfo();
        } else {
            mainFrame.setBottomInfo("No native invokations found");
        }
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error resolving native invokations", t);
    }

    @Override
    public String getWorkDescription() {
        return "Resolving native invokations";
    }

    class TraceDialog extends JDialog {
        private static final long serialVersionUID = -4423231097233811785L;
        protected JTree m_tree = new JTree();

        public TraceDialog() {
            super(MainFrame.getInstance(), "Native invokations trace", false);
            initGui();
        }

        protected void initGui() {
            m_tree.setRootVisible(false);
            m_tree.setShowsRootHandles(true);
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
                private static final long serialVersionUID = -514794659033069799L;
                protected boolean m_top = false;

                @Override
                public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean exp, boolean leaf, int row, boolean focused) {
                    m_top = !leaf;
                    return super.getTreeCellRendererComponent(tree, value, sel, exp,
                            leaf, row, focused);
                }

                @Override
                public void paint(Graphics g) {
                    if (!m_top) {
                        setForeground(Color.GRAY);
                    }
                    super.paint(g);
                }
            };
            renderer.setOpenIcon(ClassTreeRenderer.ICON_PUBLICMETHOD);
            renderer.setClosedIcon(ClassTreeRenderer.ICON_PUBLICMETHOD);
            renderer.setLeafIcon(null);
            m_tree.setCellRenderer(renderer);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(new JScrollPane(m_tree), BorderLayout.CENTER);
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TraceDialog.this.setVisible(false);
                }
            });
            buttonPanel.add(closeButton);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        }

        public void init() {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                    "Native invokations");
            for (int i = 0; i < m_result.size(); i++) {
                List<MEMethod> callStack = m_result.get(i);
                int callStackSize = callStack.size();
                if (callStackSize > 1) {
                    MEMethod nativeCall = callStack.get(callStackSize - 1);
                    DefaultMutableTreeNode nativeNode =
                            new DefaultMutableTreeNode(
                                    nativeCall.getMEClass().getName() + "." +
                                            nativeCall.getName() + "(" +
                                            nativeCall.getArgumentsString() + ")");
                    for (int j = callStackSize - 2; j >= 0; j--) {
                        MEMethod subCall = callStack.get(j);
                        DefaultMutableTreeNode subCallNode = new DefaultMutableTreeNode(
                                subCall.getMEClass().getName() + "." +
                                        subCall.getName() + "(" +
                                        subCall.getArgumentsString() + ")");
                        nativeNode.add(subCallNode);
                    }
                    root.add(nativeNode);
                }
            }
            m_tree.setModel(new DefaultTreeModel(root));
        }
    }
}
