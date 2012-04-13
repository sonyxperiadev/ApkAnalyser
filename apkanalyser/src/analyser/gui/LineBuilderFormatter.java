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

package analyser.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;

import mereflect.BytecodeVisitor;
import mereflect.CorruptBytecodeException;
import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.Type;
import mereflect.bytecode.Bytecode;
import mereflect.bytecode.Bytecodes;
import mereflect.info.AbstractRefInfo;
import mereflect.info.AiCode;
import mereflect.info.CiClass;
import mereflect.info.CiDouble;
import mereflect.info.CiFieldRef;
import mereflect.info.CiFloat;
import mereflect.info.CiInteger;
import mereflect.info.CiLong;
import mereflect.info.CiNameAndType;
import mereflect.info.CiString;
import mereflect.info.CiUtf8;
import mereflect.info.ClassInfo;
import mereflect.io.DescriptorParser;
import andreflect.DexMethod;

public class LineBuilderFormatter {
    public static final int COLOR_KEYWORD = 0x880088;
    public static final int COLOR_TEXT = 0x000000;
    public static final int COLOR_STATIC = 0x0000bb;

    public static final int COLOR_HEX = 0x008800;
    public static final int COLOR_PC = 0x888888;
    public static final int COLOR_OPCODE = 0x000000;
    public static final int COLOR_LABEL = 0x000088;
    public static final int COLOR_COMMENT = 0x888800;

    public static final int COLOR_ERROR = 0xff0000;

    static Map<Integer, String> labels;
    static int gotoLabelIx = 0;

    static class FormatVisitor implements BytecodeVisitor {
        LineBuilder lb;
        MEClass clazz;
        MEMethod method;
        ClassInfo[] cp;
        byte[] code;
        String prefix;
        ProgressReporter r;

        public FormatVisitor(LineBuilder lb, MEMethod method, String prefix, ProgressReporter r) {
            this.lb = lb;
            clazz = method.getMEClass();
            this.method = method;
            code = method.getByteCodes();
            cp = clazz.getConstantPool();
            this.prefix = prefix;
        }

        @Override
        public void visit(int pc, int bytecode, int len) throws CorruptBytecodeException {
            lb.append(" ", COLOR_TEXT);
            appendHex(code, lb, pc, len);
        }

        @Override
        public void visitConstantPool(int pc, int bytecode, int len, int cpIndex) throws CorruptBytecodeException {
            StringBuffer resolved = new StringBuffer();
            lb.append(' ');
            appendHex(code, lb, pc, len);
            ClassInfo ci = cp[cpIndex];
            int ciTag = ci.getTag();
            switch (ciTag) {
            case ClassInfo.CONSTANT_Integer:
                resolved.append(((CiInteger) ci).getInteger());
                break;
            case ClassInfo.CONSTANT_Float:
                resolved.append(((CiFloat) ci).getFloat());
                break;
            case ClassInfo.CONSTANT_Long:
                resolved.append(((CiLong) ci).getLong());
                break;
            case ClassInfo.CONSTANT_Double:
                resolved.append(((CiDouble) ci).getDouble());
                break;
            case ClassInfo.CONSTANT_String:
                resolved.append('\"');
                resolved.append(((CiUtf8) cp[((CiString) ci).getStringIndex()]).getUtf8());
                resolved.append('\"');
                break;
            }
            lb.append("\t// " + resolved, COLOR_COMMENT);
        }

        @Override
        public void visitInvokation(int pc, int bytecode, int len, int cpIndex) throws CorruptBytecodeException {
            lb.append(' ');
            appendHex(code, lb, pc, len);
            AbstractRefInfo methodRef = (AbstractRefInfo) cp[cpIndex];

            CiClass ciClass = (CiClass) cp[methodRef.getClassIndex()];
            String classname = ((CiUtf8) cp[ciClass.getNameIndex()]).getUtf8().replace('/', '.');
            CiNameAndType ciNameType = (CiNameAndType) cp[methodRef.getNameAndTypeIndex()];
            String methname = ((CiUtf8) cp[ciNameType.getNameIndex()]).getUtf8();
            String descriptor = ((CiUtf8) cp[ciNameType.getDescriptorIndex()]).getUtf8();
            try {
                MEClass oClass = clazz.getResource().getContext().getMEClass(classname);
                MEMethod[] cands = oClass.getMethods(methname);
                MEMethod m = null;
                for (int i = 0; i < cands.length; i++) {
                    if (cands[i].getDescriptor().equals(descriptor)) {
                        m = cands[i];
                        break;
                    }
                }
                lb.append("\t// " + classname + "." + m.getName() + "("
                        + m.getArgumentsString() + ")" + m.getReturnClassString(), COLOR_COMMENT);
            } catch (Exception e) {
                String args = "?";
                String retClass = "?";
                try {
                    args = method.getArgumentsString(method.getArguments(new StringBuffer(descriptor)));
                } catch (Exception e2) {
                }
                try {
                    retClass = method.getReturnClass(new StringBuffer(descriptor)).toString();
                } catch (Exception e2) {
                }

                lb.append("\t// " + classname + "." + methname + "(" + args + ")"
                        + retClass, COLOR_COMMENT);
            }
        }

