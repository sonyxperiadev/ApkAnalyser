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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import jerl.bcm.inj.InjectionClass;
import jerl.bcm.inj.InjectionMethod;
import jerl.blockformat.BFParseException;
import jerl.blockformat.BFReader;
import jerl.blockformat.BFVisitor;

public class InjectionBuilder implements BFVisitor {

    private String curClassName = null;
    private String curInjClassName = null;
    private String curArgTypes = null;
    private String[] curArgValues = null;

    private final BFReader pr;
    // key = class name, value = list of injections
    private final Hashtable<String, ClassInjContainer> injectionTable = new Hashtable<String, ClassInjContainer>();

    public InjectionBuilder(InputStream is) throws IOException, BFParseException {
        pr = new BFReader(is);
        pr.accept(this);
    }

    /**
     * Returns enumeration of all class names that have method injections.
     * @return
     */
    public Enumeration<String> getClassNamesForInjections() {
        return injectionTable.keys();
    }

    public ClassInjContainer getClassInjContainer(String className) {
        return injectionTable.get(className);
    }

    @Override
    public void visitBeginBlock(String blockName, String[] args) {
        if (blockName.equals("class")) {
            // start of class block
            String className = args[0];
            curClassName = className;
        } else if (blockName.equals("injection")) {
            int n = Integer.parseInt(args[0]);
            curInjClassName = null;
            curArgTypes = null;
            curArgValues = new String[n];
        }
    }

    @Override
    public void visitEndBlock(String blockName) {
        if (blockName.equals("class")) {
            // end of class block
            curClassName = null;
        } else if (blockName.equals("injection")) {
            // create an injection instance and add to injectionTable
            Object obj = null;
            try {
                obj = createInjection(curInjClassName, curArgTypes, curArgValues);
            } catch (Exception e) {
                System.err.println("Class: " + curInjClassName + ", argtypes=" + curArgTypes + ", argValues=" + curArgValues);
                e.printStackTrace();
            }
            if (obj != null) {
                if (obj instanceof InjectionMethod) {
                    addMethodInjection(curClassName, (InjectionMethod) obj);
                } else if (obj instanceof InjectionClass) {
                    addClassInjection(curClassName, (InjectionClass) obj);
                } else {
                    System.err.println("WARNING: Unknown class type");
                }
            }
        }
    }

    @Override
    public void visitProperty(String key, String value) {
        if (key.equals("ClassName")) {
            curInjClassName = value;
        } else if (key.equals("ArgTypes")) {
            curArgTypes = value;
        } else if (key.startsWith("ArgValue")) {
            int i = parseIndex(key);
            curArgValues[i] = value;
        } else {
            System.out.println("Unknown property: key='" + key + "', value='" + value + "'");
        }
    }

    @Override
    public String toString() {
        return injectionTable.toString();
    }

    private void addMethodInjection(String className, InjectionMethod inj) {
        ClassInjContainer c = injectionTable.get(className);
        if (c == null) {
            c = new ClassInjContainer(className);
            injectionTable.put(className, c);
        }
        c.addMethodInjection(inj);
    }

    private void addClassInjection(String className, InjectionClass inj) {
        ClassInjContainer c = injectionTable.get(className);
        if (c == null) {
            c = new ClassInjContainer(className);
            injectionTable.put(className, c);
        }
        c.addClassInjection(inj);
    }

    private static int parseIndex(String str) {
        int beginIndex = str.indexOf('[');
        int endIndex = str.indexOf(']', beginIndex);
        return Integer.parseInt(str.substring(beginIndex + 1, endIndex).trim());
    }

    private static Object[] createParameters(String types, String[] values) {
        StringTokenizer st = new StringTokenizer(types, ",");
        String[] aTypes = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            aTypes[i] = st.nextToken().trim();
        }

        Object[] ret = new Object[aTypes.length];

        // loop for all types, create object from value string
        for (int i = 0; i < aTypes.length; i++) {
            if (aTypes[i].equals(InjectionUtil.STRING_TYPE)) {
                ret[i] = values[i];
            } else if (aTypes[i].equals(InjectionUtil.INT_TYPE)) {
                ret[i] = Integer.valueOf(values[i]);
            } else if (aTypes[i].equals(InjectionUtil.BOOLEAN_TYPE)) {
                ret[i] = Boolean.valueOf(values[i]);
            } else {
                System.err.println("ERROR: unknown type, '" + aTypes[i] + "'");
            }
        }
        return ret;
    }

    private static Object createInjection(String className, String types, String[] values) throws Exception {
        Class<?> cls = Class.forName(className);
        Constructor<?>[] a = cls.getConstructors();

        int i = 0;
        for (i = 0; i < a.length; i++) {
            String tmp = a[i].toString();
            if (tmp.indexOf("(" + types + ")") != -1) {
                break;
            }
        }
        if (i == a.length) {
            // unable to find constructor
            System.err.println("ERROR: unable to find constructor");
            return null;
        }

        try {
            Object[] initargs = createParameters(types, values);
            Object obj = a[i].newInstance(initargs);
            return obj;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new Exception("ClassName='" + className + "', types='" + types + "'");
        }
    }
}
