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

public class GraphNode {
    GraphNode parent;
    List<GraphNode> childrens;
    DefaultNodePainter painter;
    Object userObject;

    int x;
    int y;
    int width;
    int height;

    public GraphNode() {
        childrens = new ArrayList<GraphNode>();
    }

    public GraphNode(Object userObject) {
        childrens = new ArrayList<GraphNode>();
        this.userObject = userObject;
    }

    public GraphNode(GraphNode parent, Object userObject) {
        this.parent = parent;
        parent.add(this);
        childrens = new ArrayList<GraphNode>();
        this.userObject = userObject;
    }

    public boolean remove(GraphNode node) {
        return childrens.remove(node);
    }

    public void add(GraphNode node) {
        node.parent = this;
        childrens.add(node);
    }

    public List<GraphNode> getChildren() {
        return childrens;
    }

    public DefaultNodePainter getPainter() {
        return painter;
    }

    public void setPainter(DefaultNodePainter painter) {
        this.painter = painter;
    }

    public boolean hasChildren() {
        return !childrens.isEmpty();
    }

    public boolean hasParent() {
        return parent != null;
    }

    public GraphNode getParent() {
        return parent;
    }

    public Object getUserObject() {
        return userObject;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    @Override
    public String toString() {
        if (userObject != null) {
            return userObject.toString();
        } else {
            return "";
        }
    }
}
