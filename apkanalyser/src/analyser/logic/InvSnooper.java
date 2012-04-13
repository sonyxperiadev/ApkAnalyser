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

package analyser.logic;

import gui.Canceable;

import java.util.ArrayList;
import java.util.List;

import analyser.gui.ProgressReporter;

import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEMethod;

public class InvSnooper {
    public static final int ABSTRACT = 1 << 0;
    public static final int OVERRIDDEN = 1 << 1;

    private static void registerInvokationResult(List<Invokation> invs,
            MEClass fromClass, MEMethod fromMethod,
            MEClass toClass, MEMethod toMethod,
            int flags,
            boolean allowDuplicates) {
        Invokation inv = new Invokation(fromClass, fromMethod, toClass, toMethod, flags);
        if (allowDuplicates || !invs.contains(inv)) {
            invs.add(inv);
        }
    }

    public static List<Invokation> findCalls(RefMethod ref, boolean virtualCall, boolean allowDuplicates, Canceable canceable,
            ProgressReporter reporter) throws Throwable {
        return findCalls(ref.getMethod(), virtualCall, allowDuplicates, canceable, reporter);
    }

    public static List<Invokation> findCalls(MEMethod fromMethod, boolean virtualCall, boolean allowDuplicates, Canceable canceable,
            ProgressReporter reporter) throws Throwable {
        MEClass fromClass = fromMethod.getMEClass();
        MEClassContext fromCtx = fromClass.getResource().getContext();
        List<Invokation> invRes = new ArrayList<Invokation>();

        // If virtual call, we need to check actual calls in method in addition to possible overrides
        if (fromMethod.isAbstract() || virtualCall) {
            // Coming from a pure interface/abstract method (1)
            List<MEClass> implementors = findClassChildren(fromClass);
            if (reporter != null) {
                reporter.reportStart(implementors.size());
            }
            implementors.remove(fromClass);
            for (int i = 0; i < implementors.size() &&
                    (canceable != null && canceable.isRunning() || canceable == null); i++) {
                MEClass candidateClass = implementors.get(i);
                MEMethod rLocalMethod = candidateClass.getMethodIsolated(fromMethod.getName(), fromMethod.getDescriptor());
                if (rLocalMethod != null) {
                    registerInvokationResult(invRes, fromClass, fromMethod, candidateClass,
                            rLocalMethod, ABSTRACT, allowDuplicates);
                    if (reporter != null) {
                        reporter.reportWork(i);
                    }
                }
            }
        }
        if (!fromMethod.isAbstract() || virtualCall) {
            // Coming from a normal method (2)
            List<MEMethod.Invokation> invokations = fromMethod.getInvokations();
            if (reporter != null) {
                reporter.reportStart(invokations.size());
            }
            for (int i = 0; i < invokations.size() &&
                    (canceable != null && canceable.isRunning() || canceable == null); i++) {
                MEMethod.Invokation inv = invokations.get(i);
                try {
                    MEClass invClass = fromCtx.getMEClass(inv.invClassname);
                    if (invClass.isAbstract() || invClass.isInterface() || inv.isInterface || inv.isVirtual) {
                        // Calling an interface or abstract method, just add this since it will be resolved in (1)
                        MEMethod invMethod = invClass.getMethodIsolated(inv.invMethodname, inv.invDescriptor);
                        if (invMethod != null) {
                            registerInvokationResult(invRes, fromClass, fromMethod, invClass, invMethod, ABSTRACT, allowDuplicates);
                        }
                    } else {
                        // Calling a normal method, check if there are overrides of this method
                        List<MEClass> implementors = findClassChildren(invClass);
                        for (int j = 0; j < implementors.size(); j++) {
                            invClass = implementors.get(j);
                            MEMethod invMethod = invClass.getMethodIsolated(inv.invMethodname, inv.invDescriptor);
                            if (invMethod != null) {
                                registerInvokationResult(invRes, fromClass, fromMethod, invClass, invMethod, implementors.size() > 1 ? OVERRIDDEN : 0, allowDuplicates);
                            }
                        }
                    }
                } catch (ClassNotFoundException cnfe) {
                    // not a class in midlet
                }
                if (reporter != null) {
                    reporter.reportWork(i);
                }
            }
        }
        if (reporter != null) {
            reporter.reportEnd();
        }
        return invRes;
    }

    public static List<Invokation> findCallers(RefMethod ref, boolean allowDuplicates, Canceable canceable,
            ProgressReporter reporter) throws Throwable {
        return findCallers(ref.getMethod(), allowDuplicates, canceable, reporter);
    }

