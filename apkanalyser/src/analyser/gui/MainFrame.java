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

import gui.AbstractMainFrame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mereflect.CorruptBytecodeException;
import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.UnknownMethod;
import mereflect.UnknownResSpec;

import org.jf.dexlib.Code.Instruction;

import analyser.gui.actions.AboutAction;
import analyser.gui.actions.AnalyseAction;
import analyser.gui.actions.AnalyseMidletAction;
import analyser.gui.actions.ExamineClassAction;
import analyser.gui.actions.ExitAction;
import analyser.gui.actions.SetPathsAction;
import analyser.gui.actions.SettingsAction;
import analyser.gui.actions.ShowBytecodeAction;
import analyser.gui.actions.bytecodemod.FieldAction;
import analyser.gui.actions.bytecodemod.GraphMarkAction;
import analyser.gui.actions.bytecodemod.MethodEntryAction;
import analyser.gui.actions.bytecodemod.MethodExitAction;
import analyser.gui.actions.bytecodemod.MethodFlowAction;
import analyser.gui.actions.bytecodemod.MethodOffsetAction;
import analyser.gui.actions.bytecodemod.MethodOffsetCurThreadAction;
import analyser.gui.actions.bytecodemod.PerformAction;
import analyser.gui.actions.bytecodemod.ReferenceCallsAction;
import analyser.gui.actions.bytecodemod.UnregisterAction;
import analyser.gui.actions.lookup.FindFieldAccessAction;
import analyser.gui.actions.lookup.FindMonitorsAction;
import analyser.gui.actions.lookup.FindNativeCallsAction;
import analyser.gui.actions.lookup.LocalLookUpCallersAction;
import analyser.gui.actions.lookup.LocalLookUpCallsAction;
import analyser.gui.actions.lookup.LookUpAction;
import analyser.gui.actions.lookup.OpenCallGraphAction;
import analyser.gui.actions.lookup.OpenCallerGraphAction;
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
import analyser.logic.Resolver;
import andreflect.ApkClassContext;
import andreflect.DexField;
import andreflect.DexMethod;
import andreflect.DexReferenceCache;
import andreflect.gui.action.AdbOpenAction;
import andreflect.gui.action.ApkExamineOdexDependencyAction;
import andreflect.gui.action.ApkInstallAndStartAction;
import andreflect.gui.action.ApkResignAction;
import andreflect.gui.action.ApkResignInstallAndStartAction;
import andreflect.gui.action.ApkUninstallAction;
import andreflect.gui.action.DalvikFindFieldAccessAction;
import andreflect.gui.action.XmlFindLabelAction;
import andreflect.gui.action.XmlResourceAction;
import andreflect.gui.action.XmlResourceIdAction;
import andreflect.gui.action.XmlSystemReferenceAction;
import andreflect.gui.action.XmlUnusedFileAction;
import andreflect.gui.action.XmlUnusedResourceAction;
import andreflect.gui.action.XmlVerifyResourceAction;
import andreflect.gui.action.injection.DalvikMethodEntryAction;
import andreflect.gui.action.injection.DalvikMethodEntryParamAction;
import andreflect.gui.action.injection.DalvikMethodExitAction;
import andreflect.gui.action.injection.DalvikMethodExitValueAction;
import andreflect.gui.action.injection.DalvikMethodFieldReadAction;
import andreflect.gui.action.injection.DalvikMethodFieldWriteAction;
import andreflect.gui.action.injection.DalvikMethodLocalReadAction;
import andreflect.gui.action.injection.DalvikMethodLocalWriteAction;
import andreflect.gui.action.injection.DalvikMethodOffsetCurThreadAction;
import andreflect.gui.action.injection.DalvikMethodOffsetCustomLogAction;
import andreflect.gui.action.injection.DalvikMethodOffsetExCatchAction;
import andreflect.gui.action.injection.DalvikMethodOffsetExThrowAction;
import andreflect.gui.action.injection.DalvikMethodOffsetGCAction;
import andreflect.gui.action.injection.DalvikMethodOffsetInstanceFinalizeAction;
import andreflect.gui.action.injection.DalvikMethodOffsetInstanceNewAction;
import andreflect.gui.action.injection.DalvikMethodOffsetInstanceNewInnerAction;
import andreflect.gui.action.injection.DalvikMethodOffsetPrintStackTraceAction;
import andreflect.gui.action.injection.DalvikMethodOffsetSyncEntryAction;
import andreflect.gui.action.injection.DalvikMethodOffsetSyncExitAction;
import andreflect.gui.action.injection.DalvikReferenceCallsParamAction;
import andreflect.gui.action.injection.DalvikReferenceCallsReturnAction;
import andreflect.gui.chart.GraphPanel;
import andreflect.gui.chart.GraphToolBar;
import andreflect.gui.linebuilder.DalvikByteCodeLineBuilder;
import andreflect.gui.linebuilder.XmlLineFormatter;
import andreflect.xml.XmlParser;
import andreflect.xml.XmlParser.XmlLine;
import brut.androlib.res.data.ResResSpec;

/**
 * TODO on MidletAnalyser  
 * C look for jad file and modify accordingly (size and jar name)  
 * C generic bytecode modifiction using reflection  
 * - invoke a special class instead of doing system.out.println  
 * - modify and compile that class in MidletAnalyser  
 * - being able to review bytecode changes  
 * - rescanning bytecode changes when showing bytecodes
 */
public class MainFrame extends AbstractMainFrame implements WindowListener
{
    private static final long serialVersionUID = 3267840509615224745L;
    protected static MainFrame m_inst = null;
    protected JSplitPane m_splitContent;
    protected JSplitPane m_splitTree;
    protected ClassTree m_resourceTree;
    protected ClassTree m_midletTree;
    protected Resolver m_resolver;
    protected JPopupMenu m_popup;
    protected ClassTree m_selectedTree;
    protected List<MAButton> m_buttons = new ArrayList<MAButton>();
    protected List<JMenuItem> m_contextMenuItems = new ArrayList<JMenuItem>();
    protected List<JMenuItem> m_apkMenuItems = new ArrayList<JMenuItem>();

    protected TextBuilder m_contentTextBuilder;
    protected GraphToolBar m_toolBar;
    protected JScrollPane m_contentScrollPane;
    protected GraphPanel m_contentViewPane;
    protected JSplitPane m_splitToolBar;

