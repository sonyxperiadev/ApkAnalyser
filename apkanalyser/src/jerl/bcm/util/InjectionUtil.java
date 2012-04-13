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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.InjectionMethod;
import jerl.bcm.inj.impl.MethodCallCrash;
import jerl.bcm.inj.impl.MethodCallGC;
import jerl.bcm.inj.impl.MethodCallOut;
import jerl.bcm.inj.impl.MethodEntryCrash;
import jerl.bcm.inj.impl.MethodEntryOut;
import jerl.bcm.inj.impl.MethodExceptionHandlerPrintStackTrace;
import jerl.bcm.inj.impl.MethodExitCrash;
import jerl.bcm.inj.impl.MethodExitOut;
import jerl.bcm.inj.impl.MethodOffsetGC;

public class InjectionUtil {
    public static final String STRING_TYPE = "java.lang.String";
    public static final String INT_TYPE = "int";
    public static final String BOOLEAN_TYPE = "boolean";

    public static void deconstructClassToStream(ClassInjContainer cic, PrintStream out) {
        out.println("\\begin{class}[" + cic.getClassName() + "]");
        Enumeration<InjectionMethod> en = cic.methodInjections();
        while (en.hasMoreElements()) {
            Injection inj = en.nextElement();
            List<String> l = deconstructInjection(inj);
            if (l.size() < 2) {
                // invalid
                continue;
            }
            out.println("\t" + l.get(0));
            for (int i = 1; i < l.size() - 1; i++) {
                out.println("\t\t" + l.get(i));
            }
            out.println("\t" + l.get(l.size() - 1));
        }
        out.println("\\end{class}");
    }

    public static List<String> deconstructInjection(Injection inj) {
        Vector<String> ret = new Vector<String>();
        Class<?> c = inj.getClass();
        List<String> l = inj.getInstanceData();

        Constructor<?> constructor = getDefaultConstructor(c);
        int numArgs = constructor.getParameterTypes().length;
        String argTypes = createTypeString(constructor);

        ret.add("\\begin{injection}[" + numArgs + "]");
        ret.add("ClassName=" + c.getName());
        ret.add("ArgTypes=" + argTypes);

        for (int i = 0; i < l.size(); i++) {
            ret.add("ArgValue[" + i + "]=" + l.get(i));
        }

        ret.add("\\end{injection}");
        return ret;
    }

    public static Constructor<?> getDefaultConstructor(Class<?> c) {
        Constructor<?>[] constList = c.getConstructors();
        if (constList.length != 1) {
            throw new IllegalArgumentException("Unable to find default constructor of class=" + c.getName());
        }
        return constList[0];
    }

    public static String createTypeString(Constructor<?> constructor) {
        StringBuffer sb = new StringBuffer();
        Class<?>[] classList = constructor.getParameterTypes();
        for (int i = 0; i < classList.length; i++) {
            String name = classList[i].getName();
            //			if (name.equals(STRING_TYPE) || name.equals(INT_TYPE) || name.equals(BOOLEAN_TYPE)) {
            sb.append(name);
            if (i < classList.length - 1) {
                sb.append(",");
            }
            //			} else {
            //				throw new IllegalArgumentException("Invalid argument type in constructor");
            //			}
        }
        return sb.toString();
    }

    public static byte[] writeTest() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        PrintStream ps = new PrintStream(baos);

        ClassInjContainer cic = new ClassInjContainer("com/sun/Test");
        cic.addMethodInjection(new MethodEntryOut("a(III[I)V", "desc"));
        cic.addMethodInjection(new MethodOffsetGC("a(III[I)V", 30));
        cic.addMethodInjection(new MethodExceptionHandlerPrintStackTrace("a(III[I)V"));
        cic.addMethodInjection(new MethodCallCrash("a(III[I)V", "java/io/DataInputStream.readInt()I", true));
        cic.addMethodInjection(new MethodCallOut("a(Lv1;)V", "java/io/DataInputStream.readInt()I", true, "message"));
        cic.addMethodInjection(new MethodExitCrash("a(III[I)V"));
        cic.addMethodInjection(new MethodExitOut("a(III[I)V", "message"));
        cic.addMethodInjection(new MethodEntryCrash("c(I)I"));

        ClassInjContainer cic2 = new ClassInjContainer("com/sun/Test2");
        cic2.addMethodInjection(new MethodEntryOut("e(III[I)V", "desc2"));
        cic2.addMethodInjection(new MethodEntryOut("e(III[I)V", "desc2"));
        cic2.addMethodInjection(new MethodEntryOut("e(III[I)V", "desc2"));
        cic2.addMethodInjection(new MethodEntryOut("f(III[I)V", "desc2"));
        cic2.addMethodInjection(new MethodEntryOut("b(III[I)V", "desc2"));
        cic2.addMethodInjection(new MethodEntryOut("d(III[I)V", "desc2"));
        cic2.addMethodInjection(new MethodOffsetGC("k(I)I", 10));
        cic2.addMethodInjection(new MethodExceptionHandlerPrintStackTrace("a(III[I)V"));
        cic2.addMethodInjection(new MethodCallCrash("f(III[I)V", "java/lang/System.currentTimeMillis()J", true));
        cic2.addMethodInjection(new MethodCallGC("f(III[I)V", "java/lang/System.currentTimeMillis()J", true));
        cic2.addMethodInjection(new MethodCallOut("a(Lv1;)V", "java/lang/System.currentTimeMillis()J", true, "message2"));
        cic2.addMethodInjection(new MethodExitCrash("b(III[I)V"));
        cic2.addMethodInjection(new MethodExitOut("d(III[I)V", "message2"));
        cic2.addMethodInjection(new MethodEntryCrash("k(I)I"));

        ClassInjContainer cic3 = new ClassInjContainer("b");
        cic3.addMethodInjection(new MethodEntryOut("a(III)Ljavax/microedition/lcdui/Font;", "desc2"));
        cic3.addMethodInjection(new MethodOffsetGC("a(Ljava/lang/String;)Ljava/io/InputStream;", 10));
        cic3.addMethodInjection(new MethodCallOut("a(Ljava/lang/String;)Ljavax/microedition/lcdui/Image;", "javax/microedition/lcdui/Image.createImage(Ljava/lang/String;)Ljavax/microedition/lcdui/Image;", true, "message"));

        deconstructClassToStream(cic, System.out);
        deconstructClassToStream(cic2, System.out);
        deconstructClassToStream(cic3, System.out);
        deconstructClassToStream(cic, ps);
        deconstructClassToStream(cic2, ps);
        deconstructClassToStream(cic3, ps);
        ps.flush();
        byte[] bBuf = baos.toByteArray();
        return bBuf;
    }

    public static void main(String[] args) {

        // simulate construction of Injections and writing them to a stream.
        byte[] bBuf = writeTest();

        System.out.println("*****************************");

        // load injections and also print them, just to verify
        try {
            InjectionBuilder ib = new InjectionBuilder(new ByteArrayInputStream(bBuf));
            Enumeration<String> enumClassNames = ib.getClassNamesForInjections();
            while (enumClassNames.hasMoreElements()) {
                String className = enumClassNames.nextElement();
                ClassInjContainer cic = ib.getClassInjContainer(className);
                deconstructClassToStream(cic, System.out);

                // when applying injections we just need this:
                cic.methodInjectionsToArray();
                // to be passed in to InjectionEngine.preformInjection()
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
