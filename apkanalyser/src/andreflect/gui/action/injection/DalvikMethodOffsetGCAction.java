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

import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.Util;
import andreflect.gui.linebuilder.DalvikByteCodeLineBuilder;
import andreflect.injection.impl.DalvikMethodOffsetGC;

public class DalvikMethodOffsetGCAction extends AbstractTreeBytecodeModAction {

    private static final long serialVersionUID = 6390553812456450251L;
    protected static DalvikMethodOffsetGCAction m_inst = null;
    protected static DalvikMethodOffsetGCAction m_inst_offset = null;

    public static DalvikMethodOffsetGCAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodOffsetGCAction("Invoke system gc at entry", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    public static DalvikMethodOffsetGCAction getInstanceOffset(MainFrame mainFrame)
    {
        if (m_inst_offset == null)
        {
            m_inst_offset = new DalvikMethodOffsetGCAction("Invoke system gc at offset", null);
            m_inst_offset.setMainFrame(mainFrame);
        }
        return m_inst_offset;
    }

    protected DalvikMethodOffsetGCAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }

        Instruction offsetIns = null;

        DalvikByteCodeLineBuilder.DalvikBytecodeOffset offset = Util.getDalvikBytecodeOffset();
        if (offset != null) {
            offsetIns = offset.instruction;
        }

        DalvikMethodOffsetGC inj = new DalvikMethodOffsetGC(getMethodSignature(method), offsetIns);

        BytecodeModificationMediator.getInstance().registerModification(
                method.getMEClass().getResource().getContext(),
                method.getMEClass(),
                inj,
                method);

        Util.printInjectionInfoInTextBuilder("System.gc()", null, inj);//TODO

        ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        //not used
        return null;
    }

}
