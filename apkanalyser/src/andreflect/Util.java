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

package andreflect;

import javax.swing.JViewport;

import mereflect.MEMethod;
import mereflect.Type;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;

import analyser.gui.Selection;
import analyser.gui.TextBuilder;
import andreflect.gui.linebuilder.DalvikByteCodeLineBuilder;
import andreflect.injection.abs.DalvikInjectionMethod;

public final class Util {

    public static String shortenClassName(String str) {
        String ret = str;
        if (str.lastIndexOf(".") != -1) {
            ret = ret.substring(ret.lastIndexOf(".") + 1);
        }

        if (str.lastIndexOf("$") != -1) {
            ret = ret.substring(ret.lastIndexOf("$") + 1);
        }
        return ret;
    }

    public static String getClassRawName(String classDescriptor) {
        String res = new String(classDescriptor);
        int i = 0;
        while (classDescriptor.charAt(i) == '[' && i < res.length()) {
            i++;
        }

        if (i == res.length()) {
            System.out.println("Unrecognized class descriptor" + res);
        } else if (i == res.length() - 1) {
            switch (classDescriptor.charAt(i)) {
            case 'I'://int
            case 'Z'://boolean
            case 'B'://byte
            case 'C'://char
            case 'F'://float
            case 'S'://short
            case 'D'://double
            case 'J'://long
                break;
            case 'L'://object
            default:
                System.out.println("Unrecognized class descriptor" + res);
                break;
            }
        } else if (res.charAt(i) != 'L' ||
                res.charAt(res.length() - 1) != ';') {
            System.out.println("Unrecognized class descriptor" + res);
        } else {
            res = res.substring(i + 1, res.length() - 1);
        }
        return res;
    }

    public static String getClassRawName(ClassDefItem classDefItem) {
        String res = new String(classDefItem.getClassType().getTypeDescriptor());
        return getClassRawName(res);
    }

    public static String getClassRawName(TypeIdItem typeIdItem) {
        String res = new String(typeIdItem.getTypeDescriptor());
        return getClassRawName(res);
    }

    public static String getClassName(ClassDefItem classDefItem) {
        return getClassRawName(classDefItem).replace('/', '.');
    }

    public static String getClassName(String classDescriptor) {
        return getClassRawName(classDescriptor).replace('/', '.');
    }

    public static String getClassName(TypeIdItem typeIdItem) {
        return getClassRawName(typeIdItem).replace('/', '.');
    }

    public static String getProtoString(String descr) {
        StringBuffer sb = new StringBuffer();
        int arrayDepth = 0;
        boolean first = true;
        char c;
        for (int i = 0; i < descr.length(); i++) {

            c = descr.charAt(i);
            switch (c) {
            case Type.CH_ARRAY:
                //if (first == false){ sb.append(", "); }
                i++;

                arrayDepth = 1;
                while ((c = descr.charAt(i)) == Type.CH_ARRAY) {
                    arrayDepth++;
                    i++;
                }
                i--;
                break;
            case Type.CH_CLASS_PRE:
                if (first == false) {
                    sb.append(", ");
                }
                i++;

                while ((c = descr.charAt(i)) != Type.CH_CLASS_POST
                        && c != '<' && c != '>') {
                    if (c == '/') {
                        sb.append('.');
                    } else {
                        sb.append(c);
                    }
                    i++;
                }
                i--;

                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_BOOLEAN:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("boolean");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_BYTE:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("byte");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_CHAR:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("char");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_DOUBLE:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("double");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_FLOAT:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("float");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_INT:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("int");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_LONG:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("long");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_SHORT:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("short");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case Type.CH_VOID:
                if (first == false) {
                    sb.append(", ");
                }
                sb.append("void");
                first = false;
                if (arrayDepth != 0) {
                    for (int j = 0; j < arrayDepth; j++) {
                        sb.append("[]");
                    }
                    arrayDepth = 0;
                }

                break;
            case '(':
            case ')':
            case '<':
            case '>':
                sb.append(c);
                first = true;
                break;
            default:
            case ';':
                break;
            }
        }
        return sb.toString();
    }

    public static String getMethodSignature(MEMethod method) {
        return method.getName() + "(" + method.getArgumentsString() + ")" + method.getReturnClassString();
    }

    public static FieldIdItem getFieldIdItem() {
        if (!(Selection.getSelectedView() instanceof TextBuilder)) {
            return null;
        }
        TextBuilder text = (TextBuilder) Selection.getSelectedView();
        if (text == null) {
            return null;
        }
        Object ref = Selection.getSelectedObject();

        if (ref == null || !DexField.class.isAssignableFrom(ref.getClass())) {
            return null;
        }
        DexField dexField = (DexField) ref;
        return dexField.getFieldIdItem();
    }

    public static DalvikByteCodeLineBuilder.DalvikBytecodeOffset getDalvikBytecodeOffset() {
        if (!(Selection.getSelectedView() instanceof TextBuilder)) {
            return null;
        }
        TextBuilder text = (TextBuilder) Selection.getSelectedView();
        if (text == null) {
            return null;
        }
        Object ref = Selection.getSelectedObject();

        if (ref == null || !DalvikByteCodeLineBuilder.DalvikBytecodeOffset.class.isAssignableFrom(ref.getClass())) {
            return null;
        }
        return (DalvikByteCodeLineBuilder.DalvikBytecodeOffset) ref;
    }

    public static void printInjectionInfoInTextBuilder(String text1, String text2, DalvikInjectionMethod injection) {
        if (!(Selection.getSelectedView() instanceof TextBuilder)) {
            return;
        }
        TextBuilder text = (TextBuilder) Selection.getSelectedView();
        if (text == null) {
            return;
        }

        int pos = text.getCaretPosition();
        JViewport view = text.getScrollPane().getViewport();
        text.getLineBuilder().insertLineBefore(text.getCurrentLine());
        text.getLineBuilder().append("        >>>    ", 0xbb0000);
        text.getLineBuilder().append("PRINT(", 0x000000);
        text.getLineBuilder().append("\"" + text1 + "\"", 0x0000bb);
        if (text2 != null) {
            text.getLineBuilder().append("+" + text2, 0x000000);
        }
        text.getLineBuilder().append(")", 0x000000);
        text.getLineBuilder().setReferenceToCurrent(injection);
        text.updateDocument();
        text.setCaretPosition(pos);
        text.getScrollPane().setViewport(view);

    }

    public static String appendCodeAddressAndLineNum(String str, Instruction ins) {
        StringBuffer sb = new StringBuffer(str);
        sb.append("(");
        sb.append(String.format("%X", ins.codeAddress));
        if (ins.line != -1) {
            sb.append(",");
            sb.append(ins.line);
        }
        sb.append(")");
        return sb.toString();
    }

}
