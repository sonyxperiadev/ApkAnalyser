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

package andreflect.gui.linebuilder;

import java.io.IOException;

import mereflect.CorruptBytecodeException;

import org.jf.baksmali.baksmali;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Analysis.SyntheticAccessorResolver;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import andreflect.ApkClassContext;
import andreflect.DexClass;
import andreflect.DexMethod;
import andreflect.DexReader;
import andreflect.definition.DexClassDefinition;

public class DalvikByteCodeLineBuilder extends LineBuilderFormatter {
    public static LineBuilder getByteCodeAssembler(DexMethod method, String prefix)
            throws CorruptBytecodeException {
        ApkClassContext apkContext = (ApkClassContext) method.getMEClass().getResource().getContext();
        DexFile dexFile = apkContext.getDex();
        if (baksmali.syntheticAccessorResolver == null
                || baksmali.syntheticAccessorResolver.dexFile != dexFile) {
            baksmali.syntheticAccessorResolver = new SyntheticAccessorResolver(dexFile);
        }

        DexClass dexClass = (DexClass) method.getMEClass();

        if (dexFile.isOdex()) {
            DexReader.prepareClassPath(dexClass);
            //set baksmali.deodex according to ui settings for decoding this method.
            baksmali.deodex = true;
        }

        LineBuilder lb = new LineBuilder();
        lb.newLine();
        try {
            DexClassDefinition classDef = dexClass.getDefinition();
            AnnotationSetItem annotationSet = classDef.getAnnotationSetItem(method.getEncodedMethod());
            AnnotationSetRefList parameterAnnotations = classDef.getAnnotationSetRefList(method.getEncodedMethod());

            DalvikIndentingWriterImpl writer = new DalvikIndentingWriterImpl(lb);
            writer.write("    ");
            method.getDefinition().writeTo(writer, annotationSet, parameterAnnotations);
        } catch (IOException e) {
            throw new CorruptBytecodeException();
        }
        return lb;
    }

    public static class DalvikBytecodeOffset extends Identifier {
        public final int line;
        public final int pc;
        public final Instruction instruction;

        public DalvikBytecodeOffset(Instruction ins, int line, int pc) {
            instruction = ins;
            this.line = line;
            this.pc = pc;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof DalvikBytecodeOffset ? ((DalvikBytecodeOffset) o).instruction == instruction : false;
        }
        //public int hashCode() {return pc & 0x3f;}
    }
}
