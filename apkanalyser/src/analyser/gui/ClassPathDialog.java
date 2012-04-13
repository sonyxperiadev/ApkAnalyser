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

import gui.AbstractMainFrame;
import gui.PathListPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.l2fprod.common.swing.JDirectoryChooser;

public class ClassPathDialog extends JDialog
{
    private static final long serialVersionUID = -5990095929676829206L;
    protected PathListPanel m_classpathPanel;
    protected PathListPanel m_midletPanel;
    protected AbstractMainFrame m_mainFrame;

    protected JTabbedPane m_classpathTab;
    protected JDirectoryChooser m_sdkChooser;
    protected File m_sdkFolder;
    protected JButton m_buttonOk;
    protected JButton m_buttonCancel;
    protected JPanel m_sdkPanel;
    protected JLabel m_sdkName;

    private static final String INVALID_ANDROIDSDK_PATH = "Invalid path, please point to SDK/platforms/android-XX";

    /**
     * @param owner
     * @param title
     * @throws java.awt.HeadlessException
     */
    public ClassPathDialog(AbstractMainFrame owner, String title) throws HeadlessException
    {
        super(owner, title);
        m_mainFrame = owner;
        initGui();
    }

    protected void initGui()
    {
        m_classpathPanel = new PathListPanel(m_mainFrame, true)
        {
            private static final long serialVersionUID = 5126338107317282302L;

            @Override
            protected String getFileListTitleText()
            {
                return "Add zip/jar/dex/odex/apk or class directory";
            }

            @Override
            protected String getFilterSuffix()
            {
                return "zip;jar;dex;odex;apk";
            }

            @Override
            protected String getFilterDescription()
            {
                return ".dex/.odex/.zip/.jar/.apk file or class directory";
            }
        };
        m_classpathPanel.setMinimumSize(new Dimension(400, 350));
        //m_classpathPanel.setBorder(BorderFactory.createTitledBorder("Classpaths"));

        if (Settings.getAndroidSDK() != null) {
            m_sdkFolder = new File(Settings.getAndroidSDK());
            m_sdkChooser = new JDirectoryChooser(m_sdkFolder);
            m_sdkName = new JLabel(m_sdkFolder.getPath());
        } else {
            m_sdkFolder = null;
            m_sdkChooser = new JDirectoryChooser(new File(Settings.getDefaultPath()));
            m_sdkName = new JLabel(INVALID_ANDROIDSDK_PATH);
        }

        m_sdkChooser.setControlButtonsAreShown(false);
        m_sdkChooser.setMultiSelectionEnabled(false);
        m_sdkChooser.setSelectedFile(m_sdkFolder);

        m_sdkChooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    File folder = (File) evt.getNewValue();
                    if (isValidAndroidSDKFolder(folder)) {
                        m_sdkFolder = folder;
                        m_sdkName.setText(folder.getPath());
                        m_buttonOk.setEnabled(true);
                    } else {
                        m_sdkFolder = null;
                        m_sdkName.setText(INVALID_ANDROIDSDK_PATH);
                        m_buttonOk.setEnabled(false);
                    }
                }
            }
        });

        JPanel jp = new JPanel();
        jp.add(m_sdkName);
        jp.setBorder(BorderFactory.createEtchedBorder());

        m_sdkPanel = new JPanel(new BorderLayout());
        m_sdkPanel.add(jp, BorderLayout.NORTH);
        m_sdkPanel.add(m_sdkChooser, BorderLayout.CENTER);

        m_classpathTab = new JTabbedPane(JTabbedPane.TOP);
        m_classpathTab.addTab("Classpaths", m_classpathPanel);
        m_classpathTab.addTab("Android SDK", m_sdkPanel);

        m_classpathTab.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                m_buttonOk.setEnabled(m_classpathTab.getSelectedComponent() == m_classpathPanel
                        || isValidAndroidSDKFolder(m_sdkFolder));
            }

        });

        m_midletPanel = new PathListPanel(m_mainFrame)
        {
            private static final long serialVersionUID = 5331505988001756380L;

            @Override
            protected String getFileListTitleText()
            {
                return "Add midlet or APK or directory";
            }

            @Override
            protected String getFilterSuffix()
            {
                return "jar;apk";
            }

            @Override
            protected String getFilterDescription()
            {
                return "MIDlet or APK";
            }
        };
        m_midletPanel.setMinimumSize(new Dimension(400, 350));
        m_midletPanel.setBorder(BorderFactory.createTitledBorder("MIDlets or APK"));

        JPanel pathPanel = new JPanel();
        pathPanel.setLayout(new GridLayout(1, 2));
        pathPanel.add(m_classpathTab);
        pathPanel.add(m_midletPanel);

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        m_buttonOk = new JButton("OK");
        m_buttonCancel = new JButton("Cancel");

        m_buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                apply();
                close();
            }
        });
        m_buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        });

        buttons.add(m_buttonOk);
        buttons.add(m_buttonCancel);

        if (Settings.getUseAndroidSDK()) {
            m_classpathTab.setSelectedComponent(m_sdkPanel);
            if (!isValidAndroidSDKFolder(m_sdkFolder)) {
                m_buttonOk.setEnabled(false);
            }
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pathPanel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        m_classpathPanel.setPaths(Settings.getSelectableClasspath());
        m_midletPanel.setPaths(Settings.getSelectableMidletsPath());
    }

    protected void apply()
    {
        if (m_sdkFolder != null) {
            Settings.setAndroidSDK(m_sdkFolder.getPath());
        }
        Settings.setUseAndroidSDK(m_classpathTab.getSelectedComponent() == m_sdkPanel);
        Settings.setClasspath(m_classpathPanel.getPaths());
        Settings.setMidletsPath(m_midletPanel.getPaths());
    }

    protected void close()
    {
        setVisible(false);
    }

    private boolean isValidAndroidSDKFolder(File folder) {
        if (folder == null
                || folder.listFiles() == null
                || folder.listFiles().length == 0) {
            return false;
        }

        boolean valid = false;
        File[] sub = folder.listFiles();

        for (int i = 0; sub != null && i < sub.length; i++)
        {
            if (sub[i].isFile() && sub[i].getName().equals("android.jar")) {
                valid = true;
            }
        }
        return valid;
    }

}