        @Override
        public void visitJump(int pc, int bytecode, int len, short relJump) throws CorruptBytecodeException {
            int pcDest = pc + relJump;
            String label = getLabel(pcDest);
            lb.append(" " + label, COLOR_LABEL);
            lb.append(" (", COLOR_HEX);
            appendHex(code, lb, pc, len);
            lb.append(")", COLOR_HEX);
        }

        @Override
        public void visitLocalFieldName(int pc, int bytecode, int len, int cpIndex) throws CorruptBytecodeException {
            lb.append(" ", COLOR_TEXT);
            appendHex(code, lb, pc, len);
            CiFieldRef ciFieldRef = (CiFieldRef) cp[cpIndex];
            CiNameAndType ciNameType = (CiNameAndType) cp[ciFieldRef.getNameAndTypeIndex()];

            String name = ((CiUtf8) cp[ciNameType.getNameIndex()]).getUtf8();
            String descr = ((CiUtf8) cp[ciNameType.getDescriptorIndex()]).getUtf8();
            Type type = null;
            try {
                type = DescriptorParser.processTypeDescriptor(clazz, new StringBuffer(descr));
            } catch (IOException ignore) {
            }
            lb.append("\t// ", COLOR_COMMENT);
            if (type != null) {
                lb.append(type.toString(), COLOR_COMMENT);
            }
            lb.append(" " + name, COLOR_COMMENT);
        }

        @Override
        public void visitLookupSwitch(int pc, int bytecode, int len, Map<Object, Object> zwitch) throws CorruptBytecodeException {
            int pairs = ((Integer) zwitch.get(Bytecode.SWITCH_PAIRS)).intValue();
            int def = ((Integer) zwitch.get(Bytecode.SWITCH_DEFAULT)).intValue();
            for (int i = 0; i < pairs; i++) {
                Bytecode.Pair pair = (Bytecode.Pair) zwitch.get(new Integer(i));
                int caze = pair.vCase;
                int jmp = pair.vJump;
                lb.newLine();
                lb.append(prefix, COLOR_TEXT);
                lb.append("          case ", COLOR_KEYWORD);
                lb.append("0x" + Integer.toHexString(caze), COLOR_HEX);
                lb.append(" : ", COLOR_TEXT);
                String label = getLabel(jmp + pc);
                lb.append(label, COLOR_LABEL);
                lb.append(" (0x" + Integer.toHexString(jmp + pc) + ")", COLOR_HEX);
            }
            lb.newLine();
            lb.append(prefix, COLOR_TEXT);
            lb.append("          default", COLOR_KEYWORD);
            lb.append("  : ", COLOR_TEXT);
            String label = getLabel(def + pc);
            lb.append(label, COLOR_LABEL);
            lb.append(" (0x" + Integer.toHexString(def + pc) + ")", COLOR_HEX);
        }

