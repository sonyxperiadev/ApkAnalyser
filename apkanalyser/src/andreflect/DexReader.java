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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import mereflect.CollaborateClassContext;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEMethod;
import mereflect.UnknownClass;

import org.jf.baksmali.Adaptors.MethodItem;
import org.jf.baksmali.Adaptors.Format.ArrayDataMethodItem;
import org.jf.baksmali.Adaptors.Format.InstructionMethodItemFactory;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.EncodedArrayItem;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Item;
import org.jf.dexlib.ItemType;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.ClassPath.ClassDef;
import org.jf.dexlib.Code.Analysis.ClassPath.PrimitiveClassDef;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction31c;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.jf.dexlib.EncodedValue.AnnotationEncodedSubValue;
import org.jf.dexlib.EncodedValue.ArrayEncodedValue;
import org.jf.dexlib.EncodedValue.BooleanEncodedValue;
import org.jf.dexlib.EncodedValue.ByteEncodedValue;
import org.jf.dexlib.EncodedValue.CharEncodedValue;
import org.jf.dexlib.EncodedValue.DoubleEncodedValue;
import org.jf.dexlib.EncodedValue.EncodedValue;
import org.jf.dexlib.EncodedValue.FloatEncodedValue;
import org.jf.dexlib.EncodedValue.IntEncodedValue;
import org.jf.dexlib.EncodedValue.LongEncodedValue;
import org.jf.dexlib.EncodedValue.ShortEncodedValue;
import org.jf.dexlib.EncodedValue.StringEncodedValue;
import org.jf.dexlib.EncodedValue.TypeEncodedValue;
import org.jf.dexlib.EncodedValue.ValueType;
import org.jf.dexlib.Util.AccessFlags;
import org.jf.dexlib.Util.ExceptionWithContext;

import analyser.gui.MainFrame;
import andreflect.definition.DexClassDefinition;

public final class DexReader {

    public static DexClass readClassFile(DexResource classResource, boolean isodex) {

        //System.out.println("[DexClassReader] reading "+classResource.getClassName());
        ClassDefItem classDefItem = classResource.getClassDefItem();
        ClassDataItem classDataItem = classDefItem.getClassData();
        DexClass c = null;

        assert classDefItem != null;

        c = new DexClass(classDefItem);
        c.setResource(classResource);
        c.setMajorVersion(0); //ignored
        c.setMinorVersion(0); //ignored

        int access_dex = classDefItem.getAccessFlags();
        int accessflag = (access_dex & AccessFlags.PUBLIC.getValue()) != 0 ? DexClass.ACC_PUBLIC : 0;
        accessflag |= (access_dex & AccessFlags.FINAL.getValue()) != 0 ? DexClass.ACC_FINAL : 0;
        accessflag |= (access_dex & AccessFlags.INTERFACE.getValue()) != 0 ? DexClass.ACC_INTERFACE : 0;
        accessflag |= (access_dex & AccessFlags.ABSTRACT.getValue()) != 0 ? DexClass.ACC_ABSTRACT : 0;

        c.setAccessFlags(accessflag);

        c.setThisClassIndex(0); //ignored
        c.setSuperClassIndex(0); //ignored

        // Interfaces
        c.setInterfaceIndices(null); //ignored

        if (classDataItem != null)
        {
            // Fields
            int staticFieldLen = (classDataItem.getStaticFields() != null) ? classDataItem.getStaticFields().length : 0;
            int instanceFieldLen = (classDataItem.getInstanceFields() != null) ? classDataItem.getInstanceFields().length : 0;
            int fLen = staticFieldLen + instanceFieldLen;
            DexField[] fields = new DexField[fLen];
            int fieldIdx = 0;

            //static initial values
            EncodedArrayItem encodedStaticInitializers = classDefItem.getStaticFieldInitializers();
            EncodedValue[] staticInitializers;
            if (encodedStaticInitializers != null) {
                staticInitializers = encodedStaticInitializers.getEncodedArray().values;
            } else {
                staticInitializers = new EncodedValue[0];
            }

            ClassDataItem.EncodedField[] encodedStaticFields = classDataItem.getStaticFields();
            for (int i = 0; i < staticFieldLen; i++)
            {
                ClassDataItem.EncodedField field = encodedStaticFields[i];
                EncodedValue encodedValue = null;
                if (i < staticInitializers.length) {
                    encodedValue = staticInitializers[i];
                }

                fields[fieldIdx] = readFieldInfo(c, field, encodedValue);
                //System.out.println("[DexClassReader] field static:"+ fields[fieldIdx].getName());
                fieldIdx++;
            }

            ClassDataItem.EncodedField[] encodedInstanceFields = classDataItem.getInstanceFields();
            for (int i = 0; i < instanceFieldLen; i++)
            {
                ClassDataItem.EncodedField field = encodedInstanceFields[i];
                fields[fieldIdx] = readFieldInfo(c, field, null);
                //System.out.println("[DexClassReader] field instance:"+ fields[fieldIdx].getName());
                fieldIdx++;
            }
            c.setFields(fields);

            // Methods
            int directMethodLen = (classDataItem.getDirectMethods() != null) ? classDataItem.getDirectMethods().length : 0;
            int virtualMethodLen = (classDataItem.getVirtualMethods() != null) ? classDataItem.getVirtualMethods().length : 0;

            int mLen = directMethodLen + virtualMethodLen;
            DexMethod[] methods = new DexMethod[mLen];
            int methodIdx = 0;

            ClassDataItem.EncodedMethod[] directMethods = classDataItem.getDirectMethods();
            for (int i = 0; i < directMethodLen; i++)
            {
                ClassDataItem.EncodedMethod method = directMethods[i];
                methods[methodIdx] = readMethodInfo(c, method, isodex);
                //System.out.println("[DexClassReader] method direct:"+ methods[methodIdx].getName());
                methodIdx++;
            }

            ClassDataItem.EncodedMethod[] virtualMethods = classDataItem.getVirtualMethods();
            for (int i = 0; i < virtualMethodLen; i++)
            {
                ClassDataItem.EncodedMethod method = virtualMethods[i];
                methods[methodIdx] = readMethodInfo(c, method, isodex);
                //System.out.println("[DexClassReader] method virtual:"+ methods[methodIdx].getName());
                methodIdx++;
            }
            c.setMethods(methods);
        } else {
            c.setFields(new DexField[0]);
            c.setMethods(new DexMethod[0]);
            //System.out.println("[DexClassReader] class no data item " + c.getName());
        }

        return c;
    }

