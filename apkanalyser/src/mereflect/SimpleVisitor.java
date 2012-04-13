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

package mereflect;

import java.util.Map;

public abstract class SimpleVisitor implements BytecodeVisitor {

    @Override
    public void visit(int pc, int bytecode, int len) {
    }

    @Override
    public void visitConstantPool(int pc, int bytecode, int len, int cpIndex) {
    }

    @Override
    public void visitInvokation(int pc, int bytecode, int len, int cpIndex) {
    }

    @Override
    public void visitJump(int pc, int bytecode, int len, short relJump) {
    }

    @Override
    public void visitLocalFieldName(int pc, int bytecode, int len, int cpIndex) {
    }

    @Override
    public void visitLookupSwitch(int pc, int bytecode, int len, Map<Object, Object> switch1) {
    }

    @Override
    public void visitTableSwitch(int pc, int bytecode, int len, Map<Object, Object> switch1) {
    }

    @Override
    public void visitNewBytecode(int pc, int bytecode) {
    }
}
