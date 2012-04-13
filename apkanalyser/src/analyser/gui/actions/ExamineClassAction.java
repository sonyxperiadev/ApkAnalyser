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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.TextDialog;

import mereflect.MEClass;

public class ExamineClassAction extends AbstractAction
{
    private static final long serialVersionUID = -1044166019759991515L;
    protected static ExamineClassAction m_inst = null;
    protected MainFrame m_mainFrame;

    public static ExamineClassAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new ExamineClassAction("Examine class", null);
            m_inst.m_mainFrame = mainFrame;
        }
        return m_inst;
    }

    protected ExamineClassAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        MEClass c = Selection.getMEClass();
        LineBuilder classText = LineBuilderFormatter.makeOutline(c);
        //MainFrame.getInstance().showText(c.toString(), classText.toString());
        TextDialog td = MainFrame.getInstance().showText("Examine class [" + c.toString() + "]", classText);
        td.setOwnerData(c);
    }
}