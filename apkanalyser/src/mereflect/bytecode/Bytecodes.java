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

package mereflect.bytecode;

public interface Bytecodes
{
    /** @AUTOGENERATE_BC_START */

    public static final String OP_UNDEFINED = "N/A";
    public static final int OP_UNDEFINED_LEN = 1;

    public static final String[] BC_OPCODES = {
            "nop", // 1
            "aconst_null", // 1
            "iconst_m1", // 1
            "iconst_0", // 1
            "iconst_1", // 1
            "iconst_2", // 1
            "iconst_3", // 1
            "iconst_4", // 1
            "iconst_5", // 1
            "lconst_0", // 1
            "lconst_1", // 1
            "fconst_0", // 1
            "fconst_1", // 1
            "fconst_2", // 1
            "dconst_0", // 1
            "dconst_1", // 1
            "bipush", // 2
            "sipush", // 3
            "ldc", // 2
            "ldc_w", // 3
            "ldc2_w", // 3
            "iload", // 2
            "lload", // 2
            "fload", // 2
            "dload", // 2
            "aload", // 2
            "iload_0", // 1
            "iload_1", // 1
            "iload_2", // 1
            "iload_3", // 1
            "lload_0", // 1
            "lload_1", // 1
            "lload_2", // 1
            "lload_3", // 1
            "fload_0", // 1
            "fload_1", // 1
            "fload_2", // 1
            "fload_3", // 1
            "dload_0", // 1
            "dload_1", // 1
            "dload_2", // 1
            "dload_3", // 1
            "aload_0", // 1
            "aload_1", // 1
            "aload_2", // 1
            "aload_3", // 1
            "iaload", // 1
            "laload", // 1
            "faload", // 1
            "daload", // 1
            "aaload", // 1
            "baload", // 1
            "caload", // 1
            "saload", // 1
            "istore", // 2
            "lstore", // 2
            "fstore", // 2
            "dstore", // 2
            "astore", // 2
            "istore_0", // 1
            "istore_1", // 1
            "istore_2", // 1
            "istore_3", // 1
            "lstore_0", // 1
            "lstore_1", // 1
            "lstore_2", // 1
            "lstore_3", // 1
            "fstore_0", // 1
            "fstore_1", // 1
            "fstore_2", // 1
            "fstore_3", // 1
            "dstore_0", // 1
            "dstore_1", // 1
            "dstore_2", // 1
            "dstore_3", // 1
            "astore_0", // 1
            "astore_1", // 1
            "astore_2", // 1
            "astore_3", // 1
            "iastore", // 1
            "lastore", // 1
            "fastore", // 1
            "dastore", // 1
            "aastore", // 1
            "bastore", // 1
            "castore", // 1
            "sastore", // 1
            "pop", // 1
            "pop2", // 1
            "dup", // 1
            "dup_x1", // 1
            "dup_x2", // 1
            "dup2", // 1
            "dup2_x1", // 1
            "dup2_x2", // 1
            "swap", // 1
            "iadd", // 1
            "ladd", // 1
            "fadd", // 1
            "dadd", // 1
            "isub", // 1
            "lsub", // 1
            "fsub", // 1
            "dsub", // 1
            "imul", // 1
            "lmul", // 1
            "fmul", // 1
            "dmul", // 1
            "idiv", // 1
            "ldiv", // 1
            "fdiv", // 1
            "ddiv", // 1
            "irem", // 1
            "lrem", // 1
            "frem", // 1
            "drem", // 1
            "ineg", // 1
            "lneg", // 1
            "fneg", // 1
            "dneg", // 1
            "ishl", // 1
            "lshl", // 1
            "ishr", // 1
            "lshr", // 1
            "iushr", // 1
            "lushr", // 1
            "iand", // 1
            "land", // 1
            "ior", // 1
            "lor", // 1
            "ixor", // 1
            "lxor", // 1
            "iinc", // 3
            "i2l", // 1
            "i2f", // 1
            "i2d", // 1
            "l2i", // 1
            "l2f", // 1
            "l2d", // 1
            "f2i", // 1
            "f2l", // 1
            "f2d", // 1
            "d2i", // 1
            "d2l", // 1
            "d2f", // 1
            "i2b", // 1
            "i2c", // 1
            "i2s", // 1
            "lcmp", // 1
            "fcmpl", // 1
            "fcmpg", // 1
            "dcmpl", // 1
            "dcmpg", // 1
            "ifeq", // 3
            "ifne", // 3
            "iflt", // 3
            "ifge", // 3
            "ifgt", // 3
            "ifle", // 3
            "if_icmpeq", // 3
            "if_icmpne", // 3
            "if_icmplt", // 3
            "if_icmpge", // 3
            "if_icmpgt", // 3
            "if_icmple", // 3
            "if_acmpeq", // 3
            "if_acmpne", // 3
            "goto", // 3
            "jsr", // 3
            "ret", // 2
            "tableswitch", // 14
            "lookupswitch", // 11
            "ireturn", // 1
            "lreturn", // 1
            "freturn", // 1
            "dreturn", // 1
            "areturn", // 1
            "return", // 1
            "getstatic", // 3
            "putstatic", // 3
            "getfield", // 3
            "putfield", // 3
            "invokevirtual", // 3
            "invokespecial", // 3
            "invokestatic", // 3
            "invokeinterface", // 5
            OP_UNDEFINED + "[ba]",
            "new", // 3
            "newarray", // 2
            "anewarray", // 3
            "arraylength", // 1
            "athrow", // 1
            "checkcast", // 3
            "instanceof", // 3
            "monitorenter", // 1
            "monitorexit", // 1
            OP_UNDEFINED + "[c4]",
            "multianewarray", // 4
            "ifnull", // 3
            "ifnonnull", // 3
            "goto_w", // 5
            "jsr_w", // 5
            OP_UNDEFINED + "[ca]",
            OP_UNDEFINED + "[cb]",
            OP_UNDEFINED + "[cc]",
            OP_UNDEFINED + "[cd]",
            OP_UNDEFINED + "[ce]",
            OP_UNDEFINED + "[cf]",
            OP_UNDEFINED + "[d0]",
            OP_UNDEFINED + "[d1]",
            OP_UNDEFINED + "[d2]",
            OP_UNDEFINED + "[d3]",
            OP_UNDEFINED + "[d4]",
            OP_UNDEFINED + "[d5]",
            OP_UNDEFINED + "[d6]",
            OP_UNDEFINED + "[d7]",
            OP_UNDEFINED + "[d8]",
            OP_UNDEFINED + "[d9]",
            OP_UNDEFINED + "[da]",
            OP_UNDEFINED + "[db]",
            OP_UNDEFINED + "[dc]",
            OP_UNDEFINED + "[dd]",
            OP_UNDEFINED + "[de]",
            OP_UNDEFINED + "[df]",
            OP_UNDEFINED + "[e0]",
            OP_UNDEFINED + "[e1]",
            OP_UNDEFINED + "[e2]",
            OP_UNDEFINED + "[e3]",
            OP_UNDEFINED + "[e4]",
            OP_UNDEFINED + "[e5]",
            OP_UNDEFINED + "[e6]",
            OP_UNDEFINED + "[e7]",
            OP_UNDEFINED + "[e8]",
            OP_UNDEFINED + "[e9]",
            OP_UNDEFINED + "[ea]",
            OP_UNDEFINED + "[eb]",
            OP_UNDEFINED + "[ec]",
            OP_UNDEFINED + "[ed]",
            OP_UNDEFINED + "[ee]",
            OP_UNDEFINED + "[ef]",
            OP_UNDEFINED + "[f0]",
            OP_UNDEFINED + "[f1]",
            OP_UNDEFINED + "[f2]",
            OP_UNDEFINED + "[f3]",
            OP_UNDEFINED + "[f4]",
            OP_UNDEFINED + "[f5]",
            OP_UNDEFINED + "[f6]",
            OP_UNDEFINED + "[f7]",
            OP_UNDEFINED + "[f8]",
            OP_UNDEFINED + "[f9]",
            OP_UNDEFINED + "[fa]",
            OP_UNDEFINED + "[fb]",
            OP_UNDEFINED + "[fc]",
            OP_UNDEFINED + "[fd]",
            OP_UNDEFINED + "[fe]",
            OP_UNDEFINED + "[ff]"
    };
    public static final int[] BC_LENGTHS = {
            1, // nop
            1, // aconst_null
            1, // iconst_m1
            1, // iconst_0
            1, // iconst_1
            1, // iconst_2
            1, // iconst_3
            1, // iconst_4
            1, // iconst_5
            1, // lconst_0
            1, // lconst_1
            1, // fconst_0
            1, // fconst_1
            1, // fconst_2
            1, // dconst_0
            1, // dconst_1
            2, // bipush
            3, // sipush
            2, // ldc
            3, // ldc_w
            3, // ldc2_w
            2, // iload
            2, // lload
            2, // fload
            2, // dload
            2, // aload
            1, // iload_0
            1, // iload_1
            1, // iload_2
            1, // iload_3
            1, // lload_0
            1, // lload_1
            1, // lload_2
            1, // lload_3
            1, // fload_0
            1, // fload_1
            1, // fload_2
            1, // fload_3
            1, // dload_0
            1, // dload_1
            1, // dload_2
            1, // dload_3
            1, // aload_0
            1, // aload_1
            1, // aload_2
            1, // aload_3
            1, // iaload
            1, // laload
            1, // faload
            1, // daload
            1, // aaload
            1, // baload
            1, // caload
            1, // saload
            2, // istore
            2, // lstore
            2, // fstore
            2, // dstore
            2, // astore
            1, // istore_0
            1, // istore_1
            1, // istore_2
            1, // istore_3
            1, // lstore_0
            1, // lstore_1
            1, // lstore_2
            1, // lstore_3
            1, // fstore_0
            1, // fstore_1
            1, // fstore_2
            1, // fstore_3
            1, // dstore_0
            1, // dstore_1
            1, // dstore_2
            1, // dstore_3
            1, // astore_0
            1, // astore_1
            1, // astore_2
            1, // astore_3
            1, // iastore
            1, // lastore
            1, // fastore
            1, // dastore
            1, // aastore
            1, // bastore
            1, // castore
            1, // sastore
            1, // pop
            1, // pop2
            1, // dup
            1, // dup_x1
            1, // dup_x2
            1, // dup2
            1, // dup2_x1
            1, // dup2_x2
            1, // swap
            1, // iadd
            1, // ladd
            1, // fadd
            1, // dadd
            1, // isub
            1, // lsub
            1, // fsub
            1, // dsub
            1, // imul
            1, // lmul
            1, // fmul
            1, // dmul
            1, // idiv
            1, // ldiv
            1, // fdiv
            1, // ddiv
            1, // irem
            1, // lrem
            1, // frem
            1, // drem
            1, // ineg
            1, // lneg
            1, // fneg
            1, // dneg
            1, // ishl
            1, // lshl
            1, // ishr
            1, // lshr
            1, // iushr
            1, // lushr
            1, // iand
            1, // land
            1, // ior
            1, // lor
            1, // ixor
            1, // lxor
            3, // iinc
            1, // i2l
            1, // i2f
            1, // i2d
            1, // l2i
            1, // l2f
            1, // l2d
            1, // f2i
            1, // f2l
            1, // f2d
            1, // d2i
            1, // d2l
            1, // d2f
            1, // i2b
            1, // i2c
            1, // i2s
            1, // lcmp
            1, // fcmpl
            1, // fcmpg
            1, // dcmpl
            1, // dcmpg
            3, // ifeq
            3, // ifne
            3, // iflt
            3, // ifge
            3, // ifgt
            3, // ifle
            3, // if_icmpeq
            3, // if_icmpne
            3, // if_icmplt
            3, // if_icmpge
            3, // if_icmpgt
            3, // if_icmple
            3, // if_acmpeq
            3, // if_acmpne
            3, // goto
            3, // jsr
            2, // ret
            14, // tableswitch
            11, // lookupswitch
            1, // ireturn
            1, // lreturn
            1, // freturn
            1, // dreturn
            1, // areturn
            1, // return
            3, // getstatic
            3, // putstatic
            3, // getfield
            3, // putfield
            3, // invokevirtual
            3, // invokespecial
            3, // invokestatic
            5, // invokeinterface
            OP_UNDEFINED_LEN,
            3, // new
            2, // newarray
            3, // anewarray
            1, // arraylength
            1, // athrow
            3, // checkcast
            3, // instanceof
            1, // monitorenter
            1, // monitorexit
            OP_UNDEFINED_LEN,
            4, // multianewarray
            3, // ifnull
            3, // ifnonnull
            5, // goto_w
            5, // jsr_w
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN,
            OP_UNDEFINED_LEN
    };

    /** @AUTOGENERATE_BC_END */

}