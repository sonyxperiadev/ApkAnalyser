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

package andreflect.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import andreflect.gui.linebuilder.XmlLineFormatter;
import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResID;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.decoder.AXmlResourceParser;

public class XmlParser {
    private ZipFile m_apk;
    private final ArrayList<ZipEntry> m_xmlFiles;

    public static final String XML_SUFFIX = ".xml";
    public static final String MANIFEST = "AndroidManifest.xml";

    private XmlManifest manifest = null;

    private final ResTable mResTable;

    private final HashMap<Integer, ArrayList<XmlLine>> mInternal = new HashMap<Integer, ArrayList<XmlLine>>();
    private final HashMap<Integer, ArrayList<XmlLine>> mExternal = new HashMap<Integer, ArrayList<XmlLine>>();

    public static class XmlLine {
        public ZipEntry entry;
        public int line;
        public int id;

        public XmlLine(ZipEntry entry, int line, int id) {
            this.id = id;
            this.entry = entry;
            this.line = line;
        }

        @Override
        public String toString() {
            return entry.getName() + " line " + line;
        }
    }

    private XmlResourceChecker resouceChecker = null;

    public XmlResourceChecker getResourceChecker() {
        if (resouceChecker == null) {
            resouceChecker = new XmlResourceChecker(this);
        }
        return resouceChecker;
    }

    public ResTable getResTable() {
        return mResTable;
    }

    public ZipEntry visitFile(String filename) {
        for (ZipEntry entry : m_xmlFiles) {
            if (!entry.isDirectory()) {
                if (entry.getName().equals(filename)) {
                    return entry;
                }
            }
        }
        return null;
    }

    public XmlParser(File apk, ResTable resTable) {
        mResTable = resTable;

        m_xmlFiles = new ArrayList<ZipEntry>();

        try {
            m_apk = new ZipFile(apk);
            Enumeration<? extends ZipEntry> e = m_apk.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                if (!entry.isDirectory()) {
                    if (entry.getName().endsWith(XML_SUFFIX)) {
                        m_xmlFiles.add(entry);
                    }
                }
            }
        } catch (IOException e) {
            //ignore because it may not be apk file but a odex file
        }

