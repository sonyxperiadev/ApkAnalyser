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

package analyser.gui;

import gui.SelectableFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import analyser.Analyser;


public class Settings {
    protected static final File SETTINGS = new File(Analyser.PROP_NAME);

    protected static Properties m_settings;

    public static final String DEFAULT_PATH = "paths.default";

    public static final String EJAVA_PATH = "paths.ejava";

    public static final String ADB_PATH = "paths.adb";

    public static final String MAINFRAME_X = "mainframe.x";

    public static final String MAINFRAME_Y = "mainframe.y";

    public static final String MAINFRAME_W = "mainframe.width";

    public static final String MAINFRAME_H = "mainframe.height";

    public static final String MAINFRAME_TEXTWINDOW_W = "mainframe.textwindow.width";

    public static final String MAINFRAME_TEXTWINDOW_H = "mainframe.textwindow.height";

    public static final String MAINFRAME_GRAPHWINDOW_W = "mainframe.graphwindow.width";

    public static final String MAINFRAME_GRAPHWINDOW_H = "mainframe.graphwindow.height";

    public static final String MAINFRAME_TREES_DIV = "mainframe.trees.div";

    public static final String INJECTIONS_USER_DEFINED = "injections.user";

    public static final String CLASSPATH = "cp";

    public static final String MIDLETS = "midlets";

    public static final String ANDROID_SDK = "androidsdk";

    public static final String ANDROID_SDK_SELECTED = "use_androidsdk";

    public static final String CONFIRMED_BREAKING = "confirmed_breaking";

    public static final String MAINFRAME_CONTENT_DIV = "mainframe.content.div";

    protected Settings() {
    }

    public static void setSettings(Properties p) {
        m_settings = p;
    }

    public static void setClasspath(Object[] files)
    {
        Settings.setClasspath(makeCpPropertyString(files));
    }

    public static void setMidletsPath(Object[] files)
    {
        Settings.setMidletsPath(makeCpPropertyString(files));
    }

    public static void setUseAndroidSDK(boolean b) {
        m_settings.setProperty(ANDROID_SDK_SELECTED, b ? "true" : "false");
    }

    public static boolean getUseAndroidSDK() {
        if (m_settings.getProperty(ANDROID_SDK_SELECTED) == null) {
            return false;
        }
        return m_settings.getProperty(ANDROID_SDK_SELECTED).equals("true");
    }

    public static void setAndroidSDK(String string) {
        m_settings.setProperty(ANDROID_SDK, string);
    }

    public static String getAndroidSDK() {
        return m_settings.getProperty(ANDROID_SDK);
    }

    public static String getDefaultPath() {
        String f = m_settings.getProperty(DEFAULT_PATH);
        if (f == null) {
            f = System.getProperty("user.home");
        }
        return f;
    }

    public static void setDefaultPath(String string) {
        m_settings.setProperty(DEFAULT_PATH, string);
    }

    public static String getEjavaPath() {
        String f = m_settings.getProperty(EJAVA_PATH);
        if (f == null) {
            f = "C:\\SonyEricsson\\JavaME_SDK_CLDC\\OnDeviceDebug\\bin\\ejava.exe";
        }
        return f;
    }

    public static String getAdbPath() {
        String f = m_settings.getProperty(ADB_PATH);
        if (f == null) {
            f = "C:\\android-sdk-windows\\platform-tools\\adb.exe";
        }
        return f;
    }

    public static void setAdbPath(String string) {
        m_settings.setProperty(ADB_PATH, string);
    }

    public static void setEjavaPath(String string) {
        m_settings.setProperty(EJAVA_PATH, string);
    }

    private static final String FSELECT = "[*]";
    private static final String FUNSELECT = "[ ]";

    public static String getClasspath() {
        return removeUnselectedFromClasspath(m_settings.getProperty(CLASSPATH));
    }

    public static void setClasspath(String cp) {
        m_settings.setProperty(CLASSPATH, cp);
    }

    public static File[] getSelectableClasspath() {
        return getSelectableFiles(Settings.breakString(m_settings.getProperty(CLASSPATH), ";"));
    }

    public static String getMidletsPath() {
        return removeUnselectedFromClasspath(m_settings.getProperty(MIDLETS));
    }

    public static File[] getSelectableMidletsPath() {
        return getSelectableFiles(Settings.breakString(m_settings.getProperty(MIDLETS), ";"));
    }

    public static void setMidletsPath(String midletsPath) {
        m_settings.setProperty(MIDLETS, midletsPath);
    }

