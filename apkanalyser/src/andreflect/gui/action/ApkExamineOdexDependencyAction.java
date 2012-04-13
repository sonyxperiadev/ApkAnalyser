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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;


import org.jf.dexlib.OdexDependencies;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefContext;
import andreflect.ApkClassContext;

public class ApkExamineOdexDependencyAction extends AbstractCanceableAction {
    private static final long serialVersionUID = 7979029802472179156L;

    protected static ApkExamineOdexDependencyAction m_inst = null;

    public static ApkExamineOdexDependencyAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new ApkExamineOdexDependencyAction("Examine Odex dependencies", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected ApkExamineOdexDependencyAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public String getWorkDescription() {
        return "Examine Odex dependcies";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error resolving xml resources", t);
    }

    private static final Pattern dalvikCacheOdexPattern = Pattern.compile("@([^@]+)@classes.dex$");

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

            if (context.getDex().isOdex() == true) {
                OdexDependencies odexDependencies = context.getDex().getOdexDependencies();
                lb = new LineBuilder();

                boolean error = false;
                int i = 0;
                for (i = 0; i < odexDependencies.getDependencyCount(); i++) {
                    String dependency = odexDependencies.getDependency(i);
                    if (dependency.endsWith(".odex")) {
                        int slashIndex = dependency.lastIndexOf("/");

                        if (slashIndex != -1) {
                            //                  dependency = dependency.substring(slashIndex+1);
                        }
                        lb.newLine();
                        lb.append(dependency, LineBuilderFormatter.COLOR_KEYWORD);

                    } else if (dependency.endsWith("@classes.dex")) {
                        Matcher m = dalvikCacheOdexPattern.matcher(dependency);

                        if (!m.find()) {
                            getMainFrame().showError(String.format("Cannot parse dependency value %s", dependency), "Examine Odex dependcies");
                            error = true;
                        } else {
                            //                dependency = m.group(1);
                            lb.newLine();
                            lb.append(dependency, LineBuilderFormatter.COLOR_KEYWORD);
                        }
                    } else {
                        getMainFrame().showError(String.format("Cannot parse dependency value %s", dependency), "Examine Odex dependcies");
                        error = true;
                    }

                }
                if (error == false) {
                    getMainFrame().showText("Odex dependency result: ", lb);
                    getMainFrame().setBottomInfo(i + " odex file(s) found");
                }
            } else {
                getMainFrame().showError(context.getFile().getName() + " is not an Odex file", "Examine Odex dependcies");
            }
        }
    }

}
