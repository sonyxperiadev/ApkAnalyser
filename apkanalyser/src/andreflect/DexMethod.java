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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mereflect.MEClass;
import mereflect.MEMethod;
import mereflect.Type;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.Code.Instruction;

import andreflect.definition.DexMethodDefinition;

public class DexMethod extends MEMethod {

    ClassDataItem.EncodedMethod m_method;
    DexMethodDefinition m_def = null;

    public List<MEMethod.Invokation> m_dexInvokations = null;

    public DexMethod(DexClass clazzDex, ClassDataItem.EncodedMethod encodedMethod) {
        super(clazzDex);
        m_method = encodedMethod;
    }

    public DexMethodDefinition getDefinition() {
        if (m_def == null) {
            m_def = new DexMethodDefinition(m_method);
        }
        return m_def;
    }

    @Override
    public String getName() {
        return m_method.method.getMethodName().getStringValue();
    }

    @Override
    public String getDescriptor() {
        //System.out.println("desc= "+ m_method.method.getMethodName().getStringValue() + " : "+ m_method.method.getPrototype().getPrototypeString());
        return m_method.method.getPrototype().getPrototypeString();
    }

    @Override
    public byte[] getByteCodes() {
        return null;
    }

    @Override
    public List<MEMethod.Invokation> getInvokations() {
        return m_dexInvokations;
    }

    @Override
    public MEClass[] getExceptions() throws IOException {
        return m_exceptions;
    }

    public void setExceptions(MEClass[] excptions) {
        m_exceptions = excptions;
    }

    public ClassDataItem.EncodedMethod getEncodedMethod() {
        return m_method;
    }

    @Override
    public String getArgumentsStringUml() {
        try {
            Type[] args = getArguments();
            ArrayList<String> params = getDefinition().getParameters();
            boolean hasParam = params.size() != 0 && params.size() == args.length;

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < args.length; i++) {
                sb.append((hasParam && params.get(i) != null) ? params.get(i) : "?");
                sb.append(" : ");
                sb.append(Util.shortenClassName(args[i].toString()));
                if (i < args.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "?";
        }
    }

    @Override
    public String getArgumentsString(Type[] args) {
        ArrayList<String> params = getDefinition().getParameters();
        boolean hasParam = params.size() != 0 && params.size() == args.length;

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(" ");
            sb.append((hasParam && params.get(i) != null) ? params.get(i) : "?");
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getParameterName(int i, int length) {
        ArrayList<String> params = getDefinition().getParameters();
        boolean hasParam = params.size() != 0 && params.size() == length;
        return (hasParam && params.get(i) != null) ? params.get(i) : "?";
    }

    public Instruction getNextInstruction(Instruction ins) {
        Instruction[] instructions = m_method.codeItem.getInstructions();
        Instruction result = null;
        for (int i = 0; i < instructions.length; i++) {
            if (ins == instructions[i]) {
                result = instructions[i + 1];
                break;
            }
        }
        return result;
    }

    public Instruction getInstructionAtCodeAddress(int codeAddress) {
        Instruction result = null;
        Instruction[] instructions = m_method.codeItem.getInstructions();
        int currentCodeAddress = 0;

        for (int i = 0; i < instructions.length; i++) {
            if (currentCodeAddress == codeAddress) {
                result = instructions[i];
                break;
            }
            currentCodeAddress += instructions[i].getSize(currentCodeAddress);
        }
        return result;
    }

    public Instruction getPreviousInstructionAtCodeAddress(int codeAddress) {
        Instruction[] instructions = m_method.codeItem.getInstructions();
        Instruction result = instructions[0];
        int currentCodeAddress = 0;

        for (int i = 0; i < instructions.length; i++) {
            if (currentCodeAddress == codeAddress) {
                break;
            } else {
                result = instructions[i];
            }
            currentCodeAddress += instructions[i].getSize(currentCodeAddress);
        }
        return result;
    }

}
