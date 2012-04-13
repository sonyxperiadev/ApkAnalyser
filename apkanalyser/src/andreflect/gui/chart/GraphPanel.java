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

package andreflect.gui.chart;

import gui.AbstractMainFrame;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import mereflect.CollaborateClassContext;
import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.UnknownClass;
import mereflect.UnknownContext;
import mereflect.UnknownResContext;
import analyser.gui.ClassTreeRenderer;
import analyser.gui.FlagIcon;
import analyser.gui.MainFrame;
import analyser.logic.InvSnooper;
import analyser.logic.RefClass;
import analyser.logic.RefContext;
import analyser.logic.RefFieldAccess;
import analyser.logic.RefInvokation;
import analyser.logic.RefMethod;
import analyser.logic.RefPackage;
import analyser.logic.Reference;
import analyser.logic.ReferredReference;
import andreflect.Util;
import andreflect.gui.chart.shape.ClassShape;
import andreflect.gui.chart.shape.FolderShape;
import andreflect.gui.chart.shape.PackageShape;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;

public class GraphPanel extends JPanel
{
    public static final String SHAPE_PACKAGE = "package_shapestyle";
    public static final String SHAPE_FOLDER = "folder_shapestyle";
    public static final String SHAPE_CLASS = "class_shapestyle";

    public static final String STYLE_PACKAGE = "package";
    public static final String STYLE_FOLDER = "folder";
    public static final String STYLE_CLASS = "class";

    public static final int CHAR_SPACING = 3;
    public static final int LINE_SPACING = 6;

    public static final ImageIcon FOLDING_ICON = mxGraphComponent.DEFAULT_COLLAPSED_ICON;

    protected RefContext mCurrentRefContext = null;
    protected Reference mCurrentReference = null;

    private static final long serialVersionUID = -6561623072112577140L;

    protected CustomGraphComponent mGraphComponent;
    protected CustomGraph mGraph;
    protected mxIGraphModel mModel;

    protected mxRubberband rubberband;

    static {
        mxGraphics2DCanvas.putShape(SHAPE_PACKAGE, new PackageShape());
        mxGraphics2DCanvas.putShape(SHAPE_FOLDER, new FolderShape());
        mxGraphics2DCanvas.putShape(SHAPE_CLASS, new ClassShape());
    }

    private void removeAll(Object cell) {
        if (cell == null) {
            cell = mGraph.getDefaultParent();
        }

        Object[] children = mGraph.getChildVertices(cell);
        for (Object child : children) {
            removeAll(child);
            mGraph.removeCells(new Object[] { child });
        }
    }

    private boolean isInnerClass(int index, String name, RefClass clazz) {
        HashMap<RefClass, TreeSet<RefClass>> classMap = mGraphComponent.getGraph().getClassMap();
        if (index != -1) {
            Iterator<RefClass> i = classMap.keySet().iterator();
            while (i.hasNext()) {
                RefClass refc = i.next();
                if (refc.getParent() == clazz.getParent() //in same package
                        && refc.getName().equals(name.substring(0, index))) {
                    TreeSet<RefClass> set = classMap.get(refc);
                    if (set == null) {
                        set = new TreeSet<RefClass>();
                        classMap.put(refc, set);
                    }
                    set.add(clazz);
                    //System.out.println(name + " inner of " +  refc.getName());
                    return true;
                }
            }
            return isInnerClass(name.indexOf('$', index + 1), name, clazz);
        } else {
            classMap.put(clazz, null);
            return false;
        }
    }

    public void refresh() {
        mModel.beginUpdate();
        mGraph.clearSelection();
        generateVertex(mCurrentRefContext);
        if (!(mCurrentReference instanceof RefContext)) {
            renderDependecy(mCurrentReference);
        }
        mModel.endUpdate();
    }

