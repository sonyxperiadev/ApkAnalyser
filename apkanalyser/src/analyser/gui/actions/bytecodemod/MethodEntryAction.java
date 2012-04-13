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

import javax.swing.Icon;

import analyser.gui.MainFrame;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.impl.MethodEntryOut;

public class MethodEntryAction extends AbstractTreeBytecodeModAction
{
    private static final long serialVersionUID = 3402525307227222493L;
    protected static MethodEntryAction m_inst = null;

    public static MethodEntryAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new MethodEntryAction("Print method entry", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected MethodEntryAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        return new MethodEntryOut(methodSignature, "> " + className + ":" + methodSignature);
    }
}