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

package andreflect.definition;

import java.util.ArrayList;

import org.jf.baksmali.Adaptors.MethodDefinition;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DebugInfoItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Debug.DebugInstructionIterator;
import org.jf.dexlib.Util.SparseIntArray;

public class DexMethodDefinition extends MethodDefinition {

    public DexMethodDefinition(ClassDataItem.EncodedMethod encodedMethod) {
        super(encodedMethod);
    }

    public ArrayList<String> getParameters() {
        final CodeItem codeItem = encodedMethod.codeItem;

        final ArrayList<String> params = new ArrayList<String>();

        DebugInfoItem debugInfoItem = null;
        if (codeItem != null) {
            debugInfoItem = codeItem.getDebugInfo();
        }

        int parameterCount = 0;
        StringIdItem[] parameterNames = null;

        if (debugInfoItem != null) {
            parameterNames = debugInfoItem.getParameterNames();
        }
        if (parameterNames == null) {
            parameterNames = new StringIdItem[0];
        }

        if (parameterCount < parameterNames.length) {
            parameterCount = parameterNames.length;
        }

        boolean unSolvedName = false;
        for (int i = 0; i < parameterCount; i++) {
            StringIdItem parameterName = null;
            if (i < parameterNames.length) {
                parameterName = parameterNames[i];
            }
            if (parameterName != null) {
                params.add(parameterName.getStringValue());

            } else {
                params.add(null);
                unSolvedName = true;
            }
        }
        if (unSolvedName) {
            DebugInstructionIterator.DecodeInstructions(debugInfoItem, codeItem.getRegisterCount(),
                    new DebugInstructionIterator.ProcessDecodedDebugInstructionDelegate() {
                        @Override
                        public void ProcessStartLocal(final int codeAddress, final int length, final int registerNum,
                                final StringIdItem name, final TypeIdItem type) {
                            int index = getEmptyIndex(params);
                            if (index != -1
                                    && codeAddress == 0) {
                                params.set(index, name.getStringValue());
                            }

                        }

                        @Override
                        public void ProcessStartLocalExtended(final int codeAddress, final int length,
                                final int registerNum, final StringIdItem name,
                                final TypeIdItem type, final StringIdItem signature) {
                            int index = getEmptyIndex(params);
                            if (index != -1
                                    && codeAddress == 0) {
                                params.set(index, name.getStringValue());
                            }
                        }

                    });
        }

        return params;
    }

    private int getEmptyIndex(ArrayList<String> params) {
        int ret = -1;
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) == null) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    public void prepareLineNumbers() {
        final CodeItem codeItem = encodedMethod.codeItem;
        if (codeItem == null) {
            return;
        }
        Instruction[] instructions = codeItem.getInstructions();
        DebugInfoItem debugInfoItem = null;
        if (codeItem != null) {
            debugInfoItem = codeItem.getDebugInfo();
        }
        if (debugInfoItem == null) {
            return;
        }

        final SparseIntArray instructionMap = new SparseIntArray(instructions.length);

        DebugInstructionIterator.DecodeInstructions(debugInfoItem, codeItem.getRegisterCount(),
                new DebugInstructionIterator.ProcessDecodedDebugInstructionDelegate() {
                    @Override
                    public void ProcessLineEmit(int codeAddress, final int line) {
                        instructionMap.append(codeAddress, line);
                    }
                });

        int currentline = instructionMap.valueAt(0);
        for (Instruction instruction : instructions) {
            int line = instructionMap.get(instruction.codeAddress, -1);
            if (line == -1) {
                instruction.line = currentline;
            } else {
                instruction.line = line;
                currentline = line;
            }
        }
    }
}
