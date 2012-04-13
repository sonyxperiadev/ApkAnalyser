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

import gui.AppException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import jerl.bcm.inj.Injection;
import jerl.bcm.inj.InjectionClass;
import jerl.bcm.inj.InjectionEngine;
import jerl.bcm.inj.InjectionMethod;
import jerl.bcm.util.ClassInfoLoader;
import jerl.bcm.util.ClassInjContainer;
import jerl.bcm.util.InjectionUtil;
import mereflect.JarClassContext;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEMethod;
import util.JarFileModifier;
import util.StringReplacer;
import analyser.gui.ProgressReporter;
import analyser.gui.Settings;
import andreflect.ApkClassContext;
import andreflect.DexMethod;
import andreflect.injection.DalvikBytecodeModificationMediator;
import andreflect.injection.abs.DalvikInjectionMethod;

public class BytecodeModificationMediator {
    protected static BytecodeModificationMediator m_inst = null;

    protected Map<MEClassContext, Map<MEClass, ClassInjContainer>> m_bytecodeMods; // Map(Key:MEClassContext Value:Map(Key:MEClass Value:ClassInjContainer))
    static final String MOD_LIST_FILENAME = "modlist.cfg";
    static final String MANIFEST_FILENAME = "META-INF/MANIFEST.MF";

    protected BytecodeModificationMediator() {
        m_bytecodeMods = new TreeMap<MEClassContext, Map<MEClass, ClassInjContainer>>();
    }

    public static synchronized BytecodeModificationMediator getInstance() {
        if (m_inst == null) {
            m_inst = new BytecodeModificationMediator();
        }
        return m_inst;
    }

    public void unregisterModifications(MEClassContext ctx, MEClass clazz,
            MEMethod method) {
        Map<MEClass, ClassInjContainer> classMods = m_bytecodeMods.get(ctx);

        if (classMods != null) {
            ClassInjContainer injections = classMods.get(clazz);
            if (injections != null) {
                Enumeration<InjectionClass> e;
                e = injections.classInjections();
                while (e.hasMoreElements()) {
                    InjectionClass ic = e.nextElement();
                    injections.removeClassInjection(ic);
                }

                Enumeration<InjectionMethod> em;
                em = injections.methodInjections();
                while (em.hasMoreElements()) {
                    InjectionMethod im = em.nextElement();
                    injections.removeMethodInjection(im);
                }

                boolean removeContainer = injections.getNumClassInjections() == 0;
                removeContainer &= injections.getNumClassInjections() == 0;

                if (removeContainer) {
                    classMods.remove(clazz);
                }
            }
        }
    }

    public void registerModification(MEClassContext ctx, MEClass clazz,
            Injection inj, MEMethod method) {
        if (method instanceof DexMethod
                && inj instanceof InjectionMethod) {
            ((DalvikInjectionMethod) inj).setMethod((DexMethod) method);
        }
        Map<MEClass, ClassInjContainer> classMods = m_bytecodeMods.get(ctx);
        if (classMods == null) {
            classMods = new HashMap<MEClass, ClassInjContainer>();
            m_bytecodeMods.put(ctx, classMods);
        }
        ClassInjContainer injections = classMods.get(clazz);
        if (injections == null) {
            injections = new ClassInjContainer(clazz.getName());
            classMods.put(clazz, injections);
        }
        if (inj instanceof InjectionMethod) {
            injections.addMethodInjection((InjectionMethod) inj);
        } else if (inj instanceof InjectionClass) {
            injections.addClassInjection((InjectionClass) inj);
        }
    }

    /**
     * Returns list of modifications as <code>Map</code>, keyed
     * <code>MEClass</code>/<code>ClassInjContainer</code>, or null if
     * there are no modifications for specified context.
     * 
     * @param ctx
     *          The context
     * @return Map of modifications per class or null.
     */
    public Map<MEClass, ClassInjContainer> getModifications(MEClassContext ctx) {
        return m_bytecodeMods.get(ctx);
    }

