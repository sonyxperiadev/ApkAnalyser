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
import analyser.gui.TextDialog;
import andreflect.ApkClassContext;
import andreflect.gui.linebuilder.XmlLineFormatter;

public class XmlViewerAction extends AbstractAction {
    private static final long serialVersionUID = -8370203878848891286L;
    protected static XmlViewerAction inst = null;
    protected MainFrame mainFrame;

    public static XmlViewerAction getInstance(MainFrame mainFrame) {
        if (inst == null) {
            inst = new XmlViewerAction("View xml", null);
            inst.mainFrame = mainFrame;
        }
        return inst;
    }

    protected XmlViewerAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Object o = Selection.getSelectedObject();
        if (o instanceof LineBuilderFormatter.Link)
        {
            LineBuilder lb = null;
            if (((LineBuilderFormatter.Link) o).getData() != null
                    && ((LineBuilderFormatter.Link) o).getData().length == 5
                    && ((LineBuilderFormatter.Link) o).getData()[0] instanceof ApkClassContext
                    && ((LineBuilderFormatter.Link) o).getData()[1] instanceof String
                    && ((LineBuilderFormatter.Link) o).getData()[2] instanceof Integer
                    && ((LineBuilderFormatter.Link) o).getData()[3] instanceof Integer
                    && ((LineBuilderFormatter.Link) o).getData()[4] instanceof Boolean) {
                ApkClassContext context = (ApkClassContext) ((LineBuilderFormatter.Link) o).getData()[0];
                String filename = (String) ((LineBuilderFormatter.Link) o).getData()[1];
                int line = (Integer) ((LineBuilderFormatter.Link) o).getData()[2];
                int resId = (Integer) ((LineBuilderFormatter.Link) o).getData()[3];

                //only package match for resource id, it used for seeking android package references
                boolean onlyPackage = (Boolean) ((LineBuilderFormatter.Link) o).getData()[4];

                XmlLineFormatter xmllb = context.getXmlParser().getXmlLineBuilder(filename, line, resId, onlyPackage);
                if (xmllb != null
                        && (lb = xmllb.getLineBuilder()) != null) {

                    String title = context.getFile().getName() + " : " + filename;
                    if (xmllb.isRaw() == true) {
                        title = title + "  (NOT ENCODED)";
                    }
                    TextDialog tw = mainFrame.showText(title, lb);
                    if (xmllb.getCaret() != -1) {
                        tw.setCaretPosition(xmllb.getCaret());
                    }
                    mainFrame.setBottomInfo("Parsed xml file: " + filename);
                }

            }
            if (lb == null) {
                mainFrame.setBottomInfo("Parse xml failed");
            }
        }
    }

}
