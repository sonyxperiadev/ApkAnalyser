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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import andreflect.ApkClassContext;
import andreflect.xml.XmlResourceChecker;
import brut.androlib.res.data.ResResSpec;

public class XmlViewReferenceAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    protected static XmlViewReferenceAction m_inst = null;
    protected MainFrame mainFrame;

    protected XmlViewReferenceAction(String actionName, Icon actionIcon) {
        super(actionName, actionIcon);
    }

    public static XmlViewReferenceAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new XmlViewReferenceAction("View resource id reference", null);
            m_inst.mainFrame = mainFrame;
        }
        return m_inst;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = Selection.getSelectedObject();
        if (o instanceof LineBuilderFormatter.Link)
        {
            if (((LineBuilderFormatter.Link) o).getData() != null
                    && ((LineBuilderFormatter.Link) o).getData().length == 2
                    && ((LineBuilderFormatter.Link) o).getData()[0] instanceof ResResSpec
                    && ((LineBuilderFormatter.Link) o).getData()[1] instanceof ApkClassContext) {
                ResResSpec spec = (ResResSpec) ((LineBuilderFormatter.Link) o).getData()[0];
                ApkClassContext apkContext = (ApkClassContext) ((LineBuilderFormatter.Link) o).getData()[1];
                XmlResourceChecker checker = apkContext.getXmlParser().getResourceChecker();
                LineBuilder lb = checker.showSpecDetail(spec, mainFrame, apkContext);
                if (lb != null) {
                    mainFrame.showText("Resource " + String.format("%08X", spec.getId().id) + " : ", lb);
                    mainFrame.setBottomInfo("Resource " + String.format("%08X", spec.getId().id) + " decoded");
                } else {
                    mainFrame.setBottomInfo("Cannot decode resource detail");
                }
            }
        }
    }
}
