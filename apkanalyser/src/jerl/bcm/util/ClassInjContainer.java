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

package jerl.bcm.util;

import java.util.Enumeration;
import java.util.Vector;

import jerl.bcm.inj.InjectionClass;
import jerl.bcm.inj.InjectionMethod;

public class ClassInjContainer {
    private final String className;
    private final Vector<InjectionMethod> methodInjectionList = new Vector<InjectionMethod>();
    private final Vector<InjectionClass> classInjectionList = new Vector<InjectionClass>();

    public ClassInjContainer(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    public void addMethodInjection(InjectionMethod inj) {
        if (!methodInjectionList.contains(inj)) {
            methodInjectionList.add(inj);
        }
    }

    public boolean removeMethodInjection(InjectionMethod inj) {
        return methodInjectionList.remove(inj);
    }

    public Enumeration<InjectionMethod> methodInjections() {
        return methodInjectionList.elements();
    }

    public InjectionMethod[] methodInjectionsToArray() {
        InjectionMethod[] a = new InjectionMethod[methodInjectionList.size()];
        methodInjectionList.toArray(a);
        return a;
    }

    public int getNumMethodInjections() {
        return methodInjectionList.size();
    }

    public void addClassInjection(InjectionClass inj) {
        if (!classInjectionList.contains(inj)) {
            classInjectionList.add(inj);
        }
    }

    public boolean removeClassInjection(InjectionClass inj) {
        return classInjectionList.remove(inj);
    }

    public Enumeration<InjectionClass> classInjections() {
        return classInjectionList.elements();
    }

    public InjectionClass[] classInjectionsToArray() {
        InjectionClass[] a = new InjectionClass[classInjectionList.size()];
        classInjectionList.toArray(a);
        return a;
    }

    public int getNumClassInjections() {
        return classInjectionList.size();
    }
}
