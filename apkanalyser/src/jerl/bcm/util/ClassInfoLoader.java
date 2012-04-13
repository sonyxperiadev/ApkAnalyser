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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassInfoLoader implements CommonSuperClassIF {
    private final Vector<ZipFile> archiveFiles = new Vector<ZipFile>();
    private boolean isDebugMode = true;

    public ClassInfoLoader(ZipFile file) {
        archiveFiles.add(file);
    }

    /**
     * @param mode
     */
    public void setDebugMode(boolean mode) {
        isDebugMode = mode;
    }

    public void addArchive(ZipFile file) {
        archiveFiles.add(file);
    }

    private String convertToFilename(String className) {
        // className.replace('.', newChar)
        return className + ".class";
    }

    private InputStream getInputStream(String className) throws IOException {
        String filename = convertToFilename(className);
        for (int i = 0; i < archiveFiles.size(); i++) {
            ZipFile zipFile = archiveFiles.elementAt(i);
            ZipEntry entry = zipFile.getEntry(filename);
            if (entry != null) {
                return zipFile.getInputStream(entry);
            }
        }
        return null;
    }

    // TODO: make a cache of ClassInfo instances
    private ClassInfo loadClassInfo(String name) throws ClassNotFoundException {
        ClassReader cr = null;
        try {
            InputStream is = getInputStream(name);
            if (is != null) {
                cr = new ClassReader(is);
            } else {
                throw new ClassNotFoundException(name);
            }
        } catch (IOException ioe) {
            throw new ClassNotFoundException(name);
        }
        ClassWriter cw = new ClassWriter(0);
        ClassInfoBuilder cv = new ClassInfoBuilder(cw);
        cr.accept(cv, 0);
        return cv.getClassInfo();
    }

    @Override
    public String getCommonSuperClass(final String type1, final String type2) {
        if (isDebugMode) {
            System.out.print("findCommonSuperClass('" + type1 + "', '" + type2 + "')");
        }

        ClassInfo ci1 = null;
        ClassInfo ci2 = null;

        try {
            ci1 = loadClassInfo(type1);
        } catch (ClassNotFoundException cnfe) {
            if (isDebugMode) {
                System.out.println("ERROR: " + cnfe);
            }
        }
        try {
            ci2 = loadClassInfo(type2);
        } catch (ClassNotFoundException cnfe) {
            if (isDebugMode) {
                System.out.println("ERROR: " + cnfe);
            }
        }

        if (ci1.isAssignableFrom(ci2)) {
            if (isDebugMode) {
                System.out.println(" : " + type1);
            }
            return type1;
        }
        if (ci2.isAssignableFrom(ci1)) {
            if (isDebugMode) {
                System.out.println(" : " + type2);
            }
            return type2;
        }

        if (ci1.isInterface || ci2.isInterface) {
            if (isDebugMode) {
                System.out.println(" : java/lang/Object");
            }
            return "java/lang/Object";
        } else {
            // step up in class hierachy to find common super class
            do {
                ci1 = ci1.getSuperClassInfo();
            } while (!ci1.isAssignableFrom(ci2));
            if (isDebugMode) {
                System.out.println(" : " + ci1.name);
            }
            return ci1.name;
        }
    }

    public static void main(String[] args) throws IOException {
        File libFile = new File("res/javaclasses.zip");
        File jarFile = new File("res/MMP-SE_K790.jar");
        ClassInfoLoader cr = new ClassInfoLoader(new ZipFile(jarFile));
        cr.setDebugMode(true);
        cr.addArchive(new ZipFile(libFile));

        //cr.findCommonSuperClass("a2", "a7");
        System.out.println(cr.getCommonSuperClass("java/lang/Object", "a7"));
        System.out.println(cr.getCommonSuperClass("oh", "dr"));
    }

    /////// ClassInfoBuilder ////////////////////////////////////////////////////////
    class ClassInfoBuilder extends ClassAdapter {
        private String superName;
        private String name;
        private String[] interfaces;
        private boolean isInterface;

        public ClassInfoBuilder(ClassVisitor cv) {
            super(cv);
        }

        @Override
        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {
            this.name = name;
            this.superName = superName;
            this.interfaces = interfaces;
            isInterface = (access & Opcodes.ACC_INTERFACE) == 0 ? false : true;
            cv.visit(version, access, name, signature, superName, interfaces);
        }

        public ClassInfo getClassInfo() {
            return new ClassInfo(name, superName, interfaces, isInterface);
        }
    }

    /////// ClassInfo ////////////////////////////////////////////////////////////
    class ClassInfo {
        public final String superName;
        public final String name;
        private final String[] interfaces;
        private final boolean isInterface;

        public ClassInfo(String name, String superName, String[] interfaces, boolean isInterface) {
            this.name = name;
            this.superName = superName;
            this.interfaces = interfaces;
            this.isInterface = isInterface;
        }

        @Override
        public String toString() {
            return isInterface ? "interface " : "class " + name + " extends " + superName + " implements " + interfaces.length + " interfaces";
        }

        public ClassInfo getSuperClassInfo() {
            try {
                return loadClassInfo(superName);
            } catch (ClassNotFoundException cnfe) {
                return null;
            }
        }

        /**
         * Determines if the class or interface represented by this <code>Class</code>
         * object is either the same as, or is a superclass or superinterface of,
         * the class or interface represented by the specified <code>Class</code>
         * parameter. It returns <code>true</code> if so; otherwise it returns
         * <code>false</code>.
         */
        public boolean isAssignableFrom(ClassInfo ci) {
            if (name.equals(ci.name) || name.equals("java/lang/Object")) {
                // same class or this is java.lang.Object in which case ci is assignable
                return true;
            }
            // is this a super class of ci
            while (!ci.name.equals("java/lang/Object")) {
                ci = ci.getSuperClassInfo();
                if (name.equals(ci.name)) {
                    // same class
                    return true;
                }
            }
            return false;
        }
    }
}
