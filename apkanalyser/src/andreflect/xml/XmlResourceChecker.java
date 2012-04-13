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

import gui.actions.AbstractCanceableAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.actions.ShowBytecodeAction;
import analyser.logic.RefClass;
import analyser.logic.RefContext;
import analyser.logic.RefPackage;
import analyser.logic.Reference;
import andreflect.ApkClassContext;
import andreflect.DexClass;
import andreflect.DexField;
import andreflect.DexReferenceCache;
import andreflect.DexReferenceCache.LoadConstRes;
import andreflect.DexReferenceCache.LoadConstString;
import andreflect.DexResSpec;
import andreflect.gui.action.XmlFindLabelAction;
import andreflect.gui.action.XmlViewReferenceAction;
import andreflect.gui.action.XmlViewerAction;
import andreflect.xml.XmlParser.XmlLine;
import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResConfig;
import brut.androlib.res.data.ResConfigFlags;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.data.value.ResArrayValue;
import brut.androlib.res.data.value.ResAttr;
import brut.androlib.res.data.value.ResBagValue;
import brut.androlib.res.data.value.ResBoolValue;
import brut.androlib.res.data.value.ResColorValue;
import brut.androlib.res.data.value.ResDimenValue;
import brut.androlib.res.data.value.ResEnumAttr;
import brut.androlib.res.data.value.ResFileValue;
import brut.androlib.res.data.value.ResFlagsAttr;
import brut.androlib.res.data.value.ResFlagsAttr.FlagItem;
import brut.androlib.res.data.value.ResFloatValue;
import brut.androlib.res.data.value.ResFractionValue;
import brut.androlib.res.data.value.ResIntValue;
import brut.androlib.res.data.value.ResPluralsValue;
import brut.androlib.res.data.value.ResReferenceValue;
import brut.androlib.res.data.value.ResScalarValue;
import brut.androlib.res.data.value.ResStringValue;
import brut.androlib.res.data.value.ResStyleValue;
import brut.androlib.res.data.value.ResValue;
import brut.util.Duo;

public class XmlResourceChecker {
    public static final int COLOR_SYMBOL = 0x00B00000;
    public static final int COLOR_KEYWORD = 0x880088;
    public static final int COLOR_TEXT = 0x000000;
    public static final int COLOR_HEX = 0x008800;
    public static final int COLOR_LABEL = 0x000088;
    public static final int COLOR_COMMENT = 0x888800;
    public static final int COLOR_ERROR = 0xff0000;

    private ResTable mResTable = null;
    private HashSet<String> mLanguageSet = null;
    private final XmlParser mParser;

    public XmlResourceChecker(XmlParser parser) {
        mResTable = parser.getResTable();
        mParser = parser;
    }

