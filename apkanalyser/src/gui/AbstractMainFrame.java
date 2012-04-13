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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import analyser.gui.LineBuilder;
import analyser.gui.Settings;
import analyser.gui.TextDialog;
import analyser.logic.Resolver;


public abstract class AbstractMainFrame extends JFrame implements WindowListener
{
    private static final long serialVersionUID = 30185815714572237L;
    protected JLabel m_bottomInfo = new JLabel();
    protected Resolver m_resolver;
    protected Map<AbstractCanceableAction, ProgressDialog> m_progressBars = new HashMap<AbstractCanceableAction, ProgressDialog>();

    protected AbstractMainFrame()
    {
        super();
    }

    public void actionStarted(AbstractCanceableAction action)
    {
        ProgressDialog pd = m_progressBars.get(action);
        if (pd == null)
        {
            pd = new ProgressDialog(this, action, action.getWorkDescription());
            int x = getLocationOnScreen().x + (getWidth() - ProgressDialog.WIDTH) / 2;
            int y = getLocationOnScreen().y + (getHeight() - ProgressDialog.HEIGHT) / 2;
            pd.setLocation(x, y);
            pd.setVisible(true);
            m_progressBars.put(action, pd);
            Thread.yield();
        }
    }

    public void actionCancelled(AbstractCanceableAction action)
    {
        actionFinished(action);
    }

    public void actionFinished(AbstractCanceableAction action)
    {
        ProgressDialog pd = m_progressBars.get(action);
        if (pd != null)
        {
            pd.setVisible(false);
            pd.dispose();
            m_progressBars.put(action, null);
        }
    }

    public void actionReportWork(AbstractCanceableAction action, int percentage)
    {
        ProgressDialog pd = m_progressBars.get(action);
        if (pd != null)
        {
            pd.reportWork(percentage);
            Thread.yield();
        }
    }

    public void initialize(int width, int height) throws HeadlessException
    {
        setTitle(getStaticTitle());
        setup(width, height);
    }

    protected String getStaticTitle()
    {
        return Settings.getApplicationName();
    }

    public void setAppendedTitle(String append)
    {
        setTitle(getStaticTitle() + " " + append);
    }

    protected void setup(int width, int height)
    {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createMenuBar(), BorderLayout.NORTH);
        getContentPane().add(m_bottomInfo, BorderLayout.SOUTH);
        initBottomInfo();
        m_bottomInfo.setForeground(Color.DARK_GRAY);

        JPanel buttons = createButtonBar();

