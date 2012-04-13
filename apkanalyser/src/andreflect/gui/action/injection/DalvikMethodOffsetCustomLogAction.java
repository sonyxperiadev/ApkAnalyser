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

import javax.swing.Icon;
import javax.swing.JOptionPane;

import jerl.bcm.inj.Injection;
import mereflect.MEMethod;

import org.jf.dexlib.Code.Instruction;

import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.Util;
import andreflect.gui.linebuilder.DalvikByteCodeLineBuilder;
import andreflect.injection.impl.DalvikMethodOffsetLog;

public class DalvikMethodOffsetCustomLogAction extends AbstractTreeBytecodeModAction {

    private static final long serialVersionUID = 7485251355117717314L;
    protected static DalvikMethodOffsetCustomLogAction m_inst = null;

    public static DalvikMethodOffsetCustomLogAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodOffsetCustomLogAction("Print custom log at offset", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikMethodOffsetCustomLogAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }

        DalvikByteCodeLineBuilder.DalvikBytecodeOffset offset = Util.getDalvikBytecodeOffset();
        if (offset == null) {
            return;
        }

        int bytecodeOffset = offset.pc;
        int line = offset.line;
        Instruction instructionOffset = offset.instruction;

        String output = (String) JOptionPane.showInputDialog(
                getMainFrame(),
                "", "Print string",
                JOptionPane.QUESTION_MESSAGE, null, null,
                method.getMEClass().getName() + ":" + getMethodSignature(method) + "@" + Integer.toHexString(bytecodeOffset) + "(line" + line + ")");
        if (output != null && output.trim().length() > 0)
        {
            DalvikMethodOffsetLog injection =
                    new DalvikMethodOffsetLog(getMethodSignature(method), instructionOffset, output);
            BytecodeModificationMediator.getInstance().registerModification(
                    method.getMEClass().getResource().getContext(),
                    method.getMEClass(),
                    injection,
                    method);
            Util.printInjectionInfoInTextBuilder(output, null, injection);

            ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
        }
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        //not used
        return null;
    }

}
