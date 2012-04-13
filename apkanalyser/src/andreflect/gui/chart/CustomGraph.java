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

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import analyser.gui.ClassTreeRenderer;
import analyser.gui.FlagIcon;
import analyser.logic.RefClass;
import andreflect.gui.chart.layout.PackageStackLayout;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class CustomGraph extends mxGraph {

    /**
     * Holds the edge to be used as a template for inserting new edges.
     */
    protected Object edgeTemplate;

    protected PackageStackLayout layout;
    protected HashMap<RefClass, TreeSet<RefClass>> classMap;

    /**
     * Custom graph that defines the alternate edge style to be used when
     * the middle control point of edges is double clicked (flipped).
     */
    public CustomGraph() {
        super();
        setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
        layout = new PackageStackLayout(this);
        classMap = new HashMap<RefClass, TreeSet<RefClass>>();
    }

    public void doLayoutFirst() {
        //first time
        doLayoutInner(null, false);

        layout.executeLastFill(getDefaultParent());

        //second time
        doLayoutInner(null, true);

        layout.execute(getDefaultParent(), false);
    }

    public void doLayoutAgain() {

        doLayoutInner(null, true);

        layout.execute(getDefaultParent(), false);
    }

    private void doLayoutInner(Object parent, boolean again) {
        if (parent == null) {
            parent = getDefaultParent();
        }

        Object[] children = getChildVertices(parent);
        for (Object child : children) {
            doLayoutInner(child, again);
        }

        Object parentVal = getModel().getValue(parent);
        if (parentVal instanceof PackageComponent) {
            layout.execute(parent, again);
        }
    }

    public HashMap<RefClass, TreeSet<RefClass>> getClassMap() {
        return classMap;
    }

    /**
     * Sets the edge template to be used to inserting edges.
     */
    public void setEdgeTemplate(Object template)
    {
        edgeTemplate = template;
    }

    /**
     * Prints out some useful information about the cell in the tooltip.
     */
    @Override
    public String getToolTipForCell(Object cell)
    {
        String tip = null;
        Object value = getModel().getValue(cell);
        if (value instanceof ClassComponent) {
            ClassComponent classSplit = (ClassComponent) value;
            RefClass refClass = classSplit.getRefClass();
            //System.out.println(refClass.getName() + " " + classMap.containsKey(refClass) + " " + classMap.get(refClass));
            tip = "<html>" + refClass.getName() + " doesn't have any inner class</html>";

            if (classMap.containsKey(refClass)
                    && classMap.get(refClass) != null
                    && classMap.get(refClass).size() != 0) {
                TreeSet<RefClass> set = classMap.get(refClass);
                tip = "<html>";
                for (RefClass refC : set) {
                    tip += refC.getName() + "<br>";
                }
                tip += "</html>";
            }

        }
        return tip;
    }

    /**
     * Overrides the method to use the currently selected edge template for
     * new edges.
     * 
     * @param graph
     * @param parent
     * @param id
     * @param value
     * @param source
     * @param target
     * @param style
     * @return
     */
    @Override
    public Object createEdge(Object parent, String id, Object value,
            Object source, Object target, String style)
    {
        if (edgeTemplate != null)
        {
            mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
            edge.setId(id);

            return edge;
        }

        return super.createEdge(parent, id, value, source, target, style);
    }

    //override to make package as container
    @Override
    public boolean isSwimlane(Object cell)
    {
        if (cell != null)
        {
            if (model.getParent(cell) != model.getRoot())
            {
                mxCellState state = view.getState(cell);
                Map<String, Object> style = (state != null) ? state.getStyle()
                        : getCellStyle(cell);

                if (style != null && !model.isEdge(cell))
                {
                    String shape = mxUtils.getString(style, mxConstants.STYLE_SHAPE, "");
                    return shape.equals(mxConstants.SHAPE_SWIMLANE)
                            || shape.equals(GraphPanel.SHAPE_PACKAGE)
                            || shape.equals(GraphPanel.SHAPE_FOLDER);
                }
            }
        }

        return false;
    }

    // ignore model.getChildCount(cell) > 0
    @Override
    public boolean isCellFoldable(Object cell, boolean collapse)
    {
        mxCellState state = view.getState(cell);
        Map<String, Object> style = (state != null) ? state.getStyle()
                : getCellStyle(cell);

        return mxUtils.isTrue(style, mxConstants.STYLE_FOLDABLE, true);
    }

    // calculate the preferred size for each component
    @Override
    public mxRectangle getPreferredSizeForCell(Object cell)
    {
        mxRectangle result = null;

        if (cell != null)
        {
            mxCellState state = view.getState(cell);
            Map<String, Object> style = (state != null) ? state.getStyle()
                    : getCellStyle(cell);

            if (style != null && !model.isEdge(cell))
            {
                String styleShape = mxUtils.getString(style, mxConstants.STYLE_SHAPE, "");
                if (!(styleShape.equals(GraphPanel.SHAPE_FOLDER)
                        || styleShape.equals(GraphPanel.SHAPE_PACKAGE)
                        || styleShape.equals(GraphPanel.SHAPE_CLASS)))
                {
                    return super.getPreferredSizeForCell(cell);
                }

                double scale = 1;

                Object val = state.getView().getGraph().getModel().getValue(state.getCell());
                String name = null;

                if (val instanceof PackageComponent) {
                    name = ((PackageComponent) val).getName();
                } else if (val instanceof ClassComponent) {
                    name = ((ClassComponent) val).getRefClass().getName();
                }

                Rectangle fontRect = mxUtils.getSizeForString(name, mxUtils.getFont(state
                        .getStyle()), scale).getRectangle();

                FlagIcon im = ClassTreeRenderer.ICON_PACKAGE;

                double s = Math.min(fontRect.height / im.getIconHeight(), 1);
                int imagewidth = (int) (im.getIconWidth() * s);

                result = new mxRectangle(0, 0, (GraphPanel.CHAR_SPACING + imagewidth + GraphPanel.CHAR_SPACING + fontRect.width + GraphPanel.CHAR_SPACING + GraphPanel.FOLDING_ICON.getIconWidth() + GraphPanel.CHAR_SPACING),
                        fontRect.height);

            }
        }

        return result;
    }

    // override the start size for swimlane
    @Override
    public mxRectangle getStartSize(Object swimlane)
    {
        mxRectangle result = new mxRectangle();
        mxCellState state = view.getState(swimlane, true);
        Map<String, Object> style = (state != null) ? state.getStyle()
                : getCellStyle(swimlane);

        if (style != null)
        {
            Rectangle fontRect = mxUtils.getSizeForString("com.sonyericsson.abc", mxUtils.getFont(state
                    .getStyle()), 1).getRectangle();
            result.setHeight(fontRect.height);
        }

        return result;
    }

}
