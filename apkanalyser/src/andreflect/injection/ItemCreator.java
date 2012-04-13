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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;

public class ItemCreator {

    public DexFile m_dex;

    public MethodIdItem[] methodItems = new MethodIdItem[DebugMethod.values().length];
    public HashMap<String, StringIdItem> stringItems = new HashMap<String, StringIdItem>();

    public ItemCreator(DexFile dexfile) {
        m_dex = dexfile;
    }

    public MethodIdItem prepareMethodIdItem(DebugMethod method) {
        if (methodItems[method.ordinal()] == null) {
            methodItems[method.ordinal()] = addMethodIdItem(method.className, method.returnName, method.params, method.methodName);
        }
        return methodItems[method.ordinal()];
    }

    public StringIdItem prepareStringIdItem(String string) {
        StringIdItem stringIdItem = stringItems.get(string);
        if (stringIdItem == null) {
            stringIdItem = addStringIdItem(string);
            stringItems.put(string, stringIdItem);
        }
        return addStringIdItem(string);
    }

    private StringIdItem addStringIdItem(String string) {
        StringIdItem item = StringIdItem.lookupStringIdItem(m_dex, string);
        if (item == null) {
            item = StringIdItem.internStringIdItem(m_dex, string);
        }
        return item;
    }

    public TypeIdItem addTypeIdItem(String string) {
        TypeIdItem item = TypeIdItem.lookupTypeIdItem(m_dex, string);
        if (item == null) {
            StringIdItem typeIdStringIdItem = addStringIdItem(string);
            item = TypeIdItem.internTypeIdItem(m_dex, typeIdStringIdItem);
        }

        return item;
    }

    private TypeListItem addTypeListItem(String[] paramType) {
        if (paramType.length == 0) {
            return null;
        }
        List<TypeIdItem> paramTypeList = new ArrayList<TypeIdItem>();
        for (String param : paramType) {
            paramTypeList.add(addTypeIdItem(param));
        }

        TypeListItem item = TypeListItem.lookupTypeListItem(m_dex, paramTypeList);
        if (item == null) {
            item = TypeListItem.internTypeListItem(m_dex, paramTypeList);
        }
        return item;
    }

    public MethodIdItem addMethodIdItem(String classType, String returnType, String[] paramType, String methodName) {
        TypeIdItem classTypeIdItem = addTypeIdItem(classType);

        TypeIdItem returnTypeIdItem = addTypeIdItem(returnType);
        TypeListItem paramTypeListItem = addTypeListItem(paramType);

        ProtoIdItem protoItem = ProtoIdItem.lookupProtoIdItem(m_dex, returnTypeIdItem, paramTypeListItem);
        if (protoItem == null) {
            protoItem = ProtoIdItem.internProtoIdItem(m_dex, returnTypeIdItem, paramTypeListItem);
        }

        StringIdItem nameStringIdItem = addStringIdItem(methodName);

        return MethodIdItem.internMethodIdItem(m_dex, classTypeIdItem, protoItem, nameStringIdItem);
    }

    public CodeItem addEmptyVirtualCodeItem() {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(new Instruction10x(Opcode.RETURN_VOID)); //NOT SET INJECT FLAG HERE
        CodeItem item = CodeItem.internCodeItem(m_dex,
                1, //registerCount
                1, //inWords
                0, //outWords
                null, //debugInfo
                instructions,
                null, //tries
                null); //encodedCatchHandlers
        return item;
    }

}