    public void showFindLabel(String label, ApkClassContext ctx, MainFrame mainFrame,
            XmlFindLabelAction action) {
        Pattern pattern = Pattern.compile(label, Pattern.CASE_INSENSITIVE);
        HashSet<ResResSpec> resSpecs = new HashSet<ResResSpec>();
        for (ResPackage pkg : mResTable.listMainPackages()) {
            for (ResResSpec spec : pkg.listResSpecs()) {
                for (ResResource res : spec.listResources()) {
                    if (res.getValue() instanceof ResStringValue) {
                        try {
                            String value = ((ResScalarValue) res.getValue()).encodeAsResXmlValue();
                            Matcher matcher = pattern.matcher(value);
                            if (matcher.find()) {
                                resSpecs.add(spec);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

        ArrayList<LoadConstString> codeString = new ArrayList<LoadConstString>();
        DexReferenceCache cache = ctx.getDexReferenceCache();
        Set<String> strings = cache.listConstString();
        for (String value : strings) {
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                for (LoadConstString cons : cache.findConstString(value)) {
                    codeString.add(cons);
                }
            }
        }

        int sum = resSpecs.size() + codeString.size();
        if (sum != 0) {
            LineBuilder lb = new LineBuilder();
            lb.newLine();
            Iterator<ResResSpec> i = resSpecs.iterator();
            int cnt = 0;
            while (i.hasNext()) {
                mainFrame.actionReportWork(action, 100 * cnt++ / sum);
                appendSpec(lb, i.next(), mainFrame, ctx, 0, 0, 0);
            }
            appendCodeReference(codeString, ctx, lb, mainFrame);
            mainFrame.showText("Label found for " + label, lb);
            mainFrame.setBottomInfo(sum + " label(s) found");
        } else {
            mainFrame.setBottomInfo("No label found");
        }

    }

    public void showSystemReference(ApkClassContext ctx, MainFrame mainFrame, AbstractCanceableAction action) {
        DexReferenceCache cache = ctx.getDexReferenceCache();
        HashSet<ResResSpec> specs = cache.findResAndroidSystemReference();
        ArrayList<XmlLine> xmllines = ctx.getXmlParser().findXmlAndroidSystemReference();
        ArrayList<LoadConstRes> loadconsts = cache.findCodeAndroidSystemReference();
        int sum = specs.size() + xmllines.size() + loadconsts.size();
        if (sum != 0) {
            LineBuilder lb = new LineBuilder();
            appendBagReference(specs, ctx, lb, mainFrame);
            appendXmlReference(xmllines, ctx, lb, mainFrame);
            appendCodeReference(loadconsts, ctx, lb, mainFrame);
            mainFrame.showText("Android resource references: ", lb);
            mainFrame.setBottomInfo(sum + " resource(es) found");
        } else {
            mainFrame.setBottomInfo("No android package reference found");
        }
    }

    public void showUnusedSpec(ApkClassContext ctx, MainFrame mainFrame, AbstractCanceableAction action) {
        //long starttime = System.currentTimeMillis();
        DexReferenceCache cache = ctx.getDexReferenceCache();
        ArrayList<ResResSpec> resSpecs = new ArrayList<ResResSpec>();
        ArrayList<ResResSpec> usedSpecs = new ArrayList<ResResSpec>();
        ArrayList<ResResSpec> unusedSpecs = new ArrayList<ResResSpec>();
        ArrayList<Integer> usedRef = new ArrayList<Integer>();

        for (ResPackage pkg : mResTable.listMainPackages()) {
            for (ResResSpec spec : pkg.listResSpecs()) {
                resSpecs.add(spec);
            }
        }

        if (resSpecs != null && resSpecs.size() > 0) {
            Comparator<ResResSpec> comp = new SpecComparator();
            Collections.sort(resSpecs, comp);

            for (int i = 0; i < resSpecs.size(); i++) {
                DexResSpec dexSpec = cache.getDexSpec(resSpecs.get(i));
                if (dexSpec.getSumReference() == 0) {
                    unusedSpecs.add(resSpecs.get(i));
                } else {
                    usedSpecs.add(resSpecs.get(i));
                    usedRef.add(dexSpec.getCountSpecReference());
                    usedRef.add(dexSpec.getCountXmlReference());
                    usedRef.add(dexSpec.getCountCodeReference());
                }
                mainFrame.actionReportWork(action, 100 * i / resSpecs.size());
            }

            LineBuilder lb = new LineBuilder();
            boolean first = true;
            for (ResResSpec unusedSpec : unusedSpecs) {
                if (first) {
                    lb.newLine();
                    lb.append("UNUSED RESOURCES: ", COLOR_ERROR);
                    lb.newLine();
                    first = false;
                }

                /*
                lb.append(String.format("echo %s %s %s >> 1234.log",unusedSpec.getName(), unusedSpec.getType().getName() ,String.format("%08X",unusedSpec.getId().id)), COLOR_COMMENT);
                lb.newLine();
                lb.append(String.format("find contacts-largeui -type f -exec grep \"%s\" -H {} \\; >> 1234.log",unusedSpec.getName()), COLOR_COMMENT);
                lb.newLine();
                lb.append(String.format("echo \"\" >> 1234.log",unusedSpec.getName()), COLOR_COMMENT);
                lb.newLine();
                 */

                appendSpec(lb, unusedSpec, mainFrame, ctx, 0, 0, 0);
            }
            if (first != true) {
                lb.newLine();
            }

            first = true;
            for (int i = 0; i < usedSpecs.size(); i++) {
                if (first) {
                    lb.newLine();
                    lb.append("USED RESOURCES: ", COLOR_ERROR);
                    lb.newLine();
                    first = false;
                }
                appendSpec(lb, usedSpecs.get(i), mainFrame, ctx, usedRef.get(i * 3), usedRef.get(i * 3 + 1), usedRef.get(i * 3 + 2));
            }
            mainFrame.showText("Resource unused IDs: ", lb);
            mainFrame.setBottomInfo(resSpecs.size() + " unused resource(es) found");
        } else {
            mainFrame.setBottomInfo("No resource found");
        }
    }

    public void showUnusedFiles(ApkClassContext ctx, RefContext cRef, MainFrame mainFrame, AbstractCanceableAction action) {
        DexReferenceCache cache = ctx.getDexReferenceCache();
        ArrayList<ResResSpec> resSpecs = new ArrayList<ResResSpec>();
        ArrayList<ResResSpec> usedSpecs = new ArrayList<ResResSpec>();
        ArrayList<ResResSpec> unusedSpecs = new ArrayList<ResResSpec>();
        ArrayList<ResResSpec> resultSpecs = new ArrayList<ResResSpec>();
        HashMap<ResResSpec, ArrayList<String>> specFileMap = new HashMap<ResResSpec, ArrayList<String>>();

        for (ResPackage pkg : mResTable.listMainPackages()) {
            for (ResResSpec spec : pkg.listResSpecs()) {
                ArrayList<String> fileNames = new ArrayList<String>();
                for (ResResource res : spec.listResources()) {
                    if (res.getValue() instanceof ResFileValue) {
                        fileNames.add(((ResFileValue) res.getValue()).getPath());
                    }
                }
                if (fileNames.size() != 0) {
                    resSpecs.add(spec);
                    specFileMap.put(spec, fileNames);
                }
            }
        }
        for (int i = 0; i < resSpecs.size(); i++) {
            if (cache.getDexSpec(resSpecs.get(i)).getSumReference() == 0) {
                unusedSpecs.add(resSpecs.get(i));
            } else {
                usedSpecs.add(resSpecs.get(i));
            }
            mainFrame.actionReportWork(action, 80 * i / resSpecs.size());
        }
        if (unusedSpecs.size() != 0) {
            for (ResResSpec unusedSpec : unusedSpecs) {
                boolean foundInUsed = false;
                for (String unusedFile : specFileMap.get(unusedSpec)) {
                    for (ResResSpec usedSpec : usedSpecs) {
                        for (String usedFile : specFileMap.get(usedSpec)) {
                            if (usedFile.equals(unusedFile)) {
                                foundInUsed = true;
                                break;
                            }
                        }
                        if (foundInUsed == true) {
                            break;
                        }
                    }
                    if (foundInUsed == true) {
                        break;
                    }
                }
                if (foundInUsed == false) {
                    resultSpecs.add(unusedSpec);
                }
            }
        }

        mainFrame.actionReportWork(action, 90);

        if (resultSpecs.size() != 0) {
            LineBuilder lb = new LineBuilder();
            lb.newLine();
            long bytes = 0;
            for (int i = 0; i < resultSpecs.size(); i++) {
                ResResSpec resultSpec = resultSpecs.get(i);
                for (String resultFileName : specFileMap.get(resultSpec)) {
                    bytes += appendUnusedFileSpec(lb, resultSpec, resultFileName, mainFrame, cRef);
                }

                mainFrame.actionReportWork(action, 10 * i / resultSpecs.size() + 90);
            }
            mainFrame.showText("Unused resource files (total " + bytes + " bytes) :", lb);
            mainFrame.setBottomInfo(resSpecs.size() + " unused resources file(s) found");
        } else {
            mainFrame.setBottomInfo("No unused resource files found");
        }
    }

    private long appendUnusedFileSpec(LineBuilder lb, ResResSpec resSpec, String fileName, MainFrame mainFrame, RefContext cRef) {
        long size = 0;
        ZipEntry entry = mParser.visitFile(fileName);
        if (entry != null) {
            size = entry.getSize();

            lb.append(String.format("%08X", resSpec.getId().id), COLOR_KEYWORD);
            lb.append(String.format("   %s ", resSpec.getType().getName()), COLOR_HEX);
            lb.append(String.format("   %s ", entry.getName()), COLOR_COMMENT);
            lb.append(String.format(" (%d bytes) ", entry.getSize()), COLOR_SYMBOL);

            Object[] data = { resSpec, cRef };
            lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                    XmlViewReferenceAction.getInstance(mainFrame),
                    data));
            lb.newLine();
        }
        return size;
    }

    public void showAllResSpec(ApkClassContext ctx, MainFrame mainFrame, AbstractCanceableAction action) {
        ArrayList<ResResSpec> resSpecs = new ArrayList<ResResSpec>();
        for (ResPackage pkg : mResTable.listMainPackages()) {
            for (ResResSpec spec : pkg.listResSpecs()) {
                resSpecs.add(spec);
            }
        }

        if (resSpecs != null && resSpecs.size() > 0) {
            Comparator<ResResSpec> comp = new SpecComparator();
            Collections.sort(resSpecs, comp);

            LineBuilder lb = new LineBuilder();
            lb.newLine();
            for (int i = 0; i < resSpecs.size(); i++) {
                mainFrame.actionReportWork(action, 100 * i / resSpecs.size());
                appendSpec(lb, resSpecs.get(i), mainFrame, ctx, 0, 0, 0);
            }
            mainFrame.showText("Resource IDs: ", lb);
            mainFrame.setBottomInfo(resSpecs.size() + " resource(es) found");
        } else {
            mainFrame.setBottomInfo("No resource found");
        }
    }

    public LineBuilder showSpecDetail(ResResSpec spec, MainFrame mainFrame, ApkClassContext ctx) {
        DexReferenceCache cache = ctx.getDexReferenceCache();
        DexResSpec dexSpec = cache.getDexSpec(spec);
        int issue = dexSpec.getIssue();
        LineBuilder lb = new LineBuilder();
        lb.newLine();
        lb.append("ID: ", COLOR_KEYWORD);
        lb.append(String.format("%08X", spec.getId().id), COLOR_TEXT);
        lb.newLine();
        lb.append("NAME: ", COLOR_KEYWORD);
        lb.append(spec.getName(), COLOR_TEXT);
        lb.newLine();
        lb.append("PACKAGE: ", COLOR_KEYWORD);
        lb.append(spec.getPackage().getName(), COLOR_TEXT);
        lb.newLine();
        lb.append("TYPE: ", COLOR_KEYWORD);
        lb.append(spec.getType().getName(), COLOR_TEXT);
        lb.newLine();
        if (issue != 0) {
            lb.newLine();
            lb.append("ISSUE: ", COLOR_KEYWORD);
            lb.newLine();
            if ((issue & DexResSpec.ISSUE_MISSING_RESOURCE) != 0) {
                lb.append(DexResSpec.getIssueName(DexResSpec.ISSUE_MISSING_RESOURCE), COLOR_ERROR);
                lb.newLine();
            }
            if ((issue & DexResSpec.ISSUE_NO_DPI) != 0) {
                lb.append(DexResSpec.getIssueName(DexResSpec.ISSUE_NO_DPI), COLOR_ERROR);
                lb.newLine();
            }
            if ((issue & DexResSpec.ISSUE_NO_DEFAULT) != 0) {
                lb.append(DexResSpec.getIssueName(DexResSpec.ISSUE_NO_DEFAULT), COLOR_ERROR);
                lb.newLine();
            }
            if ((issue & DexResSpec.ISSUE_MISS_LANGUAGE) != 0) {
                lb.append(DexResSpec.getIssueName(DexResSpec.ISSUE_MISS_LANGUAGE), COLOR_ERROR);
                lb.newLine();
            }
        }

        if (spec.hasDefaultResource()) {
            lb.newLine();
            lb.append("DEFAULT RESOURCE: ", COLOR_KEYWORD);
            lb.newLine();
            try {
                ResValue defValue = spec.getDefaultResource().getValue();

                //each line should have newline otherwise setCurrentReference will not work.
                String content = getValue(defValue, "\n", false);
                String[] sArray = content.split("\n");
                for (String oneLine : sArray) {
                    lb.append(oneLine, (defValue instanceof ResFileValue) ? COLOR_HEX : COLOR_TEXT);
                    if (defValue instanceof ResFileValue) {
                        String path = ((ResFileValue) defValue).getPath();
                        if (path.toLowerCase().endsWith(".xml")) {
                            Object[] data = { ctx, ((ResFileValue) defValue).getPath(), new Integer(-1), new Integer(-1), new Boolean(false) };
                            lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                                    XmlViewerAction.getInstance(mainFrame),
                                    data));
                        }
                    }
                    lb.newLine();
                }
            } catch (AndrolibException e) {
                e.printStackTrace();
            }
            lb.newLine();
        }

        lb.newLine();
        lb.append("CONFIGS: ", COLOR_KEYWORD);
        lb.newLine();
        for (ResResource res : spec.listResources()) {
            lb.append(res.getConfig().toString());
            lb.append("   ");
            lb.append(getValue(res.getValue(), " ", false), (res.getValue() instanceof ResFileValue) ? COLOR_HEX : COLOR_TEXT);
            if (res.getValue() instanceof ResFileValue) {
                String path = ((ResFileValue) res.getValue()).getPath();
                if (path.toLowerCase().endsWith(".xml")) {
                    Object[] data = { ctx, path, new Integer(-1), new Integer(-1), new Boolean(false) };
                    lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                            XmlViewerAction.getInstance(mainFrame),
                            data));
                }
            }
            lb.newLine();
        }

