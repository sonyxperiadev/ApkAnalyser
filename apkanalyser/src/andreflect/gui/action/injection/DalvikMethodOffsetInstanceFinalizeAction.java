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

package andreflect.gui.action.injection;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.Icon;

import jerl.bcm.inj.Injection;
import mereflect.MEClass;
import mereflect.MEMethod;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Util.AccessFlags;

import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.RefClass;
import analyser.logic.Reference;
import andreflect.ApkClassContext;
import andreflect.DexClass;
import andreflect.DexMethod;
import andreflect.injection.ItemCreator;
import andreflect.injection.impl.DalvikMethodOffsetInstanceHash;

public class DalvikMethodOffsetInstanceFinalizeAction extends AbstractTreeBytecodeModAction {
    protected DalvikMethodOffsetInstanceFinalizeAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    private static final long serialVersionUID = -1874918510046504264L;
    protected static DalvikMethodOffsetInstanceFinalizeAction m_inst = null;
    protected static DalvikMethodOffsetInstanceFinalizeAction m_inst_oneclass = null;
    int traverseCount, traverseIndex = 0;
    DexClass dexClass = null;

    public static DalvikMethodOffsetInstanceFinalizeAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodOffsetInstanceFinalizeAction("Print finalize all local classes", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    public static DalvikMethodOffsetInstanceFinalizeAction getInstanceOneClass(MainFrame mainFrame)
    {
        if (m_inst_oneclass == null)
        {
            m_inst_oneclass = new DalvikMethodOffsetInstanceFinalizeAction("Print finalize this class", null);
            m_inst_oneclass.setMainFrame(mainFrame);
        }
        return m_inst_oneclass;
    }

    @Override
    protected Injection getInjection(String className, String methodSignature)
            throws Throwable {
        // not used
        return null;
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        Object obj = Selection.getSelectedObject();
        if (obj == null || !(obj instanceof Reference)) {
            return;
        }

        if (obj instanceof RefClass) {
            DexClass dexClass = (DexClass) ((RefClass) obj).getMEClass();
            modifyClass(dexClass);
        } else {
            Reference ref = (Reference) obj;

            if (ref != null) {
                traverseCount = 0;
                traverseIndex = 0;
                getTraverseCount(ref);
                if (traverseCount != 0) {
                    traverseInside(ref);
                }
            }
        }

        if (isRunning()) {
            getMainFrame().actionFinished(this);
        }
        ((MainFrame) getMainFrame()).getSelectedTree().repaint();

    }

    protected void getTraverseCount(Reference ref) throws Throwable {
        if (ref instanceof RefClass) {
            traverseCount++;
        } else {
            Iterator<Reference> i = ref.getChildren().iterator();
            while (i.hasNext()) {
                getTraverseCount(i.next());
            }
        }
    }

    protected void traverseInside(Reference ref) throws Throwable {
        if (ref instanceof RefClass) {
            DexClass dexClass = (DexClass) ((RefClass) ref).getMEClass();
            modifyClass(dexClass);
            getMainFrame().actionReportWork(this, 100 * traverseIndex++ / traverseCount);
        } else {
            Iterator<Reference> i = ref.getChildren().iterator();
            while (i.hasNext()) {
                traverseInside(i.next());
            }
        }

    }

    private void modifyClass(DexClass dexClass) {
        DexMethod dexMethod = hasFinalize(dexClass);
        if (dexMethod == null
                && dexClass.isInterface() == false) {
            dexMethod = injectFinalizeMethod(dexClass);
        }
        if (dexMethod != null) {
            createInjection(dexMethod);
        }
    }

    private DexMethod injectFinalizeMethod(DexClass dexClass) {
        DexMethod res = null;
        ClassDefItem classDefItem = dexClass.getClassDefItem();
        ClassDataItem classDataItem = classDefItem.getClassData();
        EncodedMethod[] virtualMethodsArray = null;
        ArrayList<EncodedMethod> virtualMethods = new ArrayList<EncodedMethod>();
        if (classDataItem.getVirtualMethods() != null
                && classDataItem.getVirtualMethods().length != 0) {
            for (EncodedMethod virtualMethod : classDataItem.getVirtualMethods()) {
                virtualMethods.add(virtualMethod);
            }
        }

        ItemCreator ic = new ItemCreator(((ApkClassContext) (dexClass.getResource().getContext())).getDex());

        MethodIdItem finalizeMethodIdItem = ic.addMethodIdItem(classDefItem.getClassType().getTypeDescriptor(),
                "V", new String[0], "finalize");

        CodeItem finalizeCodeItem = ic.addEmptyVirtualCodeItem();

        EncodedMethod finalizeEncodedMethod = new EncodedMethod(finalizeMethodIdItem,
                AccessFlags.PROTECTED.getValue(),
                finalizeCodeItem);

        virtualMethods.add(finalizeEncodedMethod);

        Collections.sort(virtualMethods);
        virtualMethodsArray = new EncodedMethod[virtualMethods.size()];
        virtualMethods.toArray(virtualMethodsArray);
        classDataItem.virtualMethods = virtualMethodsArray;

        res = new DexMethod(dexClass, finalizeEncodedMethod);
        res.setAccessFlags(DexMethod.ACC_PROTECTED);
        res.setNameIndex(0); //ignored
        res.setDescriptorIndex(0); //ignored
        res.setAttributes(null); //ignored
        res.m_dexInvokations = new ArrayList<MEMethod.Invokation>();
        res.setExceptions(new MEClass[0]);

        return res;
    }

    private void createInjection(DexMethod method) {
        DalvikMethodOffsetInstanceHash inj = new DalvikMethodOffsetInstanceHash(getMethodSignature(method)
                , method.getMEClass().getName(), false);

        int totalRegisters = method.getEncodedMethod().codeItem.getRegisterCount();
        int parameterRegisters = method.getEncodedMethod().method.getPrototype().getParameterRegisterCount();
        int thisRegister = totalRegisters - parameterRegisters - 1;

        inj.setRegister((short) thisRegister);

        BytecodeModificationMediator.getInstance().registerModification(
                method.getMEClass().getResource().getContext(),
                method.getMEClass(),
                inj,
                method);

        ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
    }

    public DexMethod hasFinalize(DexClass clazz) {
        MEMethod[] methods = clazz.getMethods("finalize");
        if (methods != null) {
            for (MEMethod method : methods) {
                DexMethod dexMethod = (DexMethod) method;
                if (!dexMethod.isPrivate()) {
                    return dexMethod;
                }
            }
        }

        return null;
    }

}
