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

package analyser.gui;

import mereflect.MEClass;
import mereflect.MEMethod;

import org.jf.dexlib.Code.Instruction;

import analyser.logic.InvSnooper;
import analyser.logic.RefClass;
import analyser.logic.RefContext;
import analyser.logic.RefMethod;
import analyser.logic.Reference;

public class Selection {
    static private Object selectedObject;
    static private Object selectedView;

    public static Object getSelectedObject() {
        return selectedObject;
    }

    public static RefContext getRefContextOfSeletedObject() {
        RefContext ret = null;
        if (selectedObject != null
                && selectedObject instanceof Reference) {
            Reference ref = (Reference) selectedObject;
            while (!(ref instanceof RefContext)) {
                ref = ref.getParent();
            }
            if (ref != null
                    && ref instanceof RefContext) {
                ret = (RefContext) ref;
            }
        }
        return ret;
    }

    public static void setSelectedObject(Object view, Object selectedObject) {
        Selection.selectedObject = selectedObject;
        Selection.selectedView = view;
    }

    public static Object getSelectedView() {
        return selectedView;
    }

    public static MEMethod getMEMethod() {
        if (selectedObject instanceof MEMethod) {
            return (MEMethod) selectedObject;
        } else if (selectedObject instanceof RefMethod) {
            return ((RefMethod) selectedObject).getMethod();
        } else if (selectedObject instanceof InvSnooper.Invokation) {
            return ((InvSnooper.Invokation) selectedObject).toMethod;
        } else if (selectedObject instanceof LineBuilderFormatter.Link) {
            return (MEMethod) ((LineBuilderFormatter.Link) selectedObject).data[0];
        } else {
            return null;
        }
    }

    public static int getPc() {
        if (selectedObject instanceof LineBuilderFormatter.Link) {
            return ((Integer) ((LineBuilderFormatter.Link) selectedObject).data[1]).intValue();
        } else {
            return -1;
        }
    }

    public static Instruction getDalvikInstruction() {
        if (selectedObject instanceof LineBuilderFormatter.Link
                && ((LineBuilderFormatter.Link) selectedObject).data.length == 3) {
            return (Instruction) (((LineBuilderFormatter.Link) selectedObject).data[2]);
        } else {
            return null;
        }
    }

    public static RefMethod getRefMethod() {
        if (selectedObject instanceof RefMethod) {
            return (RefMethod) selectedObject;
        } else {
            return null;
        }
    }

    public static MEClass getMEClass() {
        if (selectedObject instanceof MEClass) {
            return (MEClass) selectedObject;
        } else if (selectedObject instanceof RefClass) {
            return ((RefClass) selectedObject).getMEClass();
        } else if (selectedObject instanceof InvSnooper.Invokation) {
            return ((InvSnooper.Invokation) selectedObject).toClass;
        } else {
            return null;
        }
    }

    public static RefClass getRefClass() {
        if (selectedObject instanceof RefClass) {
            return (RefClass) selectedObject;
        } else {
            return null;
        }
    }
}
