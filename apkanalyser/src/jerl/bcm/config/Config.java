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

package jerl.bcm.config;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.InjectionMethodCall;
import jerl.bcm.inj.InjectionMethodEntry;
import jerl.bcm.inj.InjectionMethodExceptionHandler;
import jerl.bcm.inj.InjectionMethodExit;
import jerl.bcm.inj.InjectionMethodOffset;
import jerl.bcm.inj.impl.MethodCallCrash;
import jerl.bcm.inj.impl.MethodCallOut;
import jerl.bcm.inj.impl.MethodEntryCrash;
import jerl.bcm.inj.impl.MethodEntryOut;
import jerl.bcm.inj.impl.MethodExceptionHandlerPrintStackTrace;
import jerl.bcm.inj.impl.MethodExitCrash;
import jerl.bcm.inj.impl.MethodExitOut;
import jerl.bcm.inj.impl.MethodOffsetGC;
import jerl.bcm.inj.impl.MethodOffsetOut;

public class Config {
    private static final Class<?>[] injections = { MethodEntryOut.class, MethodEntryCrash.class,
            MethodExitOut.class, MethodExitCrash.class,
            MethodCallOut.class, MethodCallCrash.class,
            MethodExceptionHandlerPrintStackTrace.class,
            MethodOffsetOut.class, MethodOffsetGC.class,
            /*MethodOffsetFieldOut.class*/};

    public static Class<?>[] getAvailableInjections() {
        return injections;
    }

    private static int detectInjectionType(Class<?> c) {
        int ret = -1;
        if (c.equals(InjectionMethodCall.class)) {
            ret = Injection.METHOD_CALL_INJECTION;
        } else if (c.equals(InjectionMethodEntry.class)) {
            ret = Injection.METHOD_ENTRY_INJECTION;
        } else if (c.equals(InjectionMethodExceptionHandler.class)) {
            ret = Injection.METHOD_EXCEPTION_HANDLER_INJECTION;
        } else if (c.equals(InjectionMethodExit.class)) {
            ret = Injection.METHOD_EXIT_INJECTION;
        } else if (c.equals(InjectionMethodOffset.class)) {
            ret = Injection.METHOD_OFFSET_INJECTION;
        }
        return ret;
    }

    public static int findInjectionType(Class<?> c) {
        int ret = -1;
        while (ret == -1 && c != null) {
            ret = detectInjectionType(c);
            c = c.getSuperclass();
        }
        return ret;
    }

    public static void main(String[] args) {
        Class<?>[] a = getAvailableInjections();
        for (int i = 0; i < a.length; i++) {
            System.out.println(findInjectionType(a[i]));
        }
        System.out.println(findInjectionType(String.class));
    }
}
