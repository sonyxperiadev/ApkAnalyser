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

package andreflect.gui.action;

import gui.ConsoleWindow;
import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;

import util.ProcessHandler;
import analyser.gui.MainFrame;
import analyser.gui.Settings;
import analyser.gui.actions.SettingsAction;
import andreflect.adb.AdbProxy;
import andreflect.adb.ConsoleWindowListener;

public class AdbOpenAction extends AbstractCanceableAction implements ConsoleWindowListener {
    private static final long serialVersionUID = 7078213584608834295L;
    protected static AdbOpenAction m_inst = null;

    AdbProxy proxy = null;

    public static AdbOpenAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new AdbOpenAction("Open logcat", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected AdbOpenAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public String getWorkDescription() {
        return "Opening logcat";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error opening adb stdout", t);
        getMainFrame().initBottomInfo();
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        if (!new File(Settings.getAdbPath()).exists()) {
            getMainFrame().showError("Please point out adb.exe", "Invalid path");
            SettingsAction.getInstance(getMainFrame()).actionPerformed(e);
            return;
        }
        if (proxy == null) {
            proxy = new AdbProxy(Settings.getAdbPath());
        }
        getMainFrame().actionStarted(this);
        getMainFrame().setBottomInfo("Opening console");
        ProcessHandler process = proxy.openStdout();
        ConsoleWindow.getInstance().start("Console", process, this);
        getMainFrame().initBottomInfo();
        getMainFrame().actionFinished(this);
    }

    @Override
    public void onD(ProcessHandler process) {
        process.setArgs(proxy.getArgsLevel(AdbProxy.LEVEL_D));
    }

    @Override
    public void onE(ProcessHandler process) {
        process.setArgs(proxy.getArgsLevel(AdbProxy.LEVEL_E));
    }

    @Override
    public void onF(ProcessHandler process) {
        process.setArgs(proxy.getArgsLevel(AdbProxy.LEVEL_F));
    }

    @Override
    public void onI(ProcessHandler process) {
        process.setArgs(proxy.getArgsLevel(AdbProxy.LEVEL_I));
    }

    @Override
    public void onS(ProcessHandler process) {
        process.setArgs(proxy.getArgsLevel(AdbProxy.LEVEL_S));
    }

    @Override
    public void onV(ProcessHandler process) {
        process.setArgs(proxy.getArgsLevel(AdbProxy.LEVEL_V));
    }

    @Override
    public void onW(ProcessHandler process) {
        process.setArgs(proxy.getArgsLevel(AdbProxy.LEVEL_W));
    }

    @Override
    public void onClear(ProcessHandler process) {
        try {
            proxy.clearStdout(this);
        } catch (Exception e) {
        }
    }
}
