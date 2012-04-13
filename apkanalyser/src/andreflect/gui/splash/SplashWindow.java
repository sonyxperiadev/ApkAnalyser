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

package andreflect.gui.splash;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SplashWindow extends JFrame {

    private static final long serialVersionUID = 9090438525613758648L;

    private static SplashWindow m_instance;

    private boolean m_stop = false;
    private static boolean m_canDispose = false;
    private float m_alpha = 0.7f;
    private final Image m_image;

    private SplashWindow(Image image) {
        super();
        m_image = image;
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(image));
        this.add(label);    
        setUndecorated(true);
        setAlwaysOnTop(true);
        pack();
        setLocationRelativeTo(null);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m_stop = true;
            }
        });
    }

    public static void splash(URL imageURL) {
        if (m_instance == null && imageURL != null) {
            m_instance = new SplashWindow(Toolkit.getDefaultToolkit().createImage(imageURL));
            m_instance.setVisible(true);
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_IN, m_alpha >= 1.0f ? 1.0f : m_alpha));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(m_image, 0, 0, this);

        m_alpha += 0.005f;

        if (!m_canDispose || (m_alpha < 1.6f && m_stop == false)) {
            repaint();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }else{
            setVisible(false);
            dispose();
        }
    }

    public static void canDispose(){
        m_canDispose = true;
    }

}