    public void loadContext(Reference ref) {
        Reference refCtx = ref;

        if (mCurrentReference == ref) {
            return;
        }

        mModel.beginUpdate();
        mGraph.clearSelection();

        while (!(refCtx instanceof RefContext)) {
            refCtx = refCtx.getParent();
        }

        if (((RefContext) refCtx).getContext().isMidlet() == true)
        {
            if (mCurrentRefContext != refCtx)
            {
                generateVertex((RefContext) refCtx);
            }
        }

        renderDependecy(ref);
        mModel.endUpdate();
    }

    private void generateVertex(RefContext refCtx) {
        removeAll(null);
        mCurrentRefContext = refCtx;
        ArrayList<Reference> refPackages = new ArrayList<Reference>();
        traverse(refCtx, refPackages, RefPackage.class);

        insertPacakge(refPackages, true);

        Collection<Reference> refResourceCtxes = MainFrame.getInstance().getResolver().getReferenceResources();
        for (Reference refResourceCtx : refResourceCtxes) {
            if (refResourceCtx.getReferred() instanceof UnknownContext
                    || refResourceCtx.getReferred() instanceof UnknownResContext) {
                //ignore unknown reference context
                continue;
            }
            ArrayList<Reference> refResourcePackages = new ArrayList<Reference>();
            traverse(refResourceCtx, refResourcePackages, RefPackage.class);

            insertPacakge(refResourcePackages, false);
        }

        removeUnnessaryPackage(null);

        mGraph.doLayoutFirst();
    }

    private void renderDependecy(Reference ref) {
        mCurrentReference = ref;

        mGraph.cellsFolded(new Object[] { mGraph.getDefaultParent() }, true, true);

        //class list of dependency
        //Object could be MEClass for inherit, MEMethod for invokation, MEField for access
        HashMap<MEClass, ArrayList<Object>> dep = new HashMap<MEClass, ArrayList<Object>>();

        if (!(ref instanceof RefContext))
        {
            ArrayList<Reference> refClasses = new ArrayList<Reference>();
            traverse(ref, refClasses, RefClass.class);

            for (Reference r : refClasses)
            {
                RefClass refClass = (RefClass) r;

                try {
                    for (MEClass parent : InvSnooper.findClassParents(refClass.getMEClass())) {
                        MEClass p = parent;
                        //System.out.println(parent.getName());
                        if (parent instanceof UnknownClass) {
                            CollaborateClassContext sctx = MainFrame.getInstance().getResolver().getReferenceContext();
                            try {
                                p = sctx.getMEClass(parent.getName());
                            } catch (ClassNotFoundException e) {
                                //System.out.println("  not found super" + parent.getName());
                            }
                        }

                        if (!dep.containsKey(p)) {
                            dep.put(p, new ArrayList<Object>());
                        }

                        dep.get(p).add(p);
                    }
                } catch (Throwable e1) {
                    e1.printStackTrace();
                }

                String unknownSuperClassName = refClass.getMEClass().getUnknownSuperClassName();
                if (unknownSuperClassName != null) {
                    CollaborateClassContext sctx = MainFrame.getInstance().getResolver().getReferenceContext();
                    try {
                        MEClass superClass = sctx.getMEClass(unknownSuperClassName);
                        if (!dep.containsKey(superClass)) {
                            dep.put(superClass, new ArrayList<Object>());
                        }

                        dep.get(superClass).add(superClass);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                    }
                }

                for (Reference refx : refClass.getChildren())
                {
                    if (refx instanceof RefMethod) {
                        RefMethod refMethod = (RefMethod) refx;
                        //internal field access
                        for (ReferredReference referRef : refMethod.getReferredReference(true)) {
                            if (referRef instanceof RefFieldAccess) {
                                RefFieldAccess refFieldAccess = (RefFieldAccess) referRef;
                                MEField f = refFieldAccess.getAccess().field;
                                MEClass c = refFieldAccess.getAccess().clazz;

                                if (!dep.containsKey(c)) {
                                    dep.put(c, new ArrayList<Object>());
                                }

                                dep.get(c).add(f);
                            }
                        }
                        //external field access
                        for (ReferredReference referRef : refMethod.getReferredReference(false)) {
                            if (referRef instanceof RefFieldAccess) {
                                RefFieldAccess refFieldAccess = (RefFieldAccess) referRef;
                                MEField f = refFieldAccess.getAccess().field;
                                MEClass c = refFieldAccess.getAccess().clazz;

                                if (!dep.containsKey(c)) {
                                    dep.put(c, new ArrayList<Object>());
                                }

                                dep.get(c).add(f);
                            }
                        }
                        for (Reference refxx : refMethod.getChildren()) {
                            RefInvokation refInvokation = (RefInvokation) refxx;
                            MEMethod m = refInvokation.getInvokation().method;
                            MEClass c = refInvokation.getInvokation().clazz;

                            if (!dep.containsKey(c)) {
                                dep.put(c, new ArrayList<Object>());
                            }

                            dep.get(c).add(m);
                        }
                    }// if (refx instanceof RefMethod)
                }// for (Reference refx : refClass.getChildren())
            }//for (Reference r: refClasses)
        }//ref instanceof RefContext

        UnfoldDependency(dep, ref, null, ref instanceof RefClass, ref instanceof RefContext);

        mGraph.doLayoutAgain();
    }

