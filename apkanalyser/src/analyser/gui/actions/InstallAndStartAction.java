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

import gui.AppException;
import gui.Canceable;
import gui.ConsoleWindow;
import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.gui.Settings;

import util.EjavaProxy;
import util.ProcessHandler;

public class InstallAndStartAction extends AbstractCanceableAction {
    private static final long serialVersionUID = -7732414864728221996L;
    protected static InstallAndStartAction m_inst = null;
    UIProgressSlider slider;

    public static InstallAndStartAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new InstallAndStartAction("Install and start midlet", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected InstallAndStartAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        // TODO
        File jad = (File) (((Object[]) e.getSource())[0]);
        File jar = (File) (((Object[]) e.getSource())[1]);
        if (!new File(Settings.getEjavaPath()).exists()) {
            getMainFrame().showError("Please point out ejava.exe", "Invalid path");
            SettingsAction.getInstance(getMainFrame()).actionPerformed(e);
            return;
        }
        EjavaProxy proxy = new EjavaProxy(Settings.getEjavaPath());
        getMainFrame().actionStarted(this);
        slider = new UIProgressSlider(this);
        new Thread(slider).start();
        File installFile = jad == null ? jar : jad;
        getMainFrame().setBottomInfo("Installing " + installFile.getAbsolutePath());
        EjavaProxy.Midlet midlet = proxy.installMidlet(installFile, this);
        if (isRunning()) {
            if (midlet == null) {
                slider.running = false;
                throw new AppException("Could not install " + installFile.getAbsolutePath());
            }
        }
        if (isRunning()) {
            getMainFrame().setBottomInfo("Opening console");
            ProcessHandler process = proxy.openStdout();
            ConsoleWindow.getInstance().start("Console", process);
        }
        if (isRunning()) {
            getMainFrame().setBottomInfo("Starting " + installFile.getAbsolutePath());
            String res = proxy.startMidlet(midlet, this);
            if (res != null && res.toLowerCase().indexOf("error") >= 0) {
                System.out.println(res);
                ConsoleWindow.getInstance().close();
                slider.running = false;
                throw new AppException("Could not start " + installFile.getAbsolutePath());
            }
        }
        getMainFrame().initBottomInfo();
        getMainFrame().actionFinished(this);
    }

    @Override
    public void handleThrowable(Throwable t) {
        slider.running = false;
        t.printStackTrace();
        getMainFrame().showError("Error installing and running midlet", t);
        getMainFrame().initBottomInfo();
    }

    @Override
    public String getWorkDescription() {
        return "Installing and starting midlet";
    }

    class UIProgressSlider implements Runnable {
        volatile boolean running = true;
        Canceable canceable;
        int update = -1;

        public UIProgressSlider(Canceable c) {
            canceable = c;
        }

        @Override
        public void run() {
            while (running && canceable.isRunning()) {
                getMainFrame().actionReportWork(InstallAndStartAction.this, update);
                update -= 8;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}