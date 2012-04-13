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

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.TextBuilder;
import analyser.gui.actions.ShowBytecodeAction;

import mereflect.BytecodeVisitor;
import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.SimpleVisitor;
import mereflect.info.CiFieldRef;
import mereflect.info.CiNameAndType;
import mereflect.info.ClassInfo;

public class FindFieldAccessAction extends AbstractCanceableAction {
    private static final long serialVersionUID = -4585625738006633388L;
    protected static FindFieldAccessAction m_inst = null;
    protected List<ResultEntry> result;
    MEClass clazz;
    MEField token;
    ClassInfo[] cp;
    MEMethod curMethod;

    public static FindFieldAccessAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new FindFieldAccessAction("Find field accesses", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected FindFieldAccessAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    protected BytecodeVisitor getVisitor() {
        return new SimpleVisitor() {
            @Override
            public void visitLocalFieldName(int pc, int bytecode, int len, int cpIndex) {
                boolean setter = (bytecode == 181 /*putfield*/|| bytecode == 179 /*putstatic*/);
                CiFieldRef ciFieldRef = (CiFieldRef) cp[cpIndex];
                CiNameAndType ciNameType = (CiNameAndType) cp[ciFieldRef.getNameAndTypeIndex()];
                if (token.getDescriptorIndex() == ciNameType.getDescriptorIndex() &&
                        token.getNameIndex() == ciNameType.getNameIndex()) {
                    result.add(new ResultEntry(curMethod, pc, setter));
                }
            }

            @Override
            public void visitNewBytecode(int pc, int bytecode) {
            }
        };
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        {
            Object ref = Selection.getSelectedView();
            if (ref == null || !(ref instanceof TextBuilder)) {
                return;
            }
            TextBuilder tb = (TextBuilder) ref;
            // END TODO
            Object lineRef = tb.getLineBuilder().getReference(tb.getCurrentLine());
            if (lineRef == null || !(lineRef instanceof MEField)) {
                return;
            }
            result = new ArrayList<ResultEntry>();
            token = (MEField) lineRef;
            if (tb.getOwnerData() instanceof MEMethod) {
                MEMethod method = (MEMethod) tb.getOwnerData();
                clazz = method.getMEClass();
            } else if (tb.getOwnerData() instanceof MEClass) {
                clazz = (MEClass) tb.getOwnerData();
            } else {
                return;
            }
            cp = clazz.getConstantPool();

            MEMethod[] meths = clazz.getMethods();
            for (int i = 0; i < meths.length && isRunning(); i++) {
                curMethod = meths[i];
                curMethod.traverseBytecodes(getVisitor());
                getMainFrame().actionReportWork(this, (100 * i) / meths.length);
            }

            if (isRunning()) {
                getMainFrame().actionFinished(this);
                showResult();
            }
        }
    }

    protected void showResult() {
        LineBuilder lb = new LineBuilder();
        if (result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                ResultEntry e = result.get(i);
                lb.newLine();
                lb.append(e.setter ? "WR  " : "RD  ", e.setter ? 0x880000 : 0x008800);
                LineBuilderFormatter.makeOutline(e.method, lb);
                lb.append(" @ ", 0x000000);
                lb.append(Integer.toHexString(e.pc), 0x000088);
                Object[] data = { e.method, new Integer(e.pc) };
                lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                        ShowBytecodeAction.getInstance((MainFrame) getMainFrame()),
                        data));
            }
            getMainFrame().showText("Field access search result: " + token, lb);
            getMainFrame().setBottomInfo(result.size() + " access(es) found");
        } else {
            getMainFrame().setBottomInfo("No accesses found");
        }
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error resolving field accesses", t);
    }

    @Override
    public String getWorkDescription() {
        return "Resolving field accesses";
    }

    class ResultEntry {
        MEMethod method;
        int pc;
        boolean setter;

        public ResultEntry(MEMethod method, int pc, boolean setter) {
            this.method = method;
            this.pc = pc;
            this.setter = setter;
        }
    }
}