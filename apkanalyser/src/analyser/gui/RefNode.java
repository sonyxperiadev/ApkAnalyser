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

import analyser.logic.InvSnooper;
import gui.graph.GraphNode;
import mereflect.MEClass;
import mereflect.MEMethod;

public class RefNode extends GraphNode {
    boolean isPopuplated = false;
    boolean isSelected = false;

    int lookupFlags;

    public RefNode() {
    }

    public RefNode(Object userObject, int lookupFlags) {
        super(userObject);
        this.lookupFlags = lookupFlags;
    }

    public RefNode(GraphNode parent, Object userObject) {
        super(parent, userObject);
    }

    public boolean isPopuplated() {
        return isPopuplated;
    }

    public void setPopuplated(boolean isPopuplated) {
        this.isPopuplated = isPopuplated;
    }

    @Override
    public String toString() {
        InvSnooper.Invokation i = (InvSnooper.Invokation) getUserObject();
        MEMethod m = i.toMethod;
        MEClass c = i.toClass;
        return c.getClassName() + "@" + m.getName() + "(" + m.getArgumentsString() + ")" + m.getReturnClassString();
    }

    public int getLookupFlags() {
        return lookupFlags;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

}
