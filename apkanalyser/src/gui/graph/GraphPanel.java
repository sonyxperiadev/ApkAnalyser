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

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class GraphPanel extends JPanel {
    private static final long serialVersionUID = -2049765664283140032L;
    DefaultGraphPainter painter;

    public GraphPanel(DefaultGraphPainter dga) {
        super();
        painter = dga;
    }

    public DefaultGraphPainter getPainter() {
        return painter;
    }

    @Override
    public int getWidth() {
        return Math.max(super.getWidth(), painter.getWidth());
    }

    @Override
    public int getHeight() {
        return Math.max(super.getHeight(), painter.getHeight());
    }

    @Override
    public void paint(Graphics g) {
        paintComponent(g);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, super.getWidth(), super.getHeight());
        painter.paint(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d1 = super.getPreferredSize();
        Dimension d2 = painter.getDimension();
        return new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
    }

}
