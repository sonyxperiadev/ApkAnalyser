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

import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEField;
import mereflect.MEMethod;
import andreflect.ApkClassContext;
import andreflect.DexReferenceCache;
import brut.androlib.res.data.ResResSpec;

public interface ResolverListener
{
    public void resolving(int midlet, int midlets, int clazz, int classes, int method, int methods,
            MEClassContext ctx, MEClass clazzref, MEMethod methodref);

    public void resolvingField(int midlet, int midlets, int clazz, int classes, int field, int fields,
            ApkClassContext ctx, MEClass clazzref, MEField methodref);

    public void resolvingFieldAccess(int midlet, int midlets, int access, int accesses, ApkClassContext ctx, DexReferenceCache.FieldAccess fieldAccess);

    public void resolvingResource(int apkIndex, int apkCount, int resource, int resources, ApkClassContext apkCtx, ResResSpec spec);

    public void resolved();
}