        for (ZipEntry entry : m_xmlFiles) {
            try {
                InputStream is = m_apk.getInputStream(entry);
                IntReader ir = new IntReader(is, false);
                int c = ir.readInt();
                if (c == 0x6D783F3C) {
                } else {
                    cacheEncodedXmlResourceId(entry);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cacheEncodedXmlResourceId(ZipEntry entry) {
        try {
            AXmlResourceParser parser = new AXmlResourceParser();
            XmlResAttrDecoder attDecoder = new XmlResAttrDecoder(mResTable);
            parser.setAttrDecoder(attDecoder);
            parser.open(m_apk.getInputStream(entry));
            while (true) {
                int type = parser.next();
                if (type == XmlPullParser.END_DOCUMENT) {
                    break;
                }
                switch (type) {
                case XmlPullParser.START_TAG: {
                    for (int i = 0; i != parser.getAttributeCount(); ++i) {
                        int id = parser.getAttributeResourceValue(i, 0);
                        int line = parser.getLineNumber();
                        if (id != 0) {
                            HashMap<Integer, ArrayList<XmlLine>> cache;

                            boolean internal = false;
                            if (mResTable != null) {
                                try {
                                    if (mResTable.getResSpec(id) != null) {
                                        internal = true;
                                    }
                                } catch (AndrolibException e) {
                                }
                            }

                            if (internal) {
                                cache = mInternal;
                            } else {
                                cache = mExternal;
                            }

                            if (cache.get(id) == null) {
                                ArrayList<XmlLine> xmlines = new ArrayList<XmlLine>();
                                cache.put(id, xmlines);
                            }
                            cache.get(id).add(new XmlLine(entry, line, id));

                            //just call att decoder to check if value can be decoded
                            parser.getAttributeValue(i);
                        }
                    }
                }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return;
    }

    public ArrayList<ZipEntry> getXmlFiles() {
        return m_xmlFiles;
    }

    public XmlManifest getManifest() {
        if (manifest == null
                && m_apk.getEntry(MANIFEST) != null) {
            try {
                manifest = parseManifest(m_apk.getInputStream(m_apk.getEntry(MANIFEST)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return manifest;
    }

    private InputStream getInputStream(String name) {
        InputStream ret = null;
        for (int i = 0; i < m_xmlFiles.size(); i++) {
            if (m_xmlFiles.get(i).getName().equals(name)) {
                try {
                    ret = m_apk.getInputStream(m_xmlFiles.get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public ArrayList<XmlLine> findXmlInternalRefenence(int resId) {
        if (mInternal.containsKey(resId)) {
            return mInternal.get(resId);
        } else {
            return new ArrayList<XmlLine>();
        }
    }

    public ArrayList<XmlLine> findXmlExternalRefenence(int resId) {
        if (mExternal.containsKey(resId)) {
            return mExternal.get(resId);
        } else {
            return new ArrayList<XmlLine>();
        }
    }

    public Set<Integer> listXmlExternalReference() {
        return mExternal.keySet();
    }

    public ArrayList<XmlLine> findXmlAndroidSystemReference() {
        ArrayList<XmlLine> result = new ArrayList<XmlLine>();
        Iterator<Integer> i = mExternal.keySet().iterator();
        while (i.hasNext()) {
            int id = i.next();
            if (new ResID(id).package_ == XmlResAttrDecoder.ANDROID_PACKAGE_ID) {
                ArrayList<XmlLine> xmlLines = mExternal.get(id);
                for (XmlLine xmlLine : xmlLines) {
                    result.add(xmlLine);
                }
            }
        }
        return result;
    }

    public XmlLineFormatter getXmlLineBuilder(String name, int line, int id, boolean onlyPackage) {
        XmlLineFormatter xmllb = null;
        InputStream is = getInputStream(name);
        IntReader ir = new IntReader(is, false);
        try {
            int c = ir.readInt();
            if (c == 0x6D783F3C) {
                xmllb = parseRawXMLLineBuilder(getInputStream(name));
            } else {
                xmllb = parseXMLLineBuilder(getInputStream(name), line, id, onlyPackage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return xmllb;
    }

    private XmlLineFormatter parseRawXMLLineBuilder(InputStream inputStream) {
        XmlLineFormatter result = new XmlLineFormatter();
        try {
            MXParser parser = new MXParser();
            parser.setInput(inputStream, "utf-8");
            StringBuilder indent = new StringBuilder(40);
            final String indentStep = "    ";
            String previousTag = null;
            boolean needLF = false;
            while (true) {
                int type = parser.next();
                if (type == XmlPullParser.END_DOCUMENT) {
                    break;
                }
                switch (type) {
                case XmlPullParser.START_DOCUMENT: {
                    result.appendXMLHeader();
                    break;
                }
                case XmlPullParser.START_TAG: {
                    if (previousTag != null) {
                        result.appendLF();
                    }

                    result.appendText(indent.toString());
                    result.appendBeginTagBegin(getNamespacePrefix(parser.getPrefix()), parser.getName());

                    indent.append(indentStep);

                    int namespaceCountBefore = parser.getNamespaceCount(parser.getDepth() - 1);
                    int namespaceCount = parser.getNamespaceCount(parser.getDepth());

                    boolean firstAttrib = false;
                    needLF = (namespaceCount - namespaceCountBefore + parser.getAttributeCount()) > 3;

                    for (int i = namespaceCountBefore; i != namespaceCount; ++i) {
                        if (firstAttrib) {
                            if (needLF) {
                                result.appendLF();
                                result.appendText(indent.toString());
                            } else {
                                result.appendSpace();
                            }
                        }
                        result.appendAttrib("xmlns:");
                        result.appendAttrib(parser.getNamespacePrefix(i));
                        result.appendEQ();
                        result.appendValue(parser.getNamespaceUri(i));
                        firstAttrib = true;
                    }

                    for (int i = 0; i != parser.getAttributeCount(); ++i) {
                        if (firstAttrib) {
                            if (needLF) {
                                result.appendLF();
                                result.appendText(indent.toString());
                            } else {
                                result.appendSpace();
                            }
                        }
                        result.appendAttrib(getNamespacePrefix(parser.getAttributePrefix(i)));
                        result.appendAttrib(parser.getAttributeName(i));
                        result.appendEQ();
                        result.appendValue(parser.getAttributeValue(i));
                        firstAttrib = true;
                    }

                    result.appendBeginTagEnd();
                    previousTag = parser.getName();
                    break;
                }
                case XmlPullParser.END_TAG: {
                    indent.setLength(indent.length() - indentStep.length());
                    if (!parser.getName().equals(previousTag))
                    {
                        if (previousTag != null) {
                            result.appendLF();
                        }
                        result.appendText(indent.toString());
                    } else if (needLF) {
                        result.appendLF();
                        result.appendText(indent.toString());
                        needLF = false;
                    }
                    previousTag = null;
                    result.appendEndTag(getNamespacePrefix(parser.getPrefix()), parser.getName());
                    break;
                }
                case XmlPullParser.TEXT: {
                    result.appendText(parser.getText());
                    break;
                }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        } catch (XmlPullParserException e) {
            result = null;
            e.printStackTrace();
        }
        return result;

    }

    private XmlManifest parseManifest(InputStream inputStream) {
        XmlManifest manifest = new XmlManifest();
        boolean hasMainActivity = false;
        boolean inActivity = false;
        boolean inIntentFilter = false;
        String activityName = null;
        try {
            AXmlResourceParser parser = new AXmlResourceParser();
            XmlResAttrDecoder attDecoder = new XmlResAttrDecoder(mResTable);
            parser.setAttrDecoder(attDecoder);
            parser.open(inputStream);
            while (true) {
                int type = parser.next();
                if (type == XmlPullParser.END_DOCUMENT) {
                    break;
                }
                switch (type) {
                case XmlPullParser.START_TAG: {
                    if (parser.getName().equals("manifest")) {
                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                            if (parser.getAttributeName(i).equals("package")) {
                                manifest.setPackage(parser.getAttributeValue(i));
                            } else if (parser.getAttributeName(i).equals("versionName")
                                    && !parser.getAttributeValue(i).startsWith("@")) {
                                manifest.setVersion(parser.getAttributeValue(i));
                            } else if (parser.getAttributeName(i).equals("versionCode")
                                    && !parser.getAttributeValue(i).startsWith("@")) {
                                manifest.setVersionCode(parser.getAttributeValue(i));
                            }

                        }
                    } else if (parser.getName().equals("activity")) {
                        inActivity = true;
                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                            if (parser.getAttributeName(i).equals("name")) {
                                activityName = parser.getAttributeValue(i);
                            }
                        }
                    } else if (parser.getName().equals("intent-filter")
                            && inActivity) {
                        inIntentFilter = true;
                    } else if (parser.getName().equals("action")
                            && inActivity && inIntentFilter && !hasMainActivity) {
                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                            if (parser.getAttributeName(i).equals("name")
                                    && parser.getAttributeValue(i).equals("android.intent.action.MAIN")) {
                                manifest.setMainActivity(activityName);
                                hasMainActivity = true;
                            }
                        }
                    } else if (parser.getName().equals("category")
                            && inActivity && inIntentFilter && !hasMainActivity) {
                        for (int i = 0; i != parser.getAttributeCount(); ++i) {
                            if (parser.getAttributeName(i).equals("name")
                                    && parser.getAttributeValue(i).equals("android.intent.category.LAUNCHER")) {
                                manifest.setMainActivity(activityName);
                                hasMainActivity = true;
                            }
                        }
                    }
                    break;
                }
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("activity")) {
                        inActivity = false;
                        activityName = null;
                    } else if (parser.getName().equals("intent-filter")) {
                        inIntentFilter = false;
                    }
                case XmlPullParser.TEXT:
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return manifest;
    }

    private XmlLineFormatter parseXMLLineBuilder(InputStream inputStream, int line, int id, boolean onlyPackage) {
        XmlLineFormatter result = new XmlLineFormatter();
        try {
            AXmlResourceParser parser = new AXmlResourceParser();
            XmlResAttrDecoder attDecoder = new XmlResAttrDecoder(mResTable);
            parser.setAttrDecoder(attDecoder);
            parser.open(inputStream);
            StringBuilder indent = new StringBuilder(40);
            final String indentStep = "    ";
            String previousTag = null;
            boolean needLF = false;
            while (true) {
                int type = parser.next();
                if (type == XmlPullParser.END_DOCUMENT) {
                    break;
                }
                switch (type) {
                case XmlPullParser.START_DOCUMENT: {
                    result.appendXMLHeader();
                    break;
                }
                case XmlPullParser.START_TAG: {
                    if (previousTag != null) {
                        result.appendLF();
                    }

                    result.appendText(indent.toString());
                    result.appendBeginTagBegin(getNamespacePrefix(parser.getPrefix()), parser.getName());

                    indent.append(indentStep);

                    int namespaceCountBefore = parser.getNamespaceCount(parser.getDepth() - 1);
                    int namespaceCount = parser.getNamespaceCount(parser.getDepth());

                    boolean firstAttrib = false;
                    needLF = (namespaceCount - namespaceCountBefore + parser.getAttributeCount()) > 3;

                    for (int i = namespaceCountBefore; i != namespaceCount; ++i) {
                        if (firstAttrib) {
                            if (needLF) {
                                result.appendLF();
                                result.appendText(indent.toString());
                            } else {
                                result.appendSpace();
                            }
                        }
                        result.appendAttrib("xmlns:");
                        result.appendAttrib(parser.getNamespacePrefix(i));
                        result.appendEQ();
                        result.appendValue(parser.getNamespaceUri(i));
                        firstAttrib = true;
                    }

                    for (int i = 0; i != parser.getAttributeCount(); ++i) {
                        if (firstAttrib) {
                            if (needLF) {
                                result.appendLF();
                                result.appendText(indent.toString());
                            } else {
                                result.appendSpace();
                            }
                        }
                        result.appendAttrib(getNamespacePrefix(parser.getAttributePrefix(i)));
                        result.appendAttrib(parser.getAttributeName(i));
                        result.appendEQ();
                        result.appendValue(parser.getAttributeValue(i));
                        firstAttrib = true;
                        if (parser.getLineNumber() == line) {
                            int resid = parser.getAttributeResourceValue(i, 0);
                            if (resid != 0) {
                                if (onlyPackage) {
                                    if (new ResID(resid).package_ == new ResID(id).package_) {
                                        result.setCurrentLine();
                                    }
                                } else if (resid == id) {
                                    result.setCurrentLine();
                                }
                            }
                        }
                    }

                    result.appendBeginTagEnd();
                    previousTag = parser.getName();
                    break;
                }
                case XmlPullParser.END_TAG: {
                    indent.setLength(indent.length() - indentStep.length());
                    if (!parser.getName().equals(previousTag))
                    {
                        if (previousTag != null) {
                            result.appendLF();
                        }
                        result.appendText(indent.toString());
                    } else if (needLF) {
                        result.appendLF();
                        result.appendText(indent.toString());
                        needLF = false;
                    }
                    previousTag = null;
                    result.appendEndTag(getNamespacePrefix(parser.getPrefix()), parser.getName());
                    break;
                }
                case XmlPullParser.TEXT: {
                    result.appendText(parser.getText());
                    break;
                }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        } catch (XmlPullParserException e) {
            result = null;
            e.printStackTrace();
        }
        return result;

    }

    // original xml parser without highlighting
    //
    //	private String parseXML(InputStream inputStream){
    //		StringBuilder result=new StringBuilder();
    //		try{
    //			AXmlResourceParser parser=new AXmlResourceParser();
    //			parser.open(inputStream);
    //			StringBuilder indent=new StringBuilder(10);
    //			final String indentStep=" ";
    //			while (true) {
    //				int type=parser.next();
    //				if (type==XmlPullParser.END_DOCUMENT) {
    //					break;
    //				}
    //				switch (type) {
    //				case XmlPullParser.START_DOCUMENT:
    //				{
    //					result.append(String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"));
    //					break;
    //				}
    //				case XmlPullParser.START_TAG:
    //				{
    //					result.append(String.format("%s<%s%s\n",indent,
    //							getNamespacePrefix(parser.getPrefix()),parser.getName()));
    //					indent.append(indentStep);
    //
    //					int namespaceCountBefore=parser.getNamespaceCount(parser.getDepth()-1);
    //					int namespaceCount=parser.getNamespaceCount(parser.getDepth());
    //					for (int i=namespaceCountBefore;i!=namespaceCount;++i) {
    //						result.append(String.format("%sxmlns:%s=\"%s\"\n",
    //								indent,
    //								parser.getNamespacePrefix(i),
    //								parser.getNamespaceUri(i)));
    //					}
    //
    //					for (int i=0;i!=parser.getAttributeCount();++i) {
    //						result.append(String.format("%s%s%s=\"%s\"\n",indent,
    //								getNamespacePrefix(parser.getAttributePrefix(i)),
    //								parser.getAttributeName(i),
    //								getAttributeValue(parser,i)));
    //					}
    //					result.append(String.format("%s>\n",indent));
    //					break;
    //				}
    //				case XmlPullParser.END_TAG:
    //				{
    //					indent.setLength(indent.length()-indentStep.length());
    //					result.append(String.format("%s</%s%s>\n",indent,
    //							getNamespacePrefix(parser.getPrefix()),
    //							parser.getName()));
    //					break;
    //				}
    //				case XmlPullParser.TEXT:
    //				{
    //					result.append(String.format("%s%s\n",indent,parser.getText()));
    //					break;
    //				}
    //				}
    //			}
    //		}
    //		catch (Exception e) {
    //			e.printStackTrace();
    //		}
    //		return result.toString();
    //	}

    private static String getNamespacePrefix(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return "";
        }
        return prefix + ":";
    }

}
