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

package andreflect.injection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jerl.bcm.inj.InjectionMethod;
import jerl.bcm.util.ClassInjContainer;
import jerl.bcm.util.InjectionUtil;
import mereflect.MEClass;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.EncodedTypeAddrPair;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.DebugInfoItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.MultiOffsetInstruction;
import org.jf.dexlib.Code.OffsetInstruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Debug.DebugInstructionIterator;
import org.jf.dexlib.Util.AccessFlags;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;
import org.jf.dexlib.Util.ByteArrayInput;
import org.jf.dexlib.Util.SparseIntArray;

import util.JarFileModifier;
import analyser.gui.ProgressReporter;
import analyser.logic.BytecodeModificationMediator;
import andreflect.ApkClassContext;
import andreflect.DexClass;
import andreflect.DexMethod;
import andreflect.injection.abs.DalvikInjectionMethod;
import andreflect.sign.ApkSign;

public class DalvikBytecodeModificationMediator {
    private static final String MOD_LIST_FILENAME = "modlist.cfg";
    private static final String DEX_FILENAME = "classes.dex";

    public static File performRegisteredModifications(BytecodeModificationMediator bmm,
            ProgressReporter pr,
            final ApkClassContext ctx,
            final String midletNamePostfix) throws IOException, Exception {
        Map<MEClass, ClassInjContainer> classInjectionsInContext = bmm.getModifications(ctx);
        if (classInjectionsInContext != null) {

            // perform bytecode modifications
            modifyClasses(pr, ctx, classInjectionsInContext);

            // create modification spec
            final ByteArrayOutputStream modSpecOut = new ByteArrayOutputStream();
            Iterator<MEClass> classI = classInjectionsInContext.keySet().iterator();
            while (classI.hasNext()) {
                MEClass clazz = classI.next();
                ClassInjContainer injContainer = classInjectionsInContext.get(clazz);
                InjectionUtil.deconstructClassToStream(injContainer,
                        new PrintStream(modSpecOut));
            } // per class

            ZipFile zipFile = new ZipFile(ctx.getFile().getAbsolutePath());

            final List<String> newEntries = new ArrayList<String>();
            newEntries.add(DEX_FILENAME);
            if (zipFile.getEntry(MOD_LIST_FILENAME) == null) {
                newEntries.add(MOD_LIST_FILENAME);
            }

            final List<String> excludeEntries = new ArrayList<String>();
            excludeEntries.add(DEX_FILENAME);

            // remove META-INF folder and all its contents
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                if (!entry.isDirectory()) {
                    if (entry.getName().startsWith("META-INF")) {
                        excludeEntries.add(entry.getName());
                    }
                }
            }

            JarFileModifier jfModder = new JarFileModifier(zipFile) {
                @Override
                public List<String> excludeEntries() {
                    return excludeEntries;
                }

                @Override
                public List<String> newEntries() {
                    return newEntries;
                }

                @Override
                public InputStream getNewEntry(String entryName, boolean modified) {
                    if (entryName.equals(MOD_LIST_FILENAME)) {
                        return new ByteArrayInputStream(modSpecOut.toByteArray());
                    } else if (entryName.equals(DEX_FILENAME)) {
                        return new ByteArrayInputStream(getAsByteArray(ctx.getDex()));
                    }
                    return null;
                }
            };

            File out = jfModder.createModifiedJar();
            File outSigned = new File(new ApkSign().sign(out));

            //      try{
            //        DexFile dex1 = new DexFile(outSigned);
            //        org.jf.baksmali.dump.dump(dex1, "c:\\dump_"+ctx.getFile().getName().replace('.', '_')+".txt", null, false);}
            //      catch(Exception e){
            //        System.out.println("exception during dump:"+e.getClass().getName()+","+e.getMessage());
            //        e.printStackTrace();
            //      }
            return outSigned;
        } else {
            return null;
        }
    }

    public static byte[] getAsByteArray(DexFile dexFile)
    {
        dexFile.place();
        CodeItem codeitem;
        for (Iterator<?> iterator = dexFile.CodeItemsSection.getItems().iterator(); iterator.hasNext(); codeitem.fixInstructions(true, true)) {
            codeitem = (CodeItem) iterator.next();
        }

        dexFile.place();
        ByteArrayAnnotatedOutput bytearrayannotatedoutput = new ByteArrayAnnotatedOutput();
        dexFile.writeTo(bytearrayannotatedoutput);
        byte abyte0[] = bytearrayannotatedoutput.toByteArray();
        DexFile.calcSignature(abyte0);
        DexFile.calcChecksum(abyte0);
        return abyte0;
    }

    public static Map<String, byte[]> modifyClasses(ProgressReporter pr, ApkClassContext ctx,
            Map<MEClass, ClassInjContainer> classInjections) throws IOException {
        DalvikInjectCollection injectCollection = new DalvikInjectCollection(ctx.getDex());

        // the modified bytecode per class name
        Map<String, byte[]> modClasses = new HashMap<String, byte[]>();

        Iterator<MEClass> classI = classInjections.keySet().iterator();
        if (pr != null) {
            pr.reportStart(classInjections.keySet().size());
        }

        // modify each class
        int ci = 0;

        while (classI.hasNext()) {
            if (pr != null) {
                pr.reportWork(ci++);
            }
            DexClass clazz = (DexClass) classI.next();
            // get modifications for class
            ClassInjContainer injContainer = classInjections.get(clazz);
            // modify
            String className = clazz.getName();
            byte[] classData;
            classData = preformInjection(ctx, injectCollection, injContainer.methodInjectionsToArray());
            // and store it
            modClasses.put(className, classData);
        } // per class
        if (pr != null) {
            pr.reportEnd();
        }
        return modClasses;
    }

    private static byte[] preformInjection(ApkClassContext ctx, DalvikInjectCollection ic, InjectionMethod[] injections) {
        byte[] b = null;

        for (int i = 0; i < injections.length; i++) {
            if (injections[i] instanceof DalvikInjectionMethod) {
                DexMethod method = ((DalvikInjectionMethod) injections[i]).getMethod();
                CodeItem codeItem = method.getEncodedMethod().codeItem;

                if (codeItem != null) {
                    DalvikCodeItemVisitor.visit(((DalvikInjectionMethod) injections[i]), method, ic);
                }
            }
        }
        for (int i = 0; i < injections.length; i++) {
            if (injections[i] instanceof DalvikInjectionMethod) {
                fixRegisters(((DalvikInjectionMethod) injections[i]).getMethod(), ic);
            }
        }
        return b;
    }

    public static void injectInsturctionsAtInstruction(DexMethod method, Instruction ins, ArrayList<Instruction> injectInstructions) {
        CodeItem codeItem = method.getEncodedMethod().codeItem;
        int index = -1;
        Instruction[] instructions = codeItem.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            if (ins == instructions[i]) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            injectInstructionsAtIndex(method, index, injectInstructions);
        }
    }

    public static void injectInsturctionsAtNextInstruction(DexMethod method, Instruction ins, ArrayList<Instruction> injectInstructions) {
        CodeItem codeItem = method.getEncodedMethod().codeItem;
        int index = -1;
        Instruction[] instructions = codeItem.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            if (ins == instructions[i]) {
                index = i;
                break;
            }
        }
        if (index != -1
                && index + 1 < instructions.length) {
            injectInstructionsAtIndex(method, index + 1, injectInstructions);
        }
    }

    private static void injectInstructionsAtIndex(DexMethod method, int instructionIndex, ArrayList<Instruction> injectInstructions) {
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        int injectLength = injectInstructions.size();
        DebugInfoItem debugInfo = codeItem.getDebugInfo();
        EncodedCatchHandler[] encodedCatchHandlers = codeItem.getHandlers();
        TryItem[] tries = codeItem.getTries();

        Instruction[] instructions = codeItem.getInstructions();

        int[] originalInstructionCodeAddresses = new int[instructions.length + injectLength + 1];
        int i = 0;
        for (i = 0; i < originalInstructionCodeAddresses.length; i++) {
            originalInstructionCodeAddresses[i] = -1;
        }

        SparseIntArray originalSwitchAddressByOriginalSwitchDataAddress = new SparseIntArray();

        int currentCodeAddress = 0;

        int injectCodeAddress = 0;

        int skip = 0;
        for (i = 0; i < instructions.length; i++) {
            Instruction instruction = instructions[i];

            if (instruction.opcode == Opcode.PACKED_SWITCH || instruction.opcode == Opcode.SPARSE_SWITCH) {
                OffsetInstruction offsetInstruction = (OffsetInstruction) instruction;

                int switchDataAddress = currentCodeAddress + offsetInstruction.getTargetAddressOffset();
                if (originalSwitchAddressByOriginalSwitchDataAddress.indexOfKey(switchDataAddress) < 0) {
                    originalSwitchAddressByOriginalSwitchDataAddress.put(switchDataAddress, currentCodeAddress);
                }
            }
            if (i == instructionIndex) {
                skip = injectLength;
                injectCodeAddress = currentCodeAddress;
            }

            originalInstructionCodeAddresses[i + skip] = currentCodeAddress;
            currentCodeAddress += instruction.getSize(currentCodeAddress);
        }

        //add the address just past the end of the last instruction, to help when fixing up try blocks that end
        //at the end of the method
        originalInstructionCodeAddresses[i + skip] = currentCodeAddress;

        ////
        ArrayList<Instruction> insList = new ArrayList<Instruction>(Arrays.asList(codeItem.getInstructions()));
        for (i = injectLength - 1; i >= 0; i--) {
            insList.add(instructionIndex, injectInstructions.get(i));
        }
        codeItem.updateCode(insList.toArray(new Instruction[insList.size()]));
        instructions = codeItem.getInstructions();
        ////

        final SparseIntArray originalAddressByNewAddress = new SparseIntArray();
        final SparseIntArray newAddressByOriginalAddress = new SparseIntArray();

        currentCodeAddress = 0;
        for (i = 0; i < instructions.length; i++) {
            Instruction instruction = instructions[i];

            int originalAddress = originalInstructionCodeAddresses[i];
            if (originalAddress != -1) {
                originalAddressByNewAddress.append(currentCodeAddress, originalAddress);
                newAddressByOriginalAddress.append(originalAddress, currentCodeAddress);
            }

            currentCodeAddress += instruction.getSize(currentCodeAddress);
        }

        //add the address just past the end of the last instruction, to help when fixing up try blocks that end
        //at the end of the method
        if (originalInstructionCodeAddresses[i] != -1) {
            originalAddressByNewAddress.append(currentCodeAddress, originalInstructionCodeAddresses[i]);
            newAddressByOriginalAddress.append(originalInstructionCodeAddresses[i], currentCodeAddress);
        }

        //update any "offset" instructions, or switch data instructions
        currentCodeAddress = 0;
        for (i = 0; i < instructions.length; i++) {
            Instruction instruction = instructions[i];

            if (instruction instanceof OffsetInstruction) {
                OffsetInstruction offsetInstruction = (OffsetInstruction) instruction;

                assert originalAddressByNewAddress.indexOfKey(currentCodeAddress) >= 0;
                int originalAddress = originalAddressByNewAddress.get(currentCodeAddress);

                int originalInstructionTarget = originalAddress + offsetInstruction.getTargetAddressOffset();

                assert newAddressByOriginalAddress.indexOfKey(originalInstructionTarget) >= 0;
                int newInstructionTarget = newAddressByOriginalAddress.get(originalInstructionTarget);
                if (originalInstructionTarget == injectCodeAddress) {
                    newInstructionTarget = injectCodeAddress;
                }
                int newCodeAddress = (newInstructionTarget - currentCodeAddress);

                if (newCodeAddress != offsetInstruction.getTargetAddressOffset()) {
                    offsetInstruction.updateTargetAddressOffset(newCodeAddress);
                }
            } else if (instruction instanceof MultiOffsetInstruction) {
                //System.out.println(codeItem.dexMethod.getMEClass().getName()+ "->"+codeItem.dexMethod.getName() + " dlisyes: ");
                MultiOffsetInstruction multiOffsetInstruction = (MultiOffsetInstruction) instruction;

                assert originalAddressByNewAddress.indexOfKey(currentCodeAddress) >= 0;
                int originalDataAddress = originalAddressByNewAddress.get(currentCodeAddress);

                int originalSwitchAddress =
                        originalSwitchAddressByOriginalSwitchDataAddress.get(originalDataAddress, -1);
                if (originalSwitchAddress == -1) {
                    throw new RuntimeException("This method contains an unreferenced switch data block at address " +
                            +currentCodeAddress + " and can't be automatically fixed.");
                }

                assert newAddressByOriginalAddress.indexOfKey(originalSwitchAddress) >= 0;
                int newSwitchAddress = newAddressByOriginalAddress.get(originalSwitchAddress);

                //System.out.println(" ori switch= " + originalSwitchAddress +" new switch= " + newSwitchAddress);

                int[] targets = multiOffsetInstruction.getTargets();
                for (int t = 0; t < targets.length; t++) {
                    int originalTargetCodeAddress = originalSwitchAddress + targets[t];
                    assert newAddressByOriginalAddress.indexOfKey(originalTargetCodeAddress) >= 0;
                    int newTargetCodeAddress = newAddressByOriginalAddress.get(originalTargetCodeAddress);
                    if (originalTargetCodeAddress == injectCodeAddress) {
                        newTargetCodeAddress = injectCodeAddress;
                    }
                    int newCodeAddress = newTargetCodeAddress - newSwitchAddress;

                    //System.out.println("  "+ t + " target= "+ targets[t]+ " ori= " + originalTargetCodeAddress + " new= "+ newTargetCodeAddress+" new code= " + newCodeAddress);

                    if (newCodeAddress != targets[t]) {
                        multiOffsetInstruction.updateTarget(t, newCodeAddress);
                    }
                }
            }
            currentCodeAddress += instruction.getSize(currentCodeAddress);
        }

        if (debugInfo != null) {
            final byte[] encodedDebugInfo = debugInfo.getEncodedDebugInfo();

            ByteArrayInput debugInput = new ByteArrayInput(encodedDebugInfo);

            CodeItem.DebugInstructionFixer debugInstructionFixer = codeItem.new DebugInstructionFixer(encodedDebugInfo,
                    newAddressByOriginalAddress);

            //      DebugInstructionFixer debugInstructionFixer = new DebugInstructionFixer(encodedDebugInfo,
            //          newAddressByOriginalAddress, injectCodeAddress);
            DebugInstructionIterator.IterateInstructions(debugInput, debugInstructionFixer);

            if (debugInstructionFixer.result != null) {
                debugInfo.setEncodedDebugInfo(debugInstructionFixer.result);
            }
        }

        if (encodedCatchHandlers != null) {
            for (EncodedCatchHandler encodedCatchHandler : encodedCatchHandlers) {
                if (encodedCatchHandler.getCatchAllHandlerAddress() != -1) {
                    assert newAddressByOriginalAddress.indexOfKey(encodedCatchHandler.getCatchAllHandlerAddress()) >= 0;
                    encodedCatchHandler.catchAllHandlerAddress =
                            newAddressByOriginalAddress.get(encodedCatchHandler.getCatchAllHandlerAddress());
                    if (encodedCatchHandler.getCatchAllHandlerAddress() == injectCodeAddress) {
                        throw new RuntimeException("Can not inject at the beginning of a catch block" +
                                +currentCodeAddress + "@" + method.getMEClass().getName() + "." + method.getName());
                    }
                }

                for (EncodedTypeAddrPair handler : encodedCatchHandler.handlers) {
                    assert newAddressByOriginalAddress.indexOfKey(handler.getHandlerAddress()) >= 0;
                    handler.handlerAddress = newAddressByOriginalAddress.get(handler.getHandlerAddress());
                    if (handler.getHandlerAddress() == injectCodeAddress) {
                        throw new RuntimeException("Can not inject at the beginning of a catch block" +
                                +currentCodeAddress + "@" + method.getMEClass().getName() + "." + method.getName());
                    }
                }
            }
        }

        if (tries != null) {
            for (TryItem tryItem : tries) {
                int startAddress = tryItem.getStartCodeAddress();
                int endAddress = tryItem.getStartCodeAddress() + tryItem.getTryLength();

                assert newAddressByOriginalAddress.indexOfKey(startAddress) >= 0;
                //				if (startAddress != injectCodeAddress){
                tryItem.startCodeAddress = newAddressByOriginalAddress.get(startAddress);
                //				}

                assert newAddressByOriginalAddress.indexOfKey(endAddress) >= 0;
                tryItem.tryLength = newAddressByOriginalAddress.get(endAddress) - tryItem.getStartCodeAddress();
            }
        }
    }

    /*
    private static class DebugInstructionFixer extends DebugInstructionIterator.ProcessRawDebugInstructionDelegate {
    private int currentCodeAddress = 0;
    private SparseIntArray newAddressByOriginalAddress;
    private final byte[] originalEncodedDebugInfo;
    public byte[] result = null;
    private int injectCodeAddress;

    public DebugInstructionFixer(byte[] originalEncodedDebugInfo, SparseIntArray newAddressByOriginalAddress, int injectCodeAddress) {
      this.newAddressByOriginalAddress = newAddressByOriginalAddress;
      this.originalEncodedDebugInfo = originalEncodedDebugInfo;
      this.injectCodeAddress = injectCodeAddress;
    }


    @Override
    public void ProcessAdvancePC(int startDebugOffset, int debugInstructionLength, int codeAddressDelta) {
      currentCodeAddress += codeAddressDelta;

      if (result != null) {
        return;
      }

      int newCodeAddress = newAddressByOriginalAddress.get(currentCodeAddress, -1);

      //The address might not point to an actual instruction in some cases, for example, if an AdvancePC
      //instruction was inserted just before a "special" instruction, to fix up the addresses for a previous
      //instruction replacement.
      //In this case, it should be safe to skip, because there will be another AdvancePC/SpecialOpcode that will
      //bump up the address to point to a valid instruction before anything (line/local/etc.) is emitted
      if (newCodeAddress == -1) {
        return;
      }

      if (newCodeAddress != currentCodeAddress
          && injectCodeAddress != currentCodeAddress) {
        int newCodeAddressDelta = newCodeAddress - (currentCodeAddress - codeAddressDelta);
        assert newCodeAddressDelta > 0;
        int codeAddressDeltaLeb128Size = Leb128Utils.unsignedLeb128Size(newCodeAddressDelta);

        //if the length of the new code address delta is the same, we can use the existing buffer
        if (codeAddressDeltaLeb128Size + 1 == debugInstructionLength) {
          result = originalEncodedDebugInfo;
          Leb128Utils.writeUnsignedLeb128(newCodeAddressDelta, result, startDebugOffset+1);
        } else {
          //The length of the new code address delta is different, so create a new buffer with enough
          //additional space to accomodate the new code address delta value.
          result = new byte[originalEncodedDebugInfo.length + codeAddressDeltaLeb128Size -
                            (debugInstructionLength - 1)];

          System.arraycopy(originalEncodedDebugInfo, 0, result, 0, startDebugOffset);

          result[startDebugOffset] = DebugOpcode.DBG_ADVANCE_PC.value;
          Leb128Utils.writeUnsignedLeb128(newCodeAddressDelta, result, startDebugOffset+1);

          System.arraycopy(originalEncodedDebugInfo, startDebugOffset + debugInstructionLength, result,
              startDebugOffset + codeAddressDeltaLeb128Size + 1,
              originalEncodedDebugInfo.length - (startDebugOffset + codeAddressDeltaLeb128Size + 1));
        }
      }
    }

    @Override
    public void ProcessSpecialOpcode(int startDebugOffset, int debugOpcode, int lineDelta,
        int codeAddressDelta) {
      currentCodeAddress += codeAddressDelta;
      if (result != null) {
        return;
      }

      int newCodeAddress = newAddressByOriginalAddress.get(currentCodeAddress, -1);
      assert newCodeAddress != -1;

      if (newCodeAddress != currentCodeAddress
          && injectCodeAddress != currentCodeAddress) {
        int newCodeAddressDelta = newCodeAddress - (currentCodeAddress - codeAddressDelta);
        assert newCodeAddressDelta > 0;

        //if the new code address delta won't fit in the special opcode, we need to insert
        //an additional DBG_ADVANCE_PC opcode
        if (lineDelta < 2 && newCodeAddressDelta > 16 || lineDelta > 1 && newCodeAddressDelta > 15) {
          int additionalCodeAddressDelta = newCodeAddress - currentCodeAddress;
          int additionalCodeAddressDeltaLeb128Size = Leb128Utils.signedLeb128Size(additionalCodeAddressDelta);

          //create a new buffer with enough additional space for the new opcode
          result = new byte[originalEncodedDebugInfo.length + additionalCodeAddressDeltaLeb128Size + 1];

          System.arraycopy(originalEncodedDebugInfo, 0, result, 0, startDebugOffset);
          result[startDebugOffset] = 0x01; //DBG_ADVANCE_PC
          Leb128Utils.writeUnsignedLeb128(additionalCodeAddressDelta, result, startDebugOffset+1);
          System.arraycopy(originalEncodedDebugInfo, startDebugOffset, result,
              startDebugOffset+additionalCodeAddressDeltaLeb128Size+1,
              result.length - (startDebugOffset+additionalCodeAddressDeltaLeb128Size+1));
        } else {
          result = originalEncodedDebugInfo;
          result[startDebugOffset] = DebugInfoBuilder.calculateSpecialOpcode(lineDelta,
              newCodeAddressDelta);
        }
      }
    }
    }
     */

    public static void fixRegisters(DexMethod method, DalvikInjectCollection ic) {
        if (method.getEncodedMethod() == null
                || method.getEncodedMethod().codeItem == null) {
            return;
        }
        CodeItem codeItem = method.getEncodedMethod().codeItem;

        int regCount = codeItem.getRegisterCount() - codeItem.registerOriginalCount;
        if (regCount != 0) {
            int parameterRegisterCount = codeItem.getParent().method.getPrototype().getParameterRegisterCount()
                    + (((codeItem.getParent().accessFlags & AccessFlags.STATIC.getValue()) == 0) ? 1 : 0);

            if (parameterRegisterCount != 0) {

                //codeItem.registerCount += parameterRegisterCount;

                boolean isStatic = (codeItem.getParent().accessFlags & AccessFlags.STATIC.getValue()) != 0;

                //int regSrc = codeItem.registerCount - parameterRegisterCount;
                int regSrc = codeItem.getRegisterCount();
                codeItem.registerCount += parameterRegisterCount;
                int regDest = codeItem.registerOriginalCount - parameterRegisterCount;

                String paramShortDesc = null;
                TypeListItem params = codeItem.getParent().method.getPrototype().getParameters();
                if (params != null) {
                    paramShortDesc = codeItem.getParent().method.getPrototype().getParameters().getShortyString();
                }
                ArrayList<Instruction> injectInstructions = ic.injectCopyParamRegs(codeItem, regDest, regSrc, isStatic, paramShortDesc);
                injectInstructionsAtIndex(method, 0, injectInstructions);
            }
            codeItem.registerOriginalCount = codeItem.getRegisterCount();
        }
    }
}
