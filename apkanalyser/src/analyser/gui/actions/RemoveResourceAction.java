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

package analyser.gui.actions;

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import analyser.gui.ClassTree;
import analyser.gui.MainFrame;
import analyser.logic.RefContext;
import analyser.logic.RefInvokation;
import analyser.logic.RefMethod;
import analyser.logic.Reference;

import mereflect.MEClassContext;

public class RemoveResourceAction extends AbstractCanceableAction
{
    private static final long serialVersionUID = 2541653496822007914L;
    protected static RemoveResourceAction m_inst = null;

    protected int m_refs;

    protected int m_curRefs;

    public static RemoveResourceAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new RemoveResourceAction("Remove resource", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected RemoveResourceAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        MainFrame mainFrame = (MainFrame) getMainFrame();
        ClassTree tree = mainFrame.getSelectedTree();
        Reference resRef = null;
        TreePath[] selPaths = tree.getSelectionPaths();
        DefaultMutableTreeNode selNode = null;
        if (selPaths != null && selPaths.length > 0)
        {
            selNode = (DefaultMutableTreeNode) selPaths[0].getLastPathComponent();
            resRef = (Reference) selNode.getUserObject();
        }
        if (resRef == null) {
            return;
        }

        // Remove invs to this resource in opposite tree
        tree = mainFrame.getOppositeSelectedTree();
        synchronized (tree.getTreeLock())
        {
            DefaultMutableTreeNode root =
                    (DefaultMutableTreeNode) tree.getModel().getRoot();
            int size = root.getChildCount();
            for (int i = 0; isRunning() && i < size; i++)
            {
                mainFrame.actionReportWork(this, (50 * (i + 1)) / size);
                recurseFind(tree, (DefaultMutableTreeNode) root.getChildAt(i),
                        (RefContext) resRef);
            }
        }

        // Remove invokations
        synchronized (tree.getTreeLock())
        {
            DefaultMutableTreeNode root =
                    (DefaultMutableTreeNode) tree.getModel().getRoot();
            int size = root.getChildCount();
            for (int i = 0; isRunning() && i < size; i++)
            {
                mainFrame.actionReportWork(this, 50 + (50 * (i + 1)) / size);
                recurseRemove(tree, (DefaultMutableTreeNode) root.getChildAt(i));
            }
        }

        // Remove resource in tree
        tree = mainFrame.getSelectedTree();
        ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(selNode);

        // Remove resource from resolver
        if (tree == MainFrame.getInstance().getMidletTree())
        {
            mainFrame.getResolver().removeMidlet(
                    ((RefContext) selNode.getUserObject()).getContext()
                    );
        }

        // Remove resource in path
        // TODO
    }

    protected int recurseFind(ClassTree tree, DefaultMutableTreeNode node,
            RefContext resRef)
    {
        if (!isRunning()) {
            return 0;
        }
        if (node.getUserObject() instanceof Reference)
        {
            Reference userRef = (Reference) node.getUserObject();
            int removed = 0;
            if (userRef instanceof RefMethod)
            {
                int size = node.getChildCount();
                for (int i = 0; i < size; i++)
                {
                    DefaultMutableTreeNode inode =
                            (DefaultMutableTreeNode) node.getChildAt(i);
                    RefInvokation refInv = (RefInvokation) inode.getUserObject();
                    MEClassContext resCtx = resRef.getContext();
                    MEClassContext invCtx = refInv.getOppositeInvokation().getContext();
                    if (invCtx.equals(resCtx))
                    {
                        ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(inode);
                        i--;
                        size--;
                        removed += refInv.getCount();
                    }
                }
            }
            else
            {
                int size = node.getChildCount();
                for (int i = 0; i < size; i++)
                {
                    removed +=
                            recurseFind(tree, (DefaultMutableTreeNode) node.getChildAt(i), resRef);
                }
            }
            userRef.setCount(userRef.getCount() - removed);
            return removed;
        }
        else
        {
            return 0;
        }
    }

    protected void recurseRemove(ClassTree tree, DefaultMutableTreeNode node)
    {
        if (!isRunning()) {
            return;
        }
        int size = node.getChildCount();
        for (int i = 0; i < size; i++)
        {
            DefaultMutableTreeNode inode =
                    (DefaultMutableTreeNode) node.getChildAt(i);
            if (inode.getUserObject() instanceof Reference)
            {
                if (((Reference) inode.getUserObject()).getCount() == 0)
                {
                    ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(inode);
                    i--;
                    size--;
                }
            }
        }
        size = node.getChildCount();
        for (int i = 0; i < size; i++)
        {
            DefaultMutableTreeNode inode =
                    (DefaultMutableTreeNode) node.getChildAt(i);
            if (inode.getUserObject() instanceof Reference)
            {
                recurseRemove(tree, inode);
            }
        }
    }

    @Override
    public void handleThrowable(Throwable t)
    {
        t.printStackTrace();
        getMainFrame().showError("Error resource removal", t);
    }

    @Override
    public String getWorkDescription()
    {
        return "Removing resource";
    }
}