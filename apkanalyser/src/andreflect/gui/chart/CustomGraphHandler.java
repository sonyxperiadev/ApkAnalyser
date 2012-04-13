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

import com.mxgraph.swing.handler.mxGraphHandler;

public class CustomGraphHandler extends mxGraphHandler {
    public CustomGraphHandler(CustomGraphComponent graphComponent) {
        super(graphComponent);
    }

    // do re-layout when folder icon clicked
    @Override
    protected void fold(Object cell)
    {
        CustomGraph graph = ((CustomGraphComponent) graphComponent).getGraph();
        boolean collapsed = graph.isCellCollapsed(cell);
        graph.getModel().beginUpdate();
        graph.cellsFolded(new Object[] { cell }, !collapsed, false);

        Object parent = graph.getModel().getParent(cell);
        if (parent != null) {
            graph.doLayoutAgain();
        }
        graph.getModel().endUpdate();
    }
}
