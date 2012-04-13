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

import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;

import andreflect.DexMethod;
import andreflect.Util;
import andreflect.injection.DalvikInjectCollection;
import andreflect.injection.abs.DalvikInjectionMethodLocal;

public class DalvikMethodLocal extends DalvikInjectionMethodLocal {
    String prompt1;
    String prompt2;

    public DalvikMethodLocal(String signature, String methodName, Instruction begin, short reg, StringIdItem str, TypeIdItem type, boolean isRead) {
        super(signature, begin, null, reg, isRead, str, type);

        StringBuffer sb2 = new StringBuffer();
        sb2.append(isRead ? "@   ReadLocal " : "@   WriteLocal ");
        sb2.append(methodName);
        prompt1 = sb2.toString();

        StringBuffer sb = new StringBuffer();
        sb.append("->");
        sb.append(Util.getProtoString(type.getTypeDescriptor()));
        sb.append(" ");
        if (str != null) {
            sb.append(str.getStringValue());
            sb.append(" (v");
            sb.append(Integer.toString(reg));
            sb.append(")");
        } else {
            sb.append("v");
            sb.append(Integer.toString(reg));
        }

        prompt2 = sb.toString();
    }

    @Override
    public ArrayList<Instruction> injectDalvik(DalvikInjectCollection dic,
            DexMethod method, Instruction instruction) {
        return dic.injectRegPrintWithTypeIdItem(method,
                reg,
                Util.appendCodeAddressAndLineNum(prompt1, instruction) + prompt2,
                instruction,
                type);
    }

}
