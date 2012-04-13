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

import analyser.gui.MainFrame;
import analyser.gui.SettingsDialog;


public class SettingsAction extends AbstractAction
{
    private static final long serialVersionUID = 4892030587730573193L;
    protected static SettingsAction inst = null;
    protected AbstractMainFrame mainFrame;
    protected SettingsDialog dialog;

    public static SettingsAction getInstance(AbstractMainFrame mainFrame)
    {
        if (inst == null)
        {
            inst = new SettingsAction("Settings", null);
            inst.mainFrame = mainFrame;
            inst.dialog = new SettingsDialog((MainFrame) mainFrame, "Settings");
        }
        return inst;
    }

    protected SettingsAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        dialog.setSize(500, 500);
        int x = mainFrame.getLocationOnScreen().x + (mainFrame.getWidth() - dialog.getWidth()) / 2;
        int y = mainFrame.getLocationOnScreen().y + (mainFrame.getHeight() - dialog.getHeight()) / 2;
        dialog.setLocation(Math.max(0, x), Math.max(0, y));
        dialog.setVisible(true);
    }
}
