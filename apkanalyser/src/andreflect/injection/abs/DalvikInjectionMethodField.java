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

package andreflect.injection.abs;

import java.util.List;
import java.util.Vector;

import jerl.bcm.inj.Injection;

import org.jf.dexlib.FieldIdItem;

public abstract class DalvikInjectionMethodField extends DalvikInjectionMethod {
    public boolean isRead = false;
    public FieldIdItem fieldIdItem;

    public DalvikInjectionMethodField(String signature, FieldIdItem fieldIdItem, boolean isRead) {
        super(signature);
        this.isRead = isRead;
        this.fieldIdItem = fieldIdItem;
    }

    @Override
    public int getInjectionType() {
        return Injection.METHOD_FIELD_ACCESS_INJECTION;
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        if (fieldIdItem != null) {
            v.add(fieldIdItem.getFieldString());
        } else {
            v.add("null");
        }
        v.add(Boolean.toString(isRead));
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            DalvikInjectionMethodField meo = (DalvikInjectionMethodField) o;
            return fieldIdItem == meo.fieldIdItem
                    && isRead == meo.isRead;
        } else {
            return false;
        }
    }

}