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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class CustomGraphComponent extends mxGraphComponent {

    @Override
    public CustomGraph getGraph()
    {
        return (CustomGraph) graph;
    }

    // move the folding icon to right corner
    @Override
    public Rectangle getFoldingIconBounds(mxCellState state, ImageIcon icon)
    {
        mxIGraphModel model = graph.getModel();
        boolean isEdge = model.isEdge(state.getCell());
        double scale = getGraph().getView().getScale();

        int x = (int) Math.round(state.getX() + state.getWidth() - 4 * scale - icon.getIconWidth() * scale);
        int y = (int) Math.round(state.getY() + 4 * scale);
        int w = (int) Math.max(8, icon.getIconWidth() * scale);
        int h = (int) Math.max(8, icon.getIconHeight() * scale);

        if (isEdge)
        {
            mxPoint pt = graph.getView().getPoint(state);

            x = (int) pt.getX() - w / 2;
            y = (int) pt.getY() - h / 2;
        }

        return new Rectangle(x, y, w, h);
    }

    // change double click to fold/unfold
    @Override
    protected void installDoubleClickHandler()
    {
        graphControl.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (isEnabled())
                {
                    if (!e.isConsumed() && isEditEvent(e))
                    {
                        Object cell = getCellAt(e.getX(), e.getY(), false);
                        if (cell != null && getGraph().getView() != null)
                        {
                            boolean collapsed = getGraph().isCellCollapsed(cell);
                            graph.getModel().beginUpdate();
                            graph.cellsFolded(new Object[] { cell }, !collapsed, false);

                            Object parent = graph.getModel().getParent(cell);
                            if (parent != null) {
                                ((CustomGraph) graph).doLayoutAgain();
                            }
                            graph.getModel().endUpdate();
                        }
                    }
                }
            }

        });
    }

    private static final long serialVersionUID = -6833603133512882012L;

    /**
     * 
     * @param graph
     */
    public CustomGraphComponent(mxGraph graph)
    {
        super(graph);

        // Sets switches typically used in an editor
        setPageVisible(false);
        setGridVisible(true);
        setToolTips(true);
        getConnectionHandler().setCreateTarget(true);

        // Sets the background to white
        getViewport().setOpaque(true);
        getViewport().setBackground(Color.WHITE);
    }

    @Override
    protected CustomGraphHandler createGraphHandler()
    {
        return new CustomGraphHandler(this);
    }

    @Override
    protected CustomGraphControl createGraphControl()
    {
        return new CustomGraphControl();
    }

    public class CustomGraphControl extends mxGraphControl {

        private static final long serialVersionUID = -7142282361479036713L;

        @Override
        protected void drawChildren(Object cell, boolean edges, boolean others)
        {
            if (!getGraph().getModel().isCollapsed(cell)) {
                super.drawChildren(cell, edges, others);
            }
        }
    }

    //transfer data exception when drag
    @Override
    public boolean isExportEnabled()
    {
        return false;
    }

    //transfer data exception when drag
    @Override
    public boolean isImportEnabled()
    {
        return false;
    }

}
