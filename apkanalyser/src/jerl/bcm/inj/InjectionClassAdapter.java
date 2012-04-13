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

import java.util.Vector;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class InjectionClassAdapter extends ClassAdapter {
    private final InjectionMethod[] methodInjections;

    private final Vector<InjectionClass> addFieldInjections = new Vector<InjectionClass>();

    private final Vector<InjectionClass> addMethodInjections = new Vector<InjectionClass>();

    private String className;

    private boolean isDebugMode = false;

    public InjectionClassAdapter(ClassVisitor cv, InjectionMethod[] injections) {
        this(cv, injections, new InjectionClassField[0]);
    }

    public InjectionClassAdapter(ClassVisitor cv,
            InjectionMethod[] methodInjections,
            InjectionClass[] classInjections) {
        super(cv);
        if (methodInjections == null) {
            throw new IllegalArgumentException();
        }
        this.methodInjections = methodInjections;

        // collect disjunct classInjection types
        for (int i = 0; i < classInjections.length; i++) {
            switch (classInjections[i].getInjectionType()) {
            case Injection.FIELD_ADD_INJECTION:
                addFieldInjections.add(classInjections[i]);
                break;

            case Injection.METHOD_ADD_INJECTION:
                addMethodInjections.add(classInjections[i]);
                break;
            }
        }
    }

    /**
     * @param on
     */
    public void setDebugMode(boolean on) {
        isDebugMode = on;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        className = name;
        if (isDebugMode) {
            System.out.println("=== visit: " + className
                    + " ==================================");
            System.out.println("Number method injections="
                    + methodInjections.length + ", add field injections="
                    + addFieldInjections.size() + ", add method injections="
                    + addMethodInjections.size());
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        if (isDebugMode) {
            System.out.println("visitField: " + name);
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
            final String desc, final String signature, final String[] exceptions) {

        // final Type[] args = Type.getArgumentTypes(desc);
        MethodVisitor v = cv.visitMethod(access, name, desc, signature,
                exceptions);

        String methodIDStr = className + "." + name + desc;
        if (isDebugMode) {
            System.out.println("visitMethod: " + methodIDStr);
        }
        InjectionMethod[] methodInjections = getParametersForMethod(name + desc);
        if (methodInjections.length != 0) {
            return new InjectionMethodAdapter(v, methodInjections, isDebugMode);
        } else {
            return v;
        }
    }

    @Override
    public void visitEnd() {
        // inject new fields
        for (int i = 0; i < addFieldInjections.size(); i++) {
            InjectionClassField ifa = (InjectionClassField) addFieldInjections
                    .elementAt(i);
            ifa.inject(this);
            if (isDebugMode) {
                System.out.println("\t**Inject: " + ifa);
            }
        }
        // inject new methods
        for (int i = 0; i < addMethodInjections.size(); i++) {
            InjectionClassMethodAdd icma = (InjectionClassMethodAdd) addMethodInjections
                    .elementAt(i);
            icma.inject(this);
            if (isDebugMode) {
                System.out.println("\t**Inject: " + icma);
            }
        }
        cv.visitEnd();
    }

    private InjectionMethod[] getParametersForMethod(String methodSignature) {
        Vector<InjectionMethod> ret = new Vector<InjectionMethod>();
        for (int i = 0; i < methodInjections.length; i++) {
            if (methodInjections[i].getMethodSignature()
                    .equals(methodSignature)) {
                ret.add(methodInjections[i]);
                if (isDebugMode) {
                    System.out.println("\t" + methodInjections[i]);
                }
            }
        }
        InjectionMethod[] a = new InjectionMethod[ret.size()];
        return ret.toArray(a);
    }
}
