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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import jerl.bcm.util.CommonSuperClassIF;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class InjectionEngine {
    private CommonSuperClassIF commonSuper = null;
    private boolean isDebugMode = false;

    public void setDebugMode(boolean on) {
        isDebugMode = on;
    }

    public void setCommonSuperClassIF(CommonSuperClassIF commonSuper) {
        this.commonSuper = commonSuper;
    }

    public byte[] preformInjection(InputStream class_is, InjectionMethod[] methodInjections) throws IOException {
        return preformInjection(class_is, methodInjections, new InjectionClassField[0]);
    }

    /**
     * Preform the specified bytecode injections on the given class.
     * @param class_is input stream to the class thath we should inject in
     * @param methodInjections
     * @return byte array of the modified class
     * @throws IOException
     */
    public byte[] preformInjection(InputStream class_is, InjectionMethod[] methodInjections, InjectionClass[] classInjections) throws IOException {
        // sort injection parameters
        Arrays.sort(methodInjections);

        // loads the original class and modify it
        ClassReader cr = new ClassReader(class_is);
        InjectionClassWriter cw = new InjectionClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.setCommonSuperClassIF(commonSuper);
        InjectionClassAdapter cv;
        cv = new InjectionClassAdapter(cw, methodInjections, classInjections);
        cv.setDebugMode(isDebugMode);

        cr.accept(cv, 0);
        byte[] b = cw.toByteArray();
        return b;
    }
}