    protected static DexField readFieldInfo(DexClass clazz, ClassDataItem.EncodedField field, EncodedValue encodedValue) {
        DexField res = null;
        res = new DexField(clazz, field);

        int access_dex = field.accessFlags;

        int accessflag = (access_dex & AccessFlags.PUBLIC.getValue()) != 0 ? DexField.ACC_PUBLIC : 0;
        accessflag |= (access_dex & AccessFlags.FINAL.getValue()) != 0 ? DexField.ACC_FINAL : 0;
        accessflag |= (access_dex & AccessFlags.PRIVATE.getValue()) != 0 ? DexField.ACC_PRIVATE : 0;
        accessflag |= (access_dex & AccessFlags.PROTECTED.getValue()) != 0 ? DexField.ACC_PROTECTED : 0;
        accessflag |= (access_dex & AccessFlags.STATIC.getValue()) != 0 ? DexField.ACC_STATIC : 0;
        accessflag |= (access_dex & AccessFlags.TRANSIENT.getValue()) != 0 ? DexField.ACC_TRANSIENT : 0;
        accessflag |= (access_dex & AccessFlags.VOLATILE.getValue()) != 0 ? DexField.ACC_VOLATILE : 0;

        res.setAccessFlags(accessflag);

        res.setNameIndex(0); //ignored
        res.setDescriptorIndex(0); //ignored
        res.setAttributes(null); //ignored

        if (encodedValue != null) {
            switch (encodedValue.getValueType()) {
            case VALUE_BOOLEAN:
                res.m_constant = new Boolean(((BooleanEncodedValue) encodedValue).value);
                break;
            case VALUE_BYTE:
                res.m_constant = new Long(((ByteEncodedValue) encodedValue).value);
                break;
            case VALUE_CHAR:
                res.m_constant = new Character(((CharEncodedValue) encodedValue).value);
                break;
            case VALUE_DOUBLE:
                res.m_constant = new Double(((DoubleEncodedValue) encodedValue).value);
                break;
            case VALUE_FLOAT:
                res.m_constant = new Float(((FloatEncodedValue) encodedValue).value);
                break;
            case VALUE_INT:
                res.m_constant = new Long(((IntEncodedValue) encodedValue).value);
                break;
            case VALUE_LONG:
                res.m_constant = new Long(((LongEncodedValue) encodedValue).value);
                break;
            case VALUE_SHORT:
                res.m_constant = new Long(((ShortEncodedValue) encodedValue).value);
                break;
            case VALUE_STRING:
                res.m_constant = new String(((StringEncodedValue) encodedValue).value.getStringValue());
                break;
            default:
                break;
            }
        }

        return res;
    }

