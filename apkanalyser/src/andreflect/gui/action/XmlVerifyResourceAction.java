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
import java.util.HashMap;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefContext;
import andreflect.ApkClassContext;
import andreflect.xml.XmlResourceChecker;

public class XmlVerifyResourceAction extends AbstractCanceableAction {
    private static final long serialVersionUID = 620830646123214850L;

    private final String m_density;

    private static HashMap<String, XmlVerifyResourceAction> m_instMap = new HashMap<String, XmlVerifyResourceAction>();

    public static XmlVerifyResourceAction getInstance(MainFrame mainFrame, String dpi) {
        if (m_instMap.containsKey(dpi)) {
            return m_instMap.get(dpi);
        } else {
            XmlVerifyResourceAction inst = new XmlVerifyResourceAction(dpi, null);
            m_instMap.put(dpi, inst);
            inst.setMainFrame(mainFrame);
            return inst;
        }
    }

    protected XmlVerifyResourceAction(String actionName, Icon actionIcon) {
        super(actionName, actionIcon);
        m_density = actionName;
    }

    @Override
    public String getWorkDescription() {
        return "Verify " + m_density + " resources";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error verifying xml resources", t);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        Object oRef = Selection.getRefContextOfSeletedObject();
        if (oRef == null || !(oRef instanceof RefContext)) {
            return;
        }
        RefContext cRef = (RefContext) oRef;

        if (cRef.getContext().getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
            ApkClassContext apkContext = (ApkClassContext) cRef.getContext();
            XmlResourceChecker checker = apkContext.getXmlParser().getResourceChecker();
            checker.showIssueSpec(m_density, (MainFrame) getMainFrame(), cRef, this, apkContext);
        }
    }

}
