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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.SwingUtilities;

public class DefaultNodePainter {
    Color foreground;
    Color background;
    Color borderColor;
    Color lineColor;
    protected Font font;
    protected FontMetrics fMetrics;

    protected int topSpacer;
    protected int leftSpacer;
    protected int rightSpacer;
    protected int bottomSpacer;

    protected void updateMetrics(Graphics g) {
        if (getMetrics() == null && g != null) {
            if (font != null) {
                fMetrics = g.getFontMetrics(font);
            } else {
                fMetrics = g.getFontMetrics();
            }
        }
    }

    protected FontMetrics getMetrics() {
        return fMetrics;
    }

    public int getChildrenIndicatorSize() {
        return 4;
    }

    protected int getBaseWidth(Graphics g, GraphNode node) {
        updateMetrics(g);
        if (getMetrics() == null) {
            return leftSpacer + rightSpacer;
        }
        return SwingUtilities.computeStringWidth(getMetrics(), node.toString()) + leftSpacer + rightSpacer;
    }

    public int getWidth(Graphics g, GraphNode node) {
        int bWidth = getBaseWidth(g, node);
        if (node.hasChildren()) {
            bWidth += getChildrenIndicatorSize();
        }
        return bWidth;
    }

    public int getHeight(Graphics g, GraphNode node) {
        updateMetrics(g);
        if (getMetrics() == null) {
            return topSpacer + rightSpacer;
        }
        return getMetrics().getHeight() + topSpacer + bottomSpacer;
    }

    public void setSpacers(int top, int left, int right, int bottom) {
        topSpacer = top;
        leftSpacer = left;
        rightSpacer = right;
        bottomSpacer = bottom;
    }

    public void paint(Graphics g, GraphNode node, boolean mirrored) {
        int w = getBaseWidth(g, node);
        int h = getHeight(g, node);
        g.setFont(font);
        fMetrics = g.getFontMetrics();
        if (node.hasChildren() && mirrored) {
            g.translate(getChildrenIndicatorSize(), 0);
        }
        paintBackground(g, node, w, h, mirrored);
        paintContent(g, node, w, h, mirrored);
        if (node.hasChildren() && mirrored) {
            g.translate(-getChildrenIndicatorSize(), 0);
        }
        g.setColor(lineColor);
        GraphNode p = node.parent;
        if (p != null && p.getUserObject() != DefaultGraphPainter.DUMMY_NODE) {
            paintLine(g, node, p, node.x, node.y, w, h, p.x, p.y, p.width, p.height, mirrored);
        }
        if (node.hasChildren()) {
            paintChildrenIndicator(g, w, h, getChildrenIndicatorSize(), mirrored);
        }
    }

    public void paintBackground(Graphics g, GraphNode node, int w, int h, boolean mirrored) {
        g.setColor(background);
        g.fillRect(0, 0, w, h);
        g.setColor(borderColor);
        g.drawRect(0, 0, w, h);
    }

    public void paintContent(Graphics g, GraphNode node, int w, int h, boolean mirrored) {
        g.setColor(foreground);
        g.drawString(node.toString(), leftSpacer, topSpacer + fMetrics.getHeight() - fMetrics.getDescent());
    }

    public void paintLine(Graphics g, GraphNode node, GraphNode parent,
            int x, int y, int w, int h,
            int px, int py, int pw, int ph, boolean mirrored) {
        if (mirrored) {
            g.drawLine(node.width + 1, h >> 1, px - x - 1, py - y + (ph >> 1));
        } else {
            g.drawLine(-1, h >> 1, px - x + pw + 1, py - y + (ph >> 1));
        }
    }

    public void paintChildrenIndicator(Graphics g, int w, int h, int size, boolean mirrored) {
        if (mirrored) {
            g.drawArc(-1, ((h - (size << 1)) >> 1), size << 1, size << 1, 90, 180);
        } else {
            g.drawArc(w - (size) + 1, ((h - (size << 1)) >> 1), size << 1, size << 1, 270, 180);
        }
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }
}
