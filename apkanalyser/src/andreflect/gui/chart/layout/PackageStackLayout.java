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

package andreflect.gui.chart.layout;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import analyser.gui.ClassTreeRenderer;
import analyser.gui.FlagIcon;
import andreflect.gui.chart.ClassComponent;
import andreflect.gui.chart.GraphPanel;
import andreflect.gui.chart.PackageComponent;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class PackageStackLayout extends mxGraphLayout
{

    /**
     * Specifies the spacing between the cells. Default is 0.
     */
    protected int spacing;

    /**
     * Border to be added if fill is true. Default is 0.
     */
    protected int border;

    /**
     * Constructs a new stack layout layout for the specified graph,
     * spacing, orientation and offset.
     */
    public PackageStackLayout(mxGraph graph)
    {
        this(graph, true, graph.getGridSize() / 2, graph.getGridSize());
    }

    /**
     * Constructs a new stack layout layout for the specified graph,
     * spacing, orientation and offset.
     */
    public PackageStackLayout(mxGraph graph, boolean horizontal, int spacing, int border)
    {
        super(graph);
        this.spacing = spacing;
        this.border = border;
    }

    /**
     * Hook for subclassers to return the container size.
     */
    public mxRectangle getContainerSize()
    {
        return new mxRectangle();
    }

    @Override
    public void execute(Object parent)
    {
        execute(parent, true);
    }

    public void execute(Object parent, boolean again)
    {
        if (parent != null)
        {
            mxIGraphModel model = graph.getModel();
            mxGeometry pgeo = model.getGeometry(parent);

            int wrap = 0;

            if (pgeo == null || parent == graph.getDefaultParent())
            {
                //System.out.println(" no parent ");
                mxRectangle tmp = getContainerSize();
                pgeo = new mxGeometry(0, 0, tmp.getWidth(), tmp.getHeight());
                wrap = 0; //only change line when type is changed
            } else {
                PackageComponent parentSplit = getPackageSplit(parent);
                //System.out.println(parentSplit.getName());
                if (again) {
                    wrap = (int) pgeo.getWidth();
                } else if (parentSplit.hasInnerPackage()) {
                    wrap = calulateWrapWidthAndSwapWithInnerPackage(parent);
                } else {
                    wrap = calulateWrapWidth(parent);
                }
            }

            // Handles swimlane start size
            mxRectangle size = graph.getStartSize(parent);
            double x0 = border;
            double y0 = size.getHeight() + border;

            model.beginUpdate();
            try
            {
                double tmp = 0;
                double x_max = 0;
                mxGeometry last = null;
                Object lastCell = null;
                PackageComponent lastPackageSplit = null;
                int childCount = model.getChildCount(parent);

                for (int i = 0; i < childCount; i++)
                {
                    Object child = model.getChildAt(parent, i);

                    if (!isVertexIgnored(child) && isVertexMovable(child))
                    {
                        mxGeometry geo = model.getGeometry(child);

                        if (geo != null)
                        {
                            geo = (mxGeometry) geo.clone();

                            if (last != null)
                            {
                                /*if ((model.getValue(lastCell) instanceof PackageSplit))
                                	System.out.println("       "+  "["+((PackageSplit)model.getValue(lastCell)).getName()+"]"  + " exec last = "+(last.getX()
                                			+ last.getWidth()) + " thiswidth = " + geo.getWidth() + " check = " + (last.getX()
                                					+ last.getWidth() + geo.getWidth() + 2 * spacing + graph.getGridSize()) + " wrap = " +wrap);*/

                                boolean change = false;

                                if (again) {

                                    if (lastPackageSplit != null) {
                                        if (lastPackageSplit.isLast()) {
                                            change = true;
                                        }
                                    } else if ((wrap != 0 && last.getX()
                                            + last.getWidth() + geo.getWidth() + 2
                                            * spacing + graph.getGridSize() > wrap)
                                            || isTypeChanged(lastCell, child))
                                    {
                                        change = true;
                                    }
                                }
                                else if ((wrap != 0 && last.getX()
                                        + last.getWidth() + geo.getWidth() + 2
                                        * spacing + graph.getGridSize() > wrap)
                                        || isTypeChanged(lastCell, child))
                                {
                                    change = true;
                                    if (lastPackageSplit != null) {
                                        lastPackageSplit.setLast();
                                    }
                                }

                                if (change) {
                                    last = null;
                                    y0 += tmp + spacing;
                                    tmp = 0;
                                }
                            }

                            tmp = Math.max(tmp, geo.getHeight());

                            if (last != null)
                            {
                                geo.setX(graph.snap(last.getX() + last.getWidth()
                                        + spacing + graph.getGridSize() / 2));
                            }
                            else
                            {
                                geo.setX(x0);
                            }

                            geo.setY(y0);

                            x_max = Math.max(x_max, geo.getX() + geo.getWidth());

                            model.setGeometry(child, geo);
                            last = geo;
                            lastCell = child;
                            lastPackageSplit = getPackageSplit(child);
                        }
                    }
                }

                if (again == false && lastPackageSplit != null) {
                    lastPackageSplit.setLast();
                }

                if (pgeo != null
                        && !graph.isCellCollapsed(parent))
                {
                    pgeo = (mxGeometry) pgeo.clone();

                    pgeo.setWidth(Math.max(x_max + border, getNameWidth(parent) + graph.getGridSize()));

                    if (last != null) {
                        pgeo.setHeight(last.getY() + tmp + border);
                    } else {
                        pgeo.setHeight(y0 + border);
                    }

                    //System.out.println("    final width = " + Math.max(x_max + border, getNameWidth(parent) + graph.getGridSize()) + " height = "+ (last.getY() + tmp+ border));

                    model.setGeometry(parent, pgeo);
                }
            } finally
            {
                model.endUpdate();
            }
        }
    }

    private boolean isTypeChanged(Object lastCell, Object child) {
        final mxIGraphModel model = graph.getModel();
        Object lastValue = model.getValue(lastCell);
        Object value = model.getValue(child);
        if (lastValue instanceof ClassComponent
                && value instanceof PackageComponent) {
            return true;
        }

        if (lastValue instanceof PackageComponent
                && value instanceof PackageComponent
                && ((PackageComponent) lastValue).isMidlet() == true
                && ((PackageComponent) value).isMidlet() == false) {
            return true;
        }
        return false;
    }

    private class SortPackage {
        Object cell;
        int orginalIndex;

        public SortPackage(Object cell, int orginalIndex) {
            this.cell = cell;
            this.orginalIndex = orginalIndex;
        }
    }

    private int calulateWrapWidthAndSwapWithInnerPackage(Object parent) {
        ArrayList<Object> children = new ArrayList<Object>();
        final mxIGraphModel model = graph.getModel();
        int childCount = model.getChildCount(parent);
        ArrayList<SortPackage> sortPackages = new ArrayList<SortPackage>();
        double maxPackageWidth = 0;
        int i;
        for (i = 0; i < childCount; i++)
        {
            Object child = model.getChildAt(parent, i);
            if (!isVertexIgnored(child) && isVertexMovable(child))
            {
                children.add(child);
                PackageComponent split = getPackageSplit(child);
                if (split != null) {
                    sortPackages.add(new SortPackage(child, i));
                    maxPackageWidth = Math.max(maxPackageWidth, model.getGeometry(child).getWidth() + border * 2);
                }
            }
        }

        Collections.sort(sortPackages, new Comparator<SortPackage>() {
            @Override
            public int compare(SortPackage o1, SortPackage o2) {
                return o1.orginalIndex - o2.orginalIndex;
            }
        });

        int[] indexes = new int[sortPackages.size()];
        i = 0;
        for (SortPackage sortPackage : sortPackages) {
            indexes[i++] = sortPackage.orginalIndex;
        }

        Collections.sort(sortPackages, new Comparator<SortPackage>() {
            @Override
            public int compare(SortPackage o1, SortPackage o2) {
                return (int) (model.getGeometry(o1.cell).getHeight() - model.getGeometry(o2.cell).getHeight());
            }
        });

        i = 0;
        for (SortPackage sortPackage : sortPackages) {
            model.add(parent, sortPackage.cell, indexes[i++]);
        }

        if (getPackageSplit(parent) != null
                && getPackageSplit(parent).isMidlet()) {
            return (int) tryLayout(children, maxPackageWidth, maxPackageWidth * 2, 0.2, 6);
        } else {
            return (int) tryLayout(children, maxPackageWidth, maxPackageWidth * 2, 0.2, 4);
        }
    }

    private int calulateWrapWidth(Object parent) {
        mxIGraphModel model = graph.getModel();
        int childCount = model.getChildCount(parent);
        double totalLen = 0;
        double tmp = 0;

        ArrayList<Object> children = new ArrayList<Object>();

        int i;
        for (i = 0; i < childCount; i++)
        {
            Object child = model.getChildAt(parent, i);
            if (!isVertexIgnored(child) && isVertexMovable(child))
            {
                mxGeometry geo = model.getGeometry(child);
                totalLen += geo.getWidth();
                totalLen += spacing + graph.getGridSize();
                children.add(child);

                tmp = Math.max(tmp, geo
                        .getHeight());
            }
        }

        totalLen += border * 2;

        double averageLen = totalLen / childCount;

        if (getPackageSplit(parent) != null
                && getPackageSplit(parent).isMidlet()) {
            return (int) tryLayout(children, averageLen * 3, totalLen, 0.25, 6);
        } else {
            return (int) tryLayout(children, averageLen * 2, totalLen, 0.5, 4);
        }

    }

    private double tryLayout(ArrayList<Object> children, double base, double max, double step, double targetRatio) {
        double times = 1;

        double min_area = 0;
        double ret = base;
        double height;
        double width;

        mxIGraphModel model = graph.getModel();

        do {
            double tmp = 0;
            double x_max = 0;
            mxGeometry last = null;
            Object lastCell = null;
            height = 0;
            width = 0;
            double area;
            for (Object child : children)
            {
                mxGeometry geo = model.getGeometry(child);
                if (geo != null)
                {
                    geo = (mxGeometry) geo.clone();
                    if (last != null)
                    {
                        /*System.out.println("       try last = "+(last.getX()
                        		+ last.getWidth()) + " thiswidth = " + geo.getWidth() + " check = " + (last.getX()
                        		+ last.getWidth() + geo.getWidth() + 2
                         * spacing + graph.getGridSize()) + " wrap = " +wrap );*/
                        if (last.getX()
                                + last.getWidth() + geo.getWidth() + 2
                                * spacing + graph.getGridSize() > base * times
                                || isTypeChanged(lastCell, child)) {
                            last = null;
                            height += tmp + spacing;
                            tmp = 0;
                        }
                    }
                    tmp = Math.max(tmp, geo.getHeight());

                    if (last != null)
                    {
                        geo.setX(graph.snap(last.getX() + last.getWidth()
                                + spacing + graph.getGridSize() / 2));
                    }
                    else
                    {
                        geo.setX(border);
                    }

                    x_max = Math.max(x_max, geo.getX() + geo.getWidth());

                    last = geo;
                    lastCell = child;
                }
            }

            width = x_max + border;
            height += tmp + spacing;

            //System.out.println ("    base = " + base+ " width = "+ width + " height = "+ height + " ratio = "+ width / height + " target = "+  targetRatio + " area = " + width * height + " min_area = "+ min_area);

            area = width * height;
            if (min_area == 0
                    || area <= min_area * 1.10) {
                if (area < min_area
                        || min_area == 0) {
                    min_area = area;
                }
                if (width / height < targetRatio) {
                    ret = width;
                }
            }

            times = times + step;

        } while (base * times < max && width / height < targetRatio);

        //System.out.println("    calculate width = "+ ret);
        return ret + 1; //simple for math.round
    }

    private PackageComponent getPackageSplit(Object cell) {
        Object val = graph.getModel().getValue(cell);
        if (val instanceof PackageComponent) {
            return (PackageComponent) val;
        }

        return null;
    }

    private double getNameWidth(Object cell) {
        if (getPackageSplit(cell) == null) {
            return 0;
        }

        double scale = 1;

        String name = getPackageSplit(cell).getName();

        Rectangle fontRect = mxUtils.getSizeForString(name, mxUtils.getFont(graph.getCellStyle(cell)), scale).getRectangle();

        FlagIcon im = ClassTreeRenderer.ICON_PACKAGE;

        double s = Math.min(fontRect.height / im.getIconHeight(), 1);
        int imagewidth = (int) (im.getIconWidth() * s);

        return GraphPanel.CHAR_SPACING + imagewidth + GraphPanel.CHAR_SPACING + fontRect.width + GraphPanel.CHAR_SPACING + GraphPanel.FOLDING_ICON.getIconWidth() + GraphPanel.CHAR_SPACING;
    }

    public void executeLastFill(Object parent) {
        if (parent != null)
        {
            mxIGraphModel model = graph.getModel();
            int childCount = model.getChildCount(parent);

            mxGeometry pgeo = model.getGeometry(parent);

            for (int i = 0; i < childCount; i++)
            {
                Object child = model.getChildAt(parent, i);

                if (pgeo != null && parent != graph.getDefaultParent())
                {
                    if (!isVertexIgnored(child) && isVertexMovable(child))
                    {
                        mxGeometry geo = model.getGeometry(child);
                        if (geo != null)
                        {
                            PackageComponent split = getPackageSplit(child);
                            if (split != null
                                    && split.isLast()) {
                                //System.out.println("    " + split.getName()+ " old = " + geo.getWidth() + " new = " + (pgeo.getWidth() - geo.getX() - border - graph.getGridSize()) + " pgeo.getX() = " +  pgeo.getX() + " pgeo.getWidth() = " + pgeo.getWidth() + " geo.getX() = "+ geo.getX());
                                geo.setWidth(Math.max(pgeo.getWidth() - geo.getX() - border - graph.getGridSize(), geo.getWidth()));
                            }
                        }
                    }
                }
                executeLastFill(model.getChildAt(parent, i));
            }
        }
    }
}
