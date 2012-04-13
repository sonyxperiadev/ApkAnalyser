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

package gui;

import gui.actions.AbstractCanceableAction;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ProgressDialog extends JDialog {
    private static final long serialVersionUID = 3594281187021075670L;
    public static final int WIDTH = 250;
    public static final int HEIGHT = 100;
    protected AbstractCanceableAction action;
    protected AbstractMainFrame mainFrame;
    protected int percentage = 0;
    protected JLabel progressBar;

    public ProgressDialog(AbstractMainFrame mainFrame, String description)
            throws HeadlessException {
        super(mainFrame, description, false);
        this.mainFrame = mainFrame;
        action = null;

        progressBar = new ProgressBar();

        getContentPane().setLayout(new GridLayout(3, 1, 0, 0));
        getContentPane().add(new JLabel("  " + description));
        getContentPane().add(progressBar);
        setResizable(false);
        setUndecorated(true);
        setSize(WIDTH, HEIGHT);
    }

    public ProgressDialog(AbstractMainFrame mainFrame,
            AbstractCanceableAction action, String description)
            throws HeadlessException {
        super(mainFrame, description, false);
        this.mainFrame = mainFrame;
        this.action = action;

        progressBar = new ProgressBar();

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton m_button = new JButton("Cancel");
        buttonPane.add(m_button);
        m_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgressDialog.this.action.cancel();
            }
        });

        getContentPane().setLayout(new GridLayout(3, 1, 0, 0));
        getContentPane().add(new JLabel("  " + description));
        getContentPane().add(progressBar);
        getContentPane().add(buttonPane);
        setResizable(false);
        setUndecorated(true);
        setSize(WIDTH, HEIGHT);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    public void reportWork(int percentage) {
        this.percentage = percentage;
        repaint();
    }

    class ProgressBar extends JLabel {
        private static final long serialVersionUID = -2896176339838820010L;

        @Override
        public void paint(Graphics g) {
            int w = getWidth() - 2;
            int h = getHeight();
            g.setColor(getBackground());
            g.fillRect(1, 0, w, h);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(4, h / 4, (w - 8), h / 2);
            g.setColor(Color.WHITE);
            g.fillRect(5, h / 4 + 1, (w - 8) - 1, h / 2 - 1);
            g.setColor(Color.BLUE);
            if (percentage >= 0) {
                int barsX = 6 + ((w - 8 - 2) * percentage) / 100;
                for (int x = 6; x < barsX; x += 7) {
                    g.fillRect(x, h / 4 + 2, 6, h / 2 - 3);
                }
            } else {
                int qw = ((w - 8) - 2) >> 2;
                int p = (-percentage) % ((w - 8) - 2 + qw * 2);
                g.setClip(6, h / 4 + 2, (w - 8) - 3, h / 2 - 3);
                g.fillRect(6 + p - qw, h / 4 + 2, qw, h / 2 - 3);
            }
        }
    }
}