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

package analyser.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import analyser.logic.RefContext;
import analyser.logic.Reference;
import andreflect.ApkClassContext;

public class ClassTree extends JTree
{
    private static final long serialVersionUID = -4687832080372737695L;
    protected Map<Class<?>, JPopupMenu> m_popups = new HashMap<Class<?>, JPopupMenu>();
    List<DefaultMutableTreeNode> markedNodes = new ArrayList<DefaultMutableTreeNode>();
    final static Color COLOR_MARK = new Color(164, 232, 255);

    Map<Class<?>, JPopupMenu> m_apk_popups = new HashMap<Class<?>, JPopupMenu>();

    public void registerApkPopup(Class<?> nodeClass, JPopupMenu popup)
    {
        m_apk_popups.put(nodeClass, popup);
    }

    public void registerPopup(Class<?> nodeClass, JPopupMenu popup)
    {
        m_popups.put(nodeClass, popup);
    }

    public void markPaths(TreePath[] paths, boolean resetPreviousMarkings) {
        if (resetPreviousMarkings) {
            markedNodes.clear();
        }
        for (int i = 0; i < paths.length; i++) {
            Object[] path = paths[i].getPath();
            for (int j = 0; j < path.length; j++) {
                markedNodes.add(((DefaultMutableTreeNode) path[j]));
            }
        }
        repaint();
        invalidate();
    }

    public boolean isMarked(DefaultMutableTreeNode o) {
        return markedNodes.contains(o);
    }

    public void setSelected(boolean s)
    {
        if (s)
        {
            ((ClassTreeRenderer) getCellRenderer()).setBackgroundSelectionColor(Color.LIGHT_GRAY);
        }
        else
        {
            ((ClassTreeRenderer) getCellRenderer()).setBackgroundSelectionColor(COLOR_MARK);
        }
    }

    public void refreshSelectedNode()
    {
        TreePath[] selPaths = getSelectionPaths();
        if (selPaths != null && selPaths.length > 0)
        {
            expandPath(selPaths[0]);
            collapse((DefaultMutableTreeNode) selPaths[0].getLastPathComponent());
        }
    }

    public JPopupMenu getPopup(DefaultMutableTreeNode node)
    {
        Object userObj = null;
        if (node != null) {
            userObj = node.getUserObject();
        }
        if (userObj != null)
        {
            Reference ref = (Reference) userObj;
            while (!(ref instanceof RefContext)) {
                ref = (ref.getParent());
            }
            if (ref instanceof RefContext
                    && ((RefContext) ref).getContext() instanceof ApkClassContext) {
                return m_apk_popups.get(userObj.getClass());
            }
            return m_popups.get(userObj.getClass());
        }
        else
        {
            return m_popups.get(void.class);
        }
    }

    public void collapseWholeTree()
    {
        Enumeration<TreePath> expands =
                getExpandedDescendants(new TreePath(getModel().getRoot()));
        while (expands != null && expands.hasMoreElements())
        {
            TreePath path = expands.nextElement();
            int depth = path.getPathCount();
            for (int i = 0; i < depth - 1; i++)
            {
                synchronized (getTreeLock()) {
                    collapsePath(path);
                }
                path = path.getParentPath();
            }
        }
    }

    public void collapse(DefaultMutableTreeNode node)
    {
        Enumeration<TreePath> expands =
                getExpandedDescendants(new TreePath(node.getPath()));
        while (expands != null && expands.hasMoreElements())
        {
            TreePath path = expands.nextElement();
            synchronized (getTreeLock()) {
                collapsePath(path);
            }
        }
    }

    /**
     * Levels 0 - 4 where 0 == resource, 1 == package, 2 == class, 3 == method, 4 == invocation
     * @param refInv
     * @param level
     * @return
     */
    public TreePath getPath(Reference ref, Class<? extends Reference>... levels) {
        //ref = null if tree is not the target
        if (ref == null) {
            return null;
        }

        LinkedList<Reference> refPath = new LinkedList<Reference>();

        Reference parentRef = ref;
        while (parentRef != null) {
            boolean isInstance = false;
            for (Class<? extends Reference> level : levels) {
                if (level.isInstance(parentRef)) {
                    isInstance = true;
                    break;
                }
            }
            if (isInstance) {
                break;
            }

            parentRef = parentRef.getParent();
        }

        while (parentRef != null) {
            refPath.add(parentRef);
            parentRef = parentRef.getParent();
        }

        Collections.reverse(refPath);

        List<TreeNode> path = new ArrayList<TreeNode>();

        DefaultTreeModel model = (DefaultTreeModel) getModel();
        TreeNode node = (TreeNode) model.getRoot();

        Iterator<Reference> it = refPath.iterator();

        path.add(node);

        while (it.hasNext()) {
            Reference refx = it.next();
            node = findNode(node, refx);
            path.add(node);
        }

        TreePath treePath = new TreePath(path.toArray());
        return treePath;
    }

    public void findAndMarkNode(Object ref, int flags)
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
        findAndMarkNode(root, ref, flags);
    }

    protected boolean findAndMarkNode(DefaultMutableTreeNode parent, Object ref, int flags)
    {
        Object uo = parent.getUserObject();
        if (uo == null) {
            return false;
        }
        if (uo instanceof Reference && (ref.equals(((Reference) uo).getReferred()) ||
                ref.equals(uo))) {
            Reference tRef = (Reference) uo;
            tRef.setFlags(tRef.getFlags() | flags);
            return true;
        }
        if (ref.equals(uo)) {
            return true;
        }
        int size = parent.getChildCount();
        for (int i = 0; i < size; i++)
        {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) parent.getChildAt(i);
            if (findAndMarkNode(child, ref, flags))
            {
                if (uo instanceof Reference) {
                    Reference tRef = (Reference) uo;
                    tRef.setFlags(tRef.getFlags() | flags);
                }
                return true;
            }
        }
        return false;
    }

    protected TreeNode findNode(TreeNode parent, Reference ref)
    {
        if (parent == null) {
            return null;
        }
        TreeNode res = null;
        int size = parent.getChildCount();
        for (int i = 0; i < size; i++)
        {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) parent.getChildAt(i);

            if (child.getUserObject().equals(ref))
            {
                res = child;
                break;
            }
        }
        return res;
    }

    public ClassTree()
    {
        super();
    }

    /**
     * @param value
     */
    public ClassTree(Object[] value)
    {
        super(value);
    }

    /**
     * @param newModel
     */
    public ClassTree(TreeModel newModel)
    {
        super(newModel);
    }

    /**
     * @param root
     */
    public ClassTree(TreeNode root)
    {
        super(root);
    }

    /**
     * @param root
     * @param asksAllowsChildren
     */
    public ClassTree(TreeNode root, boolean asksAllowsChildren)
    {
        super(root, asksAllowsChildren);
    }
}
