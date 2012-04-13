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

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction31c;
import org.jf.dexlib.Code.Format.Instruction32x;
import org.jf.dexlib.Code.Format.Instruction3rc;

public class InstructionCreator {
    ItemCreator ic;
    AlterRegister alterRegister = null;

    public void setAlterRegister(AlterRegister alterRegister) {
        this.alterRegister = alterRegister;
    }

    public void resetAlterRegister() {
        alterRegister = null;
    }

    private short getRegister(ArrayList<Instruction> instructions, short reg, boolean isWide, boolean isObject) {
        short register = reg;
        if (alterRegister != null) {
            short low = -1;
            short high = -1;
            Opcode op = Opcode.NOP;
            if (isWide) {
                if (alterRegister.checkWide() == false) {
                    throw new RuntimeException("Register is out of range but AlterRegister WIDE is not available");
                }
                low = alterRegister.lowWideRegister;
                high = alterRegister.highWideRegister;
                op = Opcode.MOVE_WIDE_16;
            } else if (isObject) {
                if (alterRegister.checkObject() == true) {
                    low = alterRegister.lowObjRegister;
                    high = alterRegister.highObjRegister;
                    op = Opcode.MOVE_OBJECT_16;
                } else if (alterRegister.checkNormal() == true) {
                    low = alterRegister.lowRegister;
                    high = alterRegister.highRegister;
                    op = Opcode.MOVE_16;
                }
            } else if (alterRegister.checkNormal() == true) {
                low = alterRegister.lowRegister;
                high = alterRegister.highRegister;
                op = Opcode.MOVE_16;
            } else if (alterRegister.checkObject() == true) {
                low = alterRegister.lowObjRegister;
                high = alterRegister.highObjRegister;
                op = Opcode.MOVE_OBJECT_16;
            } else {
                throw new RuntimeException("Register is out of range but AlterRegister is not available");
            }

            if (high != AlterRegister.INVALID_REG) {
                instructions.add(getMoveInstruction(op,
                        high,
                        low).setInject()); //65536
            }
            register = low;
        } else if (reg >= 256) {
            throw new RuntimeException("Register is out of range but AlterRegister is null");
        }
        return register;
    }

    private void restoreRegister(ArrayList<Instruction> instructions, short reg, boolean isWide, boolean isObject) {
        if (alterRegister != null) {
            short low = -1;
            short high = -1;
            Opcode op = Opcode.NOP;
            if (isWide) {
                if (alterRegister.checkWide() == false) {
                    throw new RuntimeException("Register is out of range but AlterRegister WIDE is not available");
                }
                low = alterRegister.lowWideRegister;
                high = alterRegister.highWideRegister;
                op = Opcode.MOVE_WIDE_16;
            } else if (isObject) {
                if (alterRegister.checkObject() == true) {
                    low = alterRegister.lowObjRegister;
                    high = alterRegister.highObjRegister;
                    op = Opcode.MOVE_OBJECT_16;
                } else if (alterRegister.checkNormal() == true) {
                    low = alterRegister.lowRegister;
                    high = alterRegister.highRegister;
                    op = Opcode.MOVE_16;
                }
            } else if (alterRegister.checkNormal() == true) {
                low = alterRegister.lowRegister;
                high = alterRegister.highRegister;
                op = Opcode.MOVE_16;
            } else if (alterRegister.checkObject() == true) {
                low = alterRegister.lowObjRegister;
                high = alterRegister.highObjRegister;
                op = Opcode.MOVE_OBJECT_16;
            } else {
                throw new RuntimeException("Register is out of range but AlterRegister is not available");
            }

            Opcode copyresult = Opcode.MOVE_16;
            if (isObject) {
                copyresult = Opcode.MOVE_OBJECT_16;
            }
            else if (isWide) {
                copyresult = Opcode.MOVE_WIDE_16;
            }

            instructions.add(getMoveInstruction(copyresult,
                    reg,
                    low).setInject()); //65536
            if (high != AlterRegister.INVALID_REG) {
                instructions.add(getMoveInstruction(op,
                        low,
                        high).setInject()); //65536
            }
        } else if (reg >= 256) {
            throw new RuntimeException("Register is out of range but AlterRegister is null");
        }
    }

