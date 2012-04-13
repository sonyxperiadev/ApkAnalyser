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

package analyser.gui.actions;

import gui.actions.AbstractCanceableAction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEField;
import mereflect.MEMethod;
import analyser.Analyser;
import analyser.gui.MainFrame;
import analyser.logic.AbstractReference;
import analyser.logic.Reference;
import analyser.logic.Resolver;
import analyser.logic.ResolverListener;
import andreflect.ApkClassContext;
import andreflect.DexReferenceCache;
import brut.androlib.res.data.ResResSpec;

public abstract class AbstractAnalyseAction extends AbstractCanceableAction implements
        ResolverListener
{
    private static final long serialVersionUID = 7269117050588573039L;

    protected AbstractAnalyseAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    public void buildTrees(Resolver r)
    {
        MainFrame.getInstance().setBottomInfo("Building trees");

        Collection<Reference> cRR = r.getReferenceResources();
        DefaultMutableTreeNode refRoot = new DefaultMutableTreeNode("Reference");
        populatex(cRR, refRoot);
        MainFrame.getInstance().getResourceTree().setModel(
                new DefaultTreeModel(refRoot));

        Collection<Reference> cMR = r.getMidletResources();
        DefaultMutableTreeNode midletRoot = new DefaultMutableTreeNode("MIDlets");
        populatex(cMR, midletRoot);
        MainFrame.getInstance().getMidletTree().setModel(
                new DefaultTreeModel(midletRoot));
    }

    protected void populatex(Collection<Reference> cR, DefaultMutableTreeNode root) {
        List<AbstractReference> list = Arrays.asList(cR.toArray(new AbstractReference[0]));
        Collections.sort(list);
        Iterator<AbstractReference> iR = list.iterator();
        while (iR.hasNext() && isRunning()) {
            AbstractReference abRef = iR.next();
            DefaultMutableTreeNode refNode = new DefaultMutableTreeNode(abRef);
            root.add(refNode);
            Iterator<Reference> i = abRef.getChildren().iterator();
            if (i != null
                    && i.hasNext()) {
                populatex(abRef.getChildren(), refNode);
            }
        }
    }

    @Override
    public void resolving(int midlets, int numMidlets, int classes,
            int numClasses, int methods, int numMethods, MEClassContext ctx,
            MEClass clazz, MEMethod method)
    {
        double midletP = 1.0 / numMidlets;
        double classP = 1.0 / numClasses;
        double methodP = 1.0 / numMethods;
        double p = midletP * (midlets + classP * (classes + methodP * methods));
        if (ctx instanceof ApkClassContext) {
            p *= 15;
        } else {
            p *= 100;
        }
        getMainFrame().actionReportWork(this, (int) p);
        getMainFrame().setBottomInfo(
                "Resolving method " + Analyser.getContextName(ctx) + ": "
                        + clazz.getName() + "." + method.getName());
    }

    @Override
    public void resolvingField(int midlets, int numMidlets, int classes, int numClasses, int fields, int numFields,
            ApkClassContext ctx, MEClass clazz, MEField field) {
        double midletP = 1.0 / numMidlets;
        double classP = 1.0 / numClasses;
        double fieldP = 1.0 / numFields;
        double p = midletP * (midlets + classP * (classes + fieldP * fields));
        p *= 25;
        getMainFrame().actionReportWork(this, ((int) p) + 15);
        getMainFrame().setBottomInfo(
                "Resolving field " + Analyser.getContextName(ctx) + ": "
                        + clazz.getName() + "." + field.getName());
    }

    @Override
    public void resolvingFieldAccess(int midlet, int midlets, int access, int accesses, ApkClassContext ctx, DexReferenceCache.FieldAccess fieldAccess) {
        double midletP = 1.0 / midlets;
        double accessP = 1.0 / accesses;
        double p = midletP * (midlet + accessP * access);
        p *= 20;
        getMainFrame().actionReportWork(this, ((int) p) + 40);
        getMainFrame().setBottomInfo(
                "Resolving field access " + Analyser.getContextName(ctx) + ": "
                        + fieldAccess.fieldIdItem.getFieldName().getStringValue());

    }

    @Override
    public void resolvingResource(int midlets, int numMidlets, int resources, int numResources, ApkClassContext ctx, ResResSpec spec) {
        double midletP = 1.0 / numMidlets;
        double resourceP = 1.0 / numResources;
        double p = midletP * (midlets + resourceP * resources);
        p *= 35; //left 5% for rendering
        getMainFrame().actionReportWork(this, ((int) p) + 60);

        if (resources < numResources / 2) {
            getMainFrame().setBottomInfo(
                    "Resolving resource " + Analyser.getContextName(ctx) + ": "
                            + spec.getName());
        } else {
            getMainFrame().setBottomInfo(
                    "Resolving resource reference" + Analyser.getContextName(ctx) + ": "
                            + spec.getName());
        }
    }

    @Override
    public void resolved()
    {
    }

    @Override
    public void handleThrowable(Throwable t)
    {
        t.printStackTrace();
        getMainFrame().showError("Error during analyse", t);
        MainFrame.getInstance().initBottomInfo();
    }
}