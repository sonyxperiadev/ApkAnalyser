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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import mereflect.JarClassContext;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.UnknownResContext;
import analyser.logic.RefAndroidManifest;
import analyser.logic.RefClass;
import analyser.logic.RefContext;
import analyser.logic.RefField;
import analyser.logic.RefFieldAccess;
import analyser.logic.RefFolder;
import analyser.logic.RefInvokation;
import analyser.logic.RefMethod;
import analyser.logic.RefPackage;
import analyser.logic.RefResReference;
import analyser.logic.RefResSpec;
import analyser.logic.RefXml;
import analyser.logic.Reference;
import andreflect.ApkClassContext;

public class ClassTreeRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = -931509497453027963L;
    public static FlagIcon ICON_CLASS = createImageIcon("class.png");
    public static FlagIcon ICON_INTERFACE = createImageIcon("interface.png");
    public static FlagIcon ICON_FOLDER = createImageIcon("folder.png");
    public static FlagIcon ICON_INVOKATION = createImageIcon("invokation.png");
    public static FlagIcon ICON_LOCAL_INVOKATION = createImageIcon("localinvokation.png");
    public static FlagIcon ICON_JAR = createImageIcon("jar.png");
    public static FlagIcon ICON_APK = createImageIcon("apk.png");
    public static FlagIcon ICON_PUBLICMETHOD = createImageIcon("methpub_obj.gif");
    public static FlagIcon ICON_PROTECTEDMETHOD = createImageIcon("methpro_obj.gif");
    public static FlagIcon ICON_PACKAGEMETHOD = createImageIcon("packagemethod.png");
    public static FlagIcon ICON_PRIVATEMETHOD = createImageIcon("methpri_obj.gif");
    public static FlagIcon ICON_PACKAGE = createImageIcon("package.png");

    public static FlagIcon ICON_PUBLICFIELD = createImageIcon("field_public_obj.gif");
    public static FlagIcon ICON_PROTECTEDFIELD = createImageIcon("field_protected_obj.gif");
    public static FlagIcon ICON_PACKAGEMFIELD = createImageIcon("field_default_obj.gif");
    public static FlagIcon ICON_PRIVATEFIELD = createImageIcon("field_private_obj.gif");

    public static FlagIcon ICON_XML = createImageIcon("XMLFile.gif");
    public static FlagIcon ICON_ANDROID = createImageIcon("android_file.png");
    public static FlagIcon ICON_PACKAGEFOLDER = createImageIcon("packagefolder_obj.gif");

    public static FlagIcon ICON_FIELDREAD = createImageIcon("read_obj.png");
    public static FlagIcon ICON_FIELDWRITE = createImageIcon("write_obj.png");

    public static FlagIcon ICON_JAVAFILE = createImageIcon("jcu_obj.gif");

    final static Color COLOR_MARKED = new Color(0, 0, 128);

    protected boolean m_drawCount = false;
    protected boolean m_drawCountGlobal = false;
    protected int m_count;
    static Font originalFont = null;
    static Font markedFont = null;

    public ClassTreeRenderer(boolean drawCount) {
        m_drawCountGlobal = drawCount;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (originalFont == null) {
            originalFont = getFont();
        }
        if (originalFont != null && markedFont == null) {
            markedFont = originalFont.deriveFont(Font.BOLD);
        }

        Object val = ((DefaultMutableTreeNode) value).getUserObject();
        String text = val.toString();
        if (val instanceof Reference) {
            Reference ref = (Reference) val;
            FlagIcon fIcon = null;
            int flags = 0;
            m_drawCount = m_drawCountGlobal;
            m_count = ref.getCount();
            if (ref instanceof RefContext) {
                MEClassContext ctx = ((RefContext) ref).getContext();
                if (ctx.getContextDescription().equals(JarClassContext.DESCRIPTION)) {
                    if (ctx.getDexReferenceCache().hasSpec()) {
                        fIcon = ICON_APK;
                    } else {
                        fIcon = ICON_JAR;
                    }
                } else if (ctx.getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
                    fIcon = ICON_APK;
                } else if (ctx.getContextDescription().equals(UnknownResContext.DESCRIPTION)) {
                    fIcon = ICON_FOLDER;
                    flags |= FlagIcon.FLAG_ANDROID;
                } else {
                    fIcon = ICON_FOLDER;
                }
                setToolTipText("[" + m_count + "] " + ctx.getContextName());
            } else if (ref instanceof RefPackage) {
                fIcon = ICON_PACKAGE;
                setToolTipText("[" + m_count + "] " + ((RefPackage) ref).getName());
            } else if (ref instanceof RefClass) {
                MEClass c = ((RefClass) ref).getMEClass();
                if (c.isInterface()) {
                    fIcon = ICON_INTERFACE;
                } else {
                    fIcon = ICON_CLASS;
                }
                setToolTipText("[" + m_count + "] " + ((RefClass) ref).getMEClass().getName());
            } else if (ref instanceof RefMethod) {
                MEMethod method = ((RefMethod) ref).getMethod();
                if (method.isPublic()) {
                    fIcon = ICON_PUBLICMETHOD;
                } else if (method.isProtected()) {
                    fIcon = ICON_PROTECTEDMETHOD;
                } else if (method.isPrivate()) {
                    fIcon = ICON_PRIVATEMETHOD;
                } else {
                    fIcon = ICON_PACKAGEMETHOD;
                }
                flags |= method.isNative() ? FlagIcon.FLAG_NATIVE : 0;
                flags |= method.isConstructor() ? FlagIcon.FLAG_CONSTRUCTOR : 0;
                flags |= method.isStatic() ? FlagIcon.FLAG_STATIC : 0;
                flags |= method.isFinal() ? FlagIcon.FLAG_FINAL : 0;
                setToolTipText("[" + m_count + "] " + method.toString());
            } else if (ref instanceof RefInvokation) {
                fIcon = ((RefInvokation) ref).isLocal() ? ICON_LOCAL_INVOKATION : ICON_INVOKATION;
                if (((RefInvokation) ref).getOppositeInvokation() != null) {
                    setToolTipText("[" + m_count + "] " + (((RefInvokation) ref).getOppositeInvokation().getResourceName()));
                }
            } else if (ref instanceof RefField) {
                MEField field = ((RefField) ref).getField();
                if (field.isPublic()) {
                    fIcon = ICON_PUBLICFIELD;
                } else if (field.isProtected()) {
                    fIcon = ICON_PROTECTEDFIELD;
                } else if (field.isPrivate()) {
                    fIcon = ICON_PRIVATEFIELD;
                } else {
                    fIcon = ICON_PACKAGEMFIELD;
                }
                flags |= field.isStatic() ? FlagIcon.FLAG_STATIC : 0;
                flags |= field.isFinal() ? FlagIcon.FLAG_FINAL : 0;
            } else if (ref instanceof RefFolder) {
                int type = ((RefFolder) ref).getType();
                switch (type) {
                case RefFolder.SRC:
                    fIcon = ICON_PACKAGEFOLDER;
                    break;
                case RefFolder.RES:
                    fIcon = ICON_FOLDER;
                    flags |= FlagIcon.FLAG_ANDROID;
                    break;
                case RefFolder.XML:
                    fIcon = ICON_XML;
                    break;
                case RefFolder.RESTYPE:
                case RefFolder.UNKNOWN:
                    fIcon = ICON_FOLDER;
                    m_drawCount = true;
                    break;
                }
            } else if (ref instanceof RefAndroidManifest) {
                fIcon = ICON_ANDROID;
            } else if (ref instanceof RefResSpec) {
                fIcon = ICON_ANDROID;
                setToolTipText("[" + m_count + "] " + ((RefResSpec) ref).getName());
                m_drawCount = true;
            } else if (ref instanceof RefFieldAccess) {
                if (((RefFieldAccess) ref).getAccess().isRead) {
                    fIcon = ICON_FIELDREAD;
                } else {
                    fIcon = ICON_FIELDWRITE;
                }
            } else if (ref instanceof RefResReference) {
                if (((RefResReference) ref).isRes()) {
                    fIcon = ICON_ANDROID;
                } else if (((RefResReference) ref).isXml()) {
                    fIcon = ICON_XML;
                } else if (((RefResReference) ref).isCode()) {
                    fIcon = ICON_JAVAFILE;
                }
                m_drawCount = true;
            } else if (ref instanceof RefXml) {
                fIcon = ICON_XML;
            }
            flags |= ((ref.getFlags() & Reference.FAILED) > 0) ? FlagIcon.FLAG_FAIL : 0;
            flags |= ((ref.getFlags() & Reference.NOTFOUND) > 0) ? FlagIcon.FLAG_UNKNOWN : 0;
            flags |= ((ref.getFlags() & Reference.MODIFIED) != 0) ? FlagIcon.FLAG_MODIFIED : 0;
            fIcon.setFlags(flags);
            setIcon(fIcon);
        } else {
            m_drawCount = false;
        }
        if (m_drawCount) {
            setText("             " + text);
        } else {
            setText(text);
        }

        if (((ClassTree) tree).isMarked((DefaultMutableTreeNode) value)) {
            setFont(markedFont);
            setForeground(COLOR_MARKED);
        } else {
            setFont(originalFont);
            setForeground(Color.black);
        }
        return this;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (m_drawCount) {
            String cStr = "[" + m_count + "]";
            g.setColor(Color.GRAY);
            g.drawString(cStr, getIcon().getIconWidth() + getIconTextGap(), getFont().getSize() + 1);
        }
    }

    protected static FlagIcon createImageIcon(String path) {
        java.net.URL imgURL = Thread.currentThread().getContextClassLoader().getResource(path);
        if (imgURL != null) {
            return new FlagIcon(imgURL, 0);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}