        if (atLeastOneLanguage(spec)) {
            lb.newLine();
            lb.append("LANGUAGE: ", COLOR_KEYWORD);
            lb.newLine();
            HashSet<String> have = new HashSet<String>();
            Iterator<String> i = getLanguageSet().iterator();
            while (i.hasNext()) {
                String language = i.next();
                for (ResResource res : spec.listResources()) {
                    if (res.getConfig().getFlags().getQualifiers().indexOf(language) != -1) {
                        have.add(language);
                    }
                }
            }
            HashSet<String> miss = new HashSet<String>();
            Iterator<String> ii = getLanguageSet().iterator();
            while (ii.hasNext()) {
                String language = ii.next();
                if (!have.contains(language)) {
                    miss.add(language);
                }
            }
            lb.append("CONTAINS: ", COLOR_COMMENT);
            lb.append(have.toString().equals("[]") ? "NONE" : have.toString(), COLOR_TEXT);
            lb.newLine();
            lb.append("MISSING: ", COLOR_COMMENT);
            lb.append(miss.toString().equals("[]") ? "NONE" : miss.toString(), COLOR_TEXT);
            lb.newLine();
        }

        if (dexSpec.getSumReference() == 0) {
            lb.newLine();
            lb.append("NO REFERENCE", COLOR_KEYWORD);
        } else {
            appendBagReference(dexSpec.getResReference(), ctx, lb, mainFrame);
            appendXmlReference(dexSpec.getXmlReference(), ctx, lb, mainFrame);
            appendCodeReference(dexSpec.getCodeReference(), ctx, lb, mainFrame);
        }

