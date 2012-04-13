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
import mereflect.Type;
import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.DexMethod;
import andreflect.injection.impl.DalvikMethodEntryParam;

public class DalvikMethodEntryParamAction extends AbstractTreeBytecodeModAction {
    private static final long serialVersionUID = 1502062870084319895L;
    protected static DalvikMethodEntryParamAction m_inst = null;

    public static DalvikMethodEntryParamAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodEntryParamAction("Print method entry(with params)", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikMethodEntryParamAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }
        DexMethod dexMethod = (DexMethod) method;

        Type[] args = method.getArguments();
        String[] params = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            params[i] = args[i] + " " + dexMethod.getParameterName(i, args.length);
        }

        DalvikMethodEntryParam inj = new DalvikMethodEntryParam(getMethodSignature(method)
                , "> " + method.getMEClass().getName() + ":" + getMethodSignature(method));
        inj.setParams(params);

        BytecodeModificationMediator.getInstance().registerModification(
                method.getMEClass().getResource().getContext(),
                method.getMEClass(),
                inj,
                method);

        ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        //not used
        return null;
    }

}
