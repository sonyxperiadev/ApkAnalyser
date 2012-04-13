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

package gui;

import java.io.File;
import java.net.URI;

public class SelectableFile extends File implements Selectable {
    private static final long serialVersionUID = 9028306111073897902L;
    boolean selected;

    public SelectableFile(String arg0) {
        super(arg0);
    }

    public SelectableFile(URI arg0) {
        super(arg0);
    }

    public SelectableFile(File arg0, String arg1) {
        super(arg0, arg1);
    }

    public SelectableFile(String arg0, String arg1) {
        super(arg0, arg1);
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean b) {
        selected = b;
    }

}
