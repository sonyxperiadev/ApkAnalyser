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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;

import mereflect.AbstractClassContext;
import mereflect.CollaborateClassContext;
import mereflect.CorruptBytecodeException;
import mereflect.JarClassContext;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEClassResource;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.UnknownClass;
import mereflect.UnknownContext;
import mereflect.UnknownField;
import mereflect.UnknownMethod;
import mereflect.UnknownResContext;
import mereflect.UnknownResSpec;
import mereflect.UnknownResource;
import mereflect.io.DescriptorParser;

import org.jf.dexlib.FieldIdItem;

import analyser.Analyser;
import analyser.gui.Settings;
import andreflect.ApkClassContext;
import andreflect.DexMethod;
import andreflect.DexReferenceCache;
import andreflect.DexReferenceCache.LoadConstRes;
import andreflect.DexResSpec;
import andreflect.Util;
import andreflect.xml.XmlParser;
import andreflect.xml.XmlParser.XmlLine;
import brut.androlib.res.data.ResResSpec;

public class Resolver
{
    /** Map with resolved context references on classpaths side, key String(ctxName), value RefContext */
    protected Map<String, Reference> m_refContexts = new TreeMap<String, Reference>();
    /** Map with resolved context references on midlets side, key String(ctxName), value RefContext */
    protected Map<String, Reference> m_midContexts = new TreeMap<String, Reference>();

    protected ResolverListener m_listener = null;
    protected CollaborateClassContext m_sctx;
    protected UnknownContext m_uCtx = null;
    protected UnknownResContext m_uResCtx = null;

    public CollaborateClassContext getReferenceContext()
    {
        return m_sctx;
    }

    public void removeMidlet(MEClassContext ctx)
    {
        m_midContexts.remove(Analyser.getContextName(ctx));
    }

    public void setListener(ResolverListener list)
    {
        m_listener = list;
    }