    public File performRegisteredModifications(ProgressReporter pr,
            MEClassContext ctx, final String midletNamePostfix) throws IOException,
            Exception {
        if (!(ctx instanceof JarClassContext)) {
            if (ctx instanceof ApkClassContext) {
                return DalvikBytecodeModificationMediator.performRegisteredModifications(this, pr, (ApkClassContext) ctx, midletNamePostfix);
            } else {
                throw new AppException("The context " + ctx.getContextDescription()
                        + " is not a jarfile");
            }
        }
        Map<MEClass, ClassInjContainer> classInjectionsInContext = m_bytecodeMods.get(ctx);
        if (classInjectionsInContext != null) {
            // perform bytecode modifications
            final Map<String, byte[]> modifiedClasses = modifyClasses(pr, ctx,
                    classInjectionsInContext);

            // Create new jarfile with modified classes
            // create list with modified classes
            final List<String> modifiedJarEntries = new ArrayList<String>();
            Iterator<String> classI = modifiedClasses.keySet().iterator();
            while (classI.hasNext()) {
                String className = classI.next();
                modifiedJarEntries.add(className.replace('.', '/') + ".class");
            }
            modifiedJarEntries.add(MANIFEST_FILENAME);

            // create modification spec
            final ByteArrayOutputStream modSpecOut = new ByteArrayOutputStream();
            Iterator<MEClass> classIM;
            classIM = classInjectionsInContext.keySet().iterator();
            while (classIM.hasNext()) {
                MEClass clazz = classIM.next();
                ClassInjContainer injContainer = classInjectionsInContext.get(clazz);
                InjectionUtil.deconstructClassToStream(injContainer,
                        new PrintStream(modSpecOut));
            } // per class

            // create list with new entries (only modify mod spec if first
            // modification)
            final JarFile jarFile = ((JarClassContext) ctx).getJar();
            final List<String> newEntries = new ArrayList<String>();
            if (jarFile.getEntry(MOD_LIST_FILENAME) == null) {
                newEntries.add(MOD_LIST_FILENAME);
            }

            // write new jar file
            JarFileModifier jfModder = new JarFileModifier(jarFile) {
                @Override
                public List<String> excludeEntries() {
                    return null;
                }

                @Override
                public List<String> newEntries() {
                    return newEntries;
                }

                @Override
                public InputStream getNewEntry(String entryName, boolean modified) {
                    if (entryName.equals(MOD_LIST_FILENAME)) {
                        return new ByteArrayInputStream(modSpecOut.toByteArray());
                    } else if (modifiedJarEntries.contains(entryName)) {
                        if (entryName.equalsIgnoreCase(MANIFEST_FILENAME)) {
                            try {
                                String midletName = jarFile.getManifest().getMainAttributes().getValue("MIDlet-Name");
                                String newMidletName = midletName + midletNamePostfix;
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                jarFile.getManifest().write(baos);
                                String manifest = baos.toString();
                                String newManifest = StringReplacer.replaceAllStatements(manifest, midletName, newMidletName);
                                ByteArrayInputStream bais = new ByteArrayInputStream(newManifest.getBytes());
                                return bais;
                            } catch (IOException ignore) {
                                ignore.printStackTrace();
                                return null;
                            }
                        } else {
                            String className =
                                    entryName.replace('/', '.').substring(0, entryName.length() - ".class".length());
                            return new ByteArrayInputStream(modifiedClasses.get(className));
                        }
                    } else {
                        return null;
                    }
                }
            };

            File resultJar = jfModder.createModifiedJar();
            return resultJar;
        } else {
            return null;
        }
    }

    /**
     * Performs the actual bytecode modification, returns a map keying classname
     * as string to a byte array containing modified class data.
     * 
     * @param ctx
     *          MEClassContext
     * @param classInjection
     *          Map with key of MEClass, having value ClassInjContainer
     * @throws IOException
     */
    public Map<String, byte[]> modifyClasses(ProgressReporter pr, MEClassContext ctx,
            Map<MEClass, ClassInjContainer> classInjections) throws IOException {
        // the modified bytecode per class name
        Map<String, byte[]> modClasses = new HashMap<String, byte[]>();
        if (ctx instanceof JarClassContext) {
            InjectionEngine injEngine = new InjectionEngine();

            JarFile midletJar = ((JarClassContext) ctx).getJar();
            ClassInfoLoader cr = new ClassInfoLoader(midletJar);

            // Add classpath libraries for common superclass lookup
            String[] classpaths = Settings.breakString(Settings.getClasspath(), ";");
            for (int i = 0; i < classpaths.length; i++) {
                if (classpaths[i].toLowerCase().endsWith(".jar")) {
                    File libFile = new File(classpaths[i]);
                    cr.addArchive(new ZipFile(libFile));
                }
            }

            injEngine.setCommonSuperClassIF(cr);

            Iterator<MEClass> classI = classInjections.keySet().iterator();
            if (pr != null) {
                pr.reportStart(classInjections.keySet().size());
            }

            // modify each class
            int ci = 0;
            while (classI.hasNext()) {
                if (pr != null) {
                    pr.reportWork(ci++);
                }
                MEClass clazz = classI.next();
                // get modifications for class
                ClassInjContainer injContainer = classInjections.get(clazz);
                // modify
                String className = clazz.getName();
                InputStream classIS = ctx.getClassResource(className).getInputStream();
                byte[] classData;
                try {
                    classData = injEngine.preformInjection(classIS, injContainer.methodInjectionsToArray());
                } finally {
                    if (classIS != null) {
                        classIS.close();
                    }
                }
                // and store it
                modClasses.put(className, classData);
            } // per class
            if (pr != null) {
                pr.reportEnd();
            }
        }
        return modClasses;
    }

    public Set<MEClassContext> getModifiedContexts() {
        return m_bytecodeMods.keySet();
    }

    public void unregisterAllModifications() {
        m_bytecodeMods.clear();
    }

}
