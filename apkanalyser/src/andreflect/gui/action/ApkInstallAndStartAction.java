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

import gui.AppException;
import gui.Canceable;
import gui.ConsoleWindow;
import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;

import util.ProcessHandler;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.Settings;
import analyser.gui.actions.SettingsAction;
import analyser.logic.RefContext;
import andreflect.ApkClassContext;
import andreflect.adb.AdbProxy;
import andreflect.adb.ConsoleWindowListener;

public class ApkInstallAndStartAction extends AbstractCanceableAction implements ConsoleWindowListener {
    private static final long serialVersionUID = 3506912783782178719L;

    protected static ApkInstallAndStartAction m_inst = null;

    UIProgressSlider slider;
    AdbProxy proxy = null;

    public static ApkInstallAndStartAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new ApkInstallAndStartAction("Install and start apk", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected ApkInstallAndStartAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        File apk = null;
        String launcher = null;
        String packageName = null;
        Object oRef;
        boolean success = true;

        if (e.getSource() instanceof Object[]
                && (((Object[]) e.getSource())[0]) instanceof File
                && (((Object[]) e.getSource())[1]) instanceof String
                && (((Object[]) e.getSource())[2]) instanceof String) {
            apk = (File) (((Object[]) e.getSource())[0]);
            launcher = (String) (((Object[]) e.getSource())[1]);
            packageName = (String) (((Object[]) e.getSource())[2]);
        } else {
            oRef = Selection.getRefContextOfSeletedObject();
            if (oRef != null && oRef instanceof RefContext) {

                RefContext cRef = (RefContext) oRef;
                if (cRef.getContext().getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
                    ApkClassContext context = (ApkClassContext) cRef.getContext();
                    apk = context.getFile();
                    launcher = context.getXmlParser().getManifest().getLauncher();
                    packageName = context.getXmlParser().getManifest().getPackage();

                }
            }
        }
        if (apk == null || launcher == null) {
            return;
        }

        if (!new File(Settings.getAdbPath()).exists()) {
            getMainFrame().showError("Please point out adb.exe", "Invalid path");
            SettingsAction.getInstance(getMainFrame()).actionPerformed(e);
            return;
        }

        getMainFrame().actionStarted(this);
        slider = new UIProgressSlider(this);
        new Thread(slider).start();
        File installFile = apk;

        if (proxy == null) {
            proxy = new AdbProxy(Settings.getAdbPath());
        }

        getMainFrame().setBottomInfo("Installing " + installFile.getAbsolutePath());

        String result = proxy.uninstallApk(packageName, this);
        if (result == null
                || (result != null && result.toLowerCase().indexOf("failure") >= 0)
                || (result != null && result.toLowerCase().indexOf("errors") >= 0)) {
            System.out.println("Uninstall error = " + result);
        }

        result = proxy.installApk(installFile, this);
        if (isRunning()) {
            if (result != null
                    && result.toUpperCase().indexOf("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES") >= 0) {
                getMainFrame().showError("Certification is not consistent, try resign install and start?", "Can not install");
                slider.running = false;
                success = false;
            } else if (result == null
                    || (result != null && result.toLowerCase().indexOf("failure") >= 0)
                    || (result != null && result.toLowerCase().indexOf("errors") >= 0)) {
                slider.running = false;
                success = false;
                System.out.println("install error = " + result);
                throw new AppException("Could not install " + installFile.getAbsolutePath());
            }
        }
        if (isRunning() && success) {
            getMainFrame().setBottomInfo("Opening console");
            ProcessHandler process = proxy.openStdout();
            ConsoleWindow.getInstance().start("Console", process, this);
        }
        if (isRunning() && success) {
            getMainFrame().setBottomInfo("Starting " + installFile.getAbsolutePath());
            result = proxy.startApk(launcher, this);

            if (result == null
                    || (result != null && result.toLowerCase().indexOf("failure") >= 0)
                    || (result != null && result.toLowerCase().indexOf("errors") >= 0)) {
                System.out.println("start error = " + result);
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
        getMainFrame().showError("Error installing and running apk", t);
        getMainFrame().initBottomInfo();
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
                getMainFrame().actionReportWork(ApkInstallAndStartAction.this, update);
                update -= 8;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public String getWorkDescription() {
        return "Installing and starting apk";
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
