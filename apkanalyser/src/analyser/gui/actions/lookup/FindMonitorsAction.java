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

package analyser.gui.actions.lookup;

import gui.actions.AbstractCanceableAction;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.Icon;

import mereflect.CollaborateClassContext;
import mereflect.CorruptBytecodeException;
import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEMethod;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;

import analyser.gui.LineBuilder;
import analyser.gui.MainFrame;
import analyser.gui.Selection;
import analyser.logic.RefMethod;
import analyser.logic.Reference;
import andreflect.ApkClassContext;
import andreflect.DexMethod;

public class FindMonitorsAction extends AbstractCanceableAction {
    private static final long serialVersionUID = 8382352528921219013L;
    protected static FindMonitorsAction m_inst = null;
    protected List<ArrayList<StackEntry>> m_result;
    protected int m_totalInvoks;
    protected int m_traversedInvoks = 0;

    public static FindMonitorsAction getInstance(MainFrame mainFrame) {
        if (m_inst == null) {
            m_inst = new FindMonitorsAction("Find monitor calls", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected FindMonitorsAction(String arg0, Icon arg1) {
        super(arg0, arg1);
    }

    @Override
    public void run(ActionEvent e) throws Throwable {
        m_result = new ArrayList<ArrayList<StackEntry>>();

        Object ref = Selection.getSelectedObject();
        if (!(ref instanceof Reference)) {
            return;
        }

        CollaborateClassContext ctx = MainFrame.getInstance().getResolver().getReferenceContext();
        m_totalInvoks = ((Reference) ref).getCount() + 1;
        m_traversedInvoks = 0;
        traverse(ctx, new HashSet<MEMethod>(), new Stack<StackEntry>(), (Reference) ref);
        if (isRunning()) {
            getMainFrame().actionFinished(this);
            showResult();
        }
    }

    protected void traverse(MEClassContext ctx, Set<MEMethod> resolved,
            Stack<StackEntry> callStack, Reference ref) throws Throwable {
        if (!isRunning()) {
            return;
        }
        if (ref instanceof RefMethod) {
            m_traversedInvoks += ((RefMethod) ref).getCount();
            getMainFrame().actionReportWork(this, 100 * m_traversedInvoks / m_totalInvoks);
            MEMethod mMethod = ((RefMethod) ref).getMethod();
            recurseInvokations(ctx, resolved, callStack, mMethod);
        } else {
            Iterator<Reference> i = ref.getChildren().iterator();
            while (i.hasNext()) {
                traverse(ctx, resolved, callStack, i.next());
            }
        }
    }

    protected void recurseInvokations(MEClassContext ctx, Set<MEMethod> resolved,
            Stack<StackEntry> callStack, MEMethod mMethod) throws IOException {
        if (resolved.contains(mMethod) || !isRunning()) {
            return;
        }
        resolved.add(mMethod);
        callStack.push(new StackEntry(mMethod));

        List<MEMethod.Invokation> invokations = null;
        Iterator<MEMethod.Invokation> iI = null;
        if (mMethod.getMEClass().getResource().getContext().isMidlet()) {
            if (mMethod.getMEClass().getResource().getContext().getContextDescription().equals(ApkClassContext.DESCRIPTION)) {
                DexMethod method = (DexMethod) mMethod;
                if ((method.getEncodedMethod().codeItem != null && method.getEncodedMethod().codeItem.getInstructions().length != 0)) {
                    Instruction[] instructions = method.getEncodedMethod().codeItem.getInstructions();
                    for (int i = 0; i < instructions.length; i++) {
                        Instruction instruction = instructions[i];
                        Opcode code = instruction.deodexedInstruction.opcode;
                        if (code == Opcode.MONITOR_ENTER) {
                            reportMonitor(new StackEntry(mMethod, instruction.codeAddress), callStack);
                            //System.out.println("[FindMonitorsAction] Found DexMonitor in "+ mMethod.getDescriptor());
                        }
                    }
                }
            } else {
                try {
                    List<Integer> monitors = mMethod.getBytecode(194); // monitor enter
                    for (int i = 0; i < monitors.size(); i++) {
                        reportMonitor(new StackEntry(mMethod, (monitors.get(i)).intValue()), callStack);
                    }
                } catch (CorruptBytecodeException cbe) {
                }
            }
        }
        try {
            invokations = mMethod.getInvokations();
            iI = invokations.iterator();
        } catch (CorruptBytecodeException cbe) {
        }

        while (isRunning() && iI != null && iI.hasNext()) {
            MEMethod.Invokation invok = iI.next();
            try {
                MEClass rClass = ctx.getMEClass(invok.invClassname);
                MEMethod rMethod = rClass.getMethod(invok.invMethodname, invok.invDescriptor);
                if (rMethod == null) {
                    throw new ClassNotFoundException("Method " + invok.invMethodname + ":"
                            + invok.invDescriptor + " not found in class " + rClass.getName());
                }
                if (rMethod.isSynchronized()) {
                    reportMonitor(new StackEntry(rMethod), callStack);
                }
                if (rClass.getResource().getContext().isMidlet()) {
                    recurseInvokations(ctx, resolved, callStack, rMethod);
                }
            } catch (ClassNotFoundException e2) {
            }
        }
        callStack.pop();
    }

    protected void reportMonitor(StackEntry call, Stack<StackEntry> callStack) {
        StackEntry tmp = null;
        if (call.pcOffset >= 0) {
            // replace last entry in stack
            tmp = callStack.pop();
        }
        callStack.push(call);
        m_result.add(new ArrayList<StackEntry>(callStack));
        callStack.pop();
        if (call.pcOffset >= 0) {
            callStack.push(tmp);
        }
    }

    protected void showResult() {
        LineBuilder lb = new LineBuilder();
        lb.newLine();
        if (m_result.size() > 0) {
            for (int i = 0; i < m_result.size(); i++) {
                ArrayList<StackEntry> stack = m_result.get(i);
                for (int j = 0; j < stack.size(); j++) {
                    StackEntry te = stack.get(j);
                    lb.append(te.method.getMEClass().getName(), 0x000088);
                    lb.append(':', 0x000000);
                    lb.append(te.method.getFormattedName() + te.method.getDescriptor().replace('/', '.'), 0x008888);
                    if (te.pcOffset >= 0) {
                        lb.append(te.method.getFormattedName() + te.method.getDescriptor().replace('/', '.'), 0x008888);
                        lb.append(" monitorenter @ " + Integer.toHexString(te.pcOffset), 0x880000);
                    }
                    lb.newLine();
                }
                lb.newLine();
            }
            getMainFrame().showText("Monitors search result", lb);
            getMainFrame().initBottomInfo();
        } else {
            getMainFrame().setBottomInfo("No monitors found");
        }
    }

    @Override
    public void handleThrowable(Throwable t) {
        t.printStackTrace();
        getMainFrame().showError("Error resolving monitors", t);
    }

    @Override
    public String getWorkDescription() {
        return "Resolving monitors";
    }

    class StackEntry {
        MEMethod method;
        int pcOffset = -1;

        public StackEntry(MEMethod m) {
            method = m;
            pcOffset = -1;
        }

        public StackEntry(MEMethod m, int offset) {
            method = m;
            pcOffset = offset;
        }
    }
}
