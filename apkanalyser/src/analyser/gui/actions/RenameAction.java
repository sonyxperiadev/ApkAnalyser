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

package analyser.gui.actions;

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefClass;
import analyser.logic.RefMethod;
import analyser.logic.Reference;


public class RenameAction extends AbstractCanceableAction {
    private static final long serialVersionUID = 8047703967984157779L;
    protected static RenameAction m_inst = null;

    public static RenameAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new RenameAction("Rename", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected RenameAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        Object mRef = Selection.getSelectedObject();
        if (mRef == null || (!(mRef instanceof RefMethod) && !(mRef instanceof RefClass))) {
            return;
        }

        String newName = (String) JOptionPane.showInputDialog(
                getMainFrame(),
                "", "New name",
                JOptionPane.QUESTION_MESSAGE, null, null,
                "");
        if (newName != null)
        {
            /*      int nameIx;
            ClassInfo[] cp;

            if (mRef instanceof RefClass) {
            MEClass mClass = ((RefClass)mRef).getMEClass();
            cp = mClass.getConstantPool();
            CiClass classInfo = (CiClass)cp[mClass.getThisClassIndex()];
            nameIx = classInfo.getNameIndex();
            } else {
            MEMethod mMethod = ((RefMethod) mRef).getMethod();
            cp = mMethod.getMEClass().getConstantPool();
            nameIx = mMethod.getNameIndex();
            }
            cp[nameIx] = new CiUtf8(newName);*/
            ((Reference) mRef).rename(newName);
        }
        getMainFrame().actionFinished(this);
        ((MainFrame) getMainFrame()).getSelectedTree().refreshSelectedNode();
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error renaming method", t);
    }

    @Override
    public String getWorkDescription() {
        return "Volatile rename";
    }
}