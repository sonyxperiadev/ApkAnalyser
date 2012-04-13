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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import andreflect.xml.XmlParser;
import andreflect.xml.XmlParser.XmlLine;
import brut.androlib.res.data.ResResSpec;

public class DexResSpec {

    private final ResResSpec m_spec;

    private final HashSet<ResResSpec> m_referenceSpec;
    private final ArrayList<XmlParser.XmlLine> m_referenceXml;
    private final ArrayList<DexReferenceCache.LoadConstRes> m_referenceCode;

    public static final int ISSUE_MISSING_RESOURCE = 8;
    public static final int ISSUE_NO_DEFAULT = 4;
    public static final int ISSUE_NO_DPI = 2;
    public static final int ISSUE_MISS_LANGUAGE = 1;
    public static final int NO_ISSUE = 0;

    private int m_issue = NO_ISSUE;

    public DexResSpec(ResResSpec spec) {
        m_spec = spec;
        m_referenceSpec = null;
        m_referenceXml = null;
        m_referenceCode = null;
    }

    public DexResSpec(ResResSpec spec, HashSet<ResResSpec> referenceSpec, ArrayList<XmlLine> referenceXml, ArrayList<DexReferenceCache.LoadConstRes> referenceCode) {
        m_spec = spec;
        m_referenceSpec = referenceSpec;
        m_referenceXml = referenceXml;
        m_referenceCode = referenceCode;
    }

    public static String getIssueName(int filter) {
        switch (filter) {
        case ISSUE_MISSING_RESOURCE:
            return "MISSING RESOURCE";
        case ISSUE_NO_DPI:
            return "NO DPI SPECIFIED";
        case ISSUE_NO_DEFAULT:
            return "NO DEFAULT RESOURCE";
        case ISSUE_MISS_LANGUAGE:
            return "MISSING SOME LANGUAGE TRANSLATION";
        default:
            return null;
        }
    }

    public void setIssue(int issue) {
        m_issue = issue;
    }

    public int getIssue() {
        return m_issue;
    }

    public ResResSpec getResSpec() {
        return m_spec;
    }

    public String getName() {
        return String.format("%08X", m_spec.getId().id);
    }

    public String getType() {
        if (m_spec.getType() == null) {
            return "";
        }
        return m_spec.getType().getName();
    }

    public static String getRefName(ResResSpec spec) {
        return spec.getName() + " : " + spec.getType().getName() + " " + String.format("%08X", spec.getId().id);
    }

    public static String getRefName(XmlParser.XmlLine xmlLine) {
        return xmlLine.toString();
    }

    public static String getRefName(DexReferenceCache.LoadConstRes loadConst) {
        return loadConst.toString();
    }

    public Set<ResResSpec> getResReference() {
        return m_referenceSpec;
    }

    public List<XmlParser.XmlLine> getXmlReference() {
        return m_referenceXml;
    }

    public List<DexReferenceCache.LoadConstRes> getCodeReference() {
        return m_referenceCode;
    }

    public int getSumReference() {
        return getCountXmlReference() + getCountCodeReference() + getCountSpecReference();
    }

    public int getCountSpecReference() {
        return m_referenceSpec.size();
    }

    public int getCountXmlReference() {
        return m_referenceXml.size();
    }

    public int getCountCodeReference() {
        return m_referenceCode.size();
    }
}