        @Override
        public void visitTableSwitch(int pc, int bytecode, int len, Map<Object, Object> zwitch) throws CorruptBytecodeException {
            int def = ((Integer) zwitch.get(Bytecode.SWITCH_DEFAULT)).intValue();
            int low = ((Integer) zwitch.get(Bytecode.SWITCH_LOW)).intValue();
            int high = ((Integer) zwitch.get(Bytecode.SWITCH_HIGH)).intValue();
            for (int i = low; i <= high; i++) {
                int jmp = ((Integer) zwitch.get(new Integer(i))).intValue();
                lb.newLine();
                lb.append(prefix, COLOR_TEXT);
                lb.append("          case ", COLOR_KEYWORD);
                lb.append("0x" + Integer.toHexString(i), COLOR_HEX);
                lb.append(" : ", COLOR_TEXT);
                String label = getLabel(jmp + pc);
                lb.append(label, COLOR_LABEL);
                lb.append(" (0x" + Integer.toHexString(jmp + pc) + ")", COLOR_HEX);
            }
            lb.newLine();
            lb.append(prefix, COLOR_TEXT);
            lb.append("          default", COLOR_KEYWORD);
            lb.append("  : ", COLOR_TEXT);
            String label = getLabel(def + pc);
            lb.append(label, COLOR_LABEL);
            lb.append(" (0x" + Integer.toHexString(def + pc) + ")", COLOR_HEX);
        }

        @Override
        public void visitNewBytecode(int pc, int bytecode) throws CorruptBytecodeException {
            lb.newLine();
            if (bytecode >= 172 && bytecode <= 177)
            {
                // ?return
                lb.setReferenceToCurrent(new Return(pc));
            }
            else
            {
                lb.setReferenceToCurrent(new BytecodeOffset(pc));
            }
            lb.append(prefix, COLOR_TEXT);
            String pcStr = Integer.toHexString(pc);
            lb.append(pcStr, COLOR_PC);
            for (int i = 6 - pcStr.length(); i >= 0; i--) {
                lb.append(' ');
            }
            String op = Bytecodes.BC_OPCODES[bytecode];
            lb.append(op, COLOR_OPCODE);
            if (r != null) {
                r.reportWork(pc);
            }
        }
    }

    static String getLabel(int pcDest) {
        String label = labels.get(new Integer(pcDest));
        if (label == null) {
            label = "LABEL_" + (gotoLabelIx++);
            labels.put(new Integer(pcDest), label);
        }
        return label;
    }

    public static LineBuilder getByteCodeAssembler(MEMethod method, String prefix)
            throws CorruptBytecodeException {
        return getByteCodeAssembler(method, prefix, null);
    }

    public static LineBuilder getByteCodeAssembler(MEMethod method, String prefix, ProgressReporter r)
            throws CorruptBytecodeException {
        LineBuilder lb = new LineBuilder();
        lb.newLine();
        labels = new HashMap<Integer, String>();
        gotoLabelIx = 0;

        if (r != null) {
            r.reportStart(method.countBytecodeBytes());
        }

        method.traverseBytecodes(new FormatVisitor(lb, method, prefix, r));

        if (r != null) {
            r.reportEnd();
        }

        MEClass clazz = method.getMEClass();
        // insert try/catch/finally blocks
        AiCode aiCode = (AiCode) method.getAttributeInfo(method.getAttributes(), AiCode.class);
        if (aiCode != null) {
            AiCode.ExceptionSpec[] exceptionSpecs = aiCode.getExceptions();
            for (int i = 0; exceptionSpecs != null && i < exceptionSpecs.length; i++) {
                AiCode.ExceptionSpec exSpec = exceptionSpecs[i];
                int lineNbrStart = lb.getLine(new BytecodeOffset(exSpec.getStartPc()));
                int catchType = exSpec.getCatchType();

                lb.insertLineBefore(lineNbrStart);
                lb.append("TRY_" + i + ":", COLOR_LABEL);
                lb.setReference(lineNbrStart, new TryStart());
                int lineNbrHandler = lb.getLine(new BytecodeOffset(exSpec.getHandlerPc()));

                if (catchType == 0) {
                    // finally
                    lb.insertLineBefore(lineNbrHandler);
                    lb.setReference(lineNbrHandler, new Finally(exSpec.getHandlerPc()));
                    lb.append("FINALLY_" + i + ":", COLOR_LABEL);
                } else {
                    ClassInfo[] cp = clazz.getConstantPool();
                    CiClass ciClass = (CiClass) cp[catchType];
                    String classname = ((CiUtf8) cp[ciClass.getNameIndex()]).getUtf8()
                            .replace('/', '.');
                    lb.insertLineBefore(lineNbrHandler);
                    lb.setReference(lineNbrHandler, new Catch(exSpec.getHandlerPc()));
                    lb.append("CATCH_" + i + "(" + classname + "):", COLOR_LABEL);
                }
                int lineNbrEnd = lb.getLine(new BytecodeOffset(exSpec.getEndPc()));
                lb.insertLineBefore(lineNbrEnd);
                lb.setReference(lineNbrEnd, new TryEnd());
                lb.append(":TRY_" + i, COLOR_LABEL);
            }
        }

        // insert labels
        Iterator<Integer> pcIter = labels.keySet().iterator();
        while (pcIter.hasNext()) {
            Integer pcKey = pcIter.next();
            String label = labels.get(pcKey);
            int lineNbr = lb.getLine(new BytecodeOffset(pcKey.intValue()));
            if (lineNbr >= 0) {
                lb.insertLineBefore(lineNbr);
                lb.append(label + ":", COLOR_LABEL);
                lb.setReference(lineNbr, new Label(pcKey.intValue(), label));
            }
        }
        return lb;
    }

