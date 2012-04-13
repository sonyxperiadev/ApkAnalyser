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

import jerl.bcm.inj.Injection;
import mereflect.MEMethod;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.SingleRegisterInstruction;

import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.DexMethod;
import andreflect.injection.impl.DalvikMethodOffsetExCatch;

public class DalvikMethodOffsetExCatchAction extends AbstractTreeBytecodeModAction {

    private static final long serialVersionUID = -7728262540521665990L;
    protected static DalvikMethodOffsetExCatchAction m_inst = null;

    public static DalvikMethodOffsetExCatchAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodOffsetExCatchAction("Print exception catch", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikMethodOffsetExCatchAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }
        DexMethod dexMethod = (DexMethod) method;

        if (hasCatch(dexMethod)) {
            ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
        }
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        //not used
        return null;
    }

    public boolean hasCatch(DexMethod method) {
        if (method.getEncodedMethod().codeItem == null) {
            return false;
        }
        boolean ret = false;
        Instruction[] instructions = method.getEncodedMethod().codeItem.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            switch (instructions[i].deodexedInstruction.opcode) {
            case MOVE_EXCEPTION:
                DalvikMethodOffsetExCatch catchInjection = new DalvikMethodOffsetExCatch(getMethodSignature(method),
                        instructions[i + 1],
                        method.getMEClass().getName() + ":" + getMethodSignature(method),
                        (short) ((SingleRegisterInstruction) instructions[i]).getRegisterA());
                BytecodeModificationMediator.getInstance().registerModification(
                        method.getMEClass().getResource().getContext(),
                        method.getMEClass(),
                        catchInjection,
                        method);
                ret = true;
                break;
            }
        }
        return ret;
    }
}