    public static String removeUnselectedFromClasspath(String paths) {
        String[] pathArr = breakString(paths, ";");
        StringBuffer selPaths = new StringBuffer();
        for (int i = 0; i < pathArr.length; i++) {
            String path = pathArr[i];
            if (path.startsWith(FSELECT)) {
                selPaths.append(path.substring(FSELECT.length()));
                selPaths.append(';');
            } else if (path.startsWith(FUNSELECT)) {
            } else {
                selPaths.append(path);
                selPaths.append(';');
            }
        }
        return selPaths.toString();
    }

    public static int getMainFrameX() {
        return getPropertyInt(MAINFRAME_X);
    }

    public static int getMainFrameY() {
        return getPropertyInt(MAINFRAME_Y);
    }

    public static int getMainFrameWidth() {
        return getPropertyInt(MAINFRAME_W);
    }

    public static int getMainFrameHeight() {
        return getPropertyInt(MAINFRAME_H);
    }

    public static int getMainFrameTreesDiv() {
        return getPropertyInt(MAINFRAME_TREES_DIV);
    }

    public static int getMainFrameContentDiv() {
        return getPropertyInt(MAINFRAME_CONTENT_DIV);
    }

    public static boolean getConfirmedBreaking() {
        return getPropertyBoolean(CONFIRMED_BREAKING);
    }

    public static void setConfirmedBreaking(boolean confirmed) {
        m_settings.setProperty(CONFIRMED_BREAKING, Boolean.toString(confirmed));
    }

    public static void setMainFrameX(int i) {
        m_settings.setProperty(MAINFRAME_X, Integer.toString(i));
    }

    public static void setMainFrameY(int i) {
        m_settings.setProperty(MAINFRAME_Y, Integer.toString(i));
    }

    public static void setMainFrameWidth(int i) {
        m_settings.setProperty(MAINFRAME_W, Integer.toString(i));
    }

    public static void setMainFrameHeight(int i) {
        m_settings.setProperty(MAINFRAME_H, Integer.toString(i));
    }

    public static void setMainFrameTreesDiv(int i) {
        m_settings.setProperty(MAINFRAME_TREES_DIV, Integer.toString(i));
    }

    public static void setMainFrameContentDiv(int i) {
        m_settings.setProperty(MAINFRAME_CONTENT_DIV, Integer.toString(i));
    }

    public static int getTextWindowWidth() {
        return getPropertyInt(MAINFRAME_TEXTWINDOW_W);
    }

    public static void setTextWindowWidth(int w) {
        m_settings.setProperty(MAINFRAME_TEXTWINDOW_W, Integer.toString(w));
    }

    public static int getTextWindowHeight() {
        return getPropertyInt(MAINFRAME_TEXTWINDOW_H);
    }

    public static void setTextWindowHeight(int h) {
        m_settings.setProperty(MAINFRAME_TEXTWINDOW_H, Integer.toString(h));
    }

    public static int getGraphWindowWidth() {
        return getPropertyInt(MAINFRAME_GRAPHWINDOW_W);
    }

    public static void setGraphWindowWidth(int w) {
        m_settings.setProperty(MAINFRAME_GRAPHWINDOW_W, Integer.toString(w));
    }

    public static int getGraphWindowHeight() {
        return getPropertyInt(MAINFRAME_GRAPHWINDOW_H);
    }

    public static void setGraphWindowHeight(int h) {
        m_settings.setProperty(MAINFRAME_GRAPHWINDOW_H, Integer.toString(h));
    }

    public static String getVersion() {
        return "5.2";
    }

    public static String getApplicationName() {
        return "ApkAnalyser";
    }

    public static void addUserDefinedInjection(String inj) {
        String s = m_settings.getProperty(INJECTIONS_USER_DEFINED);
        if (s != null && s.trim().length() > 0) {
            s += "," + inj;
        } else {
            s = inj;
        }
        m_settings.setProperty(INJECTIONS_USER_DEFINED, s);
    }

    public static String[] getUserDefinedInjections() {
        return breakString(m_settings.getProperty(INJECTIONS_USER_DEFINED), ",");
    }

