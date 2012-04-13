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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import analyser.gui.ClassTree;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.Reference;
import analyser.logic.ReferredReference;
import analyser.logic.ReverseReference;


public class LookUpAction extends AbstractCanceableAction {
    private static final long serialVersionUID = -9068479725468233223L;
    protected static LookUpAction m_inst = null;
    protected static LookUpAction m_instInternal = null;
    protected int m_refs;
    protected int m_curRefs;
    protected boolean m_isExternal;

    public static LookUpAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new LookUpAction("Look up external", null);
            m_inst.setMainFrame(mainFrame);
            m_inst.m_isExternal = true;
        }
        return m_inst;
    }

    public static LookUpAction getInstanceInternal(MainFrame mainFrame) {
        if (m_instInternal == null) {
            m_instInternal = new LookUpAction("Look up internal", null);
            m_instInternal.setMainFrame(mainFrame);
            m_instInternal.m_isExternal = false;
        }
        return m_instInternal;
    }

    protected LookUpAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(ActionEvent e) throws Throwable {
        Object oRef = Selection.getSelectedObject();
        if (oRef == null /* || !(oRef instanceof RefMethod) */) {
            return;
        }
        Reference mRef = (Reference) oRef;

        //boolean thisTree = (oRef instanceof RefInvokation && ((RefInvokation) oRef).isLocal());
        ClassTree tree = (m_isExternal == false) ? ((MainFrame) getMainFrame()).getSelectedTree() : ((MainFrame) getMainFrame()).getOppositeSelectedTree();
        m_refs = mRef.getCount() * 2;
        m_curRefs = 0;
        Reference[] invs = getInvokations(mRef, tree == ((MainFrame) getMainFrame()).getMidletTree());
        if (isRunning()) {
            selectPathsInTree(tree, invs, Reference.class);
        }
        getMainFrame().setBottomInfo(invs.length + " reference(s) found");
    }

    public Reference[] getInvokations(Reference ref, boolean isMidletTree) {
        List<Reference> res = new ArrayList<Reference>();
        if (ref instanceof ReferredReference) {
            appendOneReferredReference(res, (ReferredReference) ref, isMidletTree);
        } else {
            if (ref instanceof ReverseReference) {
                ReverseReference reverse = (ReverseReference) ref;
                for (ReferredReference referred : reverse.getReferredReference(isMidletTree)) {
                    res.add(referred);
                }
            }
            Iterator<Reference> i = (ref).getChildren().iterator();
            addInvokations(res, i, isMidletTree);
        }
        return res.toArray(new Reference[res.size()]);
    }

    public void selectPathsInTree(ClassTree tree, Reference[] invs, Class<? extends Reference>... levels) {
        List<TreePath> paths = new ArrayList<TreePath>();
        tree.clearSelection();
        if (m_isExternal) {
            tree.collapseWholeTree();
        }
        for (int i = 0; i < invs.length; i++) {
            TreePath path = tree.getPath(invs[i], levels);
            //System.out.println("name " + invs[i].getName() + " treemidlet "+ (tree == ((MainFrame) getMainFrame()).getMidletTree()) + " treeref "+ (tree == ((MainFrame) getMainFrame()).getResourceTree()) );
            //System.out.println("path " + path.toString());
            if (path != null) {
                if (((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject() instanceof ReferredReference) {
                    path = path.getParentPath();
                }
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            } else {
                System.err.println("No path found for " + invs[i]);
            }
            getMainFrame().actionReportWork(this, 50 + ((45 * i) / (invs.length)));
        }
        if (isRunning()) {
            TreePath[] treePaths = paths.toArray(new TreePath[paths.size()]);
            synchronized (tree.getTreeLock()) {
                tree.markPaths(treePaths, true);
                tree.setSelectionPaths(treePaths);
            }
        }
    }

    private void appendOneReferredReference(List<Reference> res, ReferredReference referred, boolean isMidletTree) {
        Reference ref;
        if (isMidletTree) {
            ref = referred.getLocalReferredReference();
        } else {
            ref = referred.getExternalReferredReference();
        }
        if (ref != null) {
            res.add(ref);
        }
    }

    private void addInvokations(List<Reference> res, Iterator<Reference> refs, boolean isMidletTree) {
        while (isRunning() && refs.hasNext()) {
            Reference ref = refs.next();
            if (ref instanceof ReferredReference) {
                appendOneReferredReference(res, (ReferredReference) ref, isMidletTree);
            } else {
                if (ref instanceof ReverseReference) {
                    ReverseReference reverse = (ReverseReference) ref;
                    for (ReferredReference referred : reverse.getReferredReference(isMidletTree)) {
                        res.add(referred);
                    }
                }
                addInvokations(res, ref.getChildren().iterator(), isMidletTree);
            }
        }
    }

    @Override
    public void handleThrowable(Throwable t) {
        getMainFrame().showError("Error during reference look up", t);
        t.printStackTrace();
    }

    @Override
    public String getWorkDescription() {
        return "Looking up references";
    }
}