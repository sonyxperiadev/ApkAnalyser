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

import gui.graph.DefaultGraphPainter;
import gui.graph.GraphNode;
import gui.graph.GraphPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import analyser.gui.actions.ShowBytecodeAction;
import analyser.gui.actions.bytecodemod.GraphMarkAction;
import analyser.gui.actions.lookup.AbstractCallGraphAction;
import analyser.logic.InvSnooper;

import mereflect.MEMethod;

public class MAGraphPanel extends GraphPanel {
    private static final long serialVersionUID = -4262422603167294033L;
    static final Color COL_NODE = new Color(238, 238, 255);
    MainFrame mainFrame;
    AbstractCallGraphAction gAction;
    RefNode selectedNode;
    List<InvSnooper.Invokation> callChain;

    MANodePainter defaultNodePainter;
    MANodePainter markedNodePainter;

    JPopupMenu nodePopup = new JPopupMenu();
    JPopupMenu defaultPopup = new JPopupMenu();

    JPopupMenu popup;

    boolean mirrored;

    public MAGraphPanel(MainFrame mf, DefaultGraphPainter dga, boolean isMirrored, AbstractCallGraphAction gAction) {
        super(dga);
        this.gAction = gAction;
        mainFrame = mf;
        mirrored = isMirrored;
        defaultNodePainter = new MANodePainter();
        markedNodePainter = new MANodePainter();
        defaultNodePainter.setSpacers(4, 16, 16, 4);
        defaultNodePainter.setBackground(COL_NODE);
        defaultNodePainter.setBorderColor(Color.gray);
        defaultNodePainter.setForeground(Color.blue);
        defaultNodePainter.setLineColor(Color.lightGray);
        defaultNodePainter.setFont(Font.decode("verdana"));
        markedNodePainter.setSpacers(4, 16, 16, 4);
        markedNodePainter.setBackground(Color.blue);
        markedNodePainter.setBorderColor(Color.gray);
        markedNodePainter.setForeground(Color.white);
        markedNodePainter.setLineColor(Color.gray);
        markedNodePainter.setFont(Font.decode("verdana"));

        dga.setMirrored(isMirrored);
        dga.setDefaultNodePainter(defaultNodePainter);
        dga.setBackground(Color.white);
        dga.setSpacers(8, 16, 16, 8);

        addMouseListener(new GraphPanelMouseListener());

        nodePopup.add(ShowBytecodeAction.getInstance(getMainFrame()));
        nodePopup.add(GraphMarkAction.getInstance(getMainFrame()));
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public MEMethod getSelectedMethod() {
        if (selectedNode == null) {
            return null;
        } else {
            return ((InvSnooper.Invokation) selectedNode.getUserObject()).toMethod;
        }
    }

    public List<InvSnooper.Invokation> getCallChain() {
        if (callChain != null && !mirrored) {
            List<InvSnooper.Invokation> reverseCallChain = new ArrayList<InvSnooper.Invokation>();
            for (int i = callChain.size() - 1; i >= 0; i--) {
                reverseCallChain.add(callChain.get(i));
            }
            return reverseCallChain;
        }
        return callChain;
    }

    class GraphPanelMouseListener extends MouseAdapter {
        GraphNode markedNode;

        @Override
        public void mouseClicked(MouseEvent me) {
            RefNode node = (RefNode) getPainter().getNodeAtPosition(me.getX(), me.getY());
            if (node != null) {
                Selection.setSelectedObject(MAGraphPanel.this, node.getUserObject());
            }
            if (SwingUtilities.isLeftMouseButton(me)) {
                if (node != null) {
                    toggleNodeOpened(node);
                }
            } else {
                if (popup != null) {
                    popup.setVisible(false);
                    popup = null;
                }
                if (node != null) {
                    selectedNode = node;
                    nodePopup.show(me.getComponent(), me.getX(), me.getY());
                    popup = nodePopup;
                }

                unmarkPath();
                markPath(node);
            }
            revalidate();
            repaint();
        }

        void unmarkPath() {
            if (markedNode != null) {
                GraphNode curNode = markedNode;
                while (curNode != null) {
                    curNode.setPainter(defaultNodePainter);
                    if (curNode instanceof RefNode) {
                        ((RefNode) curNode).setSelected(false);
                    }
                    curNode = curNode.getParent();
                }
                markedNode = null;
            }
            callChain = null;
        }

        void markPath(RefNode node) {
            if (node != null) {
                callChain = new ArrayList<InvSnooper.Invokation>();
                GraphNode curNode = node;
                markedNode = node;
                while (curNode != null && curNode.getUserObject() instanceof InvSnooper.Invokation) {
                    callChain.add((InvSnooper.Invokation) curNode.getUserObject());
                    curNode.setPainter(markedNodePainter);
                    if (curNode instanceof RefNode) {
                        ((RefNode) curNode).setSelected(true);
                    }
                    curNode = curNode.getParent();
                }
            }
        }

        void toggleNodeOpened(RefNode node) {
            List<GraphNode> children = node.getChildren();
            for (int cIx = 0; cIx < children.size(); cIx++) {
                RefNode refNode = (RefNode) children.get(cIx);
                if (!refNode.isPopuplated()) {
                    List<InvSnooper.Invokation> calls = null;
                    try {
                        InvSnooper.Invokation inv = ((InvSnooper.Invokation) refNode.getUserObject());
                        calls = gAction.getReferences(inv.toMethod, (inv.flags & InvSnooper.ABSTRACT) != 0 || (inv.flags & InvSnooper.OVERRIDDEN) != 0);
                    } catch (Throwable e1) {
                        e1.printStackTrace();
                    }
                    for (int i = 0; calls != null && i < calls.size(); i++) {
                        InvSnooper.Invokation inv = calls.get(i);
                        refNode.add(new RefNode(inv, inv.flags));
                    }
                    refNode.setPopuplated(true);
                }
            }

            if (getPainter().isNodeOpened(node)) {
                getPainter().closeNode(node);
            } else {
                getPainter().openNode(node);
            }
            revalidate();
            repaint();
        }
    }
}