    InstructionCreator(DexFile dexfile) {
        ic = new ItemCreator(dexfile);
    }

    public short addMoveResultBefore(ArrayList<Instruction> instructions, short reg, char type) {
        short register = reg;

        switch (type) {
        case 'I'://int
        case 'Z'://boolean
        case 'B'://byte
        case 'C'://char
        case 'F'://float
        case 'S'://short
            register = getRegister(instructions, reg, false, false);
            break;
        case 'D'://double
        case 'J'://long
            register = getRegister(instructions, reg, true, false);
            break;
        case 'L'://object
        default:
            register = getRegister(instructions, reg, false, true);
            break;
        }
        return register;
    }

    public void addMoveResultAfter(ArrayList<Instruction> instructions, short reg, char type) {
        switch (type) {
        case 'I'://int
        case 'Z'://boolean
        case 'B'://byte
        case 'C'://char
        case 'F'://float
        case 'S'://short
            restoreRegister(instructions, reg, false, false);
            break;
        case 'D'://double
        case 'J'://long
            restoreRegister(instructions, reg, true, false);
            break;
        case 'L'://object
        default:
            restoreRegister(instructions, reg, false, true);
            break;
        }
    }

    public void addMoveResult(ArrayList<Instruction> instructions, short reg, char type) {
        short register = reg;
        if (register >= 256) {
            throw new RuntimeException("Register is out of range in addMoveResult");
        }

        switch (type) {
        case 'I'://int
        case 'Z'://boolean
        case 'B'://byte
        case 'C'://char
        case 'F'://float
        case 'S'://short
            instructions.add(new Instruction11x(Opcode.MOVE_RESULT, register).setInject()); //256
            break;
        case 'D'://double
        case 'J'://long
            instructions.add(new Instruction11x(Opcode.MOVE_RESULT_WIDE, register).setInject()); //256
            break;
        case 'L'://object
        default:
            instructions.add(new Instruction11x(Opcode.MOVE_RESULT_OBJECT, register).setInject()); //256
            break;
        }
    }

    public void addMove(ArrayList<Instruction> instructions, short regDest, short regSrc, char type) {
        switch (type) {
        case 'I'://int
        case 'Z'://boolean
        case 'B'://byte
        case 'C'://char
        case 'F'://float
        case 'S'://short
            instructions.add(getMoveInstruction(Opcode.MOVE_16, regDest, regSrc).setInject()); //65536
            break;
        case 'D'://double
        case 'J'://long
            instructions.add(getMoveInstruction(Opcode.MOVE_WIDE_16, regDest, regSrc).setInject()); //65536
            break;
        case 'L'://object
        default:
            instructions.add(getMoveInstruction(Opcode.MOVE_OBJECT_16, regDest, regSrc).setInject()); //65536
            break;
        }
    }

    public void addNewInstance(ArrayList<Instruction> instructions, short reg, String type) {
        short register = getRegister(instructions, reg, false, true);
        instructions.add(new Instruction21c(Opcode.NEW_INSTANCE, register, ic.addTypeIdItem(type)).setInject()); //256
        restoreRegister(instructions, reg, false, true);
    }

    public void addInvokeWithReturnInt(ArrayList<Instruction> instructions, DebugMethod method, short startReg, CodeItem codeItem, short returnReg) {
        short register = getRegister(instructions, returnReg, false, true);
        addInvoke(instructions, method, startReg, codeItem);
        addMoveResult(instructions, register, 'I'); //256
        restoreRegister(instructions, returnReg, false, true);
    }

    public void addInvokeWithReturnObject(ArrayList<Instruction> instructions, DebugMethod method, short startReg, CodeItem codeItem, short returnReg) {
        short register = getRegister(instructions, returnReg, false, true);
        addInvoke(instructions, method, startReg, codeItem);
        addMoveResult(instructions, register, 'L'); //256
        restoreRegister(instructions, returnReg, false, true);
    }

