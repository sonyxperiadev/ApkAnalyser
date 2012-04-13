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

package andreflect;

import java.io.IOException;
import java.io.InputStream;

import mereflect.AbstractClassResource;

import org.jf.dexlib.ClassDefItem;

public class DexResource extends AbstractClassResource {
    protected ClassDefItem m_item;

    protected String m_classname = null;
    protected String m_contextualSpec = null;

    public DexResource(ApkClassContext cxt, ClassDefItem classItem) {
        setContext(cxt);
        m_item = classItem;
    }

    @Override
    public String getClassName() {

        if (m_classname == null) {

            m_classname = Util.getClassName(m_item);

        }
        return m_classname;
    }

    @Override
    public String getContextualSpecification() {
        if (m_contextualSpec == null) {
            m_contextualSpec = getClassName() + "@" + ((ApkClassContext) getContext()).getFile().getAbsolutePath();
            //	System.out.println("[DexResource] getContextualSpecification="+ m_contextualSpec);
        }
        return m_contextualSpec;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    public ClassDefItem getClassDefItem() {
        return m_item;
    }
}
