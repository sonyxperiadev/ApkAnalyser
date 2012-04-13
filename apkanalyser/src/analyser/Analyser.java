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

package analyser;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.UIManager;

import mereflect.CollaborateClassContext;
import mereflect.JarClassContext;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEClassResource;
import analyser.gui.MainFrame;
import analyser.gui.Settings;
import andreflect.ApkClassContext;
import andreflect.gui.splash.SplashWindow;

public class Analyser
{
    public static final String PROP_NAME = "apkanalyser.properties";
    protected File m_midletsPath;
    protected File m_classPath;
    protected Properties m_props;

    public static final String[] MIDLET_SUFFIX = { ".jar", ".zip" };
    public static final String[] APK_SUFFIX = { ".apk" };
    public static final String[] JAR_SUFFIX = { ".jar" };

    protected List<File> m_midlets = new ArrayList<File>();
    protected List<File> m_apks = new ArrayList<File>();
    protected CollaborateClassContext m_cpcontext = new CollaborateClassContext();

    public void analyse() throws Exception
    {
        getMidlets(m_midlets);
        getApks(m_apks);
        if (m_midlets.isEmpty() && m_apks.isEmpty())
        {
            throw new IllegalArgumentException("No Apk or midlets found");
        }
        setClasspath(m_cpcontext);
        Set<MEClassContext> depends;
        for (int i = 0; i < m_midlets.size(); i++)
        {
            File midlet = m_midlets.get(i);
            JarClassContext midletContext = new JarClassContext(midlet, true);
            depends = new TreeSet<MEClassContext>();
            String midletName = midlet.getName();
            midletName = midletName.substring(0, midletName.length() - MIDLET_SUFFIX[0].length());

            MEClassResource[] cRes = midletContext.getClassResources();
            for (int j = 0; j < cRes.length; j++)
            {
                MEClass midletClass = midletContext.getMEClass(cRes[j].getClassName());
                String[] depClassNames = midletClass.getDependencies();
                for (int k = 0; k < depClassNames.length; k++)
                {
                    String depClassName = depClassNames[k].replace('/', '.');
                    try
                    {
                        MEClass depClass = m_cpcontext.getMEClass(depClassName);
                        depends.add(depClass.getResource().getContext());
                    } catch (ClassNotFoundException cnfe)
                    {
                    }
                }
            }

            for (Iterator<MEClassContext> j = depends.iterator(); j.hasNext();)
            {
                MEClassContext ctx = (j.next());
                String output = getContextName(ctx);
                System.out.println("\t" + output);
            }
        }
        for (int i = 0; i < m_apks.size(); i++)
        {
            File apk = m_apks.get(i);
            ApkClassContext apkContext = new ApkClassContext(apk, true);
            depends = new TreeSet<MEClassContext>();
            String apkName = apk.getName();
            apkName = apkName.substring(0, apkName.length() - APK_SUFFIX[0].length());

            MEClassResource[] cRes = apkContext.getClassResources();
            for (int j = 0; j < cRes.length; j++)
            {
                MEClass apkClass = apkContext.getMEClass(cRes[j].getClassName());
                String[] depClassNames = apkClass.getDependencies();
                for (int k = 0; k < depClassNames.length; k++)
                {
                    String depClassName = depClassNames[k].replace('/', '.');
                    try
                    {
                        MEClass depClass = m_cpcontext.getMEClass(depClassName);
                        depends.add(depClass.getResource().getContext());
                    } catch (ClassNotFoundException cnfe)
                    {
                    }
                }
            }

            for (Iterator<MEClassContext> j = depends.iterator(); j.hasNext();)
            {
                MEClassContext ctx = (j.next());
                String output = getContextName(ctx);
                System.out.println("\t" + output);
            }
        }
    }

