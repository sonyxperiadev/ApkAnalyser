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

package jerl.bcm.inj;

public abstract class InjectionClassField extends InjectionClass implements Comparable<Object> {
    private final int access;
    private final String name;
    private final String desc;
    private final String signature;
    private final Object value;

    public InjectionClassField(int access, String name, String desc) {
        this(access, name, desc, null, null);
    }

    public InjectionClassField(int access, String name, String desc, String signature, Object value) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.value = value;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getSignature() {
        return signature;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof InjectionClassField) {
            return getName().compareTo(((InjectionClassField) o).getName());
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "[" + name + ", type=" + getInjectionType() + "]";
    }
}
