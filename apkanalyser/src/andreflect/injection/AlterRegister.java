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

public class AlterRegister {
    public static final short INVALID_REG = -1;

    public short lowRegister;
    public short highRegister;
    public short lowObjRegister;
    public short highObjRegister;
    public short lowWideRegister;
    public short highWideRegister;

    public AlterRegister() {
        reset();
    }

    public boolean checkWide() {
        return lowWideRegister != INVALID_REG;
    }

    public boolean checkNormal() {
        return lowRegister != INVALID_REG;
    }

    public boolean checkObject() {
        return lowObjRegister != INVALID_REG;
    }

    public boolean check(boolean isWide) {
        if (isWide) {
            return (checkNormal() || checkObject()) && checkWide();
        } else {
            return (checkNormal() || checkObject());
        }
    }

    public void setWide(short lowWideRegister) {
        this.lowWideRegister = lowWideRegister;
        highWideRegister = INVALID_REG;
    }

    public void setWide(short lowWideRegister, short highWideRegister) {
        this.lowWideRegister = lowWideRegister;
        this.highWideRegister = highWideRegister;
    }

    public void setNormal(short lowRegister) {
        this.lowRegister = lowRegister;
        highRegister = INVALID_REG;
    }

    public void setNormal(short lowRegister, short highRegister) {
        this.lowRegister = lowRegister;
        this.highRegister = highRegister;
    }

    public void setObject(short lowObjRegister) {
        this.lowObjRegister = lowObjRegister;
        highObjRegister = INVALID_REG;
    }

    public void setObject(short lowObjRegister, short highObjRegister) {
        this.lowObjRegister = lowObjRegister;
        this.highObjRegister = highObjRegister;
    }

    public void setAll(short lowRegister, short highRegister,
            short lowWideRegister, short highWideRegister,
            short lowObjRegister, short highObjRegister) {
        this.lowRegister = lowRegister;
        this.highRegister = highRegister;
        this.lowWideRegister = lowWideRegister;
        this.highWideRegister = highWideRegister;
        this.lowObjRegister = lowObjRegister;
        this.highObjRegister = highObjRegister;
    }

    public void reset() {
        lowRegister = INVALID_REG;
        highRegister = INVALID_REG;
        lowWideRegister = INVALID_REG;
        highWideRegister = INVALID_REG;
        lowObjRegister = INVALID_REG;
        highObjRegister = INVALID_REG;
    }
}
