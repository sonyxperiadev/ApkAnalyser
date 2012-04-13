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

import java.io.Serializable;

import analyser.logic.RefPackage;


public class PackageComponent implements Serializable {
    private static final long serialVersionUID = -6433078344695160446L;
    protected String split = null;
    protected RefPackage ref;
    protected boolean inner = false;
    protected double width = 0;
    protected boolean isMidlet;

    protected boolean isLast;

    public void setLast() {
        isLast = true;
    }

    public boolean isLast() {
        return isLast;
    }

    public PackageComponent(String str, boolean isMidlet) {
        split = str;
        ref = null;
        this.isMidlet = isMidlet;
        isLast = false;
    }

    public PackageComponent(String str, RefPackage refPackage, boolean isMidlet) {
        split = str;
        ref = refPackage;
        this.isMidlet = isMidlet;
        isLast = false;
    }

    public boolean isMidlet() {
        return isMidlet;
    }

    public void setHasInnerPacakge(boolean hasInner) {
        inner = hasInner;
    }

    public boolean hasInnerPackage() {
        return inner;
    }

    public boolean isSameName(String name) {
        return split.equals(name);
    }

    public String getName() {
        return split;
    }

    public RefPackage getRefPackage() {
        return ref;
    }
}