    protected static void appendHex(byte[] code, LineBuilder lb, int pc, int len) {
        lb.append("0x", COLOR_HEX);
        for (int i = 1; i < len; i++) {
            int data = (code[pc + i] & 0xff);
            String hex = Integer.toHexString(data);
            if (data < 0x10) {
                lb.append('0', COLOR_HEX);
            }
            lb.append(hex, COLOR_HEX);
        }
    }

    protected static void makeOutlineArguments(MEMethod method, LineBuilder lb) {
        try {
            Type[] args = method.getArguments();
            for (int i = 0; i < args.length; i++) {
                lb.append(args[i], args[i].isPrimitive() ? COLOR_KEYWORD : COLOR_TEXT);
                if (method instanceof DexMethod) {
                    lb.append(" ", COLOR_TEXT);
                    lb.append(((DexMethod) method).getParameterName(i, args.length), COLOR_HEX);
                }
                if (i < args.length - 1) {
                    lb.append(", ", COLOR_TEXT);
                }
            }
        } catch (Exception e) {
            lb.append("?", COLOR_ERROR);
        }
    }

    public static void makeOutline(MEMethod method, LineBuilder lb) {
        if (method.isStatic()) {
            lb.append("static ", COLOR_KEYWORD);
        }
        if (method.isSynchronized()) {
            lb.append("synchronized ", COLOR_KEYWORD);
        }
        if (method.isNative()) {
            lb.append("native ", COLOR_KEYWORD);
        }
        if (method.isStrict()) {
            lb.append("strict ", COLOR_KEYWORD);
        }
        if (method.isPrivate()) {
            lb.append("private ", COLOR_KEYWORD);
        } else if (method.isProtected()) {
            lb.append("protected ", COLOR_KEYWORD);
        } else if (method.isPublic()) {
            lb.append("public ", COLOR_KEYWORD);
        }
        if (method.isFinal()) {
            lb.append("final ", COLOR_KEYWORD);
        }
        if (method.isAbstract()) {
            lb.append("abstract ", COLOR_KEYWORD);
        }
        if (!method.isConstructor()) {
            try {
                lb.append(method.getReturnClassString(), method.getReturnClass()
                        .isPrimitive() ? COLOR_KEYWORD : COLOR_TEXT);
            } catch (IOException ioe) {
                lb.append('?', COLOR_ERROR);
            }
            lb.append(' ', COLOR_TEXT);
        }
        lb.append(method.getFormattedName(), COLOR_TEXT);
        lb.append('(', COLOR_TEXT);
        makeOutlineArguments(method, lb);
        lb.append(')', COLOR_TEXT);
        if (method.declaresExceptions()) {
            lb.append(" throws ", COLOR_KEYWORD);
            lb.append(method.getExceptionsString(), COLOR_TEXT);
        }
    }

    public static void makeOutline(MEField field, LineBuilder lb) {
        if (field.isPrivate()) {
            lb.append("private ", COLOR_KEYWORD);
        } else if (field.isProtected()) {
            lb.append("protected ", COLOR_KEYWORD);
        } else if (field.isPublic()) {
            lb.append("public ", COLOR_KEYWORD);
        }
        if (field.isStatic()) {
            lb.append("static ", COLOR_KEYWORD);
        }
        if (field.isFinal()) {
            lb.append("final ", COLOR_KEYWORD);
        }
        if (field.isVolatile()) {
            lb.append("volatile ", COLOR_KEYWORD);
        }
        if (field.isTransient()) {
            lb.append("transient ", COLOR_KEYWORD);
        }
        lb.append(field.getType(), field.getType().isPrimitive() ? COLOR_KEYWORD
                : COLOR_TEXT);
        lb.append(' ');
        lb.append(field.getName(), field.isStatic() ? COLOR_STATIC : COLOR_TEXT);
        String cval = field.getConstantValueString();
        if (cval != null) {
            lb.append(" = " + cval, COLOR_TEXT);
        }
        lb.append(';', COLOR_TEXT);
    }

