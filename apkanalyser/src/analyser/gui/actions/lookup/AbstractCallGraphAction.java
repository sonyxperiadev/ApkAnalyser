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
import gui.graph.DefaultGraphPainter;
import gui.graph.Graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import analyser.gui.MAGraphPanel;
import analyser.gui.MainFrame;
import analyser.gui.RefNode;
import analyser.gui.Selection;
import analyser.gui.Settings;
import analyser.logic.InvSnooper;
import analyser.logic.RefMethod;

import mereflect.MEMethod;

public abstract class AbstractCallGraphAction extends AbstractCanceableAction
{
    private static final long serialVersionUID = -8290534477020922137L;

    protected static AbstractCallGraphAction m_inst = null;

    boolean isMirrored;

    void initialize(boolean mirrored) {
        isMirrored = mirrored;
    }

    protected AbstractCallGraphAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        MainFrame mainFrame = (MainFrame) getMainFrame();
        Object oRef = Selection.getSelectedObject();
        if (oRef == null || !(oRef instanceof RefMethod)) {
            return;
        }
        mainFrame.actionFinished(this);
        MEMethod method = ((RefMethod) oRef).getMethod();
        RefNode root = new RefNode(new InvSnooper.Invokation(null, null, method.getMEClass(), method, 0), 0);
        root.setPopuplated(true);

        List<InvSnooper.Invokation> calls = getReferences(method, method.isAbstract());
        for (int i = 0; i < calls.size(); i++) {
            InvSnooper.Invokation inv = calls.get(i);
            root.add(new RefNode(inv, inv.flags));
        }
        Graph graph = new Graph(root);
        DefaultGraphPainter graphPainter = new DefaultGraphPainter(graph);

        MAGraphPanel gPanel = new MAGraphPanel((MainFrame) getMainFrame(), graphPainter, isMirrored, this);
        gPanel.setBackground(Color.white);
        JDialog dialog = new JDialog(getMainFrame(), getTitleGraphType() + " graph for " + oRef);
        JScrollPane scrollPane = new JScrollPane(gPanel);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);

        int w = 800;
        int h = 600;

        if (Settings.getGraphWindowWidth() > 0 && Settings.getGraphWindowHeight() > 0) {
            w = Settings.getGraphWindowWidth();
            h = Settings.getGraphWindowHeight();
        }

        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Component c = e.getComponent();
                Settings.setGraphWindowWidth(c.getWidth());
                Settings.setGraphWindowHeight(c.getHeight());
            }
        });

        dialog.setSize(w, h);
        int x = Math.max(0, mainFrame.getLocationOnScreen().x + (mainFrame.getWidth() - dialog.getWidth()) / 2);
        int y = Math.max(0, mainFrame.getLocationOnScreen().y + (mainFrame.getHeight() - dialog.getHeight()) / 2);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        dialog.setModal(false);
        dialog.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        gPanel.revalidate();
        gPanel.repaint();
    }

    public abstract List<InvSnooper.Invokation> getReferences(MEMethod m, boolean virtual) throws Throwable;

    @Override
    public void handleThrowable(Throwable t)
    {
        t.printStackTrace();
        getMainFrame().showError("Error during local call look up", t);
    }

    @Override
    public String getWorkDescription()
    {
        return "Opening call graph";
    }

    abstract String getTitleGraphType();
}