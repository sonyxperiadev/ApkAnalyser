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

import org.jf.dexlib.Code.Opcode;

public enum DebugMethod {

    //LOG_CLASSNAME, LOG_METHODNAME, LOG_RETURN, LOG_PARAM (WIDEPARAM is not supported!)
    LOG(Opcode.INVOKE_STATIC_RANGE, "Landroid/util/Log;", "v", "I", "Ljava/lang/String;", "Ljava/lang/String;"),
    SYSTEMOUT(Opcode.INVOKE_STATIC_RANGE, "Ljava/io/PrintStream;", "println", "V", "Ljava/lang/String;"),
    THREAD_CURRENTTHREAD(Opcode.INVOKE_STATIC, "Ljava/lang/Thread;", "currentThread", "Ljava/lang/Thread;"),
    THREAD_GETNAME(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/Thread;", "getName", "Ljava/lang/String;"),
    GC(Opcode.INVOKE_STATIC, "Ljava/lang/System;", "gc", "V"),
    STRINGBUFFER_APPEND_IBS(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "I"),
    STRINGBUFFER_APPEND_Z(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "Z"),
    STRINGBUFFER_APPEND_D(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "D"),
    STRINGBUFFER_APPEND_C(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "C"),
    STRINGBUFFER_APPEND_J(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "J"),
    STRINGBUFFER_APPEND_F(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "F"),
    STRINGBUFFER_APPEND_STRINGBUFFER(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "Ljava/lang/StringBuffer;"),
    STRINGBUFFER_APPEND_CHAR_ARRAY(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "[C"),
    STRINGBUFFER_APPEND_OBJECT(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "Ljava/lang/Object;"),
    STRINGBUFFER_APPEND_STRING(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "append", "Ljava/lang/StringBuffer;", "Ljava/lang/String;"),
    STRINGBUFFER_TOSTRING(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/StringBuffer;", "toString", "Ljava/lang/String;"),
    STRINGBUFFER_INIT(Opcode.INVOKE_DIRECT_RANGE, "Ljava/lang/StringBuffer;", "<init>", "V"),
    THROWABLE_INIT(Opcode.INVOKE_DIRECT_RANGE, "Ljava/lang/Throwable;", "<init>", "V"),
    LOG_GETSTACKTRACE(Opcode.INVOKE_STATIC_RANGE, "Landroid/util/Log;", "getStackTraceString", "Ljava/lang/String;", "Ljava/lang/Throwable;"),
    OBJECT_HASHCODE(Opcode.INVOKE_VIRTUAL_RANGE, "Ljava/lang/Object;", "hashCode", "I"),
    OBJECT_HASHCODE_SUPER(Opcode.INVOKE_SUPER_RANGE, "Ljava/lang/Object;", "hashCode", "I");

    public final Opcode opcode;
    public final String className;
    public final String methodName;
    public final String returnName;
    public final String[] params;

    DebugMethod(Opcode opcode, String className, String methodName, String returnName, String... params) {
        this.opcode = opcode;
        this.className = className;
        this.methodName = methodName;
        this.returnName = returnName;
        this.params = params;

    }

}
