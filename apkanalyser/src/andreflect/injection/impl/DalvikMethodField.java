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

package andreflect.injection.impl;

import java.util.ArrayList;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.SingleRegisterInstruction;

import andreflect.DexMethod;
import andreflect.Util;
import andreflect.injection.DalvikInjectCollection;
import andreflect.injection.abs.DalvikInjectionMethodField;

public class DalvikMethodField extends DalvikInjectionMethodField {
    String str;

    public DalvikMethodField(String signature, String str, FieldIdItem fieldIdItem, boolean isRead) {
        super(signature, fieldIdItem, isRead);
        this.str = str;
    }

    @Override
    public ArrayList<Instruction> injectDalvik(DalvikInjectCollection dic,
            DexMethod method, Instruction instruction) {
        InstructionWithReference refIns = (InstructionWithReference) instruction;
        FieldIdItem fieldIdItem = (FieldIdItem) refIns.getReferencedItem();

        StringBuffer sb = new StringBuffer();
        sb.append(isRead ? "@   ReadField " : "@   WriteField ");
        sb.append(Util.appendCodeAddressAndLineNum(str, instruction));
        sb.append("->");
        sb.append(Util.getProtoString(fieldIdItem.getContainingClass().getTypeDescriptor()));
        sb.append(":");
        sb.append(Util.getProtoString(fieldIdItem.getFieldType().getTypeDescriptor()));
        sb.append(" ");
        sb.append(fieldIdItem.getFieldName().getStringValue());

        String prompt = sb.toString();
        return dic.injectRegPrintWithTypeIdItem(method, (short) ((SingleRegisterInstruction) instruction).getRegisterA(), prompt, instruction, fieldIdItem.getFieldType());
    }
}
