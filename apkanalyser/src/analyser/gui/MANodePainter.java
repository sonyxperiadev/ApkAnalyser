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

import gui.graph.FancyNodePainter;
import gui.graph.GraphNode;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.SwingUtilities;

import analyser.logic.InvSnooper;

import mereflect.MEMethod;

public class MANodePainter extends FancyNodePainter {
    static final float[] DASH_DEF = { 4.0f, 4.0f };
    static final Stroke DASH_STROKE =
            new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 2.0f, DASH_DEF, 2.0f);

    protected String sTop;
    protected String sBot;

    void breakString(GraphNode node) {
        String s = node.toString();
        int ix = s.indexOf("@");
        sTop = s;
        sBot = null;
        if (ix > -1)
        {
            sTop = s.substring(0, ix);
            sBot = s.substring(ix + 1, s.length());
        }
    }

    @Override
    protected int getBaseWidth(Graphics g, GraphNode node) {
        updateMetrics(g);
        if (getMetrics() == null) {
            return leftSpacer + rightSpacer;
        }
        breakString(node);
        int wTop = (sTop != null) ? SwingUtilities.computeStringWidth(getMetrics(), sTop) : 0;
        int wBot = (sBot != null) ? SwingUtilities.computeStringWidth(getMetrics(), sBot) : 0;
        return Math.max(wTop, wBot) + leftSpacer + rightSpacer;
    }

    @Override
    public int getHeight(Graphics g, GraphNode node) {
        updateMetrics(g);
        if (getMetrics() == null) {
            return topSpacer + rightSpacer;
        }
        return 2 * getMetrics().getHeight() + topSpacer + bottomSpacer;
    }

    @Override
    public void paintContent(Graphics g, GraphNode node, int w, int h, boolean mirrored) {
        breakString(node);
        g.setColor(getForeground());
        if (sTop != null) {
            g.drawString(sTop, leftSpacer, topSpacer + fMetrics.getHeight() - fMetrics.getDescent());
        }
        if (sBot != null) {
            g.drawString(sBot, leftSpacer, topSpacer + 2 * fMetrics.getHeight() - fMetrics.getDescent());
        }
    }

    @Override
    public void paintLine(Graphics g, GraphNode node, GraphNode parent,
            int x, int y, int w, int h,
            int px, int py, int pw, int ph, boolean mirrored) {
        Stroke oldStroke = ((Graphics2D) g).getStroke();
        InvSnooper.Invokation inv = (InvSnooper.Invokation) node.getUserObject();
        if (!inv.toMethod.isStatic() && (inv.fromMethod != null && inv.fromMethod.isAbstract() ||
                (inv.flags & InvSnooper.OVERRIDDEN) != 0 ||
                (inv.flags & InvSnooper.ABSTRACT) != 0)) {
            ((Graphics2D) g).setStroke(DASH_STROKE);
        }
        super.paintLine(g, node, parent, x, y, w, h, px, py, pw, ph, mirrored);
        ((Graphics2D) g).setStroke(oldStroke);
    }

    @Override
    public void paintBackground(Graphics g, GraphNode node, int w, int h, boolean mirrored) {
        Stroke oldStroke = ((Graphics2D) g).getStroke();
        MEMethod m = ((InvSnooper.Invokation) node.getUserObject()).toMethod;
        if (m.isAbstract()) {
            ((Graphics2D) g).setStroke(DASH_STROKE);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, w + 1, h + 1, 32, 32);
            g.setColor(getBorderColor());
            g.drawRoundRect(0, 0, w, h, 32, 32);
            ((Graphics2D) g).setStroke(oldStroke);
        } else {
            super.paintBackground(g, node, w, h, mirrored);
        }
    }
}
