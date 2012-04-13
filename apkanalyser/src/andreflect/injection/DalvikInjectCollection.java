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

package andreflect.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.Code.FiveRegisterInstruction;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.RegisterRangeInstruction;
import org.jf.dexlib.Code.SingleRegisterInstruction;
import org.jf.dexlib.Util.AccessFlags;

import andreflect.DexMethod;
import andreflect.Util;

public class DalvikInjectCollection {
    InstructionCreator insCreator;
    public static final String APK_TAG = "APKANALYSER";
    public static final short INVALID_REG = -1;

    DalvikInjectCollection(DexFile dexFile) {
        insCreator = new InstructionCreator(dexFile);
        logRegisterMap = new HashMap<DexMethod, LogRegister>();
    }

    public ArrayList<Instruction> injectCopyParamRegs(CodeItem codeItem, int regDest, int regSrc, boolean isStatic, String paramShortDesc) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();

        if (!isStatic) {
            insCreator.addMove(instructions, (short) regDest++, (short) regSrc++, 'L');
        }

        if (paramShortDesc != null) {
            int regCount = paramShortDesc.length();
            for (int i = 0; i < regCount; i++) {
                insCreator.addMove(instructions, (short) regDest++, (short) regSrc++, paramShortDesc.charAt(i));
                if (paramShortDesc.charAt(i) == 'D'
                        || paramShortDesc.charAt(i) == 'J') {
                    regSrc++; //never use from16 opcodes
                    regDest++;
                }
            }
        }
        return instructions;
    }

    public ArrayList<Instruction> injectInstanceHash(DexMethod method, short register, String str, Instruction ins, boolean isThis) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
        short appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);

        int alter = prepareRegister(reg, method, ins, false);
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);
        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);

        insCreator.addConstString(instructions, appendValueReg, str);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);

        if (isThis == false) {
            insCreator.addInvokeWithReturnInt(instructions, DebugMethod.OBJECT_HASHCODE, register, codeItem, logTextReg);
        } else {
            insCreator.addInvokeWithReturnInt(instructions, DebugMethod.OBJECT_HASHCODE_SUPER, register, codeItem, logTextReg);
        }
        insCreator.addMove(instructions, appendValueReg, logTextReg, 'I');

        stringbuffer_append(stringBufferInstanceReg,
                stringBufferInstanceReg,
                'I',
                DebugMethod.STRINGBUFFER_APPEND_OBJECT.params[0],
                codeItem,
                instructions);
        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectRegPrintAsObject(DexMethod method, short register, String str, Instruction ins) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
        short appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);

        int alter = prepareRegister(reg, method, ins, false);
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);

        insCreator.addConstString(instructions, appendValueReg, str);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);
        insCreator.addMove(instructions, appendValueReg, register, 'L');

        stringbuffer_append(stringBufferInstanceReg,
                stringBufferInstanceReg,
                'L',
                DebugMethod.STRINGBUFFER_APPEND_OBJECT.params[0],
                codeItem,
                instructions);

        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectRegPrintWithTypeIdItem(DexMethod method, short register, String str, Instruction ins, TypeIdItem type) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        String shortDesc = type.toShorty();
        String typeDesc = type.getTypeDescriptor();

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = INVALID_REG;
        short appendValueReg = INVALID_REG;
        if (hasWideRegister(shortDesc)) {
            stringBufferInstanceReg = logReg.stringBufferInstanceReg_Wide() ? logReg.stringBufferInstanceReg_Wide : (logReg.stringBufferInstanceReg_Wide = reg++);
            appendValueReg = logReg.appendValueReg_Wide() ? logReg.appendValueReg_Wide : (logReg.appendValueReg_Wide = reg++);
            reg++;
        } else {
            stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
            appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);
        }

        int alter = prepareRegister(reg, method, ins, hasWideRegister(shortDesc));
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);

        insCreator.addConstString(instructions, appendValueReg, str + " = ");
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);
        insCreator.addMove(instructions, appendValueReg, register, shortDesc.charAt(0));

        stringbuffer_append(stringBufferInstanceReg,
                stringBufferInstanceReg,
                shortDesc.charAt(0),
                typeDesc,
                codeItem,
                instructions);

        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectLog(DexMethod method, String str, Instruction ins) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);

        int alter = prepareRegister(reg, method, ins, false);
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);
        insCreator.addConstString(instructions, logTextReg, str);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectCurThread(DexMethod method, String str, Instruction ins) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
        short appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);

        int alter = prepareRegister(reg, method, ins, false);
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);

        insCreator.addConstString(instructions, appendValueReg, str + " = ");
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);

        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.THREAD_CURRENTTHREAD, (short) 0, codeItem, appendValueReg);
        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.THREAD_GETNAME, appendValueReg, codeItem, appendValueReg);

        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);

        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectGC(DexMethod method) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;
        insCreator.addInvoke(instructions, DebugMethod.GC, (short) 0, codeItem);
        return instructions;
    }

    public ArrayList<Instruction> injectPrintStackTrace(DexMethod method, String str, Instruction ins) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
        short appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);

        int alter = prepareRegister(reg, method, ins, false);
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);

        insCreator.addConstString(instructions, appendValueReg, str + " = ");
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);

        insCreator.addNewInstance(instructions, logTextReg, DebugMethod.THROWABLE_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.THROWABLE_INIT, logTextReg, codeItem);
        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.LOG_GETSTACKTRACE, logTextReg, codeItem, appendValueReg);

        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);

        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectInvokeReturn(DexMethod method, String str, Instruction invokeIns, Instruction moveResultIns, Instruction ins, MethodIdItem methodIdItem) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        String returnShortDesc = methodIdItem.getPrototype().getReturnType().toShorty();
        String typeDesc = methodIdItem.getPrototype().getReturnType().getTypeDescriptor();

        if (returnShortDesc.charAt(0) == 'V') {
            return injectLog(method, str, ins);
        }

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg2 = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
        short appendValueReg2 = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);
        short stringBufferInstanceReg = INVALID_REG;
        short appendValueReg = INVALID_REG;
        if (hasWideRegister(returnShortDesc)) {
            stringBufferInstanceReg = logReg.stringBufferInstanceReg_Wide() ? logReg.stringBufferInstanceReg_Wide : (logReg.stringBufferInstanceReg_Wide = reg++);
            appendValueReg = logReg.appendValueReg_Wide() ? logReg.appendValueReg_Wide : (logReg.appendValueReg_Wide = reg++);
            reg++;
        } else {
            stringBufferInstanceReg = logReg.stringBufferInstanceReg2() ? logReg.stringBufferInstanceReg2 : (logReg.stringBufferInstanceReg2 = reg++);
            appendValueReg = logReg.appendValueReg2() ? logReg.appendValueReg2 : (logReg.appendValueReg2 = reg++);
        }

        int alter = prepareRegister(reg, method, ins, hasWideRegister(returnShortDesc));
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        if (moveResultIns != null) {
            insCreator.addMove(instructions, appendValueReg, (short) (((SingleRegisterInstruction) moveResultIns).getRegisterA()), returnShortDesc.charAt(0));
        } else {
            //workaround to inject the register save before the invokation
            ArrayList<Instruction> saveRegForMoveResult = new ArrayList<Instruction>();
            short moveResultAlterReg = insCreator.addMoveResultBefore(saveRegForMoveResult, appendValueReg, returnShortDesc.charAt(0));
            if (moveResultAlterReg != appendValueReg) {
                DalvikBytecodeModificationMediator.injectInsturctionsAtInstruction(method, invokeIns, saveRegForMoveResult);
            }
            insCreator.addMoveResult(instructions, moveResultAlterReg, returnShortDesc.charAt(0));
            if (moveResultAlterReg != appendValueReg) {
                insCreator.addMoveResultAfter(instructions, appendValueReg, returnShortDesc.charAt(0));
            }
        }

        insCreator.addConstString(instructions, logTagReg, APK_TAG);
        insCreator.addConstString(instructions, logTextReg, str);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);

        insCreator.addMove(instructions, stringBufferInstanceReg2, stringBufferInstanceReg, 'L');
        insCreator.addConstString(instructions, appendValueReg2, "  return: " + Util.getProtoString(typeDesc) + " = ");
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg2, codeItem);

        stringbuffer_append(stringBufferInstanceReg,
                stringBufferInstanceReg,
                returnShortDesc.charAt(0),
                typeDesc,
                codeItem,
                instructions);

        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectReturnValue(DexMethod method, String str, Instruction ins) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        String returnShortDesc = codeItem.getParent().method.getPrototype().getReturnType().toShorty();
        String typeDesc = codeItem.getParent().method.getPrototype().getReturnType().getTypeDescriptor();

        if (!(returnShortDesc.charAt(0) != 'V' && ins instanceof SingleRegisterInstruction)) {
            return injectLog(method, str, ins);
        }

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = INVALID_REG;
        short appendValueReg = INVALID_REG;
        if (hasWideRegister(returnShortDesc)) {
            stringBufferInstanceReg = logReg.stringBufferInstanceReg_Wide() ? logReg.stringBufferInstanceReg_Wide : (logReg.stringBufferInstanceReg_Wide = reg++);
            appendValueReg = logReg.appendValueReg_Wide() ? logReg.appendValueReg_Wide : (logReg.appendValueReg_Wide = reg++);
            reg++;
        } else {
            stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
            appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);
        }

        int alter = prepareRegister(reg, method, ins, hasWideRegister(returnShortDesc));
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);
        insCreator.addConstString(instructions, logTextReg, str);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);

        insCreator.addConstString(instructions, appendValueReg, "  return: " + Util.getProtoString(typeDesc) + " = ");
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);
        insCreator.addMove(instructions, appendValueReg, (short) (((SingleRegisterInstruction) ins).getRegisterA()), returnShortDesc.charAt(0));

        stringbuffer_append(stringBufferInstanceReg,
                stringBufferInstanceReg,
                returnShortDesc.charAt(0),
                typeDesc,
                codeItem,
                instructions);

        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectParams(DexMethod method, String str, String[] paramStrings) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        TypeListItem params = codeItem.getParent().method.getPrototype().getParameters();
        String paramShortDesc = params == null ? null : params.getShortyString();

        if (params == null || paramShortDesc == null) {
            return injectLog(method, str, null); //always at beginning
        }

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
        short appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);
        short stringBufferInstanceReg_Wide = INVALID_REG;
        short appendValueReg_Wide = INVALID_REG;
        if (hasWideRegister(paramShortDesc)) {
            stringBufferInstanceReg_Wide = logReg.stringBufferInstanceReg_Wide() ? logReg.stringBufferInstanceReg_Wide : (logReg.stringBufferInstanceReg_Wide = reg++);
            appendValueReg_Wide = logReg.appendValueReg_Wide() ? logReg.appendValueReg_Wide : (logReg.appendValueReg_Wide = reg++);
            reg++;
        }

        int alter = prepareRegister(reg, method, null, hasWideRegister(paramShortDesc));
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        insCreator.addConstString(instructions, logTagReg, APK_TAG);
        insCreator.addConstString(instructions, logTextReg, str);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);
        if (stringBufferInstanceReg_Wide != INVALID_REG) {
            insCreator.addMove(instructions, stringBufferInstanceReg_Wide, stringBufferInstanceReg, 'L');
        }

        int paramCount = paramShortDesc.length();
        int parameterRegisterCount = codeItem.getParent().method.getPrototype().getParameterRegisterCount()
                + (((codeItem.getParent().accessFlags & AccessFlags.STATIC.getValue()) == 0) ? 1 : 0);
        short regSrc = (short) (codeItem.registerOriginalCount - parameterRegisterCount
                + (((codeItem.getParent().accessFlags & AccessFlags.STATIC.getValue()) == 0) ? 1 : 0));

        for (int i = 0; i < paramCount; i++) {
            insCreator.addConstString(instructions, appendValueReg, "  parameter[" + i + "]: " + paramStrings[i] + " = ");
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);

            if (paramShortDesc.charAt(i) != 'D'
                    && paramShortDesc.charAt(i) != 'J') {
                insCreator.addMove(instructions, appendValueReg, regSrc++, paramShortDesc.charAt(i));
            } else {
                insCreator.addMove(instructions, appendValueReg_Wide, regSrc++, paramShortDesc.charAt(i));
                regSrc++; //never use from16 opcodes
            }
            stringbuffer_append(stringBufferInstanceReg,
                    stringBufferInstanceReg_Wide,
                    paramShortDesc.charAt(i),
                    codeItem.getParent().method.getPrototype().getParameters().getTypeIdItem(i).getTypeDescriptor(),
                    codeItem,
                    instructions);
            insCreator.addConstString(instructions, appendValueReg, "\n");
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);
        }
        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    public ArrayList<Instruction> injectInvokeParams(DexMethod method, String str, Instruction invokeIns, MethodIdItem methodIdItem) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        CodeItem codeItem = method.getEncodedMethod().codeItem;
        if (!(invokeIns instanceof FiveRegisterInstruction || invokeIns instanceof RegisterRangeInstruction)) {
            return injectLog(method, str, invokeIns);
        }

        TypeListItem params = methodIdItem.getPrototype().getParameters();
        String paramShortDesc = params == null ? null : params.getShortyString();

        if (params == null || paramShortDesc == null) {
            return injectLog(method, str, invokeIns);
        }

        LogRegister logReg = getLogRegister(method);
        short reg = (short) (codeItem.getRegisterCount());
        short logTagReg = logReg.logTagReg() ? logReg.logTagReg : (logReg.logTagReg = reg++);
        short logTextReg = logReg.logTextReg() ? logReg.logTextReg : (logReg.logTextReg = reg++);
        short stringBufferInstanceReg = logReg.stringBufferInstanceReg() ? logReg.stringBufferInstanceReg : (logReg.stringBufferInstanceReg = reg++);
        short appendValueReg = logReg.appendValueReg() ? logReg.appendValueReg : (logReg.appendValueReg = reg++);
        short stringBufferInstanceReg_Wide = INVALID_REG;
        short appendValueReg_Wide = INVALID_REG;
        if (hasWideRegister(paramShortDesc)) {
            stringBufferInstanceReg_Wide = logReg.stringBufferInstanceReg_Wide() ? logReg.stringBufferInstanceReg_Wide : (logReg.stringBufferInstanceReg_Wide = reg++);
            appendValueReg_Wide = logReg.appendValueReg_Wide() ? logReg.appendValueReg_Wide : (logReg.appendValueReg_Wide = reg++);
            reg++;
        }

        int alter = prepareRegister(reg, method, invokeIns, hasWideRegister(paramShortDesc));
        if (alter == -1) {
            return instructions;
        }
        reg += alter;

        String[] paramStrings = new String[params.getTypeCount()];
        for (int i = 0; i < params.getTypeCount(); i++) {
            paramStrings[i] = Util.getProtoString(params.getTypeIdItem(i).getTypeDescriptor());
        }

        insCreator.addConstString(instructions, logTagReg, APK_TAG);
        insCreator.addConstString(instructions, logTextReg, str);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        insCreator.addNewInstance(instructions, stringBufferInstanceReg, DebugMethod.STRINGBUFFER_INIT.className);
        insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_INIT, stringBufferInstanceReg, codeItem);
        if (stringBufferInstanceReg_Wide != INVALID_REG) {
            insCreator.addMove(instructions, stringBufferInstanceReg_Wide, stringBufferInstanceReg, 'L');
        }

        int paramCount = paramShortDesc.length();
        short[] paramRegs = null;
        int parameterRegisterCount = 0;

        if (invokeIns instanceof FiveRegisterInstruction) {
            parameterRegisterCount = ((FiveRegisterInstruction) invokeIns).getRegCount();
            paramRegs = new short[5];
            paramRegs[0] = ((FiveRegisterInstruction) invokeIns).getRegisterD();
            paramRegs[1] = ((FiveRegisterInstruction) invokeIns).getRegisterE();
            paramRegs[2] = ((FiveRegisterInstruction) invokeIns).getRegisterF();
            paramRegs[3] = ((FiveRegisterInstruction) invokeIns).getRegisterG();
            paramRegs[4] = ((FiveRegisterInstruction) invokeIns).getRegisterA();
        } else {//RegisterRangeInstruction
            parameterRegisterCount = ((RegisterRangeInstruction) invokeIns).getRegCount();
            paramRegs = new short[parameterRegisterCount];
            for (int i = 0; i < ((RegisterRangeInstruction) invokeIns).getRegCount(); i++) {
                paramRegs[i] = (short) (((RegisterRangeInstruction) invokeIns).getStartRegister() + i);
            }
        }

        short regParamIndex = (short) parameterRegisterCount;
        for (int i = 0; i < paramCount; i++) {
            if (paramShortDesc.charAt(i) == 'D'
                    || paramShortDesc.charAt(i) == 'J') {
                regParamIndex--;
            }
            regParamIndex--;
        }

        for (int i = 0; i < paramCount; i++) {
            insCreator.addConstString(instructions, appendValueReg, "  parameter[" + i + "]: " + paramStrings[i] + " = ");
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);

            if (paramShortDesc.charAt(i) != 'D'
                    && paramShortDesc.charAt(i) != 'J') {
                insCreator.addMove(instructions, appendValueReg, paramRegs[regParamIndex++], paramShortDesc.charAt(i));
            } else {
                insCreator.addMove(instructions, appendValueReg_Wide, paramRegs[regParamIndex++], paramShortDesc.charAt(i));
                regParamIndex++; //never use from16 opcodes
            }

            stringbuffer_append(stringBufferInstanceReg,
                    stringBufferInstanceReg_Wide,
                    paramShortDesc.charAt(i),
                    methodIdItem.getPrototype().getParameters().getTypeIdItem(i).getTypeDescriptor(),
                    codeItem,
                    instructions);
            insCreator.addConstString(instructions, appendValueReg, "\n");
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_STRING, stringBufferInstanceReg, codeItem);
        }
        insCreator.addInvokeWithReturnObject(instructions, DebugMethod.STRINGBUFFER_TOSTRING, stringBufferInstanceReg, codeItem, logTextReg);
        insCreator.addInvoke(instructions, DebugMethod.LOG, logTagReg, codeItem);

        codeItem.registerCount = reg;
        resetRegister();
        return instructions;
    }

    private void stringbuffer_append(short stringBufferInstanceReg, short stringBufferInstanceReg_Wide, char shortTypeDesc, String typeDesc, CodeItem codeItem, ArrayList<Instruction> instructions) {
        switch (shortTypeDesc) {
        case 'I'://int
        case 'B'://byte
        case 'S'://short
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_IBS, stringBufferInstanceReg, codeItem);
            break;
        case 'Z'://boolean
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_Z, stringBufferInstanceReg, codeItem);
            break;
        case 'C'://char
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_C, stringBufferInstanceReg, codeItem);
            break;
        case 'F'://float
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_F, stringBufferInstanceReg, codeItem);
            break;
        case 'D'://double
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_D, stringBufferInstanceReg_Wide, codeItem);
            break;
        case 'J'://long
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_J, stringBufferInstanceReg_Wide, codeItem);
            break;
        case 'L'://object
            insCreator.addInvoke(instructions, DebugMethod.STRINGBUFFER_APPEND_OBJECT, stringBufferInstanceReg, codeItem);
            break;
        default:
            break;
        }
    }

    private LogRegister getLogRegister(DexMethod method) {
        if (logRegisterMap.containsKey(method)) {
            return logRegisterMap.get(method);
        } else {
            LogRegister logRegister = new LogRegister();
            logRegisterMap.put(method, logRegister);
            return logRegister;
        }
    }

    private final Map<DexMethod, LogRegister> logRegisterMap;

    private boolean hasWideRegister(String shortTypeDesc) {
        boolean ret = false;
        for (int i = 0; i < shortTypeDesc.length(); i++) {
            if (shortTypeDesc.charAt(i) == 'D'
                    || shortTypeDesc.charAt(i) == 'J') {
                ret = true;
            }
        }
        return ret;
    }

    private void resetRegister() {
        insCreator.resetAlterRegister();
    }

    //offsetIns can be null
    private int prepareRegister(short reg, DexMethod method, Instruction offsetIns, boolean needWide) {
        if (method.getEncodedMethod().codeItem.getRegisterCount() == reg) {
            return 0;
        }
        int parameterRegisterCount = method.getEncodedMethod().codeItem.getParent().method.getPrototype().getParameterRegisterCount()
                + (((method.getEncodedMethod().codeItem.getParent().accessFlags & AccessFlags.STATIC.getValue()) == 0) ? 1 : 0);
        if (reg + parameterRegisterCount < 255) { //just an assumption
            return 0;
        }

        System.out.println("[prepareRegister] method ignored, does not have enough register " + method.getMEClass().getName() + "." + method.getName());
        //TODO analyse and set alter registers
        return -1;
    }

}
