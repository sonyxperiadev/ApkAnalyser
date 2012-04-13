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

package andreflect.gui.action.injection;

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.impl.MethodOffsetOut;
import mereflect.CorruptBytecodeException;
import mereflect.MEMethod;
import analyser.gui.ClassTree;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.RefInvokation;
import analyser.logic.Reference;
import andreflect.ApkClassContext;
import andreflect.Util;
import andreflect.injection.impl.DalvikMethodOffsetInvokeParam;

public class DalvikReferenceCallsParamAction extends AbstractCanceableAction
{
    private static final long serialVersionUID = -334735447277492453L;
    protected static DalvikReferenceCallsParamAction m_inst = null;
    protected int m_refs;
    protected int m_curRefs;

    public static DalvikReferenceCallsParamAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikReferenceCallsParamAction("Print calls to references(with param)", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikReferenceCallsParamAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        MainFrame mainFrame = (MainFrame) getMainFrame();
        Object oRef = Selection.getSelectedObject();
        if (oRef == null /*|| !(oRef instanceof RefMethod)*/) {
            return;
        }
        Reference mRef = (Reference) oRef;

        boolean thisTree = (oRef instanceof RefInvokation && ((RefInvokation) oRef).isLocal());
        ClassTree tree = thisTree ?
                mainFrame.getSelectedTree() : mainFrame.getOppositeSelectedTree();

        m_refs = mRef.getCount() * 2;
        m_curRefs = 0;
        RefInvokation[] invs = getInvokations(mRef);
        if (isRunning())
        {
            findCalls(tree, invs, 4, !thisTree);
        }
        if (isRunning())
        {
            mainFrame.actionFinished(this);
        }
        mainFrame.getMidletTree().repaint();
        mainFrame.getResourceTree().repaint();
    }

    public void findCalls(ClassTree tree, RefInvokation[] invs, int level, boolean opposite) throws CorruptBytecodeException
    {
        for (int i = 0; i < invs.length && isRunning(); i++)
        {
            RefInvokation called = opposite ? invs[i] : invs[i].getOppositeInvokation();
            RefInvokation caller = opposite ? invs[i].getOppositeInvokation() : invs[i];
            if (caller != null && called != null)
            {
                //System.out.println("caller method "+ caller.getMEClass().getClassName() + "." + caller.getMethod().getName());
                List<MEMethod.Invokation> callerInvokations = caller.getMethod().getInvokations();
                for (int j = 0; j < callerInvokations.size(); j++)
                {
                    MEMethod.Invokation callerInv = callerInvokations.get(j);
                    if (callerInv.invClassname.equals(called.getMEClass().getName()) &&
                            callerInv.invMethodname.equals(called.getMethod().getName()) &&
                            callerInv.invDescriptor.equals(called.getMethod().getDescriptor()))
                    {
                        String output = null;
                        Injection injection = null;
                        if (caller.getContext() instanceof ApkClassContext) {
                            output = "! " +
                                    caller.getMEClass().getClassName() + "." + Util.getMethodSignature(caller.getMethod()) +
                                    " -> " +
                                    called.getMEClass().getClassName() + "." + Util.getMethodSignature(called.getMethod());
                            DalvikMethodOffsetInvokeParam dalvikinjection =
                                    new DalvikMethodOffsetInvokeParam(Util.getMethodSignature(caller.getMethod()),
                                            callerInv.offsetIns, output);
                            injection = dalvikinjection;
                        } else {
                            output = "! " +
                                    caller.getMEClass().getClassName() + "." + caller.getMethod().getName() + ":" + caller.getMethod().getDescriptor() +
                                    " -> " +
                                    called.getMEClass().getClassName() + "." + called.getMethod().getName() + ":" + called.getMethod().getDescriptor();
                            injection =
                                    new MethodOffsetOut(caller.getMethod().getName() + caller.getMethod().getDescriptor(),
                                            callerInv.bytecodeIndex, output);
                        }
                        BytecodeModificationMediator.getInstance().registerModification(
                                caller.getContext(),
                                caller.getMEClass(),
                                injection,
                                caller.getMethod());
                        ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(caller, Reference.MODIFIED);
                    }
                }
            }
            getMainFrame().actionReportWork(this, 50 + ((45 * i) / (invs.length)));
        }
    }

    public RefInvokation[] getInvokations(Reference ref)
    {
        List<RefInvokation> res = new ArrayList<RefInvokation>();
        if (ref instanceof RefInvokation)
        {
            res.add((RefInvokation) ref);
        }
        else
        {
            Iterator<Reference> i = (ref).getChildren().iterator();
            addInvokations(res, i);
        }
        return res.toArray(new RefInvokation[res.size()]);
    }

    private void addInvokations(List<RefInvokation> res, Iterator<Reference> refs)
    {
        while (isRunning() && refs.hasNext())
        {
            Reference ref = refs.next();
            if (ref instanceof RefInvokation)
            {
                res.add((RefInvokation) ref);
                m_curRefs++;
                getMainFrame().actionReportWork(this,
                        (100 * m_curRefs) / (m_refs == 0 ? 1 : m_refs));
            }
            else
            {
                addInvokations(res, ref.getChildren().iterator());
            }
        }
    }

    @Override
    public void handleThrowable(Throwable t)
    {
        t.printStackTrace();
        getMainFrame().showError("Error during reference look up", t);
    }

    @Override
    public String getWorkDescription()
    {
        return "Bytecode modify, adding printout to reference invokations";
    }
}