    public void addInvoke(ArrayList<Instruction> instructions, DebugMethod method, short startReg, CodeItem codeItem) {
        short registerCount = 0;

        if (method.params.length != 0) {
            registerCount = (short) method.params.length;
            for (String param : method.params) {
                if (param.equals("J") || param.equals("D")) {
                    registerCount++;
                }
            }
        }

        if (!(method.opcode.name.equals(Opcode.INVOKE_STATIC.name)
        || method.opcode.name.equals(Opcode.INVOKE_STATIC_RANGE.name))) {
            registerCount++;
        }

        instructions.add(new Instruction3rc(method.opcode, //65536
                registerCount,
                startReg,
                ic.prepareMethodIdItem(method)).setInject());
        if (codeItem.outWords < registerCount) {
            codeItem.outWords = registerCount;
        }

    }

    public void addConstString(ArrayList<Instruction> instructions, short reg, String string) {
        short register = getRegister(instructions, reg, false, true);
        StringIdItem stringIdItem = ic.prepareStringIdItem(string);
        if (stringIdItem.getIndex() > 0xFFFF) {
            instructions.add(new Instruction31c(Opcode.CONST_STRING_JUMBO, register, stringIdItem).setInject()); //256
        } else {
            instructions.add(new Instruction21c(Opcode.CONST_STRING, register, stringIdItem).setInject()); //256
        }
        restoreRegister(instructions, reg, false, true);

    }

    public void addReturn(ArrayList<Instruction> instructions, short reg, char type) {
        short register;

        switch (type) {
        case 'V':
            instructions.add(new Instruction10x(Opcode.RETURN_VOID).setInject());
            break;
        case 'I'://int
        case 'Z'://boolean
        case 'B'://byte
        case 'C'://char
        case 'F'://float
        case 'S'://short
            register = getRegister(instructions, reg, false, false);
            instructions.add(new Instruction11x(Opcode.RETURN, register).setInject()); //256
            restoreRegister(instructions, reg, false, false);
            break;
        case 'D'://double
        case 'J'://long
            register = getRegister(instructions, reg, true, false);
            instructions.add(new Instruction11x(Opcode.RETURN_WIDE, register).setInject()); //256
            restoreRegister(instructions, reg, true, false);
            break;
        case 'L'://object
        default:
            register = getRegister(instructions, reg, false, true);
            instructions.add(new Instruction11x(Opcode.RETURN_OBJECT, register).setInject()); //256
            restoreRegister(instructions, reg, false, true);
            break;
        }
    }

    private Instruction getMoveInstruction(Opcode op, short regDest, short regSrc) {
        Instruction ins = null;
        if (op == Opcode.MOVE_16) {
            if (regDest < 16 && regSrc < 16) {
                ins = new Instruction12x(Opcode.MOVE, (byte) regDest, (byte) regSrc);
            } else if (regDest < 256) {
                ins = new Instruction22x(Opcode.MOVE_FROM16, (byte) regDest, regSrc);
            } else {
                ins = new Instruction32x(op, regDest, regSrc);
            }
        } else if (op == Opcode.MOVE_OBJECT_16) {
            if (regDest < 16 && regSrc < 16) {
                ins = new Instruction12x(Opcode.MOVE_OBJECT, (byte) regDest, (byte) regSrc);
            } else if (regDest < 16) {
                ins = new Instruction22x(Opcode.MOVE_OBJECT_FROM16, (byte) regDest, regSrc);
            } else {
                ins = new Instruction32x(op, regDest, regSrc);
            }
        } else if (op == Opcode.MOVE_WIDE_16) {
            if (regDest < 16 && regSrc < 16) {
                ins = new Instruction12x(Opcode.MOVE_WIDE, (byte) regDest, (byte) regSrc);
            } else if (regDest < 256) {
                ins = new Instruction22x(Opcode.MOVE_WIDE_FROM16, (byte) regDest, regSrc);
            } else {
                ins = new Instruction32x(op, regDest, regSrc);
            }
        }
        return ins;
    }

}