    public ClassTree getSelectedTree()
    {
        return m_selectedTree;
    }

    public ClassTree getOppositeSelectedTree()
    {
        if (m_selectedTree == null) {
            return null;
        }
        return m_selectedTree == getResourceTree() ?
                getMidletTree() :
                getResourceTree();
    }

    public ClassTree getResourceTree()
    {
        return m_resourceTree;
    }

    public ClassTree getMidletTree()
    {
        return m_midletTree;
    }

    public void setResolver(Resolver resolver)
    {
        m_resolver = resolver;
    }

    public Resolver getResolver()
    {
        return m_resolver;
    }

    protected void lookUpOppositeInvokations(ClassTree tree, Reference mRef)
    {
        Object[] source = { tree, mRef };
        LookUpAction.getInstance(this).actionPerformed(new ActionEvent(source, 0, null));
    }

    // Common

    public static MainFrame getInstance()
    {
        if (m_inst == null)
        {
            m_inst = new MainFrame();
        }
        return m_inst;
    }

    @Override
    protected void setupGui(Container pane, int width, int height)
    {
        // Popups
        JPopupMenu midletPopup = new JPopupMenu();
        midletPopup.add(AnalyseMidletAction.getInstance(this));

        JPopupMenu packagePopup = new JPopupMenu();
        packagePopup.add(LookUpAction.getInstance(this));
        packagePopup.addSeparator();
        packagePopup.add(ReferenceCallsAction.getInstance(this));
        packagePopup.add(DalvikReferenceCallsParamAction.getInstance(this));
        packagePopup.add(DalvikReferenceCallsReturnAction.getInstance(this));

        JPopupMenu classPopup = new JPopupMenu();
        classPopup.add(LookUpAction.getInstance(this));
        classPopup.add(ExamineClassAction.getInstance(this));
        //classPopup.add(RenameAction.getInstance(this));
        classPopup.add(FindMonitorsAction.getInstance(this));
        classPopup.addSeparator();
        classPopup.add(ReferenceCallsAction.getInstance(this));
        //    classPopup.add(GenericModificationAction.getInstance(this));
        classPopup.add(DalvikReferenceCallsParamAction.getInstance(this));
        classPopup.add(DalvikReferenceCallsReturnAction.getInstance(this));

        JPopupMenu resourcePopup = new JPopupMenu();
        resourcePopup.add(LookUpAction.getInstance(this));
        //resourcePopup.add(RemoveResourceAction.getInstance(this));
        resourcePopup.add(FindMonitorsAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(ReferenceCallsAction.getInstance(this));
        resourcePopup.add(DalvikReferenceCallsParamAction.getInstance(this));
        resourcePopup.add(DalvikReferenceCallsReturnAction.getInstance(this));

        JPopupMenu midletMethodPopup = new JPopupMenu();
        midletMethodPopup.add(LookUpAction.getInstance(this));
        midletMethodPopup.add(LocalLookUpCallsAction.getInstance(this));
        midletMethodPopup.add(LocalLookUpCallersAction.getInstance(this));
        midletMethodPopup.add(OpenCallGraphAction.getInstance(this));
        midletMethodPopup.add(OpenCallerGraphAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(ShowBytecodeAction.getInstance(this));
        midletMethodPopup.add(MethodEntryAction.getInstance(this));
        midletMethodPopup.add(MethodExitAction.getInstance(this));
        //    midletMethodPopup.add(GenericModificationAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(FindNativeCallsAction.getInstance(this));
        midletMethodPopup.add(FindMonitorsAction.getInstance(this));
        //midletMethodPopup.add(RenameAction.getInstance(this));

        JPopupMenu resourceMethodPopup = new JPopupMenu();
        resourceMethodPopup.add(LookUpAction.getInstance(this));
        resourceMethodPopup.add(ShowBytecodeAction.getInstance(this));
        resourceMethodPopup.add(FindNativeCallsAction.getInstance(this));
        resourceMethodPopup.addSeparator();
        resourceMethodPopup.add(ReferenceCallsAction.getInstance(this));
        resourceMethodPopup.add(DalvikReferenceCallsParamAction.getInstance(this));
        resourceMethodPopup.add(DalvikReferenceCallsReturnAction.getInstance(this));

        JPopupMenu resourceFieldPopup = new JPopupMenu();
        resourceFieldPopup.add(DalvikFindFieldAccessAction.getInstance(this));
        resourceFieldPopup.add(DalvikMethodFieldReadAction.getInstanceOneField(this));
        resourceFieldPopup.add(DalvikMethodFieldWriteAction.getInstanceOneField(this));

        JPopupMenu invPopup = new JPopupMenu();
        invPopup.add(LookUpAction.getInstance(this));

        JPopupMenu bytecodePopup = new JPopupMenu();
        bytecodePopup.add(MethodOffsetAction.getInstance(this));
        bytecodePopup.add(MethodOffsetCurThreadAction.getInstance(this));
        bytecodePopup.add(FieldAction.getInstance(this));
        bytecodePopup.add(MethodFlowAction.getInstance(this));

        JPopupMenu methodBytecodePopup = new JPopupMenu();
        methodBytecodePopup.add(MethodFlowAction.getInstance(this));

        JPopupMenu fieldPopup = new JPopupMenu();
        fieldPopup.add(FindFieldAccessAction.getInstance(this));

        // Popup text dialog registration
        TextDialog.registerPopup(LineBuilderFormatter.BytecodeOffset.class, bytecodePopup);
        TextDialog.registerPopup(LineBuilderFormatter.Return.class, bytecodePopup);
        TextDialog.registerPopup(LineBuilderFormatter.Return.class, bytecodePopup);
        TextDialog.registerPopup(LineBuilderFormatter.Finally.class, methodBytecodePopup);
        TextDialog.registerPopup(LineBuilderFormatter.Label.class, methodBytecodePopup);
        TextDialog.registerPopup(LineBuilderFormatter.TryStart.class, methodBytecodePopup);
        TextDialog.registerPopup(LineBuilderFormatter.TryEnd.class, methodBytecodePopup);
        TextDialog.registerPopup(LineBuilderFormatter.Catch.class, methodBytecodePopup);
        TextDialog.registerPopup(MEField.class, fieldPopup);

        // Popup tree registration
        m_resourceTree = new ClassTree();
        ToolTipManager.sharedInstance().registerComponent(m_resourceTree);
        m_resourceTree.setCellRenderer(new ClassTreeRenderer(true));
        m_resourceTree.setModel(null);
        m_resourceTree.addMouseListener(new ClassTreeMouseListener(m_resourceTree));
        m_resourceTree.setRootVisible(false);
        m_resourceTree.setShowsRootHandles(true);
        m_resourceTree.registerPopup(RefContext.class, resourcePopup);
        m_resourceTree.registerPopup(RefPackage.class, packagePopup);
        m_resourceTree.registerPopup(RefClass.class, classPopup);
        m_resourceTree.registerPopup(RefMethod.class, resourceMethodPopup);
        m_resourceTree.registerPopup(RefInvokation.class, invPopup);
        m_resourceTree.registerPopup(RefField.class, resourceFieldPopup);
        m_resourceTree.registerPopup(RefFieldAccess.class, invPopup);
        m_resourceTree.registerPopup(RefFolder.class, invPopup);
        m_resourceTree.registerPopup(RefResSpec.class, invPopup);
        m_resourceTree.registerPopup(RefResReference.class, invPopup);

        m_midletTree = new ClassTree();
        ToolTipManager.sharedInstance().registerComponent(m_midletTree);
        m_midletTree.setCellRenderer(new ClassTreeRenderer(false));
        m_midletTree.setModel(null);
        m_midletTree.addMouseListener(new ClassTreeMouseListener(m_midletTree));
        m_midletTree.setRootVisible(false);
        m_midletTree.setShowsRootHandles(true);
        m_midletTree.registerPopup(RefContext.class, resourcePopup);
        m_midletTree.registerPopup(RefPackage.class, packagePopup);
        m_midletTree.registerPopup(RefClass.class, classPopup);
        m_midletTree.registerPopup(RefMethod.class, midletMethodPopup);
        m_midletTree.registerPopup(RefInvokation.class, invPopup);
        m_midletTree.registerPopup(void.class, midletPopup);

        m_resourceTree.registerApkPopup(RefContext.class, resourcePopup);
        m_resourceTree.registerApkPopup(RefPackage.class, packagePopup);
        m_resourceTree.registerApkPopup(RefClass.class, classPopup);
        m_resourceTree.registerApkPopup(RefMethod.class, resourceMethodPopup);
        m_resourceTree.registerApkPopup(RefInvokation.class, invPopup);
        m_resourceTree.registerApkPopup(RefField.class, resourceFieldPopup);
        m_resourceTree.registerApkPopup(RefFieldAccess.class, invPopup);
        m_resourceTree.registerApkPopup(RefFolder.class, invPopup);
        m_resourceTree.registerApkPopup(RefResSpec.class, invPopup);
        m_resourceTree.registerApkPopup(RefResReference.class, invPopup);
        registerApkPopups();

        m_contentTextBuilder = new TextBuilder("");

        m_contentScrollPane = new JScrollPane(m_contentTextBuilder.getTextPane());
        m_contentTextBuilder.setScrollPane(m_contentScrollPane);

        m_contentViewPane = new GraphPanel(this);

        m_toolBar = new GraphToolBar(m_contentViewPane, "Preview");

        m_splitTree = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(m_resourceTree), new JScrollPane(
                m_midletTree));

        m_splitToolBar = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_toolBar, m_contentViewPane);
        m_splitToolBar.setDividerSize(0);
        m_splitToolBar.setEnabled(false);
        m_splitToolBar.setBorder(null);

        m_splitContent = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_splitTree, m_splitToolBar);

