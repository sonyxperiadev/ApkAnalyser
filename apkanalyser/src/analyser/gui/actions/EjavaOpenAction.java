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

import gui.ConsoleWindow;
import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.gui.Settings;

import util.EjavaProxy;
import util.ProcessHandler;

public class EjavaOpenAction extends AbstractCanceableAction {

    private static final long serialVersionUID = -8683675633797182689L;
    protected static EjavaOpenAction m_inst = null;

    public static EjavaOpenAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new EjavaOpenAction("Open midlet stdout", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected EjavaOpenAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        if (!new File(Settings.getEjavaPath()).exists()) {
            getMainFrame().showError("Please point out ejava.exe", "Invalid path");
            SettingsAction.getInstance(getMainFrame()).actionPerformed(e);
            return;
        }
        EjavaProxy proxy = new EjavaProxy(Settings.getEjavaPath());
        getMainFrame().actionStarted(this);
        getMainFrame().setBottomInfo("Opening console");
        ProcessHandler process = proxy.openStdout();
        ConsoleWindow.getInstance().start("Console", process);
        getMainFrame().initBottomInfo();
        getMainFrame().actionFinished(this);
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error opening midlet stdout", t);
        getMainFrame().initBottomInfo();
    }

    @Override
    public String getWorkDescription() {
        return "Opening midlet stdout";
    }
}