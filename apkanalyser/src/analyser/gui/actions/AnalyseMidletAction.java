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
import java.io.File;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.logic.Resolver;


public class AnalyseMidletAction extends AbstractAnalyseAction
{
    private static final long serialVersionUID = -1664694957398092138L;
    protected static AnalyseMidletAction m_inst = null;

    public static AnalyseMidletAction getInstance(AbstractMainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new AnalyseMidletAction("Add midlet", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected AnalyseMidletAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        File midletFile =
                MainFrame.getInstance().selectFile("Select midlet to add", "Add midlet",
                        "jar", "MIDlet file (jar)",
                        true, false);
        if (midletFile == null) {
            return;
        }
        Resolver r = MainFrame.getInstance().getResolver();
        if (MainFrame.getInstance().getResolver() == null)
        {
            r = new Resolver();
            MainFrame.getInstance().setResolver(r);
        }
        r.setListener(AnalyseMidletAction.getInstance(getMainFrame()));
        r.resolve(midletFile, this, 1, 1);
        if (isRunning())
        {
            buildTrees(r);
        }

        MainFrame.getInstance().initBottomInfo();
    }

    @Override
    public String getWorkDescription()
    {
        return "Analyzing midlet";
    }
}