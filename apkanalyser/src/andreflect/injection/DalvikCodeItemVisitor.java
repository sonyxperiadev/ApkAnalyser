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

import jerl.bcm.inj.Injection;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.FiveRegisterInstruction;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.RegisterRangeInstruction;
import org.jf.dexlib.Code.SingleRegisterInstruction;
import org.jf.dexlib.Code.ThreeRegisterInstruction;
import org.jf.dexlib.Code.TwoRegisterInstruction;

import andreflect.DexMethod;
import andreflect.injection.abs.DalvikInjectionMethod;
import andreflect.injection.abs.DalvikInjectionMethodField;
import andreflect.injection.abs.DalvikInjectionMethodLocal;
import andreflect.injection.abs.DalvikInjectionMethodOffset;

public class DalvikCodeItemVisitor {
    public static void visit(DalvikInjectionMethod injection, DexMethod method, DalvikInjectCollection ic) {
        if (injection.performed == true) {
            return;
        }
        int type = injection.getInjectionType();
        boolean isInRange = false;

        if (method.getEncodedMethod() != null
                && method.getEncodedMethod().codeItem != null) {
            Instruction[] instructions = method.getEncodedMethod().codeItem.getInstructions();
            for (Instruction instruction : instructions) {
                if (instruction.isInject() == false) {
                    if (type == Injection.METHOD_ENTRY_INJECTION) {
                        ArrayList<Instruction> injectInstructions = injection.injectDalvik(ic, method, instruction);
                        DalvikBytecodeModificationMediator.injectInsturctionsAtInstruction(method, instruction, injectInstructions);
                        break;
                    } else if (type == Injection.METHOD_OFFSET_INJECTION
                            && (((DalvikInjectionMethodOffset) injection).getOffsetInstruction() == null || instruction == ((DalvikInjectionMethodOffset) injection).getOffsetInstruction())) {
                        ArrayList<Instruction> injectInstructions = injection.injectDalvik(ic, method, instruction);
                        DalvikBytecodeModificationMediator.injectInsturctionsAtInstruction(method, instruction, injectInstructions);
                        break;
                    } else if (type == Injection.METHOD_LOCAL_ACCESS_INJECTION) {
                        DalvikInjectionMethodLocal localInjection = (DalvikInjectionMethodLocal) injection;
                        if (instruction == (localInjection.beginIns)) {
                            isInRange = true;
                        }
                        if (isInRange) {
                            if (localInjection.isRead) {
                                if (checkRegisterUsedForRead(instruction.deodexedInstruction, localInjection.reg)) {
                                    ArrayList<Instruction> injectInstructions = injection.injectDalvik(ic, method, instruction);
                                    DalvikBytecodeModificationMediator.injectInsturctionsAtInstruction(method, instruction, injectInstructions);
                                }
                            } else {
                                if (instruction.opcode.setsRegister()
                                        && localInjection.reg == ((SingleRegisterInstruction) instruction).getRegisterA()
                                        && instruction.opcode != Opcode.NEW_INSTANCE) {
                                    ArrayList<Instruction> injectInstructions = injection.injectDalvik(ic, method, instruction);
                                    DalvikBytecodeModificationMediator.injectInsturctionsAtNextInstruction(method, instruction, injectInstructions);
                                }
                            }
                        }

                        if (instruction == (localInjection.endIns)) {
                            isInRange = false;
                        }
                    } else {
                        switch (instruction.deodexedInstruction.opcode) {
                        case RETURN_VOID:
                        case RETURN:
                        case RETURN_WIDE:
                        case RETURN_OBJECT:
                            if (type == Injection.METHOD_EXIT_INJECTION) {
                                ArrayList<Instruction> injectInstructions = injection.injectDalvik(ic, method, instruction);
                                DalvikBytecodeModificationMediator.injectInsturctionsAtInstruction(method, instruction, injectInstructions);
                            }
                            break;
                        case IGET:
                        case IGET_WIDE:
                        case IGET_OBJECT:
                        case IGET_BOOLEAN:
                        case IGET_BYTE:
                        case IGET_CHAR:
                        case IGET_SHORT:
                        case SGET:
                        case SGET_WIDE:
                        case SGET_OBJECT:
                        case SGET_BOOLEAN:
                        case SGET_BYTE:
                        case SGET_CHAR:
                        case SGET_SHORT:
                            /*
                            case IGET_QUICK:
                            case IGET_WIDE_QUICK:
                            case IGET_OBJECT_QUICK:
                            //for gingerbread
                            case IGET_VOLATILE:
                            case IGET_WIDE_VOLATILE:
                            case IGET_OBJECT_VOLATILE:
                            case SGET_VOLATILE:
                            case SGET_WIDE_VOLATILE:
                            case SGET_OBJECT_VOLATILE:
                             */
                            if (type == Injection.METHOD_FIELD_ACCESS_INJECTION) {
                                DalvikInjectionMethodField fieldInjection = (DalvikInjectionMethodField) injection;
                                InstructionWithReference refIns = (InstructionWithReference) instruction.deodexedInstruction;
                                FieldIdItem refFieldIdItem = (FieldIdItem) refIns.getReferencedItem();
                                if (fieldInjection.isRead == true
                                        && (fieldInjection.fieldIdItem == null || fieldInjection.fieldIdItem == refFieldIdItem)) {
                                    ArrayList<Instruction> injectInstructions = injection.injectDalvik(ic, method, instruction);
                                    DalvikBytecodeModificationMediator.injectInsturctionsAtNextInstruction(method, instruction, injectInstructions);
                                }
                            }
                            break;
                        case IPUT:
                        case IPUT_WIDE:
                        case IPUT_OBJECT:
                        case IPUT_BOOLEAN:
                        case IPUT_BYTE:
                        case IPUT_CHAR:
                        case IPUT_SHORT:
                        case SPUT:
                        case SPUT_WIDE:
                        case SPUT_OBJECT:
                        case SPUT_BOOLEAN:
                        case SPUT_BYTE:
                        case SPUT_CHAR:
                        case SPUT_SHORT:
                            /*
                            case IPUT_QUICK:
                            case IPUT_WIDE_QUICK:
                            case IPUT_OBJECT_QUICK:
                            //for gingerbread
                            case IPUT_VOLATILE:
                            case IPUT_WIDE_VOLATILE:
                            case IPUT_OBJECT_VOLATILE:
                            case SPUT_VOLATILE:
                            case SPUT_WIDE_VOLATILE:
                            case SPUT_OBJECT_VOLATILE:
                             */
                            if (type == Injection.METHOD_FIELD_ACCESS_INJECTION) {
                                DalvikInjectionMethodField fieldInjection = (DalvikInjectionMethodField) injection;
                                InstructionWithReference refIns = (InstructionWithReference) instruction.deodexedInstruction;
                                FieldIdItem refFieldIdItem = (FieldIdItem) refIns.getReferencedItem();
                                if (fieldInjection.isRead == false
                                        && (fieldInjection.fieldIdItem == null || fieldInjection.fieldIdItem == refFieldIdItem)) {
                                    ArrayList<Instruction> injectInstructions = injection.injectDalvik(ic, method, instruction);
                                    DalvikBytecodeModificationMediator.injectInsturctionsAtInstruction(method, instruction, injectInstructions);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        injection.performed = true;
    }

    public static boolean checkRegisterUsedForRead(Instruction instruction, short register) {
        boolean ret = false;

        if (instruction.opcode.setsRegister()
                && register == ((SingleRegisterInstruction) instruction).getRegisterA()) {
            return false;
        }

        if (instruction.opcode.setsResult() == true) {
            return false;
        }

        if (instruction instanceof RegisterRangeInstruction) {
            RegisterRangeInstruction ins = (RegisterRangeInstruction) instruction;
            short count = (short) ins.getRegCount();
            int startReg = ins.getStartRegister();
            if (register >= startReg
                    && register <= startReg + count) {
                ret = true;
            }
        } else if (instruction instanceof FiveRegisterInstruction) {
            FiveRegisterInstruction ins = (FiveRegisterInstruction) instruction;
            short regCount = (short) ins.getRegCount();
            switch (regCount) {
            case 1:
                ret = (ins.getRegisterD() == register || ret) ? true : false;
                break;
            case 2:
                ret = (ins.getRegisterD() == register || ret) ? true : false;
                ret = (ins.getRegisterE() == register || ret) ? true : false;
                break;
            case 3:
                ret = (ins.getRegisterD() == register || ret) ? true : false;
                ret = (ins.getRegisterE() == register || ret) ? true : false;
                ret = (ins.getRegisterF() == register || ret) ? true : false;
                break;
            case 4:
                ret = (ins.getRegisterD() == register || ret) ? true : false;
                ret = (ins.getRegisterE() == register || ret) ? true : false;
                ret = (ins.getRegisterF() == register || ret) ? true : false;
                ret = (ins.getRegisterG() == register || ret) ? true : false;
                break;
            case 5:
                ret = (ins.getRegisterD() == register || ret) ? true : false;
                ret = (ins.getRegisterE() == register || ret) ? true : false;
                ret = (ins.getRegisterF() == register || ret) ? true : false;
                ret = (ins.getRegisterG() == register || ret) ? true : false;
                ret = (ins.getRegisterA() == register || ret) ? true : false;
                break;
            }
        } else if (instruction instanceof ThreeRegisterInstruction) {
            ThreeRegisterInstruction ins = (ThreeRegisterInstruction) instruction;
            ret = (ins.getRegisterA() == register || ret) ? true : false;
            ret = (ins.getRegisterB() == register || ret) ? true : false;
            ret = (ins.getRegisterC() == register || ret) ? true : false;
        } else if (instruction instanceof TwoRegisterInstruction) {
            TwoRegisterInstruction ins = (TwoRegisterInstruction) instruction;
            ret = (ins.getRegisterA() == register || ret) ? true : false;
            ret = (ins.getRegisterB() == register || ret) ? true : false;
        } else if (instruction instanceof SingleRegisterInstruction) {
            SingleRegisterInstruction ins = (SingleRegisterInstruction) instruction;
            ret = (ins.getRegisterA() == register || ret) ? true : false;
        }

        return ret;
    }

}
