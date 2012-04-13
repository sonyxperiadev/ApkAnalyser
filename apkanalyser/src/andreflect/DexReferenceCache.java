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

package andreflect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import mereflect.MEClass;
import mereflect.MEField;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;

import andreflect.xml.XmlResAttrDecoder;
import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResID;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.data.value.ResArrayValue;
import brut.androlib.res.data.value.ResBagValue;
import brut.androlib.res.data.value.ResEnumAttr;
import brut.androlib.res.data.value.ResFlagsAttr;
import brut.androlib.res.data.value.ResFlagsAttr.FlagItem;
import brut.androlib.res.data.value.ResIntValue;
import brut.androlib.res.data.value.ResPluralsValue;
import brut.androlib.res.data.value.ResReferenceValue;
import brut.androlib.res.data.value.ResScalarValue;
import brut.androlib.res.data.value.ResStyleValue;
import brut.androlib.res.data.value.ResValue;
import brut.androlib.res.decoder.ARSCDecoder;
import brut.util.Duo;

public class DexReferenceCache {
    public static class LoadConst {
        public DexMethod method;
        public Instruction instruction;
        public int pc;

        public LoadConst(DexMethod method, Instruction instruction, int pc) {
            this.method = method;
            this.pc = pc;
            this.instruction = instruction;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getMEClass().getName() + ".");
            sb.append(method.getFormattedName());
            sb.append("(" + method.getArgumentsString() + ")");
            sb.append(" @ " + Integer.toHexString(instruction.codeAddress));

            if (instruction.line != -1) {
                sb.append(" (line " + instruction.line + " )");
            }

