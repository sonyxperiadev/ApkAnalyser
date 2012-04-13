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
import java.util.List;

import javax.swing.Icon;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.impl.MethodEntryOut;
import mereflect.MEClass;
import mereflect.MEMethod;
import analyser.gui.MAGraphPanel;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.InvSnooper;
import analyser.logic.Reference;
import andreflect.ApkClassContext;
import andreflect.Util;
import andreflect.injection.impl.DalvikMethodEntry;

public class GraphMarkAction extends AbstractCanceableAction {
    private static final long serialVersionUID = -1014819977952096809L;
    protected static GraphMarkAction inst = null;
    protected int refs;
    protected int curRefs;

    public static GraphMarkAction getInstance(MainFrame mainFrame) {
        if (inst == null) {
            inst = new GraphMarkAction("Print call graph chain", null);
            inst.setMainFrame(mainFrame);
        }
        return inst;
    }

    protected GraphMarkAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        List<InvSnooper.Invokation> callChain = null;
        Object aref = Selection.getSelectedView();
        if (aref == null || !(aref instanceof MAGraphPanel)) {
            return;
        }
        MAGraphPanel graph = (MAGraphPanel) aref;
        callChain = graph.getCallChain();

        if (callChain == null) {
            return;
        }

        MainFrame mainFrame = (MainFrame) getMainFrame();
        MEMethod goalMethod = (callChain.get(callChain.size() - 1)).toMethod;
        String goal = goalMethod.getMEClass().getName() + "." + goalMethod.getName() + goalMethod.getDescriptor();
        int mCount = 0;
        for (int i = 0; isRunning() && i < callChain.size(); i++) {
            MEMethod m = (callChain.get(i)).toMethod;
            MEClass clazz = m.getMEClass();
            if (!(clazz.isInterface() || m.isAbstract())) {
                mCount++;
                String methodSignature = Util.getMethodSignature(m);// m.getName() + m.getDescriptor();
                Injection inj;
                if (clazz.getResource().getContext() instanceof ApkClassContext) {
                    inj = new DalvikMethodEntry(methodSignature, "> [" + (i + 1) + "/" + (callChain.size()) + "] "
                            + clazz.getName() + ":" + methodSignature + " [" + goal + "]");

                } else {
                    inj = new MethodEntryOut(methodSignature, "> [" + (i + 1) + "/" + (callChain.size()) + "] "
                            + clazz.getName() + ":" + methodSignature + " [" + goal + "]");
                }
                BytecodeModificationMediator.getInstance().registerModification(clazz.getResource().getContext(), clazz, inj, m);
                ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(m, Reference.MODIFIED);
            }
            mainFrame.actionReportWork(this, (100 * i) / callChain.size());
        }

        if (isRunning()) {
            mainFrame.actionFinished(this);
            mainFrame.setBottomInfo("Marked " + mCount + " method(s)");
        }
        mainFrame.getMidletTree().repaint();
        mainFrame.getResourceTree().repaint();
    }

    @Override
    public void handleThrowable(Throwable t) {
        getMainFrame().showError("Error during call chain marking", t);
    }

    @Override
    public String getWorkDescription() {
        return "Bytecode modify, adding printout to call graph";
    }
}