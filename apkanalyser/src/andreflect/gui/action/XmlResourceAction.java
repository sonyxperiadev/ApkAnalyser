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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.ZipEntry;

import javax.swing.Icon;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefContext;
import andreflect.ApkClassContext;

public class XmlResourceAction extends AbstractCanceableAction {

    private static final long serialVersionUID = 7979029802472179156L;

    protected static XmlResourceAction m_inst = null;

    public static XmlResourceAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new XmlResourceAction("View xml resources", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected XmlResourceAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public String getWorkDescription() {
        return "Resolving XML resources";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error resolving xml resources", t);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        LineBuilder lb = null;
        Object oRef = Selection.getRefContextOfSeletedObject();
        if (oRef == null || !(oRef instanceof RefContext)) {
            return;
        }
        RefContext cRef = (RefContext) oRef;

        if (cRef.getContext().getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
            ApkClassContext context = (ApkClassContext) cRef.getContext();
            ArrayList<ZipEntry> result = context.getXmlParser().getXmlFiles();

            Comparator<ZipEntry> comp = new zipEntryComparator();
            Collections.sort(result, comp);

            if (result.size() != 0) {
                lb = new LineBuilder();
                for (int i = 0; i < result.size(); i++) {
                    getMainFrame().actionReportWork(this, 100 * i / result.size());

                    ZipEntry entry = result.get(i);
                    lb.newLine();
                    lb.append(entry.getName(), LineBuilderFormatter.COLOR_KEYWORD);
                    lb.append("     (");
                    lb.append(entry.getSize(), LineBuilderFormatter.COLOR_COMMENT);
                    lb.append(" bytes)", LineBuilderFormatter.COLOR_COMMENT);
                    Object[] data = { context, entry.getName(), new Integer(-1), new Integer(-1), new Boolean(false) };
                    lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                            XmlViewerAction.getInstance((MainFrame) getMainFrame()),
                            data));
                }

                getMainFrame().showText("XML search result: ", lb);
                getMainFrame().setBottomInfo(result.size() + " xml file(s) found");
            }
        }
        if (lb == null) {
            getMainFrame().setBottomInfo("No xml file found");
        }
    }

    public class zipEntryComparator implements Comparator<ZipEntry> {
        @Override
        public int compare(ZipEntry o1, ZipEntry o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

}
