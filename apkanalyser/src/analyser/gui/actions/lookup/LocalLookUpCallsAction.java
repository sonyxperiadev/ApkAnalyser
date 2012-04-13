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
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreePath;

import analyser.gui.ClassTree;
import analyser.gui.MainFrame;
import analyser.gui.ProgressReporter;
import analyser.gui.Selection;
import analyser.logic.InvSnooper;
import analyser.logic.RefInvokation;
import analyser.logic.RefMethod;
import analyser.logic.Reference;


public class LocalLookUpCallsAction extends AbstractCanceableAction
{
    private static final long serialVersionUID = 7174910122020609082L;
    protected static LocalLookUpCallsAction m_inst = null;
    protected int m_count;
    protected int m_curRefs;

    public static LocalLookUpCallsAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new LocalLookUpCallsAction("Look up local calls", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected LocalLookUpCallsAction(String arg0, Icon arg1)
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
        ClassTree tree = mainFrame.getSelectedTree();
        List<InvSnooper.Invokation> invokations = InvSnooper.findCalls((RefMethod) oRef, false, true, this, new ProgressReporter() {
            int total;

            @Override
            public void reportStart(int total) {
                this.total = total;
            }

            @Override
            public void reportWork(int finished) {
                getMainFrame().actionReportWork(LocalLookUpCallsAction.this, finished * 100 / total);
            }

            @Override
            public void reportEnd() {
            }
        });
        if (isRunning())
        {
            List<RefInvokation> refInvs = InvSnooper.toRefInvokations(invokations);
            RefInvokation[] invs = refInvs.toArray(new RefInvokation[refInvs.size()]);
            selectPathsInTree(tree, invs, RefMethod.class, false);
        }
        mainFrame.setBottomInfo(invokations.size() + " reference(s) found");
        mainFrame.actionFinished(this);
    }

    @SuppressWarnings("unchecked")
    public void selectPathsInTree(ClassTree tree, RefInvokation[] invs, Class<? extends Reference> level, boolean opposite)
    {
        List<TreePath> paths = new ArrayList<TreePath>();
        tree.clearSelection();
        for (int i = 0; i < invs.length; i++)
        {
            RefInvokation inv = opposite ? invs[i].getOppositeInvokation() : invs[i];
            TreePath path = tree.getPath(inv, level);
            if (path != null)
            {
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }
            else
            {
                System.err.println("No path found for " + inv);
            }
        }
        if (isRunning())
        {
            TreePath[] treePaths = paths.toArray(new TreePath[paths.size()]);
            synchronized (tree.getTreeLock())
            {
                tree.markPaths(treePaths, true);
                tree.setSelectionPaths(treePaths);
                if (treePaths.length > 0) {
                    tree.scrollPathToVisible(treePaths[0]);
                }
            }
        }
    }

    @Override
    public void handleThrowable(Throwable t)
    {
        t.printStackTrace();
        getMainFrame().showError("Error during local call look up", t);
    }

    @Override
    public String getWorkDescription()
    {
        return "Looking up local references";
    }
}