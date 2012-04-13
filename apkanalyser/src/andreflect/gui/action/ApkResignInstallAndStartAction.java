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
import java.io.File;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefContext;
import andreflect.ApkClassContext;
import andreflect.sign.ApkSign;

public class ApkResignInstallAndStartAction extends AbstractCanceableAction {
    private static final long serialVersionUID = 1179944038824671555L;
    protected static ApkResignInstallAndStartAction m_inst = null;

    public static ApkResignInstallAndStartAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new ApkResignInstallAndStartAction("Re-sign, install and start apk", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected ApkResignInstallAndStartAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public String getWorkDescription() {
        return "Re-sign, install and start apk";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error re-sign, install and start apk", t);
        getMainFrame().initBottomInfo();
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        Object oRef = Selection.getRefContextOfSeletedObject();
        File apk = null;
        if (oRef != null && oRef instanceof RefContext) {

            RefContext cRef = (RefContext) oRef;
            if (cRef.getContext().getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
                ApkClassContext context = (ApkClassContext) cRef.getContext();
                apk = context.getFile();
                String name = new ApkSign().sign(apk);
                File newApk = new File(name);
                getMainFrame().setBottomInfo(apk.getName() + " resigned to " + newApk.getName());

                e.setSource(new Object[] { newApk
                        , context.getXmlParser().getManifest().getLauncher()
                        , context.getXmlParser().getManifest().getPackage() });
                ApkInstallAndStartAction.getInstance((MainFrame) getMainFrame()).actionPerformed(e);
            }
        }
    }

}