        if (buttons == null)
        {
            setupGui(getContentPane(), width, height);
        }
        else
        {
            JPanel subContent = new JPanel();
            subContent.setLayout(new BorderLayout());
            subContent.add(buttons, BorderLayout.NORTH);
            getContentPane().add(subContent, BorderLayout.CENTER);
            setupGui(subContent, width, height - buttons.getHeight());
        }
    }

    protected abstract void setupGui(Container pane, int width, int height);

    public int showWarning(String header, String text, int optionType)
    {
        return JOptionPane.showConfirmDialog(this, text, header, optionType,
                JOptionPane.WARNING_MESSAGE);
    }

    public void showInformation(String header, String text) {
        JOptionPane.showMessageDialog(this, text, header, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(Component c, String header, String text)
    {
        JOptionPane
                .showMessageDialog(c, text, header, JOptionPane.ERROR_MESSAGE);
    }

    public void showError(String header, String text)
    {
        showError(this, text, header);
    }

    public void showError(String header, Throwable e) {
        showError(this, header, ((e instanceof AppException) ? "" : e.getClass().getName() + ": ") + e.getMessage());
    }

    public File selectFile(String title, String buttonText)
    {
        return selectFile(title, buttonText, null, null, false, false);
    }

    public File selectFile(String title, String buttonText, String fileSuffix,
            String filterDesc)
    {
        return selectFile(title, buttonText, fileSuffix, filterDesc, false, false);
    }

    public File selectFile(String title, String buttonText, boolean filesOnly,
            boolean directoriesOnly)
    {
        return selectFile(title, buttonText, null, null, filesOnly, directoriesOnly);
    }

    public File selectFile(String title, String buttonText,
            final String filterSuffix, final String filterDescr,
            final boolean filesOnly, final boolean directoriesOnly)
    {
        JFileChooser chooser = new JFileChooser(new File(Settings.getDefaultPath()));
        int fselMode = JFileChooser.FILES_AND_DIRECTORIES;
        if (filesOnly) {
            fselMode = JFileChooser.FILES_ONLY;
        } else if (directoriesOnly) {
            fselMode = JFileChooser.DIRECTORIES_ONLY;
        }
        chooser.setFileSelectionMode(fselMode);
        if (filterSuffix != null)
        {
            chooser.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    return (f.isDirectory() ||
                    f.getName().toLowerCase().endsWith(filterSuffix.toLowerCase())
                    );
                }

                @Override
                public String getDescription()
                {
                    return filterDescr;
                }
            });
        }
        chooser.setApproveButtonText(buttonText);
        chooser.setDialogTitle(title);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File f = chooser.getSelectedFile();
            if (f.isDirectory()) {
                Settings.setDefaultPath(f.getAbsolutePath());
            } else {
                Settings.setDefaultPath(f.getParentFile().getAbsolutePath());
            }
            return f;
        } else {
            return null;
        }
    }

    public File[] selectFiles(String title, String buttonText,
            final String filterSuffix, final String filterDescr,
            final boolean filesOnly, final boolean directoriesOnly)
    {
        JFileChooser chooser = new JFileChooser(new File(Settings.getDefaultPath()));
        int fselMode = JFileChooser.FILES_AND_DIRECTORIES;
        if (filesOnly) {
            fselMode = JFileChooser.FILES_ONLY;
        } else if (directoriesOnly) {
            fselMode = JFileChooser.DIRECTORIES_ONLY;
        }
        chooser.setFileSelectionMode(fselMode);
        chooser.setMultiSelectionEnabled(true);
        if (filterSuffix != null)
        {
            chooser.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    boolean accept = f.isDirectory();

                    String[] suffixes = filterSuffix.split(";");
                    for (String suffix : suffixes) {
                        if (accept == false
                                && f.getName().toLowerCase().endsWith(suffix.toLowerCase())) {
                            accept = true;
                            break;
                        }
                    }

                    return accept;
                }

                @Override
                public String getDescription()
                {
                    return filterDescr;
                }
            });
        }
        chooser.setApproveButtonText(buttonText);
        chooser.setDialogTitle(title);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File[] f = chooser.getSelectedFiles();
            if (f != null && f.length > 0) {
                if (f[0].isDirectory()) {
                    Settings.setDefaultPath(f[0].getAbsolutePath());
                } else {
                    Settings.setDefaultPath(f[0].getParentFile().getAbsolutePath());
                }
            }
            return f;
        } else {
            return null;
        }
    }

    protected abstract JMenuBar createMenuBar();

    protected abstract JPanel createButtonBar();

    public void exit()
    {
        System.exit(0);
    }

    public void initBottomInfo()
    {
        m_bottomInfo.setText("Ready");
    }

    public void setBottomInfo(String s)
    {
        m_bottomInfo.setText(s);
    }

    public int showDialog(String title, String message, int options)
    {
        return JOptionPane.showConfirmDialog(this, message, title, options,
                JOptionPane.QUESTION_MESSAGE);
    }

    int tX;
    int tY;

    void initTextDialog(TextDialog tw)
    {
        int w = 900;
        int h = 700;

        if (Settings.getTextWindowWidth() > 0 && Settings.getTextWindowHeight() > 0) {
            w = Settings.getTextWindowWidth();
            h = Settings.getTextWindowHeight();
        }

        tw.setSize(w, h);
        int x = Math.max(0, getLocationOnScreen().x + (getWidth() - w) / 2);
        int y = Math.max(0, getLocationOnScreen().y + (getHeight() - h) / 2);
        tw.setLocation(x + tX, y + tY);
        tw.setVisible(true);
        tw.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Component c = e.getComponent();
                Settings.setTextWindowWidth(c.getWidth());
                Settings.setTextWindowHeight(c.getHeight());
            }
        });
        tX += 10;
        tX &= 0x3f;
        tY += 22;
        tY &= 0x3f;
    }

    public void showText(String title, String text)
    {
        TextDialog tw = new TextDialog(this, title, text);
        initTextDialog(tw);
    }

    public TextDialog showText(String title, LineBuilder lb)
    {
        TextDialog tw = new TextDialog(this, title, lb);
        initTextDialog(tw);
        return tw;
    }

    public void showText(String title, String text, JButton[] customButtons)
    {
        TextDialog tw = new TextDialog(this, title, text, customButtons);
        initTextDialog(tw);
    }

    public TextDialog showText(String title, LineBuilder lb, JButton[] customButtons)
    {
        TextDialog tw = new TextDialog(this, title, lb, customButtons);
        initTextDialog(tw);
        return tw;
    }

    // WindowListener Impl
    @Override
    public void windowClosing(WindowEvent e)
    {
        boolean exit = true;
        try
        {
            Settings.setMainFrameX((int) getLocationOnScreen().getX());
            Settings.setMainFrameY((int) getLocationOnScreen().getY());
            Settings.setMainFrameHeight(getHeight());
            Settings.setMainFrameWidth(getWidth());
            saveSettingsOnClose();
            Settings.store();
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            exit = showWarning("Warning!",
                    "Could not save settings file.\nProceed anyway?",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
        }
        if (exit)
        {
            dispose();
            exit();
        }
    }

    protected abstract void saveSettingsOnClose() throws IOException;

    @Override
    public void windowClosed(WindowEvent e)
    {
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
    }

    String[] breakString(String s, String separator)
    {
        StringTokenizer st = new StringTokenizer(s, separator);
        List<String> res = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
            res.add(st.nextToken());
        }
        return res.toArray(new String[res.size()]);
    }
}
