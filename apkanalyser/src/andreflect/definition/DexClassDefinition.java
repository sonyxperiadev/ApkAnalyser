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

package andreflect.definition;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Util.SparseArray;

public class DexClassDefinition {
    protected ClassDefItem classDefItem;
    protected ClassDataItem classDataItem;

    protected SparseArray<AnnotationSetItem> methodAnnotationsMap;
    protected SparseArray<AnnotationSetItem> fieldAnnotationsMap;
    protected SparseArray<AnnotationSetRefList> parameterAnnotationsMap;

    public DexClassDefinition(ClassDefItem classDefItem) {
        this.classDefItem = classDefItem;
        classDataItem = classDefItem.getClassData();
        buildAnnotationMaps();
        //findFieldsSetInStaticConstructor();
    }

    private void buildAnnotationMaps() {
        AnnotationDirectoryItem annotationDirectory = classDefItem.getAnnotations();
        if (annotationDirectory == null) {
            methodAnnotationsMap = new SparseArray<AnnotationSetItem>(0);
            fieldAnnotationsMap = new SparseArray<AnnotationSetItem>(0);
            parameterAnnotationsMap = new SparseArray<AnnotationSetRefList>(0);
            return;
        }

        methodAnnotationsMap = new SparseArray<AnnotationSetItem>(annotationDirectory.getMethodAnnotationCount());
        annotationDirectory.iterateMethodAnnotations(new AnnotationDirectoryItem.MethodAnnotationIteratorDelegate() {
            @Override
            public void processMethodAnnotations(MethodIdItem method, AnnotationSetItem methodAnnotations) {
                methodAnnotationsMap.put(method.getIndex(), methodAnnotations);
            }
        });

        fieldAnnotationsMap = new SparseArray<AnnotationSetItem>(annotationDirectory.getFieldAnnotationCount());
        annotationDirectory.iterateFieldAnnotations(new AnnotationDirectoryItem.FieldAnnotationIteratorDelegate() {
            @Override
            public void processFieldAnnotations(FieldIdItem field, AnnotationSetItem fieldAnnotations) {
                fieldAnnotationsMap.put(field.getIndex(), fieldAnnotations);
            }
        });

        parameterAnnotationsMap = new SparseArray<AnnotationSetRefList>(
                annotationDirectory.getParameterAnnotationCount());
        annotationDirectory.iterateParameterAnnotations(
                new AnnotationDirectoryItem.ParameterAnnotationIteratorDelegate() {
                    @Override
                    public void processParameterAnnotations(MethodIdItem method, AnnotationSetRefList parameterAnnotations) {
                        parameterAnnotationsMap.put(method.getIndex(), parameterAnnotations);
                    }
                });
    }

    public AnnotationSetItem getAnnotationSetItem(ClassDataItem.EncodedMethod method) {
        return methodAnnotationsMap.get(method.method.getIndex());
    }

    public AnnotationSetRefList getAnnotationSetRefList(ClassDataItem.EncodedMethod method) {
        return parameterAnnotationsMap.get(method.method.getIndex());
    }
}