    private void UnfoldDependency(HashMap<MEClass, ArrayList<Object>> dep, Reference selected, Object cell, boolean unfoldClass, boolean unfoldPackage) {
        if (cell == null) {
            cell = mGraph.getDefaultParent();
        }

        Object[] children = mGraph.getChildVertices(cell);
        for (Object child : children) {
            Object val = mModel.getValue(child);
            if (val instanceof ClassComponent)
            {
                ClassComponent classSplit = (ClassComponent) val;
                classSplit.resetToArch();
                classSplit.clean();

                RefClass refClass = classSplit.getRefClass();

                if (selected == refClass
                        || (selected instanceof RefPackage && refClass.getParent() == selected)) {
                    classSplit.setFocus();
                }

                boolean hit = false;

                ArrayList<RefClass> classes = new ArrayList<RefClass>();
                classes.add(refClass);

                HashMap<RefClass, TreeSet<RefClass>> classMap = mGraphComponent.getGraph().getClassMap();
                if (classMap.containsKey(refClass)) {
                    TreeSet<RefClass> tree = classMap.get(refClass);
                    if (tree != null) {
                        for (RefClass ref : tree) {
                            classes.add(ref);

                            if (!classSplit.isFocus()) {
                                if (selected == ref
                                        || (selected instanceof RefPackage && refClass.getParent() == selected)) {
                                    classSplit.setFocus();
                                }
                            }
                        }
                    }
                }

                for (RefClass refc : classes)
                {
                    MEClass clazz = refc.getMEClass();
                    if (dep.containsKey(clazz))
                    {
                        for (Object o : dep.get(clazz))
                        {
                            if (o instanceof MEClass) {
                                if (clazz == o) {
                                    hit = true;
                                    classSplit.setDepSuper();
                                }
                            } else if (o instanceof MEField) {
                                for (Reference ro : refc.getChildren()) {
                                    if (ro.getReferred() == o) {
                                        hit = true;
                                        classSplit.addField((MEField) o);
                                        classSplit.setDependecy();
                                    }
                                }
                            } else if (o instanceof MEMethod) {
                                for (Reference ro : refc.getChildren()) {
                                    if (ro.getReferred() == o) {
                                        hit = true;
                                        classSplit.addMethod((MEMethod) o);
                                        classSplit.setDependecy();
                                    }
                                }
                            }
                        }

                        if (!hit) {
                            //this could be the same hashcode
                            //System.out.println(" not hit " + clazz.getName());
                        }
                    }
                }

                if (!classSplit.isArch()) {
                    if (unfoldClass) {
                        recursiveUnfolder(child);
                    } else {
                        recursiveUnfolder(mModel.getParent(child));
                    }
                } else if (unfoldPackage) {
                    recursiveUnfolder(mModel.getParent(child));
                }

                Rectangle size = calculateClassSize(classSplit, child);
                mxGeometry geo = mGraph.getModel().getGeometry(child);
                if (mModel.isCollapsed(child)) {
                    geo.setAlternateBounds(new mxRectangle(geo.getX(), geo.getY(), size.getWidth(), size.getHeight()));
                } else {
                    geo = (mxGeometry) geo.clone();
                    geo.setWidth(size.getWidth());
                    geo.setHeight(size.getHeight());
                    mGraph.cellsResized(new Object[] { child }, new mxRectangle[] { geo });
                }
            } else {
                UnfoldDependency(dep, selected, child, unfoldClass, unfoldPackage);
            }
        }
    }

