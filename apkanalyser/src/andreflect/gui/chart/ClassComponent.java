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

package andreflect.gui.chart;

import java.io.Serializable;
import java.util.ArrayList;

import analyser.logic.RefClass;

import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;

public class ClassComponent implements Serializable {
    private static final long serialVersionUID = 8626642222587181515L;
    protected RefClass ref;
    protected ArrayList<MEMethod> methods = new ArrayList<MEMethod>();
    protected ArrayList<MEField> fields = new ArrayList<MEField>();

    protected int type;

    public final int ARCHITECTURE = 0;
    public final int DEPENDENCY = 1;
    public final int DEP_SUPER = 2;
    public final int DEP_FOCUS = 4;

    public ClassComponent(RefClass r) {
        ref = r;
        type = ARCHITECTURE;
    }

    public void clean() {
        methods.clear();
        fields.clear();
    }

    public void addMethod(MEMethod method) {
        if (!methods.contains(method)) {
            methods.add(method);
        }
    }

    public void addField(MEField field) {
        if (!fields.contains(field)) {
            fields.add(field);
        }
    }

    public ArrayList<MEMethod> getMethods() {
        if (isArch() || isFocus()) {
            ArrayList<MEMethod> ret = new ArrayList<MEMethod>();
            for (MEMethod meth : ref.getMEClass().getMethods()) {
                if (meth.isPublic()) {
                    ret.add(meth);
                }
            }
            return ret;
        } else {
            return methods;
        }
    }

    public ArrayList<MEField> getFields() {
        if (isArch() || isFocus()) {
            ArrayList<MEField> ret = new ArrayList<MEField>();
            for (MEField field : ref.getMEClass().getFields()) {
                if (field.isPublic()) {
                    ret.add(field);
                }
            }
            return ret;
        } else {
            return fields;
        }
    }

    public RefClass getRefClass() {
        return ref;
    }

    public boolean isArch() {
        return type == ARCHITECTURE;
    }

    public boolean isDependency() {
        return (type & DEPENDENCY) != 0;
    }

    public boolean isFocus() {
        return (type & DEP_FOCUS) != 0;
    }

    public boolean isDepSuper() {
        return (type & DEP_SUPER) != 0;
    }

    public MEClass getMEClass() {
        return ref.getMEClass();
    }

    public void resetToArch() {
        type = ARCHITECTURE;
    }

    public void setDependecy() {
        type |= DEPENDENCY;
    }

    public void setFocus() {
        type |= DEP_FOCUS;
    }

    public void setDepSuper() {
        type |= DEP_SUPER;
    }

    public int getType() {
        return type;
    }

}
