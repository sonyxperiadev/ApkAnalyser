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

package analyser.gui.actions;

import gui.AbstractMainFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import analyser.gui.Settings;


public class AboutAction extends AbstractAction
{
    private static final long serialVersionUID = -8317214951025788061L;
    protected static AboutAction m_inst = null;
    protected AbstractMainFrame m_mainFrame;
    protected JDialog m_dialog;
    protected JDialog m_licenseDialog;

    private static final String ABOUT = "/about.html";
    private static final String LICENSE = "/NOTICE";

    public static AboutAction getInstance(AbstractMainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new AboutAction("About", null);
            m_inst.m_mainFrame = mainFrame;

            JEditorPane pane = new JEditorPane();
            pane.setEditable(false);
            pane.setContentType("text/html");
            pane.setText("<html><body bgcolor=#eeeeee face=\"verdana\"><h1>" + Settings.getApplicationName() + " v" + Settings.getVersion() + "</h1>" + m_inst.getAbout(ABOUT) + "</body></html>");

            m_inst.m_dialog = new JDialog(mainFrame, "About", true);
            m_inst.m_dialog.getContentPane().add(new JScrollPane(pane));

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_inst.m_dialog.dispose();
                }
            });

            JButton licenseButton = new JButton("Open Source Licenses");
            licenseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_inst.m_licenseDialog.setSize(800, 700);
                    int x = m_inst.m_mainFrame.getLocationOnScreen().x + 200;
                    int y = m_inst.m_mainFrame.getLocationOnScreen().y + 100;
                    m_inst.m_licenseDialog.setLocation(Math.max(0, x), Math.max(0, y));
                    m_inst.m_licenseDialog.setVisible(true);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            buttonPanel.add(licenseButton);
            buttonPanel.add(closeButton);

            m_inst.m_dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

            JEditorPane licensePane = new JEditorPane();
            licensePane.setEditable(false);
            licensePane.setText(m_inst.getAbout(LICENSE));
            m_inst.m_licenseDialog = new JDialog(mainFrame, "Open Source Licenses", true);
            m_inst.m_licenseDialog.getContentPane().add(new JScrollPane(licensePane));
        }
        return m_inst;
    }

    protected AboutAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        m_dialog.setSize(800, 700);
        int x = m_mainFrame.getLocationOnScreen().x + 100;
        int y = m_mainFrame.getLocationOnScreen().y + 100;
        m_dialog.setLocation(Math.max(0, x), Math.max(0, y));
        m_dialog.setVisible(true);
    }

    private String getAbout(String filename)
    {
        try
        {
            InputStream is = getClass().getResourceAsStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int d;
            while ((d = is.read()) != -1) {
                baos.write(d);
            }
            is.close();
            return baos.toString();

        } catch (Throwable t)
        {
            return "";
        }
    }
}
