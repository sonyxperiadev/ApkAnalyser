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

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import analyser.gui.GenericBytecodeModDialog;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefClass;
import analyser.logic.RefMethod;

import jerl.bcm.inj.Injection;
import mereflect.MEClass;
import mereflect.MEMethod;

public class GenericModificationAction extends AbstractTreeBytecodeModAction
{
    private static final long serialVersionUID = 8869581981011106743L;
    protected static GenericModificationAction m_inst = null;
    Class<?> injClass;
    Object[] injClassArgs;

    public static GenericModificationAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new GenericModificationAction("Generic modification", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        MainFrame mainFrame = (MainFrame) getMainFrame();
        Object ref = Selection.getRefClass();
        if (ref == null) {
            ref = Selection.getRefMethod();
        }
        MEClass clazz = null;
        MEMethod method = null;
        if (ref instanceof RefClass)
        {
            clazz = ((RefClass) ref).getMEClass();
        }
        else if (ref instanceof RefMethod)
        {
            method = ((RefMethod) ref).getMethod();
            clazz = method.getMEClass();
        }
        GenericBytecodeModDialog gbmd =
                new GenericBytecodeModDialog(mainFrame, "Generic bytecode modification", clazz, method, this);
        gbmd.setSize(700, 500);
        int x = mainFrame.getLocationOnScreen().x + (mainFrame.getWidth() - gbmd.getWidth()) / 2;
        int y = mainFrame.getLocationOnScreen().y + (mainFrame.getHeight() - gbmd.getHeight()) / 2;
        gbmd.setLocation(Math.max(0, x), Math.max(0, y));
        gbmd.setVisible(true);
        gbmd.setModal(true);
        gbmd.awaitAcknowledge();
        injClass = gbmd.getInjectionClass();
        injClassArgs = gbmd.getArguments();
        if (injClass != null && injClassArgs != null) {
            super.run(e);
        }
    }

    protected GenericModificationAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        injClassArgs[0] = methodSignature;
        Injection inj = null;
        try {
            inj = (Injection) injClass.getConstructors()[0].newInstance(injClassArgs);
        } catch (Exception e) {
            e.printStackTrace();
            getMainFrame().showError("Generic bytecode modification error", e);
        }
        return inj;
    }
}