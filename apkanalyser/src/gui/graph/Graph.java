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

package gui.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    List<GraphNode> roots;

    public Graph() {
        roots = new ArrayList<GraphNode>();
    }

    public Graph(GraphNode root) {
        roots = new ArrayList<GraphNode>();
        roots.add(root);
    }

    public List<GraphNode> getRoots() {
        return roots;
    }

    public void addRoot(GraphNode node) {
        roots.add(node);
    }

    public boolean removeRoot(GraphNode node) {
        return roots.remove(node);
    }

    public GraphNode getRoot() {
        return roots.get(0);
    }
}