    private void recursiveUnfolder(Object cell) {
        if (cell == null) {
            return;
        }
        mGraph.cellsFolded(new Object[] { cell }, false, false);
        recursiveUnfolder(mModel.getParent(cell));
    }

    private void traverse(Reference ref, ArrayList<Reference> result, Class<? extends Reference> clazz) {
        if (clazz.isInstance(ref)) {
            result.add(ref);
        }

        Iterator<Reference> refs = ref.getChildren().iterator();
        while (refs.hasNext()) {
            Reference child = (refs.next());
            if (clazz.isInstance(child)) {
                result.add(child);
            } else {
                traverse(child, result, clazz);
            }
        }
    }

    private void removeUnnessaryPackage(Object parent) {
        if (parent == null) {
            parent = mGraph.getDefaultParent();
        }
        Object parentVal = mModel.getValue(parent);
        Object[] children = mGraph.getChildVertices(parent);

        if (children.length == 1) {
            Object onlyChild = children[0];
            Object val = mModel.getValue(onlyChild);
            if (val instanceof PackageComponent
                    && parentVal instanceof PackageComponent
                    && ((PackageComponent) parentVal).getRefPackage() == null) {
                PackageComponent split = new PackageComponent(((PackageComponent) parentVal).getName() + "." + ((PackageComponent) val).getName(), ((PackageComponent) parentVal).isMidlet);
                split.setHasInnerPacakge(((PackageComponent) parentVal).hasInnerPackage() || ((PackageComponent) val).hasInnerPackage());
                mModel.setValue(parent, split);

                Object[] childrenChildren = mGraph.getChildVertices(onlyChild);

                for (Object child : childrenChildren) {
                    mGraph.addCell(child, parent);
                }

                mGraph.removeCells(new Object[] { onlyChild });

                removeUnnessaryPackage(parent);
                return;
            }
        }

        for (Object child : children) {
            removeUnnessaryPackage(child);
        }
    }

