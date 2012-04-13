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
import java.util.Iterator;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.RefClass;
import analyser.logic.RefContext;
import analyser.logic.RefMethod;
import analyser.logic.RefPackage;
import analyser.logic.Reference;

import mereflect.CorruptBytecodeException;

public class UnregisterAction extends AbstractCanceableAction
{
    private static final long serialVersionUID = -5580979682528796676L;
    protected static UnregisterAction m_inst = null;

    public static UnregisterAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new UnregisterAction("Unregisters bytecode modifications", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected UnregisterAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        Object ref = Selection.getSelectedObject();
        if (ref == null || !(ref instanceof Reference)) {
            return;
        }

        traverse((Reference) ref);

        if (isRunning())
        {
            getMainFrame().actionFinished(this);
        }
        ((MainFrame) getMainFrame()).getSelectedTree().repaint();
    }

    protected void traverse(Reference ref) throws CorruptBytecodeException
    {
        ref.setFlags(ref.getFlags() & ~Reference.MODIFIED);
        if (ref instanceof RefMethod)
        {
            modify((RefMethod) ref);
        }
        else
        {
            Iterator<Reference> i = ref.getChildren().iterator();
            while (i.hasNext())
            {
                traverse(i.next());
            }
        }
    }

    protected void modify(RefMethod refMethod) throws CorruptBytecodeException
    {
        RefClass refClass = (RefClass) refMethod.getParent();
        RefPackage refPackage = (RefPackage) refClass.getParent();
        RefContext refRes = (RefContext) refPackage.getParent();

        BytecodeModificationMediator.getInstance().unregisterModifications(
                refRes.getContext(),
                refClass.getMEClass(),
                refMethod.getMethod());

    }

    @Override
    public void handleThrowable(Throwable t)
    {
        t.printStackTrace();
        getMainFrame().showError("Warning", t);
    }

    @Override
    public String getWorkDescription()
    {
        return "Registering bytecode modification";
    }

}