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

package andreflect.gui.action;

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import mereflect.MEField;
import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.gui.TextBuilder;
import analyser.gui.actions.ShowBytecodeAction;
import analyser.logic.RefContext;
import analyser.logic.RefField;
import analyser.logic.Reference;
import andreflect.ApkClassContext;
import andreflect.DexField;
import andreflect.DexReferenceCache;

public class DalvikFindFieldAccessAction extends AbstractCanceableAction {
    private static final long serialVersionUID = -6753655310053867336L;
    protected static DalvikFindFieldAccessAction m_inst = null;

    protected DalvikFindFieldAccessAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    public static DalvikFindFieldAccessAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new DalvikFindFieldAccessAction("Find field accesses (excluding subclass)", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    @Override
    public String getWorkDescription() {
        return "Resolving field accesses (excluding subclass)";
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error resolving field accesses", t);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {

        MEField field = null;

        if (Selection.getSelectedView() instanceof TextBuilder) {
            TextBuilder tb = (TextBuilder) Selection.getSelectedView();
            Object lineRef = tb.getLineBuilder().getReference(tb.getCurrentLine());
            if (lineRef != null
                    && lineRef instanceof DexField) {
                field = (MEField) lineRef;
            }
        } else {
            Object ref = Selection.getSelectedObject();
            if (ref != null && ref instanceof RefField) {
                RefField rf = (RefField) ref;
                field = rf.getField();
            }

        }

        if (field == null) {
            return;
        }

        List<DexReferenceCache.FieldAccess> result = new ArrayList<DexReferenceCache.FieldAccess>();
        Collection<Reference> references = MainFrame.getInstance().getResolver().getMidletResources();
        for (Reference ref : references) {
            if (ref instanceof RefContext
                    && ((RefContext) ref).getContext() instanceof ApkClassContext) {
                ApkClassContext apkContext = (ApkClassContext) ((RefContext) ref).getContext();

                List<DexReferenceCache.FieldAccess> accesses = apkContext.getDexReferenceCache().findFieldAccesses(field.getName(), field.getDescriptor(), field.getMEClass().getName());
                for (DexReferenceCache.FieldAccess access : accesses) {
                    result.add(access);
                }
            }
        }

        if (isRunning()) {
            getMainFrame().actionFinished(this);
            showResult(result);
        }

    }

    protected void showResult(List<DexReferenceCache.FieldAccess> result) {
        LineBuilder lb = new LineBuilder();
        if (result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                DexReferenceCache.FieldAccess e = result.get(i);
                lb.newLine();
                lb.append(e.isRead ? "RD  " : "WR  ", e.isRead ? 0x880000 : 0x008800);
                lb.append(e.method.getMEClass().getName() + " : ", 0x000000);
                LineBuilderFormatter.makeOutline(e.method, lb);
                lb.append(" @ ", 0x000000);
                lb.append(Integer.toHexString(e.pc), 0x000088);
                if (e.instruction.line != -1) {
                    lb.append(" (line " + e.instruction.line + " )", 0x000088);
                }
                Object[] data = { e.method, new Integer(e.pc), e.instruction };
                lb.setReferenceToCurrent(new LineBuilderFormatter.Link(
                        ShowBytecodeAction.getInstance((MainFrame) getMainFrame()),
                        data));
            }
            getMainFrame().showText("Field access search result", lb);
            getMainFrame().setBottomInfo(result.size() + " access(es) found");
        } else {
            getMainFrame().setBottomInfo("No accesses found");
        }
    }

}