    public static String getContextName(MEClassContext ctx)
    {
        String name = ctx.getContextName();
        try
        {
            if (ctx.getContextDescription().equals(JarClassContext.DESCRIPTION))
            {
                int slashIndex = name.lastIndexOf(File.separatorChar);
                name = slashIndex >= 0 ? name.substring(slashIndex + 1) : name;

                JarFile jf = ((JarClassContext) ctx).getJar();
                if (jf != null)
                {
                    Manifest mf = jf.getManifest();
                    String manifest = null;
                    if (mf != null)
                    {
                        String mName = mf.getMainAttributes().getValue("MIDlet-Name");
                        String mVersion = mf.getMainAttributes().getValue("MIDlet-Version");
                        String mVend = mf.getMainAttributes().getValue("MIDlet-Vendor");

                        if (mVersion != null)
                        {
                            mName += " v" + mVersion;
                        }
                        if (mVend != null)
                        {
                            mName += " (" + mVend + ")";
                        }
                        if (mName != null)
                        {
                            manifest = mName;
                        }
                        if (manifest != null)
                        {
                            name = manifest;
                        }
                        else
                        {
                            String formOutput = mf.getMainAttributes().getValue("API");
                            String formVersion = mf.getMainAttributes().getValue("API-Specification-Version");
                            if (formOutput != null)
                            {
                                name = formOutput;
                                if (formVersion != null) {
                                    name += " " + formVersion;
                                }
                            }
                        }
                    }
                }
            } else if (ctx.getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
                name = ((ApkClassContext) ctx).getContextDescriptionName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;

    }

    public void setClasspath(CollaborateClassContext cpCtx)
    {
        String cp;
        if (Settings.getUseAndroidSDK()
                && Settings.getAndroidSDK() != null) {
            ArrayList<File> jarFiles = new ArrayList<File>();
            getFiles(jarFiles, Settings.getAndroidSDK(), JAR_SUFFIX);

            StringBuffer jarPaths = new StringBuffer();
            for (int i = 0; i < jarFiles.size(); i++)
            {
                File aJar = jarFiles.get(i);
                jarPaths.append(aJar.getPath());
                jarPaths.append(";");
            }
            cp = jarPaths.toString();
        } else {
            cp = Settings.getClasspath();
        }
        cpCtx.setClasspath(cp);
    }

    private void getFiles(List<File> result, String path, String[] suffix) {
        if (path != null
                && suffix != null
                && suffix.length > 0) {
            StringTokenizer st = new StringTokenizer(path, ";");
            while (st.hasMoreTokens()) {
                String p = st.nextToken();
                File f = new File(p);
                if (f.exists()) {
                    if (f.isDirectory()) {
                        addFiles(f, result, suffix);
                    } else {
                        addFile(f, result, suffix);
                    }
                }
            }
        }
    }

    private void addFiles(File path, List<File> result, String[] suffix) {
        File[] sub = path.listFiles();
        for (int i = 0; sub != null && i < sub.length; i++)
        {
            if (sub[i].isDirectory())
            {
                addFiles(sub[i], result, suffix);
            }
            else if (sub[i].isFile())
            {
                addFile(sub[i], result, suffix);
            }
        }
    }

    private void addFile(File path, List<File> result, String[] suffix) {
        if (path.exists())
        {
            String mName = path.getName().toLowerCase();
            boolean found = false;
            for (int i = 0; !found && i < suffix.length; i++)
            {
                found = (mName.endsWith(suffix[i]));
            }
            if (found) {
                result.add(path);
            }
        }
    }

    public void getMidlets(List<File> midlets) {
        getFiles(midlets, Settings.getMidletsPath(), MIDLET_SUFFIX);
    }

    public void getApks(List<File> apks) {
        getFiles(apks, Settings.getMidletsPath(), APK_SUFFIX);
    }

    // Startup

    public Analyser()
    {
        this(new Properties());
    }

    public Analyser(Properties p)
    {
        m_props = p;
    }

    public static void ctxmain(String[] args)
    {
        try
        {
            Class.forName("org.objectweb.asm.ClassVisitor");
        } catch (ClassNotFoundException e1)
        {
            e1.printStackTrace();
        }

        Analyser a = new Analyser();
        a.loadProperties();

        // Start gui
        SplashWindow.splash(SplashWindow.class.getResource("/splash.jpg"));
        Locale.setDefault(Locale.ENGLISH);
        sun.awt.AppContext.getAppContext().put("JComponent.defaultLocale", Locale.ENGLISH);

        Settings.setSettings(a.m_props);
        try {
            Settings.load();
        } catch (IOException e2) {
        }

        // init gui
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int inset = 100;
        int x = inset;
        int y = inset;
        int width = Math.max(1000, screenSize.width - inset * 2);
        int height = Math.max(800, screenSize.height - inset * 2);
        if (Settings.getMainFrameX() > 0) {
            x = Settings.getMainFrameX();
        }
        if (Settings.getMainFrameY() > 0) {
            y = Settings.getMainFrameY();
        }
        if (Settings.getMainFrameWidth() > 0) {
            width = Settings.getMainFrameWidth();
        }
        if (Settings.getMainFrameHeight() > 0) {
            height = Settings.getMainFrameHeight();
        }

        final MainFrame f = MainFrame.getInstance();
        f.initialize(width, height);
        f.setBounds(x, y, width, height);
        f.setVisible(true);
        f.addComponentListener(new ComponentListener(){
            @Override
            public void componentShown(ComponentEvent e) {
                SplashWindow.canDispose();
                f.removeComponentListener(this);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                // nothing to do
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // nothing to do
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                // nothing to do
            }
        });
    }

    protected boolean loadProperties()
    {
        File propFile = new File(PROP_NAME);
        if (!propFile.exists())
        {
            return false;
        }
        else
        {
            try
            {
                m_props.load(new FileInputStream(propFile));
            } catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

}
