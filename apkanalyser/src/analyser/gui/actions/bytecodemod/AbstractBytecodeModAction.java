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

package analyser.gui.actions.bytecodemod;

import gui.AppException;
import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.Icon;

import analyser.gui.ClassTree;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.RefMethod;
import analyser.logic.Reference;

import jerl.bcm.inj.Injection;
import mereflect.MEMethod;

public abstract class AbstractBytecodeModAction extends AbstractCanceableAction {
    private static final long serialVersionUID = 7648872114637938867L;
    protected static AbstractBytecodeModAction m_inst = null;

    protected AbstractBytecodeModAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    public static String getMethodSignature(MEMethod method) {
        return method.getName() + method.getDescriptor();
    }

    protected abstract Injection getInjection(String className,
            String methodSignature) throws Throwable;

    protected void modify(MEMethod method, ClassTree tree) throws Throwable {
        if (method.isAbstract()) {
            return;
        }

        BytecodeModificationMediator.getInstance().registerModification(
                method.getMEClass().getResource().getContext(),
                method.getMEClass(),
                getInjection(method.getMEClass().getName(),
                        getMethodSignature(method)),
                method);

        tree.findAndMarkNode(method, Reference.MODIFIED);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        ClassTree tree = ((MainFrame) getMainFrame()).getSelectedTree();
        Object ref = Selection.getSelectedObject();
        if (ref == null || !(ref instanceof Reference)) {
            return;
        }
        if (ref instanceof RefMethod && ((RefMethod) ref).getMethod().isAbstract()) {
            throw new AppException("Cannot modify an abstract method: " + ((RefMethod) ref).getName());
        }

        traverse((Reference) ref, tree);

        if (isRunning()) {
            getMainFrame().actionFinished(this);
        }
        ((MainFrame) getMainFrame()).getSelectedTree().repaint();
    }

    protected void traverse(Reference ref, ClassTree tree) throws Throwable {
        if (ref instanceof RefMethod) {
            modify(((RefMethod) ref).getMethod(), tree);
        } else {
            Iterator<Reference> i = ref.getChildren().iterator();
            while (i.hasNext()) {
                traverse(i.next(), tree);
            }
        }
    }

    @Override
    public String getWorkDescription() {
        return "Perform bytecode modifications";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Bytecode modification error", t);
        getMainFrame().initBottomInfo();
    }
}
