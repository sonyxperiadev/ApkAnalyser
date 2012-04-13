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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

public abstract class InjectionClassFieldAdd extends InjectionClassField {

    public InjectionClassFieldAdd(int access, String name, String desc) {
        super(access, name, desc);
    }

    @Override
    public int getInjectionType() {
        return Injection.FIELD_ADD_INJECTION;
    }

    @Override
    public void inject(ClassVisitor cv) {
        FieldVisitor fv = cv.visitField(getAccess(), getName(), getDesc(), getSignature(), getValue());
        fv.visitEnd();
    }
}
