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
import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.injection.impl.DalvikMethodExitValue;

public class DalvikMethodExitValueAction extends AbstractTreeBytecodeModAction {

    private static final long serialVersionUID = -758895927503271605L;
    protected static DalvikMethodExitValueAction m_inst = null;

    public static DalvikMethodExitValueAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodExitValueAction("Print method exit(with return value)", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikMethodExitValueAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }

        DalvikMethodExitValue inj = new DalvikMethodExitValue(getMethodSignature(method)
                , "< " + method.getMEClass().getName() + ":" + getMethodSignature(method));

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
