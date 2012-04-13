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

package mereflect;

import java.util.LinkedHashSet;
import java.util.Set;

import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResConfig;
import brut.androlib.res.data.ResConfigFlags;
import brut.androlib.res.data.ResID;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.data.ResType;

public class UnknownResSpec extends ResResSpec {

    private final ResID mId;

    public UnknownResSpec(int id) {
        super(null, null, null, null);
        mId = new ResID(id);
    }

    @Override
    public Set<ResResource> listResources() {
        return new LinkedHashSet<ResResource>();
    }

    @Override
    public ResResource getResource(ResConfig config) throws AndrolibException {
        return null;
    }

    @Override
    public ResResource getResource(ResConfigFlags config)
            throws AndrolibException {
        return null;
    }

    @Override
    public boolean hasResource(ResConfig config) {
        return false;
    }

    @Override
    public ResResource getDefaultResource() throws AndrolibException {
        return null;
    }

    @Override
    public boolean hasDefaultResource() {
        return false;
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public String getFullName(ResPackage relativeToPackage,
            boolean excludeType) {
        return getName();
    }

    @Override
    public String getFullName(boolean excludePackage, boolean excludeType) {
        return getName();
    }

    @Override
    public ResID getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mId.toString();
    }

    @Override
    public ResPackage getPackage() {
        return null;
    }

    @Override
    public ResType getType() {
        return null;
    }

    @Override
    public void addResource(ResResource res)
            throws AndrolibException {
    }

    @Override
    public void addResource(ResResource res, boolean overwrite)
            throws AndrolibException {
    }

    @Override
    public String toString() {
        return getName();
    }
}