        return lb;
    }

    public void showIssueSpec(String density, MainFrame mainFrame, RefContext cRef, AbstractCanceableAction action, ApkClassContext ctx) {
        //this step without output just show it in log
        verifyResourceRARSC(cRef);

        DexReferenceCache cache = ctx.getDexReferenceCache();
        mainFrame.actionReportWork(action, 10);
        ArrayList<ResResSpec> resSpecs = verify(density, cache);

        mainFrame.actionReportWork(action, 20);

        if (resSpecs == null || resSpecs.size() == 0) {
            mainFrame.setBottomInfo("No resource issue found");
            return;
        }

        LineBuilder lb = new LineBuilder();
        lb.newLine();

        showOneIssueSpec(resSpecs, mainFrame, ctx, DexResSpec.ISSUE_MISSING_RESOURCE, "MISSING RESOURCE:", lb);
        mainFrame.actionReportWork(action, 40);
        showOneIssueSpec(resSpecs, mainFrame, ctx, DexResSpec.ISSUE_NO_DPI, "NO DPI SPECIFIED:", lb);
        mainFrame.actionReportWork(action, 60);
        showOneIssueSpec(resSpecs, mainFrame, ctx, DexResSpec.ISSUE_NO_DEFAULT, "NO DEFAULT RESOURCE:", lb);
        mainFrame.actionReportWork(action, 80);
        showOneIssueSpec(resSpecs, mainFrame, ctx, DexResSpec.ISSUE_MISS_LANGUAGE, "MISSING SOME LANGUAGE TRANSLATION:", lb);
        mainFrame.actionReportWork(action, 100);
        mainFrame.showText("Resource Issues: ", lb);
        mainFrame.setBottomInfo(resSpecs.size() + " issue(s) found");

    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    /*
    private boolean isReferredId(int id, ResValue resValue, boolean onlyPackage){
    	boolean ret = false;
    	if (resValue != null
    			&& resValue instanceof ResReferenceValue){
    		int value = ((ResReferenceValue)resValue).getValue();
    		if (onlyPackage){
    			ret = (new ResID(value).package_ == new ResID(id).package_);
    		}else{
    			ret = (value == id);
    		}
    	}
    	return ret;
    }
     */

    private void appendBagReference(Set<ResResSpec> specs, ApkClassContext ctx, LineBuilder lb, MainFrame mainFrame) {
        boolean first = true;
        if (specs.size() != 0
                && lb != null
                && mainFrame != null) {
            for (ResResSpec spec : specs) {
                if (first) {
                    lb.newLine();
                    lb.append("RESOURCE INTERNAL REFERENCE (double-clickable):", COLOR_KEYWORD);
                    lb.newLine();
                    first = false;
                }
                appendSpec(lb, spec, mainFrame, ctx, 0, 0, 0);
            }
        }
    }

    private void appendXmlReference(List<XmlLine> xmllines, ApkClassContext ctx, LineBuilder lb, MainFrame mainFrame) {
        boolean first = true;
        if (xmllines.size() != 0
                && lb != null
                && mainFrame != null) {
            for (XmlLine xmlline : xmllines) {
                if (first) {
                    lb.newLine();
                    lb.append("XML REFERENCE (double-clickable):", COLOR_KEYWORD);
                    lb.newLine();
                    first = false;
                }
                lb.append(xmlline.entry.getName(), 0x000000);
                lb.append(" line ");
                lb.append(xmlline.line, LineBuilderFormatter.COLOR_COMMENT);
                Object[] data = { ctx, xmlline.entry.getName(), xmlline.line, new Integer(xmlline.id), new Boolean(false) };
                lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                        XmlViewerAction.getInstance(mainFrame),
                        data));
                lb.newLine();
            }
        }

    }

    private void appendCodeReference(List<? extends DexReferenceCache.LoadConst> loadConsts, ApkClassContext ctx, LineBuilder lb, MainFrame mainFrame) {
        boolean first = true;
        if (loadConsts.size() != 0
                && lb != null
                && mainFrame != null) {
            for (DexReferenceCache.LoadConst e : loadConsts) {
                if (first) {
                    lb.newLine();
                    lb.append("CODE REFERENCE (double-clickable):", COLOR_KEYWORD);
                    lb.newLine();
                    first = false;
                }
                if (e instanceof LoadConstRes) {
                    lb.append(e.method.getMEClass().getName() + " : ", 0x000000);
                    LineBuilderFormatter.makeOutline(e.method, lb);
                    lb.append(" @ ", 0x000000);
                    lb.append(Integer.toHexString(e.instruction.codeAddress), 0x000088);
                    if (e.instruction.line != -1) {
                        lb.append(" (line" + e.instruction.line + ")", 0x000088);
                    }
                } else {
                    lb.append("\"" + ((LoadConstString) e).string + "\" ", 0x880000);
                    lb.append(" @ " + e.method.getMEClass().getClassName(), 0x000000);
                    if (e.instruction.line != -1) {
                        lb.append(" (line" + e.instruction.line + ")", 0x000088);
                    }
                }

                Object[] data = { e.method, new Integer(e.instruction.codeAddress), e.instruction };
                lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                        ShowBytecodeAction.getInstance(mainFrame),
                        data));
                lb.newLine();

            }
        }
    }

    private boolean atLeastOneLanguage(ResResSpec spec) {
        boolean atLeastOneLanguage = false;
        Iterator<String> ii = getLanguageSet().iterator();
        while (ii.hasNext() && atLeastOneLanguage == false) {
            String language = ii.next();
            for (ResResource res : spec.listResources()) {
                if (res.getConfig().getFlags().getQualifiers().indexOf(language) != -1) {
                    atLeastOneLanguage = true;
                    break;
                }
            }
        }
        return atLeastOneLanguage;
    }

    private ArrayList<ResResSpec> verify(String density, DexReferenceCache cache) {
        ArrayList<ResResSpec> problems = new ArrayList<ResResSpec>();
        HashSet<String> languageSet = getLanguageSet();
        for (ResPackage pkg : mResTable.listMainPackages()) {
            for (ResResSpec spec : pkg.listResSpecs()) {
                int issue = 0;

                if (spec.getName().startsWith("MISSING_RESOURCE_")) {
                    issue = issue | DexResSpec.ISSUE_MISSING_RESOURCE;
                }

                if (!spec.hasDefaultResource()) {
                    issue = issue | DexResSpec.ISSUE_NO_DEFAULT;
                }

                String typeName = spec.getType().getName();
                StringBuilder sb = new StringBuilder();

                if (atLeastOneLanguage(spec)
                        || typeName.equals("string")
                        || typeName.equals("plurals")) {
                    boolean hasLanguage = true;
                    Iterator<String> i = languageSet.iterator();

                    while (i.hasNext()) {
                        String language = i.next();
                        boolean containsLanguage = false;
                        if (language.equals("-en")
                                && spec.hasDefaultResource()) {
                            containsLanguage = true;
                        } else {
                            for (ResResource res : spec.listResources()) {
                                if (res.getConfig().getFlags().getQualifiers().indexOf(language) != -1) {
                                    containsLanguage = true;
                                }
                            }
                        }
                        if (!containsLanguage) {
                            hasLanguage = false;
                            sb.append(language);
                        }
                    }

                    if (!hasLanguage) {
                        issue = issue | DexResSpec.ISSUE_MISS_LANGUAGE;
                    }
                }

                if ((!spec.hasDefaultResource())
                        && (!density.equals("nodpi"))) {
                    boolean hasDensity = false;
                    for (ResResource res : spec.listResources()) {
                        if (res.getConfig().getFlags().getQualifiers().indexOf(density) != -1
                                || res.getConfig().getFlags().getQualifiers().indexOf("nodpi") != -1) {
                            hasDensity = true;
                        }
                    }
                    if (!hasDensity) {
                        issue = issue | DexResSpec.ISSUE_NO_DPI;
                    } else {
                        issue = issue & ~DexResSpec.ISSUE_NO_DEFAULT;
                    }
                }

                if (issue != 0) {
                    cache.getDexSpec(spec).setIssue(issue);
                    problems.add(spec);
                }
            }

        }
        return problems;
    }

    private void appendSpec(LineBuilder lb, ResResSpec resSpec, MainFrame mainFrame, ApkClassContext ctx, int res, int xml, int code) {
        lb.append(String.format("%08X", resSpec.getId().id), COLOR_KEYWORD);
        lb.append(String.format("   %s ", resSpec.getType().getName()), COLOR_HEX);
        if (res != 0) {
            lb.append(String.format(" [RES %d] ", res), COLOR_LABEL);
        }
        if (xml != 0) {
            lb.append(String.format(" [XML %d] ", xml), COLOR_LABEL);
        }
        if (code != 0) {
            lb.append(String.format(" [CODE %d] ", code), COLOR_LABEL);
        }

        lb.append(String.format("  %s", resSpec.getName()), COLOR_COMMENT);

        ResResource defaultRes = null;
        try {
            defaultRes = resSpec.getDefaultResource();
        } catch (AndrolibException e) {
        }
        if (defaultRes != null) {
            //lb.append(String.format("   %s", defaultRes.getValue().getClass().getSimpleName()),COLOR_PC);
            lb.append(String.format("   %s", getValue(defaultRes.getValue(), " ", true)), (defaultRes.getValue() instanceof ResFileValue) ? COLOR_HEX : COLOR_TEXT);
        } else {
            lb.append("   NO DEFAULT", COLOR_ERROR);
        }
        Object[] data = { resSpec, ctx };
        lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                XmlViewReferenceAction.getInstance(mainFrame),
                data));
        lb.newLine();
    }

    private void showOneIssueSpec(ArrayList<ResResSpec> resSpecs, MainFrame mainFrame, ApkClassContext ctx, int filter, String Title, LineBuilder lb) {
        boolean displayed = false;
        ArrayList<ResResSpec> issueSpec1 = new ArrayList<ResResSpec>();
        for (ResResSpec resSpec : resSpecs) {
            if ((ctx.getDexReferenceCache().getDexSpec(resSpec).getIssue() & filter) != 0) {
                issueSpec1.add(resSpec);
            }
        }
        if (issueSpec1.size() != 0) {
            lb.append(DexResSpec.getIssueName(filter), COLOR_ERROR);
            lb.newLine();
            for (ResResSpec spec : issueSpec1) {
                appendSpec(lb, spec, mainFrame, ctx, 0, 0, 0);
            }
            displayed = true;
        }

        if (displayed == true) {
            lb.newLine();
        }
    }

    private void verifyResourceRARSC(RefContext cRef) {
        ArrayList<DexField> fields = new ArrayList<DexField>();

        //verify resource from R.java to arsc
        Iterator<Reference> i = cRef.getChildren().iterator();
        while (i.hasNext()) {
            Reference ref = i.next();
            if (ref instanceof RefPackage
                    && ((RefPackage) ref).getName().equals(getMainPackageName())) {
                Iterator<Reference> iclass = ref.getChildren().iterator();
                while (iclass.hasNext()) {
                    Reference refClass = iclass.next();
                    if (refClass instanceof RefClass
                            && (((RefClass) refClass).getName().equals("R") || ((RefClass) refClass).getName().startsWith("R$"))
                            && !((RefClass) refClass).getName().equals("R$styleable")) {
                        DexClass dexClass = (DexClass) ((RefClass) refClass).getMEClass();
                        DexField[] dexFields = (DexField[]) dexClass.getFields();
                        for (DexField field : dexFields) {
                            if (field.isPublic()
                                    && field.isStatic()
                                    && field.isFinal()
                                    && field.getDescriptor().equals("I")
                                    && field.getConstantValue() instanceof Long) {

                                int id = ((Long) field.getConstantValue()).intValue();
                                try {
                                    if (mResTable.getResSpec(id) != null) {
                                        fields.add(field);
                                    }
                                } catch (AndrolibException e) {
                                    System.out.println("ERROR: Resource field " + field.getName() + " (" + String.format("%08X", id) + ") could not be found in spec");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }

        //verify resource from arsc to R.java
        for (ResPackage pkg : mResTable.listMainPackages()) {
            List<ResResSpec> specs = pkg.listResSpecs();
            for (ResResSpec spec : specs) {
                int specid = spec.getId().id;
                boolean found = false;
                for (DexField field : fields) {
                    if (found == false) {
                        int id = ((Long) field.getConstantValue()).intValue();
                        if (id == specid) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found == false) {
                    System.out.println("ERROR: Resource spec (" + String.format("%08X", specid) + ") could not be found in R.java");
                }
            }
        }

    }

    private String getMainPackageName() {
        for (ResPackage pkg : mResTable.listMainPackages()) {
            return pkg.getName();
        }
        return null;
    }

    private HashSet<String> getLanguageSet() {
        if (mLanguageSet == null) {
            mLanguageSet = new HashSet<String>();
            for (ResPackage pkg : mResTable.listMainPackages()) {
                for (ResConfig config : pkg.getConfigs()) {
                    ResConfigFlags flags = config.getFlags();
                    StringBuilder ret = new StringBuilder();
                    if (flags.language[0] != '\00') {
                        ret.append('-').append(flags.language);
                        /*if (flags.country[0] != '\00') {
                        ret.append("-r").append(flags.country);
                        }*/
                    }
                    if (!ret.toString().equals("")) {
                        mLanguageSet.add(ret.toString());
                    }
                }
            }
        }
        return mLanguageSet;
    }

    private String getValue(ResValue resValue, String seperator, boolean shortString) {
        String value = null;
        if (resValue instanceof ResReferenceValue) {
            value = XmlResAttrDecoder.getResReferenceValue((ResReferenceValue) resValue, mResTable, false);
        } else if (resValue instanceof ResStringValue
                || resValue instanceof ResFloatValue
                || resValue instanceof ResDimenValue
                || resValue instanceof ResFractionValue
                || resValue instanceof ResBoolValue
                || resValue instanceof ResColorValue
                || resValue instanceof ResIntValue) {
            try {
                value = ((ResScalarValue) resValue).encodeAsResXmlValue().replace('\n', ' ');
            } catch (AndrolibException e) {
                e.printStackTrace();
            }
            if (shortString == true
                    && value.length() > 50) {
                value = value.substring(0, 50) + "...";
            }

            if (resValue instanceof ResStringValue
                    && value.equals("")) {
                value = "EMPTY STRING";
            } else {
                value = "\"" + value + "\"";
            }
        } else if (resValue instanceof ResFileValue) {
            value = ((ResFileValue) resValue).getPath();
        } else if (resValue instanceof ResBagValue) {
            value = "";
            if (((ResBagValue) resValue).getParent() != null) {
                String parent = getValue(((ResBagValue) resValue).getParent(), " ", shortString);
                if (!parent.equals("@null")) {
                    value += "parent=" + parent + seperator;
                }
            }
            if (resValue instanceof ResAttr) {
                ResAttr attr = (ResAttr) resValue;
                String type = attr.getTypeAsString();

                if (type != null) {
                    value += "format=" + type + " ";
                }
                if (attr.mMin != null) {
                    value += "min=" + attr.mMin.toString() + " ";
                }
                if (attr.mMax != null) {
                    value += "max=" + attr.mMax.toString() + " ";
                }
                if (attr.mL10n != null && attr.mL10n) {
                    value += "localization=suggested";
                }
                value += " ";
                if (resValue instanceof ResFlagsAttr) {
                    int length = ((ResFlagsAttr) resValue).mItems.length;
                    for (int i = 0; i < length; i++) {
                        FlagItem item = ((ResFlagsAttr) resValue).mItems[i];
                        value += getValue(item.ref, " ", shortString);
                        value += "=" + String.format("0x%08x", item.flag);
                        if (shortString) {
                            break;
                        }
                    }
                    if (shortString
                            && length > 1) {
                        value += " ... ";
                    }
                } else if (resValue instanceof ResEnumAttr) {
                    for (Duo<ResReferenceValue, ResIntValue> duoValue : ((ResEnumAttr) resValue).mItems) {
                        String m1 = getValue(duoValue.m1, " ", shortString);
                        value += m1 + "=";
                        value += getValue(duoValue.m2, " ", shortString) + seperator;
                        if (shortString) {
                            break;
                        }
                    }
                    if (shortString
                            && ((ResEnumAttr) resValue).mItems.length > 1) {
                        value += " ... ";
                    }
                }
            } else if (resValue instanceof ResArrayValue) {
                for (ResScalarValue resScalarValue : ((ResArrayValue) resValue).mItems) {
                    value += getValue(resScalarValue, " ", shortString) + seperator;
                    if (shortString) {
                        break;
                    }
                }
                if (shortString
                        && ((ResArrayValue) resValue).mItems.length > 1) {
                    value += " ... ";
                }
            } else if (resValue instanceof ResPluralsValue) {
                for (int i = 0; i < ((ResPluralsValue) resValue).mItems.length; i++) {
                    ResScalarValue item = ((ResPluralsValue) resValue).mItems[i];
                    if (item == null) {
                        continue;
                    }
                    value += ResPluralsValue.QUANTITY_MAP[i] + " = " + getValue(item, " ", shortString) + seperator;
                    if (shortString) {
                        break;
                    }
                }
                if (shortString) {
                    value += " ... ";
                }
            } else if (resValue instanceof ResStyleValue) {
                for (Duo<ResReferenceValue, ResScalarValue> duoValue : ((ResStyleValue) resValue).mItems) {
                    String m1 = getValue(duoValue.m1, " ", shortString);
                    value += m1 + "=";
                    value += getValue(duoValue.m2, " ", shortString) + seperator;
                    if (shortString) {
                        break;
                    }
                }

                if (shortString
                        && ((ResStyleValue) resValue).mItems.length > 1) {
                    value += " ... ";
                }
            }
        }

        if (value == null) {
            System.out.println("[ERROR] unhandled resource value: " + resValue.toString());
        }
        return value;
    }

    private class SpecComparator implements Comparator<ResResSpec> {
        @Override
        public int compare(ResResSpec o1, ResResSpec o2) {
            return o1.getId().id > o2.getId().id ? 1 : 0;
        }
    }

}
