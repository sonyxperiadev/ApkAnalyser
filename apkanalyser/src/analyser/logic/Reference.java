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

package analyser.logic;

import java.util.Collection;

/**
 * A Reference indicates a reference from either a context element to another, including all subcategories.
 */
public interface Reference extends Comparable<Reference>
{
    public static final int FAILED = 0x01;
    public static final int NOTFOUND = 0x02;
    public static final int MODIFIED = 0x04;

    public String getName();

    public int getCount();

    public void setCount(int i);

    public void addCount(int i);

    public Collection<Reference> getChildren();

    public int getFlags();

    public void setFlags(int flags);

    public Reference getParent();

    public Object getReferred();

    public void rename(String name);

    public void removeRename();
}
