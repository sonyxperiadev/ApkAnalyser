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

package analyser.gui.actions.bytecodemod;

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;

import jerl.bcm.util.ClassInjContainer;
import mereflect.MEClass;
import mereflect.MEClassContext;
import util.JadFileModifier;
import analyser.gui.MainFrame;
import analyser.gui.ProgressReporter;
import analyser.gui.actions.InstallAndStartAction;
import analyser.logic.BytecodeModificationMediator;
import andreflect.ApkClassContext;
import andreflect.gui.action.ApkInstallAndStartAction;

public class PerformAction extends AbstractCanceableAction {
    private static final long serialVersionUID = 1850052413392021437L;
    protected static PerformAction m_inst = null;
    static final String MOD_MIDLET_NAME_POSTFIX = "'";
    protected MainFrame m_mainFrame;

    public static PerformAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new PerformAction("Perform bytecode modifications", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected PerformAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public String getWorkDescription()
    {
        return "Perform bytecode modifications";
    }

    @Override
    public void handleThrowable(Throwable t)
    {
        t.printStackTrace();
        getMainFrame().showError("Bytecode modification error", t);
        getMainFrame().initBottomInfo();
    }

    int curCtx;
    MEClassContext m_ctx;

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        final MainFrame mainFrame = (MainFrame) getMainFrame();
        BytecodeModificationMediator bmm = BytecodeModificationMediator.getInstance();
        Set<MEClassContext> ctxs = bmm.getModifiedContexts();
        StringBuffer log = new StringBuffer();

        File modifiedJar = null;
        File modifiedJad = null;

        mainFrame.actionStarted(this);
        curCtx = 0;
        final int ctxCount = ctxs.size();
        if (ctxCount == 0) {
            return;
        }

        Iterator<MEClassContext> ctxsI = ctxs.iterator();
        int classCounter = 0;
        while (ctxsI.hasNext()) {
            File newJar = null;
            MEClassContext ctx = ctxsI.next();
            log.append("Original: " + ctx.getContextName() + "\n");
            mainFrame.setBottomInfo("Modifying " + ctx.getContextName());
            newJar = bmm.performRegisteredModifications(new ProgressReporter() {
                int total;

                @Override
                public void reportEnd() {
                }

                @Override
                public void reportStart(int total) {
                    this.total = total;
                }

                @Override
                public void reportWork(int finished) {
                    mainFrame.actionReportWork(
                            PerformAction.this, (curCtx * 100 + (finished * 100) / total) / ctxCount);
                }
            }, ctx, MOD_MIDLET_NAME_POSTFIX);
            Map<MEClass, ClassInjContainer> mods = bmm.getModifications(ctx);
            Iterator<MEClass> iClass = mods.keySet().iterator();
            while (iClass.hasNext()) {
                classCounter++;
                MEClass c = iClass.next();
                log.append("  .. " + c.getName() + "\n");
            }

            if (newJar != null) {
                log.append("Modified: " + newJar.getAbsolutePath() + "\n");
                modifiedJar = newJar;
                if (!(ctx instanceof ApkClassContext)) {
                    // Try to modify jad as well
                    try {
                        File jadFile = new File(ctx.getContextName().replaceAll(".jar", ".jad"));
                        String newJadFile = newJar.getAbsolutePath().replaceAll(".jar", ".jad");
                        if (jadFile.exists()) {
                            log.append("Found jad: " + jadFile.getAbsolutePath() + "\n");
                            JadFileModifier jfm = new JadFileModifier(jadFile, newJadFile, null);
                            modifiedJad = jfm.modify(newJar.getName(), MOD_MIDLET_NAME_POSTFIX, newJar.length());
                            log.append("Modified jad: " + newJadFile + "\n");
                        }
                    } catch (IOException ignore) {
                    }
                }
            }
            log.append("\n");
            m_ctx = ctx;
            curCtx++;
            mainFrame.actionReportWork(this, (curCtx * 100) / ctxCount);
        }
        log.append("Done - modified " + curCtx + " application(s) and " + classCounter + " class(es).\n");
        mainFrame.actionFinished(this);
        mainFrame.initBottomInfo();
        JButton[] customButtons = null;
        if (curCtx == 1) {
            final File fModifiedJad = modifiedJad;
            final File fModifiedJar = modifiedJar;
            customButtons = new JButton[1];
            customButtons[0] = new JButton("Install and run");
            customButtons[0].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (m_ctx instanceof ApkClassContext) {
                                e.setSource(new Object[] { fModifiedJar
                                        , ((ApkClassContext) m_ctx).getXmlParser().getManifest().getLauncher()
                                        , ((ApkClassContext) m_ctx).getXmlParser().getManifest().getPackage() });
                                ApkInstallAndStartAction.getInstance((MainFrame) getMainFrame()).actionPerformed(e);
                            } else {
                                e.setSource(new Object[] { fModifiedJad, fModifiedJar });
                                InstallAndStartAction.getInstance((MainFrame) getMainFrame()).actionPerformed(e);
                            }
                        }
                    }).start();
                }
            });
        }
        mainFrame.showText("Modification report", log.toString(), customButtons);
    }

}