    private void insertPacakge(ArrayList<Reference> refPackages, boolean isMidlet) {
        //make sure packages are sorted that parent package should be a head of sub package
        Collections.sort(refPackages);

        //insert all packages
        for (Reference refx : refPackages) {
            RefPackage refPackage = (RefPackage) refx;
            Object packageObj;

            String[] names = refPackage.getName().split("\\.");
            if (names.length > 1) {
                Object p = mGraph.getDefaultParent();
                for (int i = 0; i < names.length - 1; i++) {
                    Object[] children = mGraph.getChildCells(p);
                    Object found = null;
                    for (Object child : children) {
                        Object val = mGraph.getModel().getValue(child);
                        if (val instanceof PackageComponent
                                && ((PackageComponent) val).isSameName(names[i])
                                && ((PackageComponent) val).isMidlet == isMidlet) {
                            found = child;
                            ((PackageComponent) val).setHasInnerPacakge(true);
                            break;
                        }
                    }
                    if (found == null) {
                        p = mGraph.insertVertex(p, null, new PackageComponent(names[i], isMidlet), 20, 20, 20, 10, STYLE_PACKAGE + ";noLabel=true;resizable=true");
                    } else {
                        p = found;
                    }
                }
                packageObj = mGraph.insertVertex(p, null, new PackageComponent(names[names.length - 1], refPackage, isMidlet), 20, 20, 20, 10, STYLE_PACKAGE + ";noLabel=true;resizable=true");
            } else {
                packageObj = mGraph.insertVertex(mGraph.getDefaultParent(), null, new PackageComponent(refPackage.getName(), refPackage, isMidlet), 20, 20, 20, 10, STYLE_PACKAGE + ";noLabel=true;resizable=true");
            }

            //insert all classes
            List<RefClass> list = Arrays.asList(refPackage.getChildren().toArray(new RefClass[0]));
            //sort the class because the outer class should be a head of inner class
            Collections.sort(list);
            Iterator<RefClass> i = list.iterator();
            while (i.hasNext()) {
                RefClass refClass = i.next();
                String name = refClass.getName();
                if (isInnerClass(name.indexOf('$'), name, refClass) == false) {
                    ClassComponent classSplit = new ClassComponent(refClass);
                    Object v = mGraph.insertVertex(packageObj, null, classSplit, 0, 0, 0, 0, STYLE_CLASS + ";noLabel=true;fillColor=white;resizable=false");
                    Rectangle size = calculateClassSize(classSplit, v);
                    mxGeometry geo = mGraph.getModel().getGeometry(v);
                    geo = (mxGeometry) geo.clone();
                    geo.setWidth(size.getWidth());
                    geo.setHeight(size.getHeight());
                    mGraph.cellsResized(new Object[] { v }, new mxRectangle[] { geo });
                    mGraph.cellsFolded(new Object[] { v }, true, false);
                }
            }
        }
    }

    private Rectangle getLineSize(String text, Object cell, boolean firstClassName) {
        double scale = 1;

        Rectangle fontRect = mxUtils.getSizeForString(text, mxUtils.getFont(mGraph.getView().getState(cell, true)
                .getStyle()), scale).getRectangle();

        FlagIcon im = ClassTreeRenderer.ICON_CLASS;

        double imageheight = Math.min(im.getIconHeight(), fontRect.height);
        double imagewidth = imageheight / im.getIconHeight() * im.getIconWidth(); //ratio is fixed it will changed in canvas.drawImage()

        if (firstClassName) {
            return new Rectangle((int) (CHAR_SPACING + imagewidth + CHAR_SPACING + fontRect.width + CHAR_SPACING + FOLDING_ICON.getIconWidth() * 2 + CHAR_SPACING * 2), (int) fontRect.getHeight());
        } else {
            return new Rectangle((int) (CHAR_SPACING + imagewidth + CHAR_SPACING + fontRect.width + CHAR_SPACING), (int) fontRect.getHeight());
        }
    }

    private Rectangle calculateClassSize(ClassComponent classSplit, Object cell) {
        MEClass clazz = classSplit.getMEClass();

        Rectangle ret = getLineSize(clazz.getClassName(), cell, true);
        boolean hasField = false;

        for (MEField field : classSplit.getFields()) {
            String text = field.getName() + " : " + Util.shortenClassName(field.getType().toString());
            Rectangle oneLine = getLineSize(text, cell, false);
            ret.height += oneLine.height;
            if (ret.width < oneLine.width) {
                ret.width = oneLine.width;
            }
            hasField = true;
        }

        if (!hasField) {
            ret.height += GraphPanel.LINE_SPACING;
        }

        boolean hasMethod = false;

        for (MEMethod method : classSplit.getMethods()) {
            hasMethod = true;
            String text = method.getFormattedName() + "(" + method.getArgumentsStringUml() + ") : " + Util.shortenClassName(method.getReturnClassString());
            Rectangle oneLine = getLineSize(text, cell, false);
            ret.height += oneLine.height;
            if (ret.width < oneLine.width) {
                ret.width = oneLine.width;
            }
        }

        if (!hasMethod) {
            ret.height += GraphPanel.LINE_SPACING;
        }

        return ret;
    }