    // HELPERS
    private static File[] getSelectableFiles(String[] paths)
    {
        File[] res = new File[paths.length];
        for (int i = 0; i < res.length; i++)
        {
            if (paths[i].startsWith(Settings.FSELECT)) {
                SelectableFile sf = new SelectableFile(paths[i].substring(Settings.FSELECT.length()));
                res[i] = sf;
                sf.setSelected(true);
            } else if (paths[i].startsWith(Settings.FUNSELECT)) {
                SelectableFile sf = new SelectableFile(paths[i].substring(Settings.FUNSELECT.length()));
                res[i] = sf;
                sf.setSelected(false);
            } else {
                SelectableFile sf = new SelectableFile(paths[i]);
                res[i] = sf;
                sf.setSelected(true);
            }
        }
        return res;
    }

    private static String makeCpPropertyString(Object[] files) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i] instanceof SelectableFile) {
                if (((SelectableFile) files[i]).isSelected()) {
                    s.append(Settings.FSELECT + ((File) files[i]).getAbsolutePath());
                } else {
                    s.append(Settings.FUNSELECT + ((File) files[i]).getAbsolutePath());
                }
            } else {
                s.append(Settings.FSELECT + ((File) files[i]).getAbsolutePath());
            }
            if (i < files.length - 1) {
                s.append(';');
            }
        }
        return s.toString();
    }

    public static String getProperty(String key) {
        return m_settings.getProperty(key);
    }

    public static int getPropertyInt(String key) {
        String iStr = m_settings.getProperty(key);
        int i = 0;
        try {
            i = Integer.parseInt(iStr);
        } catch (Exception e) {
        }
        return i;
    }

    public static long getPropertyLong(String key) {
        String lStr = m_settings.getProperty(key);
        long l = 0;
        try {
            l = Long.parseLong(lStr);
        } catch (Exception e) {
        }
        return l;
    }

    public static double getPropertyDouble(String key) {
        String dStr = m_settings.getProperty(key);
        double d = 0;
        try {
            d = Double.parseDouble(dStr);
        } catch (Exception e) {
        }
        return d;
    }

    public static boolean getPropertyBoolean(String key) {
        String bStr = m_settings.getProperty(key);
        boolean b = false;
        try {
            b = Boolean.parseBoolean(bStr);
        } catch (Exception e) {
        }
        return b;
    }

    public static String[] getPropertyKeys(String keyPrefix) {
        ArrayList<String> res = new ArrayList<String>();
        Enumeration<Object> e = m_settings.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(keyPrefix)) {
                res.add(key);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    public static String[] getPropertyKeysUnique(String keyPrefix) {
        ArrayList<String> res = new ArrayList<String>();
        Enumeration<Object> e = m_settings.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(keyPrefix) && key.length() > keyPrefix.length()) {
                String postKey = key.substring(keyPrefix.length() + 1); // +1 = '.'
                String uKey;
                int pIndex = postKey.indexOf('.');
                if (pIndex < 0) {
                    uKey = keyPrefix + "." + postKey;
                } else {
                    uKey = keyPrefix + "." + postKey.substring(0, pIndex);
                }

                if (!res.contains(uKey)) {
                    res.add(uKey);
                }
            }
        }
        return res.toArray(new String[res.size()]);
    }

    public static Properties getProperties(String keyPrefix) {
        String[] keys = getPropertyKeys(keyPrefix);
        Properties props = new Properties();
        int l = keyPrefix.length();
        for (int i = 0; i < keys.length; i++) {
            props.setProperty(keys[i].substring(l + 1), m_settings.getProperty(keys[i]));
        }
        return props;
    }

    public static void setProperties(Properties props, String keyPrefix) {
        Enumeration<Object> e = props.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            m_settings.setProperty(keyPrefix + "." + key, m_settings.getProperty(key));
        }
    }

    public static void load() throws IOException {
        if (!SETTINGS.exists()) {
            setDefault();
        } else {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(SETTINGS);
                m_settings = new Properties();
                m_settings.load(fis);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        }
    }

    public static void store() throws IOException {
        if (!SETTINGS.exists()) {
            if (SETTINGS.getParentFile() != null) {
                SETTINGS.getParentFile().mkdirs();
            }
            SETTINGS.createNewFile();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(SETTINGS);
            m_settings.store(fos, getApplicationName() + " " + getVersion());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    protected static void setDefault() {
        m_settings = new Properties();
        setDefaultPath(System.getProperty("user.home"));
    }

    public static Properties getSettings() {
        return m_settings;
    }

    public static String[] breakString(String s, String separator) {
        if (s == null) {
            return new String[0];
        } else {
            StringTokenizer st = new StringTokenizer(s, separator);
            List<String> res = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                res.add(st.nextToken());
            }
            return res.toArray(new String[res.size()]);
        }
    }
}
