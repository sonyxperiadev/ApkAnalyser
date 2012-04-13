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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SettingsDialog extends JDialog {
    private static final long serialVersionUID = -4743615594951389745L;

    MainFrame mainFrame;
    JPanel mainPanel;
    JTextField ejava;
    JTextField adb;

    public SettingsDialog(MainFrame owner, String title) throws HeadlessException
    {
        super(owner, title);
        mainFrame = owner;
        initGui();
    }

    void initGui() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel allPanel = new JPanel();
        allPanel.setLayout(new GridLayout(2, 0));

        JPanel contPanel = new JPanel();
        contPanel.setBorder(BorderFactory.createTitledBorder("Midlet settings"));
        contPanel.setLayout(new GridLayout(2, 0, 4, 4));

        ejava = new JTextField();
        contPanel.add(new JLabel("ejava executable:"));
        ejava.setText(Settings.getEjavaPath());
        contPanel.add(getBrowseFieldPanel(ejava, "exe", "*.exe (ejava executable)"));

        JPanel contAndroidPanel = new JPanel();
        contAndroidPanel.setBorder(BorderFactory.createTitledBorder("Android settings"));
        contAndroidPanel.setLayout(new GridLayout(2, 0, 4, 4));

        adb = new JTextField();
        contAndroidPanel.add(new JLabel("adb executable:"));
        adb.setText(Settings.getAdbPath());
        contAndroidPanel.add(getBrowseFieldPanel(adb, "exe", "*.exe (adb executable)"));

        //allPanel.add(contPanel);
        allPanel.add(contAndroidPanel);
        mainPanel.add(allPanel, BorderLayout.NORTH);

        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton okBut = new JButton("OK");
        JButton cancelBut = new JButton("Cancel");
        bPanel.add(okBut);
        bPanel.add(cancelBut);
        mainPanel.add(bPanel, BorderLayout.SOUTH);

        okBut.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                apply();
                close();
            }
        });
        cancelBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    void apply() {
        Settings.setEjavaPath(ejava.getText());
        Settings.setAdbPath(adb.getText());
    }

    void close() {
        dispose();
    }

    JPanel getBrowseFieldPanel(final JTextField tf, final String filter, final String filterDesc) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(tf, BorderLayout.CENTER);
        JButton browseBut = new JButton("...");
        p.add(browseBut, BorderLayout.EAST);
        browseBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = mainFrame.selectFile("Select file", "OK", filter, filterDesc);
                if (f != null) {
                    tf.setText(f.getAbsolutePath());
                }
            }
        });
        return p;
    }

}