    protected AbstractMainFrame m_mainFrame;

    public GraphPanel(AbstractMainFrame mf)
    {
        m_mainFrame = mf;

        // Stores a reference to the graph and creates the command history
        mGraph = new CustomGraph();
        mGraphComponent = new CustomGraphComponent(mGraph);
        mModel = mGraph.getModel();

        // Do not change the scale and translation after files have been loaded
        mGraph.setResetViewOnRootChange(false);

        // Puts everything together
        setLayout(new BorderLayout());
        add(mGraphComponent, BorderLayout.CENTER);

        // Installs rubberband selection and handling for some special
        // keystrokes such as F2, Control-C, -V, X, A etc.
        installHandlers();
        installListeners();

        getGraphComponent().getGraph().setCellsResizable(true);
        getGraphComponent().setConnectable(false);
        getGraphComponent().getGraphHandler().setCloneEnabled(false);
        getGraphComponent().getGraphHandler().setImagePreview(false);

        Map<String, Object> style;
        style = new HashMap<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, SHAPE_PACKAGE);
        style.put(mxConstants.STYLE_FOLDABLE, "true");
        mGraph.getStylesheet().putCellStyle(STYLE_PACKAGE, style);

        style = new HashMap<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, SHAPE_FOLDER);
        style.put(mxConstants.STYLE_FOLDABLE, "true");
        mGraph.getStylesheet().putCellStyle(STYLE_FOLDER, style);

        style = new HashMap<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, SHAPE_CLASS);
        style.put(mxConstants.STYLE_FOLDABLE, "true");
        mGraph.getStylesheet().putCellStyle(STYLE_CLASS, style);

    }

    protected void installHandlers()
    {
        rubberband = new mxRubberband(mGraphComponent);
    }

    protected void mouseWheelMoved(MouseWheelEvent e)
    {

        if (e.getWheelRotation() < 0)
        {
            mGraphComponent.zoomIn();
        }
        else
        {
            mGraphComponent.zoomOut();
        }

        double scale = mGraphComponent.getGraph().getView().getScale();
        double percent = Math.round(100 * scale);
        double newscale = Math.min(8, Math.max(0.05, percent / 100));
        mGraphComponent.zoomTo(newscale, mGraphComponent
                .isCenterZoom());
    }

    protected void installListeners()
    {
        // Installs mouse wheel listener for zooming
        MouseWheelListener wheelTracker = new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                if (e.getSource() instanceof mxGraphOutline
                        || e.isControlDown())
                {
                    GraphPanel.this.mouseWheelMoved(e);
                }
            }

        };

        // Handles mouse wheel events in the outline and graph component
        mGraphComponent.addMouseWheelListener(wheelTracker);

        // Installs the popup menu in the graph component
        mGraphComponent.getGraphControl().addMouseListener(new MouseAdapter()
        {

            @Override
            public void mousePressed(MouseEvent e)
            {

            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
            }

        });
    }

    public mxGraphComponent getGraphComponent()
    {
        return mGraphComponent;
    }

    /**
     * 
     * @param name
     * @param action
     * @return a new Action bound to the specified string name
     */
    public Action bind(String name, final Action action)
    {
        return bind(name, action, null);
    }

    public static ImageIcon createImageIcon(String path)
    {
        java.net.URL imgURL = Thread.currentThread().getContextClassLoader().getResource(path);
        if (imgURL != null)
        {
            return new ImageIcon(imgURL);
        }
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * 
     * @param name
     * @param action
     * @return a new Action bound to the specified string name and icon
     */
    @SuppressWarnings("serial")
    public Action bind(String name, final Action action, String iconUrl)
    {
        return new AbstractAction(name, (iconUrl != null) ? createImageIcon(iconUrl) : null)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                action.actionPerformed(new ActionEvent(getGraphComponent(), e
                        .getID(), e.getActionCommand()));
            }
        };
    }

}