    protected static DexMethod readMethodInfo(DexClass clazz, ClassDataItem.EncodedMethod method, boolean isodex)
    {
        DexMethod res = null;
        res = new DexMethod(clazz, method);
        int access_dex = method.accessFlags;

        int accessflag = (access_dex & AccessFlags.PUBLIC.getValue()) != 0 ? DexMethod.ACC_PUBLIC : 0;
        accessflag |= (access_dex & AccessFlags.FINAL.getValue()) != 0 ? DexMethod.ACC_FINAL : 0;
        accessflag |= (access_dex & AccessFlags.PRIVATE.getValue()) != 0 ? DexMethod.ACC_PRIVATE : 0;
        accessflag |= (access_dex & AccessFlags.PROTECTED.getValue()) != 0 ? DexMethod.ACC_PROTECTED : 0;
        accessflag |= (access_dex & AccessFlags.STATIC.getValue()) != 0 ? DexMethod.ACC_STATIC : 0;
        accessflag |= (access_dex & AccessFlags.NATIVE.getValue()) != 0 ? DexMethod.ACC_NATIVE : 0;
        accessflag |= (access_dex & AccessFlags.ABSTRACT.getValue()) != 0 ? DexMethod.ACC_ABSTRACT : 0;
        accessflag |= (access_dex & AccessFlags.STRICTFP.getValue()) != 0 ? DexMethod.ACC_STRICT : 0;
        accessflag |= (access_dex & (AccessFlags.SYNCHRONIZED.getValue() | AccessFlags.DECLARED_SYNCHRONIZED.getValue())) != 0 ? DexMethod.ACC_SYNCHRONIZED : 0;

        res.setAccessFlags(accessflag);
        //System.out.println("[DexClassReader] method="+ method.method.getMethodString()+" acc="+accessflag + " oriacc="+ access_dex);

        res.setNameIndex(0); //ignored
        res.setDescriptorIndex(0); //ignored
        res.setAttributes(null); //ignored
        res.m_dexInvokations = new ArrayList<MEMethod.Invokation>();
        res.setExceptions(new MEClass[0]);

        DexReferenceCache desRefCache = ((ApkClassContext) clazz.getResource().getContext()).getDexReferenceCache();

        if ((accessflag &
                (DexMethod.ACC_ABSTRACT | DexMethod.ACC_NATIVE)) == 0
                && method.codeItem != null
                && clazz.getResource().getContext().isMidlet() == true) {
            try {
                method.codeItem.registerOriginalCount = method.codeItem.getRegisterCount();
                Instruction[] instructions = method.codeItem.getInstructions();
                int currentCodeAddress = 0;

                List<AnalyzedInstruction> analysedInstructions = null;
                if (isodex) {
                    prepareClassPath(clazz);
                    //TODO: do not support customlized inline table now, could be read from device later.
                    MethodAnalyzer methodAnalyser = new MethodAnalyzer(method, isodex, null);
                    methodAnalyser.analyze();
                    analysedInstructions = methodAnalyser.getInstructions();
                }

                for (int i = 0; i < instructions.length; i++) {
                    Instruction instruction = instructions[i];
                    instruction.codeAddress = currentCodeAddress;
                    instruction.line = -1;
                    instruction.deodexedInstruction = analysedInstructions != null ? analysedInstructions.get(i).getInstruction() : instruction;

                    //add pc
                    currentCodeAddress += instruction.getSize(currentCodeAddress);

                    //method invocations
                    boolean isVirtual = false;
                    boolean isInterface = false;
                    boolean isInvoke = false;
                    //boolean isOdexInvoke = false;
                    switch (instruction.deodexedInstruction.opcode) {
                    /*
                    case INVOKE_VIRTUAL_QUICK:
                    case INVOKE_VIRTUAL_QUICK_RANGE:
                    	isOdexInvoke = true;
                     */
                    case INVOKE_VIRTUAL:
                    case INVOKE_VIRTUAL_RANGE:
                        isVirtual = true;
                        isInvoke = true;
                        break;
                    case INVOKE_INTERFACE:
                    case INVOKE_INTERFACE_RANGE:
                        isInterface = true;
                        isInvoke = true;
                        break;
                    /*
                    case EXECUTE_INLINE:
                    case EXECUTE_INLINE_RANGE:
                    case INVOKE_DIRECT_EMPTY:
                    case INVOKE_SUPER_QUICK:
                    case INVOKE_SUPER_QUICK_RANGE:
                    isOdexInvoke = true;
                     */
                    case INVOKE_SUPER:
                    case INVOKE_DIRECT:
                    case INVOKE_STATIC:
                    case INVOKE_DIRECT_RANGE:
                    case INVOKE_SUPER_RANGE:
                    case INVOKE_STATIC_RANGE:
                        isInvoke = true;
                        break;
                    default:
                        break;
                    }

                    InstructionWithReference instructionRef = null;
                    if (isInvoke == true
                            && instruction instanceof InstructionWithReference) {
                        instructionRef = (InstructionWithReference) instruction;
                    }/*else if(isOdexInvoke == true
                     	&& methodAnalyser!= null){
                     AnalyzedInstruction analysedIns = analysedInstructions.get(i);
                     Instruction ins = analysedIns.getInstruction();
                     if (ins instanceof InstructionWithReference){
                     	instructionRef = (InstructionWithReference)ins;
                     }
                     }*/

                    if (instructionRef != null) {
                        Item<?> item = instructionRef.getReferencedItem();
                        if (item.getItemType() == ItemType.TYPE_METHOD_ID_ITEM) {
                            MethodIdItem methodIdItem = (MethodIdItem) item;
                            TypeIdItem methodClass = methodIdItem.getContainingClass();
                            MEMethod.Invokation inv = new MEMethod.Invokation(Util.getClassName(methodClass.getTypeDescriptor()),
                                    methodIdItem.getMethodName().getStringValue(),
                                    methodIdItem.getPrototype().getPrototypeString(),
                                    instruction.codeAddress,
                                    i,
                                    isVirtual,
                                    isInterface,
                                    instruction);
                            //System.out.println("[DexClassReader] invoke:" + Util.getClassName(methodClass.getTypeDescriptor()) + "->" +methodIdItem.getMethodName().getStringValue() + "->" +methodIdItem.getPrototype().getPrototypeString());
                            res.m_dexInvokations.add(inv);
                            continue;
                        }
                    }

                    //field accesses
                    boolean read = true;
                    Item<?> item = null;
                    switch (instruction.deodexedInstruction.opcode) {
                    case IPUT:
                    case IPUT_WIDE:
                    case IPUT_OBJECT:
                    case IPUT_BOOLEAN:
                    case IPUT_BYTE:
                    case IPUT_CHAR:
                    case IPUT_SHORT:
                    case SPUT:
                    case SPUT_WIDE:
                    case SPUT_OBJECT:
                    case SPUT_BOOLEAN:
                    case SPUT_BYTE:
                    case SPUT_CHAR:
                    case SPUT_SHORT:
                        read = false;
                        item = ((InstructionWithReference) instruction).getReferencedItem();
                        break;
                    /*
                    case IPUT_QUICK:
                    case IPUT_WIDE_QUICK:
                    case IPUT_OBJECT_QUICK:
                    //for gingerbread
                    case IPUT_VOLATILE:
                    case IPUT_WIDE_VOLATILE:
                    case IPUT_OBJECT_VOLATILE:
                    case SPUT_VOLATILE:
                    case SPUT_WIDE_VOLATILE:
                    case SPUT_OBJECT_VOLATILE:
                    {
                    Instruction deodexedIns = analysedInstructions.get(i).getInstruction();
                    if (deodexedIns instanceof InstructionWithReference){
                    	read = false;
                    	item = ((InstructionWithReference)deodexedIns).getReferencedItem();
                    }
                    break;
                    }
                     */
                    case IGET:
                    case IGET_WIDE:
                    case IGET_OBJECT:
                    case IGET_BOOLEAN:
                    case IGET_BYTE:
                    case IGET_CHAR:
                    case IGET_SHORT:
                    case SGET:
                    case SGET_WIDE:
                    case SGET_OBJECT:
                    case SGET_BOOLEAN:
                    case SGET_BYTE:
                    case SGET_CHAR:
                    case SGET_SHORT:
                        read = true;
                        item = ((InstructionWithReference) instruction).getReferencedItem();
                        break;
                    /*
                    case IGET_QUICK:
                    case IGET_WIDE_QUICK:
                    case IGET_OBJECT_QUICK:
                    //for gingerbread
                    case IGET_VOLATILE:
                    case IGET_WIDE_VOLATILE:
                    case IGET_OBJECT_VOLATILE:
                    case SGET_VOLATILE:
                    case SGET_WIDE_VOLATILE:
                    case SGET_OBJECT_VOLATILE:
                    {
                    Instruction deodexedIns = analysedInstructions.get(i).getInstruction();
                    if (deodexedIns instanceof InstructionWithReference){
                    	read = true;
                    	item = ((InstructionWithReference)deodexedIns).getReferencedItem();
                    }
                    break;
                    }
                     */
                    default:
                        break;
                    }

                    if (item != null) {
                        desRefCache.addFieldAccessReference(new DexReferenceCache.FieldAccess((FieldIdItem) item, res, instruction, instruction.codeAddress, read));
                        continue;
                    }

                    //const access for resources
                    int literal = 0;
                    switch (instruction.deodexedInstruction.opcode) {
                    case CONST:
                        literal = (int) ((Instruction31i) instruction).getLiteral();
                        break;
                    case CONST_HIGH16:
                        literal = (int) ((Instruction21h) instruction).getLiteral() << 16;
                        break;
                    default:
                        break;
                    }

                    if (literal != 0) {
                        desRefCache.addCodeReference(new DexReferenceCache.LoadConstRes(literal, res, instruction, instruction.codeAddress));
                        continue;
                    }

                    MethodItem methodItem = InstructionMethodItemFactory.makeInstructionFormatMethodItem(res.getDefinition(),
                            method.codeItem, instruction.codeAddress, instruction);

                    if (methodItem instanceof ArrayDataMethodItem) {
                        ArrayDataMethodItem arrayDataItem = (ArrayDataMethodItem) methodItem;
                        Iterator<ArrayDataPseudoInstruction.ArrayElement> iterator = arrayDataItem.instruction.getElements();
                        while (iterator.hasNext()) {
                            ArrayDataPseudoInstruction.ArrayElement element = iterator.next();
                            if (element.elementWidth == 4) {
                                int id = 0;
                                for (int j = 0; j < 4; j++) {
                                    id |= (element.buffer[element.bufferIndex + j] & 0xFF) << (j * 8);
                                }
                                //FIXME cannot locate array item correctly
                                desRefCache.addCodeReference(new DexReferenceCache.LoadConstRes(id, res, instructions[0], instructions[0].codeAddress));
                            }
                        }
                    }

                    String constSting = null;
                    switch (instruction.deodexedInstruction.opcode) {
                    case CONST_STRING:
                        constSting = ((StringIdItem) ((Instruction21c) instruction).getReferencedItem()).getStringDataItem().getStringValue();
                        break;
                    case CONST_STRING_JUMBO:
                        constSting = ((StringIdItem) ((Instruction31c) instruction).getReferencedItem()).getStringDataItem().getStringValue();
                        break;
                    default:
                        break;
                    }

                    if (constSting != null) {
                        desRefCache.addCodeConstString(new DexReferenceCache.LoadConstString(constSting, res, instruction, instruction.codeAddress));
                    }

                }

                res.getDefinition().prepareLineNumbers();

                int exceptionSize = 0;
                ArrayList<MEClass> exceptions = new ArrayList<MEClass>();
                DexClassDefinition def = clazz.getDefinition();
                AnnotationSetItem annotationSet = def.getAnnotationSetItem(method);
                if (annotationSet != null) {
                    for (AnnotationItem annotationItem : annotationSet.getAnnotations()) {

                        if (annotationItem.getEncodedAnnotation().annotationType.getTypeDescriptor().equals("Ldalvik/annotation/Throws;")) {
                            AnnotationEncodedSubValue encodedAnnotation = annotationItem.getEncodedAnnotation();
                            for (int m = 0; m < encodedAnnotation.names.length; m++) {
                                //System.out.println("[DexClassReader] annType:"+ annotationItem.getEncodedAnnotation().annotationType.getTypeDescriptor());
                                EncodedValue v = encodedAnnotation.values[m];
                                if (v.getValueType() == ValueType.VALUE_ARRAY) {
                                    ArrayEncodedValue array = (ArrayEncodedValue) v;
                                    EncodedValue[] values = array.values;
                                    for (EncodedValue encodedValue : values) {
                                        String name = null;
                                        if (encodedValue.getValueType() == ValueType.VALUE_TYPE) {
                                            name = ((TypeEncodedValue) encodedValue).value.getTypeDescriptor();
                                            MEClass exceptionClass;
                                            try {
                                                //System.out.println("[DexClassReader] exceptionClass:  "+ Util.getClassName(name));

                                                //bugfix, if a new exception class throw itself in some method, it will overflow.
                                                if (Util.getClassName(name).equals(clazz.getName())) {
                                                    exceptionClass = clazz;
                                                } else {
                                                    exceptionClass = clazz.getResource().getContext().getMEClass(Util.getClassName(name));
                                                }
                                            } catch (ClassNotFoundException cnfe) {
                                                exceptionClass = new UnknownClass(Util.getClassName(name), clazz.getResource());
                                            }
                                            exceptions.add(exceptionClass);
                                            exceptionSize++;
                                        }

                                    }

                                }
                            }
                        }
                    }
                    res.setExceptions(exceptions.toArray(new MEClass[exceptionSize]));
                }
            }

            catch (IllegalArgumentException e) {
                if (!e.getMessage().equals("The method has no code")) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static void prepareClassPath(DexClass clazz) {

        ApkClassContext thisContext = (ApkClassContext) ((clazz.getResource().getContext()));
        if (ClassPath.theClassPath == null
                || ClassPath.dexFile != thisContext.getDex()) {
            System.out.println("[DexClassReader] Classpath reset for " + thisContext.getFile().getPath());
            ClassPath.theClassPath = null;
            ClassPath.theClassPath = new ClassPath();
            ClassPath.theClassPath.tempClasses = new LinkedHashMap<String, ClassPath.TempClassInfo>();

            CollaborateClassContext ctx = MainFrame.getInstance().getResolver().getReferenceContext();
            MEClassContext[] contexts = ctx.getContexts();
            for (MEClassContext context : contexts) {
                if (context instanceof ApkClassContext) {
                    ApkClassContext apkContext = (ApkClassContext) context;
                    ClassPath.theClassPath.loadDexFile(apkContext.getFile().getPath(), apkContext.getDex());
                }
            }
            ClassPath.theClassPath.loadDexFile(thisContext.getFile().getPath(), thisContext.getDex());

            if (ClassPath.theClassPath.tempClasses.containsKey("Ljava/lang/Object;")) {
                ClassDef classDef = null;
                try {
                    classDef = ClassPath.loadClassDef("Ljava/lang/Object;");
                    assert classDef != null;
                } catch (Exception ex) {
                    throw ExceptionWithContext.withContext(ex,
                            "Can not load java/lang/object");
                }
                ClassPath.theClassPath.javaLangObjectClassDef = classDef;
            } else {
                throw new RuntimeException("Can not find java/lang/object");
            }

            for (String classType : ClassPath.theClassPath.tempClasses.keySet()) {
                ClassDef classDef = null;
                try {
                    classDef = ClassPath.loadClassDef(classType);
                    assert classDef != null;
                } catch (Exception ex) {
                    throw ExceptionWithContext.withContext(ex,
                            String.format("Error while loading ClassPath class %s", classType));
                }
            }

            for (String primitiveType : new String[] { "Z", "B", "S", "C", "I", "J", "F", "D" }) {
                ClassDef classDef = new PrimitiveClassDef(primitiveType);
                ClassPath.theClassPath.classDefs.put(primitiveType, classDef);
            }

            ClassPath.theClassPath.tempClasses = null;

            ClassPath.dexFile = thisContext.getDex();
        }
    }
}
