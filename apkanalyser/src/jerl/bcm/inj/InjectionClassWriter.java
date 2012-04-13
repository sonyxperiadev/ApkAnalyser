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

package jerl.bcm.inj;

import jerl.bcm.util.CommonSuperClassIF;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class InjectionClassWriter extends ClassWriter {
    private CommonSuperClassIF commonSuper = null;

    public InjectionClassWriter(int flags) {
        super(flags);
    }

    public InjectionClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    public void setCommonSuperClassIF(CommonSuperClassIF commonSuper) {
        this.commonSuper = commonSuper;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        if (commonSuper != null) {
            return commonSuper.getCommonSuperClass(type1, type2);
        } else {
            return super.getCommonSuperClass(type1, type2);
        }
    }
}
