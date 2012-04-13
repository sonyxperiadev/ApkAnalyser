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

import javax.swing.AbstractAction;
import javax.swing.Icon;

import analyser.gui.ClassPathDialog;


public class SetPathsAction extends AbstractAction
{
    private static final long serialVersionUID = 2262871959871777190L;
    protected static SetPathsAction m_inst = null;
    protected AbstractMainFrame m_mainFrame;
    protected ClassPathDialog m_dialog;

    public static SetPathsAction getInstance(AbstractMainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new SetPathsAction("Set paths", null); //, AppSettings.getSaveAsIcon());
            m_inst.m_mainFrame = mainFrame;
            m_inst.m_dialog = new ClassPathDialog(mainFrame, "Set paths");
        }
        return m_inst;
    }

    protected SetPathsAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        m_dialog.setSize(800, 500);
        int x = m_mainFrame.getLocationOnScreen().x + (m_mainFrame.getWidth() - m_dialog.getWidth()) / 2;
        int y = m_mainFrame.getLocationOnScreen().y + (m_mainFrame.getHeight() - m_dialog.getHeight()) / 2;
        m_dialog.setLocation(Math.max(0, x), Math.max(0, y));
        m_dialog.setVisible(true);
    }
}