    public static List<Invokation> findCallers(MEMethod toMethod, boolean allowDuplicates, Canceable canceable,
            ProgressReporter reporter) throws Throwable {
        MEClass toClass = toMethod.getMEClass();
        MEClassContext ctx = toClass.getResource().getContext();

        String[] classNames = ctx.getClassnames();
        List<Invokation> invRes = new ArrayList<Invokation>();
        List<MEClass> definitors = findClassParents(toClass);
        if (reporter != null) {
            reporter.reportStart(classNames.length);
        }

        // All classes in toClass context
        for (int i = 0; i < classNames.length && (canceable != null && canceable.isRunning() || canceable == null); i++) {
            MEClass fromCandidateClass = ctx.getMEClass(classNames[i]);
            MEMethod[] candidateMethods = fromCandidateClass.getMethods();
            // All methods
            if (candidateMethods != null) {
                for (int j = 0; j < candidateMethods.length; j++) {
                    MEMethod fromCandidateMethod = candidateMethods[j];
                    List<MEMethod.Invokation> invokations = fromCandidateMethod.getInvokations();
                    // From all classes, all methods, to all parents of toClass incl toClass
                    for (int k = 0; k < definitors.size() && (canceable != null && canceable.isRunning() || canceable == null); k++) {
                        MEClass toDefiningClass = definitors.get(k);
                        // All classes, all methods, all invokations
                        for (int l = 0; l < invokations.size() && (canceable != null && canceable.isRunning() || canceable == null); l++) {
                            MEMethod.Invokation inv = invokations.get(l);
                            if (inv.invClassname.equals(toDefiningClass.getName()) &&
                                    inv.invMethodname.equals(toMethod.getName()) &&
                                    inv.invDescriptor.equals(toMethod.getDescriptor())) {
                                MEMethod toDefiningMethod = toDefiningClass.getMethodIsolated(inv.invMethodname, inv.invDescriptor);
                                if (toDefiningMethod != null) {
                                    if ((inv.isInterface || inv.isVirtual) && !toDefiningClass.equals(toClass) && !toDefiningMethod.equals(toMethod)) {
                                        // put in interface or overridden class in between actual call
                                        registerInvokationResult(invRes, toClass, toMethod, toDefiningClass, toDefiningMethod,
                                                toDefiningClass.equals(toClass) ? InvSnooper.ABSTRACT : InvSnooper.OVERRIDDEN, allowDuplicates);
                                    } else {
                                        registerInvokationResult(invRes, toDefiningClass, toDefiningMethod, fromCandidateClass, fromCandidateMethod,
                                                (inv.isInterface || inv.isVirtual) ? InvSnooper.ABSTRACT : 0, allowDuplicates);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (reporter != null) {
                reporter.reportWork(i);
            }
        }
        if (reporter != null && canceable != null && canceable.isRunning()) {
            reporter.reportEnd();
        }
        return invRes;
    }

    public static List<MEClass> findClassChildren(MEClass clazz) throws Throwable {
        List<MEClass> res = new ArrayList<MEClass>();
        MEClassContext ctx = clazz.getResource().getContext();
        String[] classNames = ctx.getClassnames();
        for (int i = 0; i < classNames.length; i++) {
            MEClass candidate = ctx.getMEClass(classNames[i]);
            if (candidate.isInstanceOf(clazz)) {
                res.add(candidate);
            }
        }
        return res;
    }

    public static List<MEClass> findClassParents(MEClass clazz) throws Throwable {
        List<MEClass> res = new ArrayList<MEClass>();
        res.add(clazz);
        recurseParents(clazz, res);
        return res;
    }

    private static void recurseParents(MEClass clazz, List<MEClass> res) throws Throwable {
        MEClass superClass = clazz.getSuperClass();
        if (superClass != null) {
            res.add(superClass);
            recurseParents(superClass, res);
        }
        MEClass[] ifcs = clazz.getInterfaces();
        if (ifcs != null) {
            for (int i = 0; i < ifcs.length; i++) {
                res.add(ifcs[i]);
            }
            for (int i = 0; i < ifcs.length; i++) {
                recurseParents(ifcs[i], res);
            }
        }
    }

    public static List<RefInvokation> toRefInvokations(List<Invokation> invokations) {
        List<RefInvokation> res = new ArrayList<RefInvokation>();
        for (int i = 0; i < invokations.size(); i++) {
            Invokation inv = invokations.get(i);
            String invStr = inv.fromClass.getName() + "." +
                    inv.fromMethod.getFormattedName() +
                    "(" + inv.fromMethod.getArgumentsString() + ")";
            RefContext refContext = new RefContext(inv.fromClass.getResource().getContext());
            String refPackName = inv.toClass.getResource().getPackage();
            RefPackage refPack = refContext.registerPackage(refPackName);
            RefClass refClass = refPack.registerClass(inv.toClass);
            RefMethod refMethod = refClass.registerMethod(inv.toMethod);
            RefInvokation refInv = new RefInvokation(invStr, refContext,
                    refPack, refClass, refMethod, true, null);
            refMethod.registerInvokation(refInv);
            res.add(refInv);
        }
        return res;
    }

    public static class Invokation {
        public MEClass fromClass;
        public MEMethod fromMethod;
        public MEClass toClass;
        public MEMethod toMethod;
        public int flags;

        public Invokation(MEClass fromClass, MEMethod fromMethod, MEClass toClass, MEMethod toMethod, int flags) {
            this.fromClass = fromClass;
            this.fromMethod = fromMethod;
            this.toClass = toClass;
            this.toMethod = toMethod;
            this.flags = flags;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Invokation) {
                Invokation i = (Invokation) o;
                return (i.fromClass.equals(fromClass) &&
                        i.fromMethod.equals(fromMethod) &&
                        i.toClass.equals(toClass) && i.toMethod.equals(toMethod));
            } else {
                return false;
            }
        }
    }
}
