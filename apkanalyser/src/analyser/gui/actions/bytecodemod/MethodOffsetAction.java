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
import javax.swing.JOptionPane;
import javax.swing.JViewport;

import analyser.gui.ClassTree;
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

public class MethodOffsetAction extends AbstractTreeBytecodeModAction
{
    private static final long serialVersionUID = 5436439203982339701L;
    protected static MethodOffsetAction m_inst = null;

    public static MethodOffsetAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new MethodOffsetAction("Add printout to offset", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected MethodOffsetAction(String arg0, Icon arg1)
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
        Object ref = Selection.getSelectedObject();
        MEMethod method = (MEMethod) text.getOwnerData();

        if (ref == null || !LineBuilderFormatter.BytecodeOffset.class.isAssignableFrom(ref.getClass())) {
            return;
        }
        int bytecodeOffset = ((LineBuilderFormatter.BytecodeOffset) ref).pc;

        String output = (String) JOptionPane.showInputDialog(
                getMainFrame(),
                "", "Print string",
                JOptionPane.QUESTION_MESSAGE, null, null,
                method.getMEClass().getName() + ":" + method.getName() + "@" + Integer.toHexString(bytecodeOffset));
        if (output != null && output.trim().length() > 0)
        {
            int bcIx = Bytecode.getBytecodeIndexForOffset(method, bytecodeOffset);
            MethodOffsetOut injection =
                    new MethodOffsetOut(method.getName() + method.getDescriptor(),
                            bcIx <= 0 ? bcIx : bcIx - 1, output);
            BytecodeModificationMediator.getInstance().registerModification(
                    method.getMEClass().getResource().getContext(),
                    method.getMEClass(),
                    injection,
                    method);
            if (isRunning())
            {
                mainFrame.actionFinished(this);
            }
            tree.findAndMarkNode(method, Reference.MODIFIED);
            tree.repaint();
            int pos = text.getCaretPosition();
            JViewport view = text.getScrollPane().getViewport();
            text.getLineBuilder().insertLineBefore(text.getCurrentLine());
            text.getLineBuilder().append("        >>>    ", 0xbb0000);
            text.getLineBuilder().append("PRINT(", 0x000000);
            text.getLineBuilder().append("\"" + output + "\"", 0x0000bb);
            text.getLineBuilder().append(")", 0x000000);
            text.getLineBuilder().setReferenceToCurrent(injection);
            text.updateDocument();
            text.setCaretPosition(pos);
            text.getScrollPane().setViewport(view);
        } else {
            mainFrame.getSelectedTree().repaint();
        }
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        // not used
        return null;
    }
}