    public static LineBuilder makeOutline(MEClass clazz) {
        LineBuilder lb = new LineBuilder();
        lb.newLine();
        String pakkage = clazz.getResource().getPackage();
        if (pakkage != null && pakkage.length() > 0) {
            lb.append("package ", COLOR_KEYWORD);
            lb.append(pakkage + ";", COLOR_TEXT);
            lb.newLine();
            lb.newLine();
        }

        if (clazz.isInterface()) {
            lb.append("interface", COLOR_KEYWORD);
        } else {
            lb.append("class", COLOR_KEYWORD);
        }
        lb.append(' ');
        lb.append(clazz.getClassName(), COLOR_TEXT);
        if (!clazz.isInterface()) {
            MEClass superClazz = clazz.getSuperClass();
            if (superClazz != null && !superClazz.getName().equals("java.lang.Object")) {
                lb.append(" extends ", COLOR_KEYWORD);
                lb.append(superClazz.getName(), COLOR_TEXT);
            }
        }
        Type[] ifcs = clazz.getInterfaces();
        if (ifcs.length > 0) {
            lb.newLine();
            for (int i = 0; i < ifcs.length; i++) {
                if (i == 0) {
                    if (!clazz.isInterface()) {
                        lb.append("    implements ", COLOR_KEYWORD);
                    } else {
                        lb.append("    extends ", COLOR_KEYWORD);
                    }
                } else {
                    lb.append("    ", COLOR_TEXT);
                }
                lb.append(ifcs[i].getName(), COLOR_TEXT);
                if (i < ifcs.length - 1) {
                    lb.append(',', COLOR_TEXT);
                }
                lb.newLine();
            }
        } else {
            lb.newLine();
        }
        lb.append("{", COLOR_TEXT);
        lb.newLine();
        MEField[] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            lb.append("    ");
            makeOutline(fields[i], lb);
            lb.setReferenceToCurrent(fields[i]);
            lb.newLine();
        }
        if (fields.length > 0) {
            lb.newLine();
        }
        MEMethod[] meths = clazz.getMethods();
        for (int i = 0; i < meths.length; i++) {
            lb.append("    ", COLOR_TEXT);
            makeOutline(meths[i], lb);
            lb.append(';', COLOR_TEXT);
            lb.setReferenceToCurrent(meths[i]);
            lb.newLine();
        }
        lb.append("}", COLOR_TEXT);
        return lb;
    }

    public abstract static class Identifier {
    }

    public static class BytecodeOffset extends Identifier {
        public final int pc;

        public BytecodeOffset(int pc) {
            this.pc = pc;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof BytecodeOffset ? ((BytecodeOffset) o).pc == pc : false;
        }

        @Override
        public int hashCode() {
            return pc & 0x3f;
        }
    }

    public static class Return extends BytecodeOffset {
        public Return(int pc) {
            super(pc);
        }
    }

    public static class Label extends Identifier {
        public final int pc;
        public final String label;

        public Label(int pc, String label) {
            this.pc = pc;
            this.label = label;
        }
    }

    public static class Link extends Identifier {
        Object[] data;
        Action link;

        public Link(Action linkedAction, Object[] data) {
            link = linkedAction;
            this.data = data;
        }

        public Object[] getData() {
            return data;
        }

        public Action getLinkedAction() {
            return link;
        }
    }

    public static class QuickLink extends Link {
        public QuickLink(Action linkedAction, Object[] data) {
            super(linkedAction, data);
        }
    }

    public static class Try extends Identifier {
    }

    public static class TryStart extends Try {
    }

    public static class TryEnd extends Try {
    }

    public static class Catch extends Identifier {
        public final int pc;

        public Catch(int pc) {
            this.pc = pc;
        }
    }

    public static class Finally extends Catch {
        public Finally(int pc) {
            super(pc);
        }
    }
}
