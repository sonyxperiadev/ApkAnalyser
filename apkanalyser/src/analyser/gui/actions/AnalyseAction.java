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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import analyser.gui.MainFrame;
import analyser.gui.Settings;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Resolver;


public class AnalyseAction extends AbstractAnalyseAction
{
    private static final long serialVersionUID = -2731954853275442513L;
    protected static AnalyseAction m_inst = null;

    public static AnalyseAction getInstance(AbstractMainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new AnalyseAction("Analyse", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected AnalyseAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        if (Settings.getConfirmedBreaking() == false
                && askConfirmBreaking() == false) {
            return;
        }

        Resolver r = new Resolver();
        MainFrame.getInstance().setResolver(r);
        r.setListener(AnalyseAction.getInstance(getMainFrame()));
        try
        {
            r.resolve(this);
            if (isRunning())
            {
                buildTrees(r);
                BytecodeModificationMediator.getInstance().unregisterAllModifications();

                MainFrame.getInstance().showContent(r.getMidletResources().iterator().next());
            }
        } catch (IllegalArgumentException iae)
        {
            iae.printStackTrace();
            MainFrame.getInstance().showError("Path configuration error", "Classpaths or midlets path are misconfigured.\n" + iae.getMessage());
            SetPathsAction.getInstance(MainFrame.getInstance()).actionPerformed(e);
        }

        MainFrame.getInstance().initBottomInfo();
    }

    @Override
    public String getWorkDescription()
    {
        return "Analyzing ";
    }

    private boolean askConfirmBreaking() {
        boolean ret = false;
        final String message = "ApkAnalyser will disassemble the application and then you could modify it by a set of pre-defined Dalvik bytecode modifications. \n"
                + "These behaviors may break APK's Software License about modification and End User License Agreement about reverse engineering. \n"
                + "The author of ApkAnalyser won't take any responsibility for damaged content or the breaking of APK's licenses and agreements. \n"
                + "Check the license and EULA of your APK file(s) before you continue. \n\n"
                + "Are you sure to continue ?\n\n";
        JCheckBox checkbox = new JCheckBox("Do not show this dialog again.");

        Object[] params = { message, checkbox };

        final JOptionPane optionPane = new JOptionPane(
                params,
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.YES_NO_OPTION);

        final JDialog dialog = new JDialog(getMainFrame(), "Confirmation of licenses and agreements", true);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();

                        if (dialog.isVisible()
                                && (e.getSource() == optionPane)
                                && (JOptionPane.VALUE_PROPERTY.equals(prop))) {
                            dialog.setVisible(false);
                        }
                    }
                });
        dialog.pack();
        dialog.setLocationRelativeTo(getMainFrame());
        dialog.setVisible(true);

        int value = ((Integer) optionPane.getValue()).intValue();
        if (value == JOptionPane.YES_OPTION) {
            ret = true;
            Settings.setConfirmedBreaking(checkbox.isSelected());
        }

        return ret;
    }
}