    /**
     * Looks up all invokations on midlets side and registers them in midContext map.
     * All found are also mirrored in refContext map for quicker lookup.
     * @param c
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void resolve(Canceable c) throws IOException, ClassNotFoundException
    {
        Analyser a = new Analyser(Settings.getSettings());
        List<File> midlets = new ArrayList<File>();
        List<File> apks = new ArrayList<File>();
        a.getMidlets(midlets);
        a.getApks(apks);

        if (midlets.isEmpty() && apks.isEmpty())
        {
            throw new IllegalArgumentException("No Apk or midlets found");
        }

        int midletSize = midlets.size();
        int apkSize = apks.size();
        int midletIdx = 0;
        int apkIdx = 0;
        for (midletIdx = 0; c.isRunning() && midletIdx < midletSize; midletIdx++)
        {
            File midletFile = midlets.get(midletIdx);
            resolve(midletFile, c, apkIdx + midletIdx, midletSize + apkSize);
        }

        for (apkIdx = 0; c.isRunning() && apkIdx < apkSize; apkIdx++)
        {
            System.out.println((apks.get(apkIdx)).getName());
        }

        for (apkIdx = 0; c.isRunning() && apkIdx < apkSize; apkIdx++)
        {
            File apkFile = apks.get(apkIdx);
            resolveApk(apkFile, c, apkIdx + midletIdx, midletSize + apkSize);
        }
        if (m_listener != null
                && midletIdx >= midletSize - 1
                && apkIdx >= apkSize - 1)
        {
            m_listener.resolved();
        }
    }

    public void resolve(File midletFile, Canceable c,
            int midletIndex, int midletCount)
            throws IOException, ClassNotFoundException
    {
        // Get class context for path entries
        if (m_sctx == null)
        {
            Analyser a = new Analyser(Settings.getSettings());
            m_sctx = new CollaborateClassContext();
            a.setClasspath(m_sctx);
            DescriptorParser.setSuperContext(m_sctx); // Set supercontext
        }

        // Resolve midlet
        JarClassContext midletCtx = new JarClassContext(midletFile, true);
        if (m_midContexts.containsKey(Analyser.getContextName(midletCtx)))
        {
            // Already resolved
            return;
        }

        // Go through all classes in context
        String[] cNames = midletCtx.getClassnames();

        for (int iC = 0; c.isRunning() && iC < cNames.length; iC++)
        {
            MEClass mClass = midletCtx.getMEClass(cNames[iC]);
            MEMethod[] mMethods = mClass.getMethods();
            if (mMethods == null) {
                continue;
            }
            // Go through all methods
            for (int iM = 0; c.isRunning() && iM < mMethods.length; iM++)
            {
                boolean methFail = false;
                boolean methNotFound = false;
                MEMethod mMethod = mMethods[iM];
                if (m_listener != null)
                {
                    m_listener.resolving(midletIndex, midletCount, iC, cNames.length, iM, mMethods.length,
                            midletCtx, mClass, mMethod);
                }
                List<MEMethod.Invokation> invokations = null; // Key string(invokation), value integer(nbrOfInvokations)
                Iterator<MEMethod.Invokation> iI = null;
                try
                {
                    invokations = mMethod.getInvokations();
                    iI = invokations.iterator();
                } catch (CorruptBytecodeException cbe)
                {
                    methFail = true;
                }
                if (!methFail && (iI == null || !iI.hasNext()))
                {
                    // No invokations in this method
                    buildEmptyRef(midletCtx, mClass, mMethod, null, null);
                }
                // Go through all invokations in method
                while (c.isRunning() && iI != null && iI.hasNext())
                {
                    MEMethod.Invokation inv = iI.next();
                    boolean externalInvokation = false;

                    // Try getting invoked class from midlet jar
                    try
                    {
                        MEClass rLocalClass = midletCtx.getMEClass(inv.invClassname);
                        boolean showAllInvocations = true;
                        if (showAllInvocations)
                        {
                            MEMethod rLocalMethod = rLocalClass.getMethod(inv.invMethodname, inv.invDescriptor);
                            methNotFound = true;
                            rLocalMethod = new UnknownMethod(inv.invMethodname, inv.invDescriptor, rLocalClass);

                            buildRef(midletCtx, mClass, mMethod,
                                    methFail, methNotFound,
                                    null, rLocalClass, rLocalMethod, inv, null, null);
                        }
                    } catch (ClassNotFoundException cnfe)
                    {
                        externalInvokation = true;
                    }

                    // Could not get invoked class from midlet jar, try api classes
                    if (externalInvokation)
                    {
                        try
                        {
                            MEClass rClass = m_sctx.getMEClass(inv.invClassname);
                            MEMethod rMethod = rClass.getMethod(inv.invMethodname, inv.invDescriptor);
                            if (rMethod == null)
                            {
                                // Invoked method not found in api class, make an unknown method reference
                                methNotFound = true;
                                rMethod = new UnknownMethod(inv.invMethodname, inv.invDescriptor, rClass);
                            }
                            MEClassResource rRsc = rClass.getResource();
                            MEClassContext rCtx = rRsc.getContext();
                            buildRef(midletCtx, mClass, mMethod,
                                    methFail, methNotFound,
                                    rCtx, rClass, rMethod, inv, null, null);
                        } catch (ClassNotFoundException e)
                        {

                            // Class not found in api classes, make an unknown resource reference
                            methNotFound = true;
                            if (m_uCtx == null)
                            {
                                m_uCtx = new UnknownContext();
                            }
                            UnknownClass uClass = null;
                            try
                            {
                                uClass = (UnknownClass) m_uCtx.getMEClass(inv.invClassname);
                            } catch (ClassNotFoundException cnfe)
                            {
                                UnknownResource uRes = new UnknownResource(inv.invClassname, m_uCtx);
                                uClass = new UnknownClass(inv.invClassname, uRes);
                                m_uCtx.defineClass(uClass);
                            }
                            UnknownMethod uMethod = null;
                            uMethod = (UnknownMethod) uClass.getMethod(inv.invMethodname, inv.invDescriptor);
                            if (uMethod == null)
                            {
                                uMethod = new UnknownMethod(inv.invMethodname, inv.invDescriptor, uClass);
                            }

                            buildRef(midletCtx, mClass, mMethod,
                                    methFail, methNotFound,
                                    m_uCtx, uClass, uMethod, inv, null, null);
                        } // unknown class
                    } // external invokation
                } // per invokation
            } // per method
        } // per class
    }

    public void resolveApk(File apkFile, Canceable c,
            int apkIndex, int apkCount)
            throws IOException, ClassNotFoundException
    {
        //workaround
        ArrayList<Object> objs = new ArrayList<Object>();
        ArrayList<ReverseReference> refs = new ArrayList<ReverseReference>();

        if (m_sctx == null)
        {
            Analyser a = new Analyser(Settings.getSettings());
            m_sctx = new CollaborateClassContext();
            a.setClasspath(m_sctx);
            DescriptorParser.setSuperContext(m_sctx); // Set supercontext
        }

        // Resolve apk
        ApkClassContext apkCtx = new ApkClassContext(apkFile, true);
        if (m_midContexts.containsKey(Analyser.getContextName(apkCtx)))
        {
            // Already resolved
            return;
        }

        if (apkCtx.getXmlParser().getManifest() != null) {
            RefContext midResource = (RefContext) m_midContexts.get(Analyser.getContextName(apkCtx));
            if (midResource == null)
            {
                midResource = new RefContext(apkCtx);
                m_midContexts.put(Analyser.getContextName(apkCtx), midResource);
            }
            ZipEntry manifest = apkCtx.getXmlParser().visitFile(XmlParser.MANIFEST);
            RefAndroidManifest refManifest = midResource.registerManifest(manifest);
            objs.add(manifest);
            refs.add(refManifest);
        }

        String[] cNames = apkCtx.getClassnames();

        for (int iC = 0; c.isRunning() && iC < cNames.length; iC++)
        {
            MEClass mClass = apkCtx.getMEClass(cNames[iC]);
            RefContext refContext = (RefContext) m_midContexts.get(Analyser.getContextName(apkCtx));
            if (refContext == null)
            {
                refContext = new RefContext(apkCtx);
                m_midContexts.put(Analyser.getContextName(apkCtx), refContext);
            }
            String refPackName = mClass.getResource().getPackage();
            RefPackage refPack = refContext.registerPackage(refPackName);
            refPack.registerClass(mClass);

            try {
                for (MEClass parent : InvSnooper.findClassParents(mClass)) {
                    MEClass p = parent;
                    Map<String, Reference> contextTree = m_midContexts;
                    if (p instanceof UnknownClass) {
                        String pname = parent.getName();
                        contextTree = m_refContexts;
                        try {
                            p = m_sctx.getMEClass(pname);
                        } catch (ClassNotFoundException e) {
                            if (m_uCtx == null)
                            {
                                m_uCtx = new UnknownContext();
                            }
                            try
                            {
                                p = m_uCtx.getMEClass(pname);
                            } catch (ClassNotFoundException cnfe)
                            {
                                UnknownResource uRes = new UnknownResource(pname, m_uCtx);
                                p = new UnknownClass(pname, uRes);
                                m_uCtx.defineClass((UnknownClass) p);
                            }
                        }
                    }
                    MEClassContext ctx = p.getResource().getContext();
                    RefContext rContext = (RefContext) contextTree.get(Analyser.getContextName(ctx));
                    if (rContext == null)
                    {
                        rContext = new RefContext(ctx);
                        contextTree.put(Analyser.getContextName(ctx), rContext);
                    }
                    String packname = p.getResource().getPackage();
                    RefPackage refPackage = rContext.registerPackage(packname);
                    refPackage.registerClass(p);
                }
            } catch (Throwable e1) {
                e1.printStackTrace();
            }

            MEMethod[] mMethods = mClass.getMethods();
            if (mMethods == null) {
                continue;
            }
            // Go through all methods
            for (int iM = 0; c.isRunning() && iM < mMethods.length; iM++)
            {
                boolean methFail = false;
                boolean methNotFound = false;
                MEMethod mMethod = mMethods[iM];
                if (m_listener != null)
                {
                    m_listener.resolving(apkIndex, apkCount, iC, cNames.length, iM, mMethods.length,
                            apkCtx, mClass, mMethod);
                }
                List<MEMethod.Invokation> invokations = null; // Key string(invokation), value integer(nbrOfInvokations)
                Iterator<MEMethod.Invokation> iI = null;
                try
                {
                    invokations = mMethod.getInvokations();
                    iI = invokations.iterator();
                } catch (CorruptBytecodeException cbe)
                {
                    methFail = true;
                }
                if (!methFail && (iI == null || !iI.hasNext()))
                {
                    // No invokations in this method
                    buildEmptyRef(apkCtx, mClass, mMethod, objs, refs);
                }
                // Go through all invokations in method
                while (c.isRunning() && iI != null && iI.hasNext())
                {
                    MEMethod.Invokation inv = iI.next();
                    boolean found = false;
                    MEClass rClassFound = null;
                    MEClass localClassFound = null;

                    // Try getting invoked class from midlet jar
                    try
                    {
                        MEClass rLocalClass = apkCtx.getMEClass(inv.invClassname);
                        MEMethod rLocalMethod = rLocalClass.getMethod(inv.invMethodname, inv.invDescriptor);
                        if (rLocalMethod != null)
                        {
                            buildRef(apkCtx, mClass, mMethod,
                                    methFail, methNotFound,
                                    null, rLocalClass, rLocalMethod, inv, objs, refs);
                            found = true;
                        } else {
                            localClassFound = rLocalClass;
                        }
                    } catch (ClassNotFoundException cnfe) {
                    }

                    if (found) {
                        continue;
                    }

                    // Could not get invoked class from midlet jar, try api classes
                    try
                    {
                        MEClass rClass = m_sctx.getMEClass(inv.invClassname);
                        MEMethod rMethod = rClass.getMethod(inv.invMethodname, inv.invDescriptor);
                        if (rMethod != null)
                        {
                            MEClassResource rRsc = rClass.getResource();
                            MEClassContext rCtx = rRsc.getContext();
                            buildRef(apkCtx, mClass, mMethod,
                                    methFail, methNotFound,
                                    rCtx, rClass, rMethod, inv, objs, refs);
                            found = true;
                        } else {
                            rClassFound = rClass;
                        }
                    } catch (ClassNotFoundException e) {
                    }

                    if (found) {
                        continue;
                    }

                    if (localClassFound != null)
                    {
                        String unknownSuperClassName = localClassFound.getUnknownSuperClassName();
                        if (unknownSuperClassName != null) {
                            try
                            {
                                MEClass rClass = m_sctx.getMEClass(unknownSuperClassName);
                                MEMethod rMethod = rClass.getMethod(inv.invMethodname, inv.invDescriptor);
                                if (rMethod != null)
                                {
                                    MEClassResource rRsc = rClass.getResource();
                                    MEClassContext rCtx = rRsc.getContext();
                                    buildRef(apkCtx, mClass, mMethod,
                                            methFail, methNotFound,
                                            rCtx, rClass, rMethod, inv, objs, refs);
                                    found = true;
                                } else if (rClassFound == null) {
                                    rClassFound = rClass;
                                }
                            } catch (ClassNotFoundException e) {
                            }
                        }
                    }

                    if (found) {
                        continue;
                    }

                    if (localClassFound != null)
                    {
                        methNotFound = true;
                        MEMethod u = new UnknownMethod(inv.invMethodname, inv.invDescriptor, localClassFound);

                        buildRef(apkCtx, mClass, mMethod,
                                methFail, methNotFound,
                                null, localClassFound, u, inv, objs, refs);
                    }
                    else if (rClassFound != null)
                    {
                        methNotFound = true;
                        MEMethod u = new UnknownMethod(inv.invMethodname, inv.invDescriptor, rClassFound);
                        MEClassResource rRsc = rClassFound.getResource();
                        MEClassContext rCtx = rRsc.getContext();
                        buildRef(apkCtx, mClass, mMethod,
                                methFail, methNotFound,
                                rCtx, rClassFound, u, inv, objs, refs);
                    }
                    else
                    {
                        // Class not found in api classes, make an unknown resource reference
                        methNotFound = true;
                        if (m_uCtx == null)
                        {
                            m_uCtx = new UnknownContext();
                        }
                        UnknownClass uClass = null;
                        try
                        {
                            uClass = (UnknownClass) m_uCtx.getMEClass(inv.invClassname);
                        } catch (ClassNotFoundException cnfe)
                        {
                            UnknownResource uRes = new UnknownResource(inv.invClassname, m_uCtx);
                            uClass = new UnknownClass(inv.invClassname, uRes);
                            m_uCtx.defineClass(uClass);
                        }
                        UnknownMethod uMethod = null;
                        uMethod = (UnknownMethod) uClass.getMethod(inv.invMethodname, inv.invDescriptor);
                        if (uMethod == null)
                        {
                            uMethod = new UnknownMethod(inv.invMethodname, inv.invDescriptor, uClass);
                        }

                        buildRef(apkCtx, mClass, mMethod,
                                methFail, methNotFound,
                                m_uCtx, uClass, uMethod, inv, objs, refs);
                    } // unknown class
                } // per invokation
            }//per method
        }// per class

        //build field ref
        for (int iC = 0; c.isRunning() && iC < cNames.length; iC++)
        {
            MEClass mClass = apkCtx.getMEClass(cNames[iC]);
            MEField[] mFields = mClass.getFields();
            if (mFields != null) {
                for (int iF = 0; c.isRunning() && iF < mFields.length; iF++)
                {
                    if (m_listener != null)
                    {
                        m_listener.resolvingField(apkIndex, apkCount, iC, cNames.length, iF, mFields.length,
                                apkCtx, mClass, mFields[iF]);
                    }
                    buildField(m_midContexts, apkCtx, mClass, mFields[iF], false);
                }
            }
        }

        ArrayList<DexReferenceCache.FieldAccess> accesses = apkCtx.getDexReferenceCache().getFieldAccesses();
        for (int iAcc = 0; c.isRunning() && iAcc < accesses.size(); iAcc++)
        {
            DexReferenceCache.FieldAccess access = accesses.get(iAcc);

            if (m_listener != null)
            {
                m_listener.resolvingFieldAccess(apkIndex, apkCount, iAcc, accesses.size(), apkCtx, access);
            }

            FieldIdItem item = access.fieldIdItem;
            String className = Util.getClassName(item.getContainingClass().getTypeDescriptor());
            String fieldType = item.getFieldType().getTypeDescriptor();
            String fieldName = item.getFieldName().getStringValue();
            boolean found = false;
            MEClass rClassFound = null;
            MEClass localClassFound = null;

            try
            {
                MEClass clazz = apkCtx.getMEClass(className);
                MEField field = clazz.getField(fieldName, fieldType);

                if (field != null) {
                    buildFieldRef(m_midContexts, apkCtx, clazz, field, access, false, objs, refs);
                    found = true;
                } else {
                    localClassFound = clazz;
                }
            } catch (ClassNotFoundException cnfe) {
            }

            if (found) {
                continue;
            }

            try
            {
                MEClass rClass = m_sctx.getMEClass(className);
                MEField rField = rClass.getField(fieldName, fieldType);

                if (rField != null)
                {
                    MEClassResource rRsc = rClass.getResource();
                    MEClassContext rCtx = rRsc.getContext();
                    buildFieldRef(m_refContexts, rCtx, rClass, rField, access, false, objs, refs);
                    found = true;
                } else {
                    rClassFound = rClass;
                }
            } catch (ClassNotFoundException e) {
            }

            if (found) {
                continue;
            }

            if (localClassFound != null)
            {
                String unknownSuperClassName = localClassFound.getUnknownSuperClassName();
                if (unknownSuperClassName != null) {
                    try {
                        MEClass rClass = m_sctx.getMEClass(unknownSuperClassName);
                        MEField rField = rClass.getField(fieldName, fieldType);

                        if (rField != null)
                        {
                            MEClassResource rRsc = rClass.getResource();
                            MEClassContext rCtx = rRsc.getContext();
                            buildFieldRef(m_refContexts, rCtx, rClass, rField, access, false, objs, refs);
                            found = true;
                        } else if (rClassFound == null) {
                            rClassFound = rClass;
                        }
                    } catch (ClassNotFoundException e) {
                    }
                }
            }

            if (found) {
                continue;
            }

            if (localClassFound != null)
            {
                MEField u = new UnknownField(fieldName, fieldType, localClassFound);
                buildFieldRef(m_midContexts, apkCtx, localClassFound, u, access, true, objs, refs);
            }
            else if (rClassFound != null)
            {
                MEField u = new UnknownField(fieldName, fieldType, rClassFound);
                MEClassResource rRsc = rClassFound.getResource();
                MEClassContext rCtx = rRsc.getContext();
                buildFieldRef(m_refContexts, rCtx, rClassFound, u, access, true, objs, refs);
            }
            else
            {
                // Class not found in api classes, make an unknown resource reference
                if (m_uCtx == null)
                {
                    m_uCtx = new UnknownContext();
                }
                UnknownClass uClass = null;
                try
                {
                    uClass = (UnknownClass) m_uCtx.getMEClass(className);
                } catch (ClassNotFoundException cnfe)
                {
                    UnknownResource uRes = new UnknownResource(className, m_uCtx);
                    uClass = new UnknownClass(className, uRes);
                    m_uCtx.defineClass(uClass);
                }
                UnknownField uField = null;
                uField = (UnknownField) uClass.getField(fieldName, fieldType);
                if (uField == null)
                {
                    uField = new UnknownField(fieldName, fieldType, uClass);
                }

                buildFieldRef(m_refContexts, m_uCtx, uClass, uField, access, true, objs, refs);
            } // unknown class
        }

        if (apkCtx.getXmlParser().getManifest() != null && c.isRunning()) {
            ///xmls
            ArrayList<ZipEntry> xmlentries = apkCtx.getXmlParser().getXmlFiles();
            for (int iXml = 0; c.isRunning() && iXml < xmlentries.size(); iXml++)
            {
                ZipEntry xml = xmlentries.get(iXml);
                if (xml.getName().equals(XmlParser.MANIFEST)) {
                    continue;
                }

                RefContext context = (RefContext) m_midContexts.get(Analyser.getContextName(apkCtx));
                if (context == null)
                {
                    context = new RefContext(apkCtx);
                    m_midContexts.put(Analyser.getContextName(apkCtx), context);
                }
                RefFolder refFolder = context.registerSubFolder("xml", RefFolder.XML);
                RefXml refxml = new RefXml(xml, apkCtx);

                objs.add(xml);
                refs.add(refxml);

                refFolder.registerChild(xml.getName(), refxml);
            }

            //android resources
            Set<ResResSpec> specs = apkCtx.getDexReferenceCache().getSpecs();
            Iterator<ResResSpec> iSpec = specs.iterator();
            int specCnt = 0;

            while (c.isRunning() && iSpec != null && iSpec.hasNext())
            {
                ResResSpec spec = iSpec.next();

                DexResSpec dexSpec = new DexResSpec(spec);
                apkCtx.getDexReferenceCache().putDexSpec(spec, dexSpec);
                buildResSpec(m_midContexts, apkCtx, dexSpec, apkCtx, dexSpec.getType(), true, objs, refs);

                if (m_listener != null)
                {
                    m_listener.resolvingResource(apkIndex, apkCount, specCnt, specs.size() * 2,
                            apkCtx, spec);
                }
                specCnt++;
            }

            iSpec = specs.iterator();
            specCnt = 0;
            while (c.isRunning() && iSpec != null && iSpec.hasNext())
            {
                ResResSpec spec = iSpec.next();

                DexResSpec dexSpec = new DexResSpec(spec,
                        apkCtx.getDexReferenceCache().findResInternalRefenence(spec.getId().id),
                        apkCtx.getXmlParser().findXmlInternalRefenence(spec.getId().id),
                        apkCtx.getDexReferenceCache().findCodeInternalRefenence(spec.getId().id));
                apkCtx.getDexReferenceCache().putDexSpec(spec, dexSpec);
                buildResSpecRef(m_midContexts, apkCtx, dexSpec, apkCtx, dexSpec.getType(), true, objs, refs);

                if (m_listener != null)
                {
                    m_listener.resolvingResource(apkIndex, apkCount, specCnt + specs.size(), specs.size() * 2,
                            apkCtx, spec);
                }
                specCnt++;
            }

            HashSet<Integer> externalids = new HashSet<Integer>();

            externalids.addAll(apkCtx.getDexReferenceCache().listResExternalReference());
            externalids.addAll(apkCtx.getXmlParser().listXmlExternalReference());
            externalids.addAll(apkCtx.getDexReferenceCache().listCodeExternalReference());

            MEClassContext[] contexts = m_sctx.getContexts();

            Iterator<Integer> iExtResId = externalids.iterator();
            while (c.isRunning() && iExtResId.hasNext())
            {
                int id = iExtResId.next();

                if (id == 0) {
                    continue;//special for @null
                }

                HashSet<ResResSpec> specRef = apkCtx.getDexReferenceCache().findResExternalRefenence(id);
                ArrayList<XmlLine> xmlRef = apkCtx.getXmlParser().findXmlExternalRefenence(id);
                ArrayList<LoadConstRes> codeRef = apkCtx.getDexReferenceCache().findCodeExternalRefenence(id);

                String foldername = null;
                boolean found = false;

                for (int iContext = 0; c.isRunning() && iContext < contexts.length; iContext++)
                {
                    MEClassContext context = contexts[iContext];
                    DexReferenceCache cache = context.getDexReferenceCache();
                    if (cache != null) {
                        ResResSpec spec = cache.getSpec(id);
                        if (spec != null) {
                            DexResSpec dexSpec = new DexResSpec(spec,
                                    specRef,
                                    xmlRef,
                                    codeRef);
                            apkCtx.getDexReferenceCache().putDexSpec(spec, dexSpec);
                            buildResSpecRef(m_refContexts, context, dexSpec, apkCtx, dexSpec.getType(), true, objs, refs);
                            found = true;
                            break;
                        } else if (cache.isSamePackage(id)) {
                            foldername = Analyser.getContextName(context);
                        }
                    }
                }
                if (!found
                        && specRef.size() + xmlRef.size() != 0 //ignore code const
                ) {
                    UnknownResSpec spec = new UnknownResSpec(id);
                    DexResSpec dexSpec = new DexResSpec(spec,
                            specRef,
                            xmlRef,
                            codeRef);
                    if (m_uResCtx == null)
                    {
                        m_uResCtx = new UnknownResContext();
                    }
                    buildResSpecRef(m_refContexts, m_uResCtx, dexSpec, apkCtx, foldername == null ? "Unknown Package" : foldername, false, objs, refs);
                }
            }
        }
    }

    protected RefResSpec buildResSpec(Map<String, Reference> treeContext, MEClassContext mMEContext, DexResSpec dexSpec, ApkClassContext rContext, String foldername, boolean addRes, ArrayList<Object> objs, ArrayList<ReverseReference> refs) {
        RefContext midResource = (RefContext) treeContext.get(Analyser.getContextName(mMEContext));
        if (midResource == null)
        {
            midResource = new RefContext(mMEContext);
            treeContext.put(Analyser.getContextName(mMEContext), midResource);
        }

        RefFolder refFolder;
        if (addRes) {
            refFolder = midResource.registerResSubFolder(foldername);
        } else {
            refFolder = midResource.registerSubFolder(foldername, foldername.equals("Unknown Package") ? RefFolder.UNKNOWN : RefFolder.RES);
        }

        RefResSpec refResSpec = new RefResSpec(dexSpec, rContext);
        refResSpec = (RefResSpec) refFolder.registerChild(dexSpec.getName(), refResSpec);

        if (objs != null && refs != null) {
            objs.add(dexSpec.getResSpec());
            refs.add(refResSpec);
        }

        return refResSpec;
    }

    protected void buildResSpecRef(Map<String, Reference> treeContext,
            MEClassContext mMEContext,
            DexResSpec dexSpec,
            ApkClassContext rContext,
            String foldername,
            boolean addRes,
            ArrayList<Object> objs,
            ArrayList<ReverseReference> refs) {

        if (dexSpec.getSumReference() == 0) {
            return;
        }

        RefResSpec refResSpec = buildResSpec(treeContext, mMEContext, dexSpec, rContext, foldername, addRes, null, null); //not record twice

        Iterator<ResResSpec> iSpec = dexSpec.getResReference().iterator();
        while (iSpec.hasNext()) {
            ResResSpec specRef = iSpec.next();
            ReverseReference reverse = refs.get(objs.indexOf(specRef));
            RefResReference refResRef = refResSpec.registerResReference(specRef, RefResReference.RESREF, reverse);
            reverse.addReferredReference(refResRef, treeContext == m_midContexts);
        }

        Iterator<XmlParser.XmlLine> iXmlLine = dexSpec.getXmlReference().iterator();
        while (iXmlLine.hasNext()) {
            XmlLine xmlRef = iXmlLine.next();
            ReverseReference reverse = refs.get(objs.indexOf(xmlRef.entry));
            RefResReference refResRef = refResSpec.registerResReference(xmlRef, RefResReference.XML, reverse);
            reverse.addReferredReference(refResRef, treeContext == m_midContexts);
        }

        Iterator<DexReferenceCache.LoadConstRes> iCode = dexSpec.getCodeReference().iterator();
        while (iCode.hasNext()) {
            DexReferenceCache.LoadConstRes loadConst = iCode.next();
            ReverseReference reverse = refs.get(objs.indexOf(loadConst.method));
            RefResReference refResRef = refResSpec.registerResReference(loadConst, RefResReference.CODE, reverse);
            reverse.addReferredReference(refResRef, treeContext == m_midContexts);
        }
    }

    protected RefField buildField(Map<String, Reference> treeContext, MEClassContext mMEContext, MEClass mMEClass, MEField mMEField, boolean fieldNotFound) {
        int flags = (fieldNotFound && treeContext == m_midContexts) ? Reference.NOTFOUND : 0;

        RefContext resourceContext = (RefContext) treeContext.get(Analyser.getContextName(mMEContext));
        if (resourceContext == null)
        {
            resourceContext = new RefContext(mMEContext);
            treeContext.put(Analyser.getContextName(mMEContext), resourceContext);
        }
        resourceContext.setFlags(resourceContext.getFlags() | flags);
        String midPackName = mMEClass.getResource().getPackage();
        RefPackage midPack = resourceContext.registerPackage(midPackName);
        midPack.setFlags(midPack.getFlags() | flags);
        RefClass midClass = midPack.registerClass(mMEClass);
        midClass.setFlags(midClass.getFlags() | flags);
        RefField refField = midClass.registerField(mMEField);
        refField.setFlags(refField.getFlags() | flags);

        return refField;
    }

    protected void buildFieldRef(Map<String, Reference> treeContext, MEClassContext mMEContext, MEClass mMEClass, MEField mMEField, DexReferenceCache.FieldAccess access, boolean fieldNotFound, ArrayList<Object> objs, ArrayList<ReverseReference> refs) {
        access.setMEField(mMEField, mMEClass);

        int flags = (fieldNotFound && treeContext == m_midContexts) ? Reference.NOTFOUND : 0;

        RefField refField = buildField(treeContext, mMEContext, mMEClass, mMEField, fieldNotFound);
        ReverseReference reverse = refs.get(objs.indexOf(access.method));
        RefFieldAccess refAccess = new RefFieldAccess(access, (RefMethod) reverse);
        refAccess.setFlags(flags);
        reverse.addReferredReference(refAccess, treeContext == m_midContexts);
        refField.registerAccess(refAccess);
    }

    protected void buildEmptyRef(AbstractClassContext mContext, MEClass mClass, MEMethod mMethod, ArrayList<Object> objs, ArrayList<ReverseReference> refs)
    {
        RefContext refContext = (RefContext) m_midContexts.get(Analyser.getContextName(mContext));
        if (refContext == null)
        {
            refContext = new RefContext(mContext);
            m_midContexts.put(Analyser.getContextName(mContext), refContext);
        }
        String refPackName = mClass.getResource().getPackage();
        RefPackage refPack = refContext.registerPackage(refPackName);
        RefClass refClass = refPack.registerClass(mClass);
        RefMethod refMethod = refClass.registerMethod(mMethod);

        if (mMethod instanceof DexMethod
                && objs != null
                && refs != null
                && objs.contains(mMethod) == false) {
            objs.add(mMethod);
            refs.add(refMethod);
        }

        if (mMethod instanceof UnknownMethod)
        {
            refMethod.setFlags(refMethod.getFlags() | Reference.NOTFOUND);
        }
    }

    protected void buildRef(
            AbstractClassContext mMEContext, MEClass mMEClass, MEMethod mMEMethod,
            boolean methFail, boolean methNotFound,
            MEClassContext rMEContext, MEClass rMEClass, MEMethod rMEMethod,
            MEMethod.Invokation invokation,
            ArrayList<Object> objs, ArrayList<ReverseReference> refs)
    {
        invokation.setMethod(rMEMethod, rMEClass);

        int flags = (methFail ? Reference.FAILED : 0) | (methNotFound ? Reference.NOTFOUND : 0);

        RefInvokation refInv = null;
        RefInvokation midInv = null;

        RefMethod refMethod = null;
        RefMethod midMethod = null;

        // Ref side
        if (rMEContext != null)
        {
            String refInvStr = mMEClass.getName() + "." + mMEMethod.getFormattedName() + "(" + mMEMethod.getArgumentsString() + ")";
            RefContext refContext = (RefContext) m_refContexts.get(Analyser.getContextName(rMEContext));
            if (refContext == null)
            {
                refContext = new RefContext(rMEContext);
                m_refContexts.put(Analyser.getContextName(rMEContext), refContext);
            }
            String refPackName = rMEClass.getResource().getPackage();
            RefPackage refPack = refContext.registerPackage(refPackName);
            RefClass refClass = refPack.registerClass(rMEClass);
            refMethod = refClass.registerMethod(rMEMethod);
            if (rMEMethod instanceof UnknownMethod)
            {
                refMethod.setFlags(refMethod.getFlags() | Reference.NOTFOUND);
            }
            refInv =
                    new RefInvokation(refInvStr, refContext, refPack, refClass, refMethod, false, invokation);
        }

        // Midlet side
        if (mMEContext != null)
        {
            String midInvStr = rMEClass.getName() + "." + rMEMethod.getFormattedName() + "(" + rMEMethod.getArgumentsString() + ")";
            RefContext midResource = (RefContext) m_midContexts.get(Analyser.getContextName(mMEContext));
            if (midResource == null)
            {
                midResource = new RefContext(mMEContext);
                m_midContexts.put(Analyser.getContextName(mMEContext), midResource);
            }
            midResource.setFlags(midResource.getFlags() | flags);
            String midPackName = mMEClass.getResource().getPackage();
            RefPackage midPack = midResource.registerPackage(midPackName);
            midPack.setFlags(midPack.getFlags() | flags);
            RefClass midClass = midPack.registerClass(mMEClass);
            midClass.setFlags(midClass.getFlags() | flags);
            midMethod = midClass.registerMethod(mMEMethod);

            if (mMEMethod instanceof DexMethod
                    && objs != null
                    && refs != null
                    && objs.contains(mMEMethod) == false) {
                objs.add(mMEMethod);
                refs.add(midMethod);
            }

            midMethod.setFlags(midMethod.getFlags() | flags);
            midInv =
                    new RefInvokation(midInvStr, midResource, midPack, midClass, midMethod, rMEContext == null, invokation);
            midInv.setFlags(flags);
        }

        // Coupling
        if (refInv != null && midInv != null)
        {
            refInv.setOppositeInvokation(midInv);
            midInv.setOppositeInvokation(refInv);
        }

        if (refMethod != null && refInv != null)
        {
            refMethod.registerInvokation(refInv);
        }
        if (midMethod != null && midInv != null)
        {
            midMethod.registerInvokation(midInv);
        }
    }

    public Collection<Reference> getReferenceResources()
    {
        return m_refContexts.values();
    }

    public Collection<Reference> getMidletResources()
    {
        return m_midContexts.values();
    }
}
