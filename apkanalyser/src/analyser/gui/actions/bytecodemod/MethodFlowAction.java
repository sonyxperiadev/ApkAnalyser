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

import analyser.gui.ClassTree;
import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.TextBuilder;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.impl.MethodOffsetOut;
import mereflect.MEMethod;
import mereflect.bytecode.Bytecode;

public class MethodFlowAction extends AbstractTreeBytecodeModAction
{
    private static final long serialVersionUID = 773142688952750098L;
    protected static MethodFlowAction m_inst = null;

    public static MethodFlowAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new MethodFlowAction("Add prints to trace method flow", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected MethodFlowAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable
    {
        MainFrame mainFrame = (MainFrame) getMainFrame();
        ClassTree tree = mainFrame.getSelectedTree();
        if (!(Selection.getSelectedView() instanceof TextBuilder)) {
            return;
        }
        TextBuilder text = (TextBuilder) Selection.getSelectedView();
        if (text == null) {
            return;
        }
        LineBuilder lb = text.getLineBuilder();
        MEMethod method = (MEMethod) text.getOwnerData();
        mainFrame.actionStarted(this);
        for (int line = 0; line < lb.lineCount(); line++)
        {
            mainFrame.actionReportWork(this, (line * 100) / lb.lineCount());
            MethodOffsetOut injection = null;
            Object lRef = lb.getReference(line);
            String methId =
                    method.getName() + method.getDescriptor();
            if (lRef instanceof LineBuilderFormatter.Return)
            {
                int returnPc = ((LineBuilderFormatter.Return) lRef).pc;
                String txt = methId + " return @ " + Integer.toHexString(returnPc);
                injection =
                        new MethodOffsetOut(methId, Bytecode.getBytecodeIndexForOffset(method, returnPc),
                                txt);
                insertPrintInDoc(lb, line - 1, txt, injection);
                line++;
            }
            else if (lRef instanceof LineBuilderFormatter.Label)
            {
                int pc = ((LineBuilderFormatter.Label) lRef).pc;
                String label = ((LineBuilderFormatter.Label) lRef).label;
                int bcIx = Bytecode.getBytecodeIndexForOffset(method, pc);
                if (bcIx > 0)
                {
                    String txt = methId + " -" + label;
                    BytecodeModificationMediator.getInstance().registerModification(
                            method.getMEClass().getResource().getContext(),
                            method.getMEClass(),
                            new MethodOffsetOut(methId, bcIx - 1, txt),
                            method);
                    insertPrintInDoc(lb, line - 1, txt, injection);
                    line++;
                }
                String txt = methId + " +" + label;
                injection = new MethodOffsetOut(methId, bcIx, txt);
                insertPrintInDoc(lb, line, txt, injection);
                line++;
            }
            else if (lRef instanceof LineBuilderFormatter.Finally)
            {
                int pc = ((LineBuilderFormatter.Finally) lRef).pc;
                String txt = methId + " finally @ " + Integer.toHexString(pc);
                injection =
                        new MethodOffsetOut(methId, Bytecode.getBytecodeIndexForOffset(method, pc),
                                txt);
                insertPrintInDoc(lb, line, txt, injection);
                line++;
            }
            else if (lRef instanceof LineBuilderFormatter.Catch)
            {
                int pc = ((LineBuilderFormatter.Catch) lRef).pc;
                String txt = methId + " catch @ " + Integer.toHexString(pc);
                injection =
                        new MethodOffsetOut(methId, Bytecode.getBytecodeIndexForOffset(method, pc),
                                txt);
                insertPrintInDoc(lb, line, txt, injection);
                line++;
            }
            if (injection != null)
            {
                BytecodeModificationMediator.getInstance().registerModification(
                        method.getMEClass().getResource().getContext(),
                        method.getMEClass(),
                        injection,
                        method);
            }
        }

        if (isRunning())
        {
            mainFrame.actionFinished(this);
        }
        text.updateDocument();
        tree.findAndMarkNode(method, Reference.MODIFIED);
        tree.repaint();
    }

    protected void insertPrintInDoc(LineBuilder lb, int line, String txt, Injection injection) {
        lb.insertLineBefore(line + 1);
        lb.append("        >>>    ", 0xbb0000);
        lb.append("PRINT(", 0x000000);
        lb.append("\"" + txt + "\"", 0x0000bb);
        lb.append(")", 0x000000);
        lb.setReferenceToCurrent(injection);

    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        // not used
        return null;
    }
}