        // Other
        pane.add(m_splitContent, BorderLayout.CENTER);

        // Set divider settings
        int divTree = width / 2;
        if (Settings.getMainFrameTreesDiv() > 0) {
            divTree = Settings.getMainFrameTreesDiv();
        }
        m_splitTree.setDividerLocation(divTree);

        int divContent = height / 2;
        if (Settings.getMainFrameContentDiv() > 0) {
            divContent = Settings.getMainFrameContentDiv();
        }
        m_splitContent.setDividerLocation(divContent);
    }

    protected void registerApkPopups() {
        // Popups
        JPopupMenu midletPopup = new JPopupMenu();
        midletPopup.add(AnalyseMidletAction.getInstance(this));

        JPopupMenu packagePopup = new JPopupMenu();
        packagePopup.add(LookUpAction.getInstanceInternal(this));
        packagePopup.add(LookUpAction.getInstance(this));

        packagePopup.add(FindMonitorsAction.getInstance(this));
        //packagePopup.add(ReferenceCallsAction.getInstance(this));

        packagePopup.addSeparator();
        packagePopup.add(DalvikMethodEntryAction.getInstance(this));
        packagePopup.add(DalvikMethodEntryParamAction.getInstance(this));
        packagePopup.add(DalvikMethodExitAction.getInstance(this));
        packagePopup.add(DalvikMethodExitValueAction.getInstance(this));
        packagePopup.addSeparator();
        packagePopup.add(DalvikMethodOffsetCurThreadAction.getInstance(this));
        packagePopup.add(DalvikMethodOffsetGCAction.getInstance(this));
        packagePopup.add(DalvikMethodOffsetPrintStackTraceAction.getInstance(this));
        packagePopup.add(DalvikMethodOffsetInstanceNewAction.getInstance(this));
        packagePopup.addSeparator();
        packagePopup.add(DalvikMethodOffsetInstanceNewInnerAction.getInstance(this));
        packagePopup.add(DalvikMethodOffsetInstanceFinalizeAction.getInstance(this));
        packagePopup.addSeparator();
        packagePopup.add(DalvikMethodFieldReadAction.getInstance(this));
        packagePopup.add(DalvikMethodFieldWriteAction.getInstance(this));
        packagePopup.addSeparator();
        packagePopup.add(DalvikMethodLocalReadAction.getInstance(this));
        packagePopup.add(DalvikMethodLocalWriteAction.getInstance(this));
        packagePopup.addSeparator();
        packagePopup.add(DalvikMethodOffsetExThrowAction.getInstance(this));
        packagePopup.add(DalvikMethodOffsetExCatchAction.getInstance(this));
        packagePopup.addSeparator();
        packagePopup.add(DalvikMethodOffsetSyncEntryAction.getInstance(this));
        packagePopup.add(DalvikMethodOffsetSyncExitAction.getInstance(this));

        JPopupMenu classPopup = new JPopupMenu();
        classPopup.add(LookUpAction.getInstanceInternal(this));
        classPopup.add(LookUpAction.getInstance(this));
        classPopup.add(ExamineClassAction.getInstance(this));
        //classPopup.add(RenameAction.getInstance(this));
        classPopup.add(FindMonitorsAction.getInstance(this));
        //classPopup.add(ReferenceCallsAction.getInstance(this));
        //classPopup.add(GenericModificationAction.getInstance(this));

        classPopup.addSeparator();
        classPopup.add(DalvikMethodEntryAction.getInstance(this));
        classPopup.add(DalvikMethodEntryParamAction.getInstance(this));
        classPopup.add(DalvikMethodExitAction.getInstance(this));
        classPopup.add(DalvikMethodExitValueAction.getInstance(this));
        classPopup.addSeparator();
        classPopup.add(DalvikMethodOffsetCurThreadAction.getInstance(this));
        classPopup.add(DalvikMethodOffsetGCAction.getInstance(this));
        classPopup.add(DalvikMethodOffsetPrintStackTraceAction.getInstance(this));
        classPopup.add(DalvikMethodOffsetInstanceNewAction.getInstance(this));
        classPopup.addSeparator();
        classPopup.add(DalvikMethodOffsetInstanceNewInnerAction.getInstanceOneClass(this));
        classPopup.add(DalvikMethodOffsetInstanceFinalizeAction.getInstanceOneClass(this));
        classPopup.addSeparator();
        classPopup.add(DalvikMethodFieldReadAction.getInstance(this));
        classPopup.add(DalvikMethodFieldWriteAction.getInstance(this));
        classPopup.addSeparator();
        classPopup.add(DalvikMethodLocalReadAction.getInstance(this));
        classPopup.add(DalvikMethodLocalWriteAction.getInstance(this));
        classPopup.addSeparator();
        classPopup.add(DalvikMethodOffsetExThrowAction.getInstance(this));
        classPopup.add(DalvikMethodOffsetExCatchAction.getInstance(this));
        classPopup.addSeparator();
        classPopup.add(DalvikMethodOffsetSyncEntryAction.getInstance(this));
        classPopup.add(DalvikMethodOffsetSyncExitAction.getInstance(this));

        JPopupMenu resourcePopup = new JPopupMenu();
        JMenu popDensity = new JMenu("Verify xml resources");
        popDensity.add(XmlVerifyResourceAction.getInstance(this, "nodpi"));
        popDensity.add(XmlVerifyResourceAction.getInstance(this, "ldpi"));
        popDensity.add(XmlVerifyResourceAction.getInstance(this, "mdpi"));
        popDensity.add(XmlVerifyResourceAction.getInstance(this, "hdpi"));
        popDensity.add(XmlVerifyResourceAction.getInstance(this, "xhdpi"));

        resourcePopup.add(LookUpAction.getInstance(this));
        resourcePopup.add(FindMonitorsAction.getInstance(this));
        //resourcePopup.add(RemoveResourceAction.getInstance(this));
        //resourcePopup.add(ReferenceCallsAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(ApkInstallAndStartAction.getInstance(this));
        resourcePopup.add(ApkResignInstallAndStartAction.getInstance(this));
        resourcePopup.add(ApkResignAction.getInstance(this));
        resourcePopup.add(ApkUninstallAction.getInstance(this));
        resourcePopup.add(ApkExamineOdexDependencyAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(XmlResourceAction.getInstance(this));
        resourcePopup.add(XmlResourceIdAction.getInstance(this));
        resourcePopup.add(XmlUnusedResourceAction.getInstance(this));
        resourcePopup.add(XmlUnusedFileAction.getInstance(this));
        resourcePopup.add(XmlSystemReferenceAction.getInstance(this));
        resourcePopup.add(XmlFindLabelAction.getInstance(this));
        resourcePopup.add(popDensity);
        resourcePopup.addSeparator();
        resourcePopup.add(DalvikMethodEntryAction.getInstance(this));
        resourcePopup.add(DalvikMethodEntryParamAction.getInstance(this));
        resourcePopup.add(DalvikMethodExitAction.getInstance(this));
        resourcePopup.add(DalvikMethodExitValueAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(DalvikMethodOffsetCurThreadAction.getInstance(this));
        resourcePopup.add(DalvikMethodOffsetGCAction.getInstance(this));
        resourcePopup.add(DalvikMethodOffsetPrintStackTraceAction.getInstance(this));
        resourcePopup.add(DalvikMethodOffsetInstanceNewAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(DalvikMethodOffsetInstanceNewInnerAction.getInstance(this));
        resourcePopup.add(DalvikMethodOffsetInstanceFinalizeAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(DalvikMethodFieldReadAction.getInstance(this));
        resourcePopup.add(DalvikMethodFieldWriteAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(DalvikMethodLocalReadAction.getInstance(this));
        resourcePopup.add(DalvikMethodLocalWriteAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(DalvikMethodOffsetExThrowAction.getInstance(this));
        resourcePopup.add(DalvikMethodOffsetExCatchAction.getInstance(this));
        resourcePopup.addSeparator();
        resourcePopup.add(DalvikMethodOffsetSyncEntryAction.getInstance(this));
        resourcePopup.add(DalvikMethodOffsetSyncExitAction.getInstance(this));

        JPopupMenu midletMethodPopup = new JPopupMenu();
        midletMethodPopup.add(LookUpAction.getInstance(this));
        midletMethodPopup.add(FindMonitorsAction.getInstance(this));
        midletMethodPopup.add(FindNativeCallsAction.getInstance(this));
        midletMethodPopup.add(LocalLookUpCallsAction.getInstance(this));
        midletMethodPopup.add(LocalLookUpCallersAction.getInstance(this));
        midletMethodPopup.add(OpenCallGraphAction.getInstance(this));
        midletMethodPopup.add(OpenCallerGraphAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(ShowBytecodeAction.getInstance(this));

        //midletMethodPopup.add(GenericModificationAction.getInstance(this));
        midletMethodPopup.addSeparator();
        //midletMethodPopup.add(RenameAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodEntryAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodEntryParamAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodExitAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodExitValueAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(DalvikMethodOffsetCurThreadAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodOffsetGCAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodOffsetPrintStackTraceAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodOffsetInstanceNewAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(DalvikMethodFieldReadAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodFieldWriteAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(DalvikMethodLocalReadAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodLocalWriteAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(DalvikMethodOffsetExThrowAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodOffsetExCatchAction.getInstance(this));
        midletMethodPopup.addSeparator();
        midletMethodPopup.add(DalvikMethodOffsetSyncEntryAction.getInstance(this));
        midletMethodPopup.add(DalvikMethodOffsetSyncExitAction.getInstance(this));

        JPopupMenu invPopup = new JPopupMenu();
        invPopup.add(LookUpAction.getInstanceInternal(this));
        invPopup.add(LookUpAction.getInstance(this));

        JPopupMenu bytecodePopup = new JPopupMenu();
        bytecodePopup.add(DalvikMethodOffsetCustomLogAction.getInstance(this));
        bytecodePopup.add(DalvikMethodOffsetCurThreadAction.getInstanceOffset(this));
        bytecodePopup.add(DalvikMethodOffsetGCAction.getInstanceOffset(this));
        bytecodePopup.add(DalvikMethodOffsetPrintStackTraceAction.getInstanceOffset(this));

        JPopupMenu fieldPopup = new JPopupMenu();
        fieldPopup.add(LookUpAction.getInstanceInternal(this));
        fieldPopup.addSeparator();
        fieldPopup.add(DalvikFindFieldAccessAction.getInstance(this));
        fieldPopup.add(DalvikMethodFieldReadAction.getInstanceOneField(this));
        fieldPopup.add(DalvikMethodFieldWriteAction.getInstanceOneField(this));

        // Popup text dialog registration
        TextDialog.registerPopup(DalvikByteCodeLineBuilder.DalvikBytecodeOffset.class, bytecodePopup);
        TextDialog.registerPopup(DexField.class, fieldPopup);

        m_midletTree.registerApkPopup(RefContext.class, resourcePopup);
        m_midletTree.registerApkPopup(RefPackage.class, packagePopup);
        m_midletTree.registerApkPopup(RefClass.class, classPopup);
        m_midletTree.registerApkPopup(RefMethod.class, midletMethodPopup);
        m_midletTree.registerApkPopup(RefInvokation.class, invPopup);
        m_midletTree.registerApkPopup(void.class, midletPopup);
        m_midletTree.registerApkPopup(RefField.class, fieldPopup);
        m_midletTree.registerApkPopup(RefFieldAccess.class, invPopup);
        m_midletTree.registerApkPopup(RefFolder.class, invPopup);
        m_midletTree.registerApkPopup(RefResSpec.class, invPopup);
        m_midletTree.registerApkPopup(RefResReference.class, invPopup);
        m_midletTree.registerApkPopup(RefXml.class, invPopup);
        m_midletTree.registerApkPopup(RefAndroidManifest.class, invPopup);
    }

    @Override
    protected JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        // File menu
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menu.add(AnalyseAction.getInstance(this));
        menu.add(new JSeparator());
        menu.add(SetPathsAction.getInstance(this));
        menu.add(SettingsAction.getInstance(this));
        menu.add(new JSeparator());
        menu.add(ExitAction.getInstance());

        // View menu
        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menu);

        m_contextMenuItems.add(menu.add(ExamineClassAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(ShowBytecodeAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(OpenCallGraphAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(OpenCallerGraphAction.getInstance(this)));

        // Lookup menu
        menu = new JMenu("Lookup");
        menu.setMnemonic(KeyEvent.VK_L);
        menuBar.add(menu);

        m_apkMenuItems.add(menu.add(XmlFindLabelAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(LookUpAction.getInstanceInternal(this)));
        m_contextMenuItems.add(menu.add(LookUpAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(LocalLookUpCallsAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(LocalLookUpCallersAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(FindMonitorsAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(FindNativeCallsAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikFindFieldAccessAction.getInstance(this)));

        // Bytecode mod menu
        menu = new JMenu("Modifications");
        menu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(menu);

        menu.add(UnregisterAction.getInstance(this));
        menu.add(PerformAction.getInstance(this));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodEntryAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodEntryParamAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodExitAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodExitValueAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetCurThreadAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetGCAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetPrintStackTraceAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetInstanceNewAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetInstanceNewInnerAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetInstanceFinalizeAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetInstanceNewInnerAction.getInstanceOneClass(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetInstanceFinalizeAction.getInstanceOneClass(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodFieldReadAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodFieldWriteAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodFieldReadAction.getInstanceOneField(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodFieldWriteAction.getInstanceOneField(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodLocalReadAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodLocalWriteAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetExThrowAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetExCatchAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetSyncEntryAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetSyncExitAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetCustomLogAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetCurThreadAction.getInstanceOffset(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetGCAction.getInstanceOffset(this)));
        m_contextMenuItems.add(menu.add(DalvikMethodOffsetPrintStackTraceAction.getInstanceOffset(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(ReferenceCallsAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikReferenceCallsParamAction.getInstance(this)));
        m_contextMenuItems.add(menu.add(DalvikReferenceCallsReturnAction.getInstance(this)));
        menu.addSeparator();
        m_contextMenuItems.add(menu.add(GraphMarkAction.getInstance(this)));

        // Resource menu
        menu = new JMenu("Resource");
        menu.setMnemonic(KeyEvent.VK_R);
        menuBar.add(menu);

        JMenu popDensity = new JMenu("Verify xml resources");
        m_apkMenuItems.add(popDensity.add(XmlVerifyResourceAction.getInstance(this, "nodpi")));
        m_apkMenuItems.add(popDensity.add(XmlVerifyResourceAction.getInstance(this, "ldpi")));
        m_apkMenuItems.add(popDensity.add(XmlVerifyResourceAction.getInstance(this, "mdpi")));
        m_apkMenuItems.add(popDensity.add(XmlVerifyResourceAction.getInstance(this, "hdpi")));
        m_apkMenuItems.add(popDensity.add(XmlVerifyResourceAction.getInstance(this, "xhdpi")));

        m_apkMenuItems.add(menu.add(XmlResourceAction.getInstance(this)));
        m_apkMenuItems.add(menu.add(XmlResourceIdAction.getInstance(this)));
        menu.addSeparator();
        m_apkMenuItems.add(menu.add(XmlUnusedResourceAction.getInstance(this)));
        m_apkMenuItems.add(menu.add(XmlUnusedFileAction.getInstance(this)));
        m_apkMenuItems.add(menu.add(XmlSystemReferenceAction.getInstance(this)));
        menu.addSeparator();
        m_apkMenuItems.add(menu.add(popDensity));

        // Device menu
        menu = new JMenu("Device");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);

        m_apkMenuItems.add(menu.add(ApkInstallAndStartAction.getInstance(this)));
        m_apkMenuItems.add(menu.add(ApkResignInstallAndStartAction.getInstance(this)));
        m_apkMenuItems.add(menu.add(ApkResignAction.getInstance(this)));
        m_apkMenuItems.add(menu.add(ApkUninstallAction.getInstance(this)));
        menu.addSeparator();
        m_apkMenuItems.add(menu.add(ApkExamineOdexDependencyAction.getInstance(this)));
        menu.addSeparator();
        //menu.add(EjavaOpenAction.getInstance(this));
        menu.add(AdbOpenAction.getInstance(this));

        // About menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menu.add(AboutAction.getInstance(this));

        for (JMenuItem item : m_contextMenuItems) {
            if (!(item instanceof JMenu)) {
                item.setEnabled(false);
            }
        }
        for (JMenuItem item : m_apkMenuItems) {
            if (!(item instanceof JMenu)) {
                item.setEnabled(false);
            }
        }
        return menuBar;
    }

    @Override
    protected JPanel createButtonBar() {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        MAButton butt;

        // Analyse
        butt = new MAButton(AnalyseAction.getInstance(this), FlagIcon.createImageIcon("b_analyse.png"), null,
                "Load midlets and resolve against classpaths");
        p.add(butt);
        m_buttons.add(butt);

        // Resolve
        Class<?>[] luRelevance = { RefContext.class, RefPackage.class, RefInvokation.class, RefMethod.class, RefClass.class };
        butt = new MAButton(LookUpAction.getInstance(this), FlagIcon.createImageIcon("b_lookup.png"), luRelevance,
                "Find references to selected element in opposite tree");
        p.add(butt);
        m_buttons.add(butt);

        // Examine = {Examine class, View bytecodes}
        Class<?>[] examineRelevance = { RefMethod.class, RefClass.class };
        butt = new MAButton(new AbstractAction() {
            private static final long serialVersionUID = -723730167439372591L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object mRef = Selection.getSelectedObject();
                if (mRef instanceof RefClass)
                {
                    ExamineClassAction.getInstance(MainFrame.this).actionPerformed(e);
                }
                else if (mRef instanceof RefMethod)
                {
                    ShowBytecodeAction.getInstance(MainFrame.this).actionPerformed(e);
                }
            }
        }, FlagIcon.createImageIcon("b_examine.png"), examineRelevance, "Show detailed information, class definition or bytecodes");
        p.add(butt);
        m_buttons.add(butt);

        p.add(new JLabel("     ")); // beautiful!!

        // Add printout to method entry
        Class<?>[] bytecodeModRelevance = { RefMethod.class, RefClass.class, RefPackage.class, RefContext.class };
        butt = new MAButton(new AbstractAction() {
            private static final long serialVersionUID = 2505914358357543450L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                RefContext ref = Selection.getRefContextOfSeletedObject();
                if (ref != null) {
                    if (ref.getContext() instanceof ApkClassContext) {
                        DalvikMethodEntryAction.getInstance(MainFrame.this).actionPerformed(e);
                    } else {
                        MethodEntryAction.getInstance(MainFrame.this).actionPerformed(e);
                    }
                }
            }
        }, FlagIcon.createImageIcon("b_bmpome.png"), bytecodeModRelevance, "Bytecode modify, add printout at method entry");
        p.add(butt);
        m_buttons.add(butt);

        // Add printout to method exit
        butt = new MAButton(new AbstractAction() {
            private static final long serialVersionUID = -3216711373704635316L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                RefContext ref = Selection.getRefContextOfSeletedObject();
                if (ref != null) {
                    if (ref.getContext() instanceof ApkClassContext) {
                        DalvikMethodExitAction.getInstance(MainFrame.this).actionPerformed(e);
                    } else {
                        MethodExitAction.getInstance(MainFrame.this).actionPerformed(e);
                    }
                }
            }
        }, FlagIcon.createImageIcon("b_bmpomx.png"), bytecodeModRelevance, "Bytecode modify, add printout at method exits");
        p.add(butt);
        m_buttons.add(butt);

        // Add printout to exceptionhandler
        /*butt = new MAButton(new AbstractAction() {
            private static final long serialVersionUID = 7363575542575354414L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                RefContext ref = Selection.getRefContextOfSeletedObject();
                if (ref != null) {
                    if (ref.getContext() instanceof ApkClassContext) {
                        DalvikMethodOffsetExCatchAction.getInstance(MainFrame.this).actionPerformed(e);
                    } else {
                        ExceptionHandlerAction.getInstance(MainFrame.this).actionPerformed(e);
                    }
                }
            }
        }, FlagIcon.createImageIcon("b_bmpexhand.png"), bytecodeModRelevance, "Bytecode modify, add printout in exception handlers");
        p.add(butt);
        m_buttons.add(butt);*/

        p.add(new JLabel("     ")); // beautiful!!

        // Remove registered bytecode modifications
        butt = new MAButton(UnregisterAction.getInstance(this), FlagIcon.createImageIcon("b_bmunreg.png"), null, "Unregister bytecode modifications");
        p.add(butt);
        m_buttons.add(butt);

        // Save byte code modifications
        butt = new MAButton(PerformAction.getInstance(this), FlagIcon.createImageIcon("b_bmperform.png"), null, "Performs all registered bytecode modifications");
        p.add(butt);
        m_buttons.add(butt);

        for (int i = 0; i < m_buttons.size(); i++)
        {
            butt = m_buttons.get(i);
            butt.activate(null);
        }

        return p;
    }

    @Override
    protected void saveSettingsOnClose() throws IOException
    {
        Settings.setMainFrameTreesDiv(m_splitTree.getDividerLocation());
        Settings.setMainFrameContentDiv(m_splitContent.getDividerLocation());
    }

    class ClassTreeMouseListener extends MouseAdapter
    {
        protected ClassTree m_tree;

        public ClassTreeMouseListener(ClassTree tree)
        {
            m_tree = tree;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            m_selectedTree = null;
            if (m_popup != null)
            {
                m_popup.setVisible(false);
                m_popup = null;
            }
            int selRow = m_tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = m_tree.getPathForLocation(e.getX(), e.getY());
            DefaultMutableTreeNode node = null;
            if (selRow > -1)
            {
                node =
                        ((DefaultMutableTreeNode) selPath.getLastPathComponent());
            }
            if (node == null) {
                return;
            }
            Selection.setSelectedObject(m_tree, node.getUserObject());
            m_selectedTree = m_tree;
            m_tree.setSelected(true);
            getOppositeSelectedTree().setSelected(false);
            if (SwingUtilities.isLeftMouseButton(e))
            {
                Object o = node != null ? node.getUserObject() : null;
                showContent(o);
                if ((e.getModifiers() & MouseEvent.ALT_MASK) != 0)
                {
                    markUsedResources(node);
                }
            }
            m_popup = m_tree.getPopup(node);
            if (SwingUtilities.isRightMouseButton(e))
            {
                if (m_popup != null)
                {
                    m_tree.setSelectionPath(selPath);
                    m_popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            for (int i = 0; i < m_buttons.size(); i++)
            {
                MAButton butt = m_buttons.get(i);
                butt.activate(node.getUserObject().getClass());
            }

            for (int i = 0; i < m_contextMenuItems.size(); i++) {
                m_contextMenuItems.get(i).setEnabled(false);
            }

            if (m_popup != null)
            {
                int itemCount = m_popup.getComponentCount();
                for (int j = 0; j < itemCount; j++) {
                    if (m_popup.getComponent(j) instanceof JMenuItem) {
                        for (int i = 0; i < m_contextMenuItems.size(); i++) {
                            if (m_contextMenuItems.get(i).getAction() == ((JMenuItem) m_popup.getComponent(j)).getAction()) {
                                m_contextMenuItems.get(i).setEnabled(true);
                                break;
                            }
                        }
                    }
                }
            }
            boolean apkActionEnabled = false;
            RefContext ref = Selection.getRefContextOfSeletedObject();
            if (ref != null
                    && ref.getContext() instanceof ApkClassContext
                    && ref.getContext().isMidlet()) {
                apkActionEnabled = true;
            }
            for (JMenuItem item : m_apkMenuItems) {
                item.setEnabled(apkActionEnabled);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void markUsedResources(DefaultMutableTreeNode node)
    {
        if (getSelectedTree() == m_midletTree)
        {
            Object o = node != null ? node.getUserObject() : null;
            if (o != null
                    && o instanceof Reference)
            {
                LookUpAction lua = LookUpAction.getInstance(this);
                lua.setRunning(true);
                Reference[] invs = lua.getInvokations((Reference) o, false);
                ClassTree tree = getOppositeSelectedTree();
                lua.selectPathsInTree(tree, invs, RefPackage.class, RefFolder.class);
                lua.setRunning(false);
            }
        }
    }

    private void switchContentPanel(JComponent jp) {
        if (m_splitToolBar.getRightComponent().equals(jp)) {
            return;
        }

        m_splitToolBar.setRightComponent(jp);
        m_toolBar.setZoom(jp == m_contentViewPane);
    }

    public void showContent(Object o) {
        if (o == null) {
            return;
        }

        if (o instanceof RefContext) {
            RefContext ctx = (RefContext) o;
            m_toolBar.setText(ctx.getName(), ClassTreeRenderer.ICON_APK);
            m_contentViewPane.loadContext(ctx);
            switchContentPanel(m_contentViewPane);
        } else if (o instanceof RefPackage) {
            RefPackage pack = (RefPackage) o;
            m_toolBar.setText(pack.getName(), ClassTreeRenderer.ICON_PACKAGE);
            m_contentViewPane.loadContext(pack);
            switchContentPanel(m_contentViewPane);
        } else if (o instanceof RefClass) {
            RefClass refClass = (RefClass) o;
            MEClass c = refClass.getMEClass();
            m_toolBar.setText(c.getName(), ClassTreeRenderer.ICON_CLASS);
            m_contentViewPane.loadContext(refClass);
            switchContentPanel(m_contentViewPane);
        } else if (o instanceof RefMethod) {
            RefMethod refMethod = (RefMethod) o;
            int pc = Selection.getPc();
            Instruction instruction = Selection.getDalvikInstruction();
            showBytecodeContent(refMethod.getMethod(), instruction, pc);
            switchContentPanel(m_contentScrollPane);
        } else if (o instanceof RefAndroidManifest) {
            RefAndroidManifest refManifest = (RefAndroidManifest) o;
            XmlLineFormatter xmllb = refManifest.getContext().getXmlParser().getXmlLineBuilder(XmlParser.MANIFEST, -1, -1, false);
            LineBuilder lb = null;
            if (xmllb != null
                    && (lb = xmllb.getLineBuilder()) != null) {

                m_contentTextBuilder.init(lb);
                m_toolBar.setText(XmlParser.MANIFEST, ClassTreeRenderer.ICON_ANDROID);
                switchContentPanel(m_contentScrollPane);
            }
        } else if (o instanceof RefResSpec) {
            RefResSpec refSpec = (RefResSpec) o;
            ApkClassContext apkCtx = refSpec.getRefContext();
            if (!(refSpec.getDexSpec().getResSpec() instanceof UnknownResSpec)) {
                LineBuilder lb = apkCtx.getXmlParser().getResourceChecker().showSpecDetail(refSpec.getDexSpec().getResSpec(), this, refSpec.getRefContext());
                if (lb != null) {
                    m_contentTextBuilder.init(lb);
                    m_toolBar.setText(refSpec.getDexSpec().getResSpec().getFullName(), ClassTreeRenderer.ICON_ANDROID);
                    switchContentPanel(m_contentScrollPane);
                }
            }
        } else if (o instanceof RefInvokation) {
            RefInvokation refInvo = (RefInvokation) o;
            if (refInvo.isLocal() == false
                    && refInvo.getContext().isMidlet() == false) {
                refInvo = refInvo.getOppositeInvokation();
            }
            showBytecodeContent(refInvo.getMethod(), refInvo.getInvokation().offsetIns, refInvo.getInvokation().bytecodePc);
            switchContentPanel(m_contentScrollPane);
        } else if (o instanceof RefFieldAccess) {
            DexReferenceCache.FieldAccess access = ((RefFieldAccess) o).getAccess();
            showBytecodeContent(access.method, access.instruction, access.pc);
            switchContentPanel(m_contentScrollPane);
        } else if (o instanceof RefResReference) {
            RefResReference refResReference = (RefResReference) o;
            ApkClassContext apkCtx = refResReference.getRefContext();
            if (refResReference.isRes()) {
                LineBuilder lb = apkCtx.getXmlParser().getResourceChecker().showSpecDetail((ResResSpec) refResReference.getReferred(), this, refResReference.getRefContext());
                if (lb != null) {
                    m_contentTextBuilder.init(lb);
                    m_toolBar.setText(((ResResSpec) refResReference.getReferred()).getFullName(), ClassTreeRenderer.ICON_ANDROID);
                    switchContentPanel(m_contentScrollPane);
                }
            } else if (refResReference.isXml()) {
                XmlLine xmlLine = (XmlLine) refResReference.getReferred();
                XmlLineFormatter xmllb = apkCtx.getXmlParser().getXmlLineBuilder(xmlLine.entry.getName(), xmlLine.line, xmlLine.id, false);
                LineBuilder lb = null;
                if (xmllb != null
                        && (lb = xmllb.getLineBuilder()) != null) {

                    m_contentTextBuilder.init(lb);
                    if (xmllb.getCaret() != -1) {
                        m_contentTextBuilder.setCaretPosition(xmllb.getCaret());
                    }
                    m_toolBar.setText(xmlLine.entry.getName(), ClassTreeRenderer.ICON_XML);
                    switchContentPanel(m_contentScrollPane);
                }
            } else if (refResReference.isCode()) {
                DexReferenceCache.LoadConstRes dexSpec = (DexReferenceCache.LoadConstRes) refResReference.getReferred();
                showBytecodeContent(dexSpec.method, dexSpec.instruction, dexSpec.pc);
                switchContentPanel(m_contentScrollPane);
            }

        } else if (o instanceof RefXml) {
            RefXml ref = (RefXml) o;
            ApkClassContext apkCtx = ref.getRefContext();
            XmlLineFormatter xmllb = apkCtx.getXmlParser().getXmlLineBuilder(ref.getXml().getName(), -1, -1, false);
            LineBuilder lb = null;
            if (xmllb != null
                    && (lb = xmllb.getLineBuilder()) != null) {
                m_contentTextBuilder.init(lb);
                m_toolBar.setText(ref.getXml().getName(), ClassTreeRenderer.ICON_XML);
                switchContentPanel(m_contentScrollPane);
            }
        }
    }

    private void showBytecodeContent(MEMethod method, Instruction instruction, int pc) {
        final String PREFIX = "        ";

        LineBuilder result = null;

        if (method == null) {
            return;
        }

        m_contentTextBuilder.setOwnerData(method);

        MEClass clazz = method.getMEClass();

        String methodName = method.getName();
        if (methodName.equals("<init>")) {
            methodName = "Constructor";
        } else if (methodName.equals("<cinit>")) {
            methodName = "Static constructor";
        }
        m_toolBar.setText(methodName + " method of " + clazz.getName(), ClassTreeRenderer.ICON_JAVAFILE);

        if (method instanceof UnknownMethod) {
            LineBuilder classLines = new LineBuilder();
            m_contentTextBuilder.init(classLines);
            return;
        }

        LineBuilder classLines = LineBuilderFormatter.makeOutline(clazz);
        classLines.blendLines(0xffffff, 50);
        int lineNbr = classLines.getLine(method);
        try {
            LineBuilder codeLines;
            if (method instanceof DexMethod) {
                codeLines = DalvikByteCodeLineBuilder.getByteCodeAssembler((DexMethod) method, PREFIX);
            } else {
                codeLines = LineBuilderFormatter.getByteCodeAssembler(method, PREFIX);
            }
            if (lineNbr >= 0) {
                classLines.insertAfter(lineNbr, codeLines);
                classLines.insertLineAfter(lineNbr + codeLines.lineCount());
                result = classLines;
            } else {
                result = codeLines;
            }
        } catch (CorruptBytecodeException cbe) {
            return;
        }
        if (result != null) {
            m_contentTextBuilder.init(result);

            if (instruction == null
                    && pc < 0
                    && method instanceof DexMethod
                    && !method.isAbstract()) {
                DexMethod dexMethod = (DexMethod) method;
                instruction = dexMethod.getInstructionAtCodeAddress(0);
            }

            if (instruction != null) {
                LineBuilder lb = m_contentTextBuilder.getLineBuilder();
                for (int i = 0; i < lb.lineCount(); i++) {
                    Object ref = lb.getReference(i);
                    if (ref instanceof DalvikByteCodeLineBuilder.DalvikBytecodeOffset
                            && ((DalvikByteCodeLineBuilder.DalvikBytecodeOffset) ref).instruction == instruction) {
                        lb.gotoLine(i);
                        m_contentTextBuilder.findNext(lb.currentLineString());
                    }
                }
            } else if (pc >= 0) {
                int line = -1;
                LineBuilder lb = m_contentTextBuilder.getLineBuilder();
                for (int i = 0; i < lb.lineCount(); i++) {
                    Object ref = lb.getReference(i);
                    if (ref instanceof LineBuilderFormatter.BytecodeOffset) {
                        line = i;
                        break;
                    } else if (ref instanceof DalvikByteCodeLineBuilder.DalvikBytecodeOffset) {
                        line = i;
                        break;
                    }
                }
                if (line >= 0) {
                    m_contentTextBuilder.findNext(PREFIX + Integer.toHexString(pc) + ' '); // TODO fix up this sordid stuff
                }
            }

        }
    }
}
