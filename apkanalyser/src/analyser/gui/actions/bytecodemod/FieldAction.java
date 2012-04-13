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
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JViewport;

import analyser.gui.ClassTree;
import analyser.gui.FieldDialog;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.TextBuilder;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.InjectionMethodOffset;
import jerl.bcm.inj.impl.MethodOffsetFieldArrayOut;
import jerl.bcm.inj.impl.MethodOffsetFieldOut;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.Type;
import mereflect.bytecode.Bytecode;
import mereflect.io.DescriptorParser;
import mereflect.primitives.MEArray;

public class FieldAction extends AbstractTreeBytecodeModAction {
    private static final long serialVersionUID = -7338862962364584726L;
    protected static FieldAction m_inst = null;

    public static FieldAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new FieldAction("Print a local variable", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected FieldAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        ClassTree tree = ((MainFrame) getMainFrame()).getSelectedTree();
        if (!(Selection.getSelectedView() instanceof TextBuilder)) {
            return;
        }
        TextBuilder text = (TextBuilder) Selection.getSelectedView();
        Object ref = text.getLineBuilder().getReference(text.getCurrentLine());

        MainFrame mainFrame = (MainFrame) getMainFrame();
        if (ref != null && LineBuilderFormatter.BytecodeOffset.class.isAssignableFrom(ref.getClass())) {
            int bytecodeOffset = ((LineBuilderFormatter.BytecodeOffset) ref).pc;
            MEMethod method = (MEMethod) text.getOwnerData();

            MEField[] meFields = method.getMEClass().getFields();
            if (meFields != null && meFields.length > 0) {
                Vector<MEField> fields = new Vector<MEField>();
                for (int i = 0; i < meFields.length; i++) {
                    fields.add(meFields[i]);
                }
                FieldDialog fd = new FieldDialog(
                        mainFrame, method.getMEClass(), "Select field",
                        method.getMEClass().getName() + ":" + method.getName() + "@" +
                                Integer.toHexString(((LineBuilderFormatter.BytecodeOffset) ref).pc), fields,
                        this);
                fd.setSize(700, 500);
                int x = mainFrame.getLocationOnScreen().x + (mainFrame.getWidth() - fd.getWidth()) / 2;
                int y = mainFrame.getLocationOnScreen().y + (mainFrame.getHeight() - fd.getHeight()) / 2;
                fd.setLocation(Math.max(0, x), Math.max(0, y));
                fd.setVisible(true);
                fd.setModal(false);
                fd.awaitAcknowledge();
                String output = fd.getOutput();
                MEField field = fd.getChosenField();

                InjectionMethodOffset[] injections = null;
                if (output != null && output.trim().length() > 0 && field != null && isRunning()) {
                    if (output.endsWith("=")) {
                        output = output.substring(0, output.length() - 1);
                    }
                    StringBuffer fieldType = new StringBuffer();
                    if (field.getType().isArray() && fd.getArraySpecification() != FieldDialog.INSTANCE) {
                        Type arrayType = ((MEArray) field.getType()).getArrayType();
                        DescriptorParser.processType(field.getType(), fieldType);
                        StringBuffer fieldPrintType = new StringBuffer();
                        DescriptorParser.processType(arrayType, fieldPrintType);
                        if (fd.getArraySpecification() == FieldDialog.ELEMENTS) {
                            int[] ixs = fd.getArrayIndicesSpecification();
                            injections = new InjectionMethodOffset[ixs.length];
                            for (int i = 0; i < ixs.length; i++) {
                                injections[i] =
                                        new MethodOffsetFieldArrayOut(method.getName() + method.getDescriptor(),
                                                Bytecode.getBytecodeIndexForOffset(method, bytecodeOffset),
                                                method.getMEClass().getRawName(),
                                                field.getName(),
                                                fieldType.toString(),
                                                arrayType.isPrimitive() ? fieldPrintType.toString() : "Ljava/lang/Object;",
                                                output + "[" + ixs[i] + "]=", field.isStatic(), ixs[i]);
                            }
                        } else if (fd.getArraySpecification() == FieldDialog.ALL_ELEMENTS) {
                            // TODO implement
                        }
                    } else {
                        DescriptorParser.processType(field.getType(), fieldType);
                        injections = new InjectionMethodOffset[1];
                        injections[0] =
                                new MethodOffsetFieldOut(method.getName() + method.getDescriptor(),
                                        Bytecode.getBytecodeIndexForOffset(method, bytecodeOffset),
                                        method.getMEClass().getRawName(),
                                        field.getName(),
                                        fieldType.toString(),
                                        field.getType().isPrimitive() ? fieldType.toString() : "Ljava/lang/Object;",
                                        output + "=", field.isStatic());
                    }
                    for (int i = 0; injections != null && i < injections.length; i++) {
                        BytecodeModificationMediator.getInstance().registerModification(
                                method.getMEClass().getResource().getContext(),
                                method.getMEClass(), injections[i], method);
                    }
                    if (isRunning()) {
                        mainFrame.actionFinished(this);
                    }
                    if (injections != null && isRunning()) {
                        boolean arrayElements =
                                field.getType().isArray() && fd.getArraySpecification() != FieldDialog.INSTANCE &&
                                        fd.getArraySpecification() == FieldDialog.ELEMENTS;
                        tree.findAndMarkNode(method, Reference.MODIFIED);
                        tree.repaint();
                        int pos = text.getCaretPosition();
                        JViewport view = text.getScrollPane().getViewport();
                        int[] ixs = fd.getArrayIndicesSpecification();
                        for (int i = 0; i < injections.length; i++) {
                            text.getLineBuilder().insertLineBefore(text.getCurrentLine() + i);
                            text.getLineBuilder().append("        >>>    ", 0xbb0000);
                            text.getLineBuilder().append("PRINT(", 0x000000);
                            text.getLineBuilder().append("\"" + output + "=\"", 0x0000bb);
                            text.getLineBuilder().append(" + ", 0x000000);
                            text.getLineBuilder().append(field.getName(), 0x000000);
                            if (arrayElements) {
                                text.getLineBuilder().append("[" + ixs[i] + "]", 0xbb0000);
                            }
                            text.getLineBuilder().append(")", 0x000000);
                            text.getLineBuilder().setReferenceToCurrent(injections[i]);
                        }
                        text.updateDocument();
                        text.setCaretPosition(pos);
                        text.getScrollPane().setViewport(view);
                    }
                }
            } else {
                mainFrame.getSelectedTree().repaint();
            }
        }
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        // not used
        return null;
    }
}