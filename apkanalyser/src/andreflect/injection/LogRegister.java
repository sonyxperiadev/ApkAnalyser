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

public class LogRegister {
    public static final short INVALID_REG = -1;

    public short logTagReg;
    public short logTextReg;
    public short stringBufferInstanceReg;
    public short appendValueReg;
    public short stringBufferInstanceReg_Wide;
    public short appendValueReg_Wide;
    public short stringBufferInstanceReg2;
    public short appendValueReg2;

    public boolean logTagReg() {
        return logTagReg != INVALID_REG;
    }

    public boolean logTextReg() {
        return logTextReg != INVALID_REG;
    }

    public boolean stringBufferInstanceReg() {
        return stringBufferInstanceReg != INVALID_REG;
    }

    public boolean appendValueReg() {
        return appendValueReg != INVALID_REG;
    }

    public boolean stringBufferInstanceReg_Wide() {
        return stringBufferInstanceReg_Wide != INVALID_REG;
    }

    public boolean appendValueReg_Wide() {
        return appendValueReg_Wide != INVALID_REG;
    }

    public boolean stringBufferInstanceReg2() {
        return stringBufferInstanceReg2 != INVALID_REG;
    }

    public boolean appendValueReg2() {
        return appendValueReg2 != INVALID_REG;
    }

    public LogRegister() {
        reset();
    }

    public void reset() {
        logTagReg = INVALID_REG;
        logTextReg = INVALID_REG;
        stringBufferInstanceReg = INVALID_REG;
        appendValueReg = INVALID_REG;
        stringBufferInstanceReg_Wide = INVALID_REG;
        appendValueReg_Wide = INVALID_REG;
        stringBufferInstanceReg2 = INVALID_REG;
        appendValueReg2 = INVALID_REG;
    }
}