            return sb.toString();
        }
    }

    public static class LoadConstRes extends LoadConst {
        public int resId;

        public LoadConstRes(int resId, DexMethod method, Instruction instruction, int pc) {
            super(method, instruction, pc);
            this.resId = resId;
        }
    }

    public static class LoadConstString extends LoadConst {
        public String string;

        public LoadConstString(String string, DexMethod method, Instruction instruction, int pc) {
            super(method, instruction, pc);
            this.string = string;
        }
    }

    public static class FieldAccess {
        public FieldIdItem fieldIdItem;
        public DexMethod method;
        public Instruction instruction;
        public int pc;
        public boolean isRead;

        public MEField field;
        public MEClass clazz;

        public FieldAccess(FieldIdItem fieldIdItem, DexMethod method, Instruction instruction, int pc, boolean isRead) {
            this.fieldIdItem = fieldIdItem;
            this.method = method;
            this.pc = pc;
            this.instruction = instruction;
            this.isRead = isRead;
            field = null;
            clazz = null;
        }

        public void setMEField(MEField field, MEClass clazz) {
            this.field = field;
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getMEClass().getName() + ".");
            sb.append(method.getFormattedName());
            sb.append("(" + method.getArgumentsString() + ")");
            sb.append(" @ " + Integer.toHexString(instruction.codeAddress));

            if (instruction.line != -1) {
                sb.append(" (line " + instruction.line + " )");
            }
            return sb.toString();
        }
    }

    public static final String RESOURCE = "resources.arsc";

    private ResTable m_resTable = null;

    private final boolean m_isMidlet;
    protected File m_file;

    private final HashMap<ResResSpec, DexResSpec> m_specMap = new HashMap<ResResSpec, DexResSpec>();

    private final HashMap<Integer, HashSet<ResResSpec>> m_internalSpecsRef = new HashMap<Integer, HashSet<ResResSpec>>();
    private final HashMap<Integer, HashSet<ResResSpec>> m_externalSpecsRef = new HashMap<Integer, HashSet<ResResSpec>>();

    private final HashMap<Integer, ArrayList<LoadConstRes>> m_internalCodeRef = new HashMap<Integer, ArrayList<LoadConstRes>>();
    private final HashMap<Integer, ArrayList<LoadConstRes>> m_externalCodeRef = new HashMap<Integer, ArrayList<LoadConstRes>>();

    private final HashMap<String, ArrayList<LoadConstString>> m_constStringRef = new HashMap<String, ArrayList<LoadConstString>>();

    private final ArrayList<FieldAccess> m_fieldAccessRef = new ArrayList<FieldAccess>();

    public DexReferenceCache(File file, boolean isMidlet) {
        m_file = file;
        m_isMidlet = isMidlet;
        initResourceTable();
        if (m_resTable != null && m_isMidlet) {
            checkBagReference();
        }
    }

    public ResTable getResTable() {
        return m_resTable;
    }

    public Set<ResResSpec> getSpecs() {
        return m_specMap.keySet();
    }

    public boolean hasSpec() {
        return m_specMap.keySet().size() != 0;
    }

    public DexResSpec getDexSpec(ResResSpec spec) {
        return m_specMap.get(spec);
    }

    public void putDexSpec(ResResSpec spec, DexResSpec dexSpec) {
        m_specMap.put(spec, dexSpec);
    }

    public boolean isSamePackage(int id) {
        if (m_resTable == null) {
            return false;
        }
        return m_resTable.hasPackage(new ResID(id).package_);
    }

    public ResResSpec getSpec(int id) {
        if (m_resTable == null) {
            return null;
        }

        try {
            return m_resTable.getResSpec(id);
        } catch (AndrolibException e) {
        }
        return null;
    }

    // below only for apk
    public HashSet<ResResSpec> findResInternalRefenence(int resId) {
        if (m_internalSpecsRef.containsKey(resId)) {
            return m_internalSpecsRef.get(resId);
        } else {
            return new HashSet<ResResSpec>();
        }
    }

    public HashSet<ResResSpec> findResExternalRefenence(int resId) {
        if (m_externalSpecsRef.containsKey(resId)) {
            return m_externalSpecsRef.get(resId);
        } else {
            return new HashSet<ResResSpec>();
        }
    }

    public Set<Integer> listResExternalReference() {
        return m_externalSpecsRef.keySet();
    }

    public HashSet<ResResSpec> findResAndroidSystemReference() {
        HashSet<ResResSpec> result = new HashSet<ResResSpec>();
        Iterator<Integer> i = m_externalSpecsRef.keySet().iterator();
        while (i.hasNext()) {
            int id = i.next();
            if (new ResID(id).package_ == XmlResAttrDecoder.ANDROID_PACKAGE_ID) {
                HashSet<ResResSpec> specs = m_externalSpecsRef.get(id);
                for (ResResSpec spec : specs) {
                    result.add(spec);
                }
            }
        }
        return result;
    }

    public ArrayList<LoadConstRes> findCodeInternalRefenence(int resId) {
        if (m_internalCodeRef.containsKey(resId)) {
            return m_internalCodeRef.get(resId);
        } else {
            return new ArrayList<LoadConstRes>();
        }
    }

    public ArrayList<LoadConstRes> findCodeExternalRefenence(int resId) {
        if (m_externalCodeRef.containsKey(resId)) {
            return m_externalCodeRef.get(resId);
        } else {
            return new ArrayList<LoadConstRes>();
        }
    }

    public Set<Integer> listCodeExternalReference() {
        return m_externalCodeRef.keySet();
    }

    public ArrayList<LoadConstRes> findCodeAndroidSystemReference() {
        ArrayList<LoadConstRes> result = new ArrayList<LoadConstRes>();
        Iterator<Integer> i = m_externalCodeRef.keySet().iterator();
        while (i.hasNext()) {
            int id = i.next();
            if (new ResID(id).package_ == XmlResAttrDecoder.ANDROID_PACKAGE_ID) {
                ArrayList<LoadConstRes> loadConsts = m_externalCodeRef.get(id);
                for (LoadConstRes loadConst : loadConsts) {
                    result.add(loadConst);
                }
            }
        }
        return result;
    }

    public ArrayList<LoadConstString> findConstString(String value) {
        if (m_constStringRef.containsKey(value)) {
            return m_constStringRef.get(value);
        } else {
            return new ArrayList<LoadConstString>();
        }
    }

    public Set<String> listConstString() {
        return m_constStringRef.keySet();
    }

    public void addCodeConstString(LoadConstString loadConstString) {
        if (!m_isMidlet) {
            return;
        }
        if (m_constStringRef.get(loadConstString.string) == null) {
            ArrayList<LoadConstString> load = new ArrayList<LoadConstString>();
            m_constStringRef.put(loadConstString.string, load);
        }
        m_constStringRef.get(loadConstString.string).add(loadConstString);
    }

    public void addCodeReference(LoadConstRes loadConst) {
        if (!m_isMidlet) {
            return;
        }

        ResID resId = new ResID(loadConst.resId);
        HashMap<Integer, ArrayList<LoadConstRes>> cache;
        if (getSpec(resId.id) != null) {
            cache = m_internalCodeRef;
        } else {
            cache = m_externalCodeRef;
        }

        if (cache.get(loadConst.resId) == null) {
            ArrayList<LoadConstRes> specs = new ArrayList<LoadConstRes>();
            cache.put(loadConst.resId, specs);
        }
        cache.get(loadConst.resId).add(loadConst);
    }

    public ArrayList<FieldAccess> getFieldAccesses() {
        return m_fieldAccessRef;
    }

    public void addFieldAccessReference(FieldAccess fieldAccess) {
        m_fieldAccessRef.add(fieldAccess);
    }

    public List<FieldAccess> findFieldAccesses(String name, String descriptor, String clazz) {
        ArrayList<FieldAccess> result = new ArrayList<FieldAccess>();
        for (FieldAccess access : m_fieldAccessRef) {
            if (access.fieldIdItem.getFieldName().getStringValue().equals(name)
                    && access.fieldIdItem.getFieldType().getTypeDescriptor().equals(descriptor)
                    && Util.getClassName(access.fieldIdItem.getContainingClass().getTypeDescriptor()).equals(clazz)) {
                result.add(access);
            }
        }
        return result;
    }

    private void initResourceTable() {
        InputStream is = getResourceInputStream();
        if (is != null)
        {
            try {
                ResTable resTable = new ResTable(null);
                ResPackage[] pkgs = ARSCDecoder.decode(is, false, true, resTable).getPackages();
                if (pkgs.length != 1) {
                    System.out.println("System resource package (package length > 1) from " + m_file.getName());
                    for (int i = 0; i < pkgs.length; i++) {
                        System.out.println(" pkg[" + i + "] = " + pkgs[i].getName() + " (" + pkgs[i].getId() + ")");
                    }
                }

                m_resTable = resTable;

                for (ResPackage pkg : pkgs) {
                    resTable.addPackage(pkg, true);
                }

                for (ResPackage pkg : m_resTable.listMainPackages()) {
                    for (ResResSpec spec : pkg.listResSpecs()) {
                        putDexSpec(spec, null);
                    }
                }
            } catch (AndrolibException e) {
                e.printStackTrace();
            }
        }
    }

    private InputStream getResourceInputStream() {
        try {
            ZipFile zipFile = new ZipFile(m_file);
            ZipEntry zipEntry = zipFile.getEntry(RESOURCE);
            if (zipEntry != null) {
                return zipFile.getInputStream(zipEntry);
            }
        } catch (ZipException e) {
            //ignore because it may not be apk file but a odex file
        } catch (IOException e) {
            //ignore because it may not be apk file but a odex file
        }
        return null;
    }

    private void addReference(ResResSpec resSpec, ResValue resValue) {
        if (resValue != null
                && resValue instanceof ResReferenceValue) {
            int value = ((ResReferenceValue) resValue).getValue();
            ResID resId = new ResID(value);
            HashMap<Integer, HashSet<ResResSpec>> cache;
            if (getSpec(resId.id) != null) {
                cache = m_internalSpecsRef;
            } else {
                cache = m_externalSpecsRef;
            }

            if (cache.get(value) == null) {
                HashSet<ResResSpec> specs = new HashSet<ResResSpec>();
                cache.put(value, specs);
            }
            cache.get(value).add(resSpec);
        }
    }

    private void checkBagReference() {
        for (ResPackage pkg : m_resTable.listMainPackages()) {
            for (ResResSpec spec : pkg.listResSpecs()) {
                for (ResResource res : spec.listResources()) {
                    ResValue resValue = res.getValue();
                    if (resValue == null) {
                        continue;
                    } else if (resValue instanceof ResReferenceValue) {
                        addReference(spec, resValue);
                    } else if (resValue instanceof ResArrayValue) {
                        for (ResScalarValue resScalarValue : ((ResArrayValue) resValue).mItems) {
                            addReference(spec, resScalarValue);
                        }
                    } else if (resValue instanceof ResPluralsValue) {
                        for (int i = 0; i < ((ResPluralsValue) resValue).mItems.length; i++) {
                            ResScalarValue item = ((ResPluralsValue) resValue).mItems[i];
                            if (item == null) {
                                continue;
                            }
                            addReference(spec, item);
                        }
                    } else if (resValue instanceof ResStyleValue) {
                        for (Duo<ResReferenceValue, ResScalarValue> duoValue : ((ResStyleValue) resValue).mItems) {
                            addReference(spec, duoValue.m1);
                            addReference(spec, duoValue.m2);
                        }
                    } else if (resValue instanceof ResEnumAttr) {
                        for (Duo<ResReferenceValue, ResIntValue> duoValue : ((ResEnumAttr) resValue).mItems) {
                            addReference(spec, duoValue.m1);
                            addReference(spec, duoValue.m2);
                        }
                    } else if (resValue instanceof ResFlagsAttr) {
                        int length = ((ResFlagsAttr) resValue).mItems.length;
                        for (int i = 0; i < length; i++) {
                            FlagItem item = ((ResFlagsAttr) resValue).mItems[i];
                            addReference(spec, item.ref);
                        }
                    }

                    if (resValue instanceof ResBagValue) {
                        addReference(spec, ((ResBagValue) resValue).getParent());
                    }
                }
            }
        }
    }

}
