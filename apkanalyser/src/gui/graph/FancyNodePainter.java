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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class FancyNodePainter extends DefaultNodePainter {
    @Override
    public void paint(Graphics g, GraphNode node, boolean mirrored) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g, node, mirrored);
    }

    @Override
    public void paintBackground(Graphics g, GraphNode node, int w, int h, boolean mirrored) {
        g.setColor(background);
        g.fillRoundRect(0, 0, w + 1, h + 1, 6, 6);
        g.setColor(borderColor);
        g.drawRoundRect(0, 0, w, h, 6, 6);
    }
}
