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

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.Settings;
import analyser.logic.RefContext;
import andreflect.ApkClassContext;
import andreflect.adb.AdbProxy;

public class ApkUninstallAction extends AbstractCanceableAction {
    AdbProxy m_proxy = null;
    private static final long serialVersionUID = 409579906133057146L;
    protected static ApkUninstallAction m_inst = null;

    public static ApkUninstallAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new ApkUninstallAction("Uninstall apk", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected ApkUninstallAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public String getWorkDescription() {
        return "Uninstall apk";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error uninstall apk", t);
        getMainFrame().initBottomInfo();
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        Object oRef = Selection.getRefContextOfSeletedObject();
        if (oRef != null && oRef instanceof RefContext) {

            RefContext cRef = (RefContext) oRef;
            if (cRef.getContext().getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
                ApkClassContext context = (ApkClassContext) cRef.getContext();
                if (m_proxy == null) {
                    m_proxy = new AdbProxy(Settings.getAdbPath());
                }

                String packageName = context.getXmlParser().getManifest().getPackage();

                String result = m_proxy.uninstallApk(packageName, this);
                if (result == null
                        || (result != null && result.toLowerCase().indexOf("failure") >= 0)
                        || (result != null && result.toLowerCase().indexOf("errors") >= 0)) {
                    getMainFrame().showInformation("Uninstall", "Uninstall error:" + result);
                } else {
                    getMainFrame().showInformation("Uninstall", "Success");
                }

                getMainFrame().initBottomInfo();
            }
        }
    }

}
