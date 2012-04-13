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

package analyser.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import mereflect.CorruptBytecodeException;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEMethod;
import mereflect.UnknownMethod;

import org.jf.dexlib.Code.Instruction;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.TextDialog;
import analyser.logic.RefMethod;
import andreflect.DexMethod;
import andreflect.gui.linebuilder.DalvikByteCodeLineBuilder;

public class ShowBytecodeAction extends AbstractAction {
    private static final long serialVersionUID = -1200143170943192070L;
    static final String PREFIX = "        ";

    protected static ShowBytecodeAction inst = null;
    protected MainFrame mainFrame;
    protected MEClassContext classContext;
    protected MEClass clazz;
    protected MEMethod method;
    protected RefMethod refMethod;
    protected TextDialog textDialog;

    public static ShowBytecodeAction getInstance(MainFrame mainFrame) {
        if (inst == null) {
            inst = new ShowBytecodeAction("View bytecodes", null);
            inst.mainFrame = mainFrame;
        }
        return inst;
    }

    protected ShowBytecodeAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        method = null;
        LineBuilder result = null;
        int pc = Selection.getPc();
        method = Selection.getMEMethod();
        Instruction instruction = Selection.getDalvikInstruction();
        if (method == null
                || method instanceof UnknownMethod) {
            return;
        }
        clazz = method.getMEClass();
        classContext = clazz.getResource().getContext();
        LineBuilder classLines = LineBuilderFormatter.makeOutline(clazz);
        classLines.blendLines(0xffffff, 50);
        int lineNbr = classLines.getLine(method);
        try {
            LineBuilder codeLines;
            if (method instanceof DexMethod) {
                codeLines = DalvikByteCodeLineBuilder.getByteCodeAssembler((DexMethod) method, PREFIX);
            } else {
                codeLines = LineBuilderFormatter.getByteCodeAssembler(method, PREFIX);
            }
            if (lineNbr >= 0) {
                classLines.insertAfter(lineNbr, codeLines);
                classLines.insertLineAfter(lineNbr + codeLines.lineCount());
                result = classLines;
            } else {
                result = codeLines;
            }
        } catch (CorruptBytecodeException cbe) {
            mainFrame.showError(method.toString(), "Bytecode is corrupt (index: " + cbe.getPc() + ")");
            return;
        }
        if (result != null) {
            textDialog = mainFrame.showText("Examine method [" + method.toString() + "]", result);
            if (instruction == null
                    && pc < 0
                    && method instanceof DexMethod
                    && !method.isAbstract()) {
                DexMethod dexMethod = (DexMethod) method;
                instruction = dexMethod.getInstructionAtCodeAddress(0);
            }

            if (instruction != null) {
                LineBuilder lb = textDialog.getLineBuilder();
                for (int i = 0; i < lb.lineCount(); i++) {
                    Object ref = lb.getReference(i);
                    if (ref instanceof DalvikByteCodeLineBuilder.DalvikBytecodeOffset
                            && ((DalvikByteCodeLineBuilder.DalvikBytecodeOffset) ref).instruction == instruction) {
                        lb.gotoLine(i);
                        textDialog.findNext(lb.currentLineString());
                    }
                }
            } else if (pc >= 0) {
                int line = -1;
                LineBuilder lb = textDialog.getLineBuilder();
                for (int i = 0; i < lb.lineCount(); i++) {
                    Object ref = lb.getReference(i);
                    if (ref instanceof LineBuilderFormatter.BytecodeOffset) {
                        line = i;
                        break;
                    } else if (ref instanceof DalvikByteCodeLineBuilder.DalvikBytecodeOffset) {
                        line = i;
                        break;
                    }
                }
                if (line >= 0) {
                    textDialog.findNext(PREFIX + Integer.toHexString(pc) + ' '); // TODO fix up this sordid stuff
                }
            }
            textDialog.setOwnerData(method);
        }
    }

    public MEClass getMEClass() {
        return clazz;
    }

    /**
     * @return
     */
    public MEClassContext getClassContext() {
        return classContext;
    }

    /**
     * @return
     */
    public MEMethod getMethod() {
        return method;
    }

    /**
     * @return
     */
    public TextDialog getTextDialog() {
        return textDialog;
    }
}