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

package analyser.gui.actions.lookup;

import java.awt.Color;
import java.util.List;

import javax.swing.Icon;

import analyser.gui.MainFrame;
import analyser.logic.InvSnooper;

import mereflect.MEMethod;

public class OpenCallerGraphAction extends AbstractCallGraphAction
{
    private static final long serialVersionUID = 346999425702531720L;

    protected static OpenCallerGraphAction m_inst = null;

    static final Color COL_NODE = new Color(238, 238, 255);

    public static OpenCallerGraphAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new OpenCallerGraphAction("Show graph for local callers", null);
            m_inst.setMainFrame(mainFrame);
            m_inst.initialize(true);
        }
        return m_inst;
    }

    protected OpenCallerGraphAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public List<InvSnooper.Invokation> getReferences(MEMethod m, boolean virtual) throws Throwable {
        return InvSnooper.findCallers(m, false, null, null);
    }

    @Override
    String getTitleGraphType() {
        return "Caller";
    }
}