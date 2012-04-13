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

package gui.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DefaultGraphPainter {
    Graph graph;
    List<GraphNode> openNodes;
    DefaultNodePainter defaultNodePainter;
    Color background;
    Dimension dimension;

    int topSpacer;
    int leftSpacer;
    int rightSpacer;
    int bottomSpacer;

    List<GraphNode> preRootList;
    List<Integer> columnHeights;
    List<Integer> columnWidths;
    List<List<GraphNode>> rowNodes; // List(GraphNode)

    boolean mirrored = false;

    public static final String DUMMY_NODE = "";

    public DefaultGraphPainter(Graph graph) {
        this.graph = graph;
        GraphNode preRoot = new GraphNode(DUMMY_NODE);
        preRoot.add(graph.getRoot());
        preRootList = new ArrayList<GraphNode>();
        preRootList.add(preRoot);
        openNodes = new ArrayList<GraphNode>();
        openNodes.add(preRoot);
        defaultNodePainter = new DefaultNodePainter();
    }

    public void openNode(GraphNode node) {
        if (!openNodes.contains(node)) {
            openNodes.add(node);
        }
    }

    public void closeNode(GraphNode node) {
        openNodes.remove(node);
    }

    public boolean isNodeOpened(GraphNode node) {
        return openNodes.contains(node);
    }

    static final boolean DBG = false;

    public void paint(Graphics g) {
        GraphNode root = graph.getRoot();
        dimension = getDimension(g, root);
        g.setColor(background);
        g.fillRect(0, 0, dimension.width, dimension.height);
        Point p;
        if (isMirrored()) {
            p = new Point(dimension.width - rightSpacer, dimension.height >> 1);
        } else {
            p = new Point(leftSpacer, dimension.height >> 1);
        }
        rowNodes = new ArrayList<List<GraphNode>>();
        paintNodes(preRootList, p, g, 0);

        if (DBG) {
            int curX = 0;
            if (isMirrored()) {
                curX = getDimension().width - rightSpacer - (columnWidths.get(0)).intValue();
            }
            int curW = 0;
            int ix = 0;
            curW = (columnWidths.get(ix)).intValue();
            while (ix < columnWidths.size()) {
                g.drawRect(curX, 0, curW, getHeight());
                g.drawLine(curX, 0, curX + curW, getHeight());
                g.drawLine(curX + curW, 0, curX, getHeight());
                ix++;
                if (!isMirrored()) {
                    curX += curW;
                }
                if (ix < columnWidths.size()) {
                    curW = (columnWidths.get(ix)).intValue();
                }
                if (isMirrored()) {
                    curX -= curW;
                }
            }
        }
    }

    void paintNodes(List<GraphNode> nodes, Point p, Graphics g, int column) {
        List<GraphNode> allChildren = new ArrayList<GraphNode>();
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode node = nodes.get(i);
            if (openNodes.contains(node)) {
                allChildren.addAll((nodes.get(i)).getChildren());
            }
        }
        if (!allChildren.isEmpty()) {
            if (isMirrored()) {
                p.x -= (columnWidths.get(column)).intValue();
            }
            int y = (dimension.height - (((columnHeights.get(column)).intValue()))) >> 1;
            for (int i = 0; i < allChildren.size(); i++) {
                GraphNode n = allChildren.get(i);
                // visit node
                if (isMirrored()) {
                    paintNode(g, n, p.x + (columnWidths.get(column)).intValue() - n.width, y);
                } else {
                    paintNode(g, n, p.x, y);
                }
                int h = topSpacer + getHeight(g, n) + bottomSpacer;
                y += h;
            }
            rowNodes.add(allChildren);
            if (!isMirrored()) {
                p.x += (columnWidths.get(column)).intValue();
            }

            paintNodes(allChildren, p, g, column + 1);
        }
    }

    public int getWidth() {
        dimension = getDimension(graph.getRoot());
        return dimension.width;
    }

    public int getHeight() {
        dimension = getDimension(graph.getRoot());
        return dimension.height;
    }

    /**
     * @return
     */
    public Dimension getDimension() {
        Dimension d = getDimension(graph.getRoot());
        return d;
    }

    Dimension getDimension(GraphNode node) {
        return getDimension(null, node);
    }

    Dimension getDimension(Graphics g, GraphNode node) {
        columnWidths = new ArrayList<Integer>();
        columnHeights = new ArrayList<Integer>();
        if (openNodes.contains(node)) {
            Dimension d = new Dimension();
            findTotalDimension(g, preRootList, d);
            d.height += topSpacer + bottomSpacer;
            d.width += leftSpacer + rightSpacer;
            return d;
        } else {
            int nw = getWidth(g, node);
            int nh = getHeight(g, node);
            int w = leftSpacer + nw + rightSpacer;
            int h = topSpacer + nh + bottomSpacer;
            node.width = nw;
            node.height = nh;
            columnWidths.add(new Integer(w));
            columnHeights.add(new Integer(h));
            return new Dimension(w, h);
        }
    }

    void findTotalDimension(Graphics g, List<GraphNode> nodes, Dimension d) {
        List<GraphNode> allChildren = new ArrayList<GraphNode>();
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode node = nodes.get(i);
            if (openNodes.contains(node)) {
                allChildren.addAll((nodes.get(i)).getChildren());
            }
        }
        int maxW = leftSpacer + rightSpacer;
        int totH = 0;
        for (int i = 0; i < allChildren.size(); i++) {
            GraphNode n = allChildren.get(i);
            int nw = getWidth(g, n);
            int nh = getHeight(g, n);
            n.width = nw;
            n.height = nh;

            // visit node
            int w = leftSpacer + nw + rightSpacer;
            int h = topSpacer + nh + bottomSpacer;
            maxW = Math.max(maxW, w);
            totH += h;
        }
        if (!allChildren.isEmpty()) {
            d.width += maxW;
            columnWidths.add(new Integer(maxW));
            columnHeights.add(new Integer(totH));
            d.height = Math.max(d.height, totH);
            findTotalDimension(g, allChildren, d);
        }
    }

    int getWidth(Graphics g, GraphNode node) {
        if (node.getPainter() == null) {
            return defaultNodePainter.getWidth(g, node);
        } else {
            return node.getPainter().getWidth(g, node);
        }
    }

    int getHeight(Graphics g, GraphNode node) {
        if (node.getPainter() == null) {
            return defaultNodePainter.getHeight(g, node);
        } else {
            return node.getPainter().getHeight(g, node);
        }
    }

    void paintNode(Graphics g, GraphNode node, int x, int y) {
        DefaultNodePainter dnp = node.getPainter();
        g.translate(x, y);
        node.x = x;
        node.y = y;
        if (dnp == null) {
            defaultNodePainter.paint(g, node, isMirrored());
        } else {
            dnp.paint(g, node, isMirrored());
        }
        g.translate(-x, -y);
    }

    public GraphNode getNodeAtPosition(int x, int y) {
        GraphNode res = null;
        int curX = 0;
        if (isMirrored()) {
            curX = getDimension().width - rightSpacer - (columnWidths.get(0)).intValue();
        }
        int curW = 0;
        int ix = 0;
        curW = (columnWidths.get(ix)).intValue();
        while (!(x >= curX && x <= curX + curW) && ix < columnWidths.size() - 1) {
            ix++;
            if (!isMirrored()) {
                curX += curW;
            }
            curW = (columnWidths.get(ix)).intValue();
            if (isMirrored()) {
                curX -= curW;
            }
        }
        if (ix < rowNodes.size()) {
            List<GraphNode> children = rowNodes.get(ix);
            for (int cix = 0; res == null && cix < children.size(); cix++) {
                GraphNode cand = children.get(cix);
                if (cand.y <= y && cand.y + cand.height >= y) {
                    res = cand;
                }
            }
        }
        return res;
    }

    /**
     * @return
     */
    public DefaultNodePainter getDefaultNodePainter() {
        return defaultNodePainter;
    }

    /**
     * @param defaultNodePainter
     */
    public void setDefaultNodePainter(DefaultNodePainter defaultNodePainter) {
        this.defaultNodePainter = defaultNodePainter;
    }

    /**
     * @return
     */
    public Color getBackground() {
        return background;
    }

    /**
     * @param background
     */
    public void setBackground(Color background) {
        this.background = background;
    }

    public void setSpacers(int top, int left, int right, int bottom) {
        topSpacer = top;
        leftSpacer = left;
        rightSpacer = right;
        bottomSpacer = bottom;
    }

    /**
     * @return
     */
    public boolean isMirrored() {
        return mirrored;
    }

    /**
     * @param mirrored
     */
    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }
}
