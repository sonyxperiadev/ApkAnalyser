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

package mereflect.io;

import java.io.IOException;

import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.Type;
import mereflect.UnknownClass;
import mereflect.primitives.AbstractPrimitive;
import mereflect.primitives.MEArray;
import mereflect.primitives.MEBoolean;
import mereflect.primitives.MEByte;
import mereflect.primitives.MEChar;
import mereflect.primitives.MEDouble;
import mereflect.primitives.MEFloat;
import mereflect.primitives.MEInt;
import mereflect.primitives.MELong;
import mereflect.primitives.MEShort;
import mereflect.primitives.MEVoid;

/**
 * Simple helper class for parsing type descriptors.
 */
public class DescriptorParser {
    static MEClassContext superContext;

    private DescriptorParser() {
    }

    /**
     * When processing descriptors and types that are not found in specified class context, the supercontext will be searched in instead.
     * @TODO  solve another way, this is superUgly
     * @param  ctx
     */
    public static void setSuperContext(MEClassContext ctx) {
        superContext = ctx;
    }

    public static void processType(Type t, StringBuffer s) {
        if (t.isArray()) {
            s.append(Type.CH_ARRAY);
            processType(((MEArray) t).getArrayType(), s);
        } else if (t.isPrimitive()) {
            s.append(((AbstractPrimitive) t).getDescriptorChar());
        } else {
            s.append(Type.CH_CLASS_PRE);
            s.append(((MEClass) t).getRawName());
            s.append(Type.CH_CLASS_POST);
        }
    }

    /**
     * Processes a type descriptor and returns a Type for the descriptor.
     * @param clazz any class of the context of where the descriptor is valid
     * @param descr The descriptor
     * @return the resolved type as a <code>Type</code>
     * @throws IOException, IllegalArgumentException
     */
    public static Type processTypeDescriptor(MEClass clazz, StringBuffer descr)
            throws IOException {
        if (descr == null || descr.length() == 0) {
            return null;
        }
        Type t = null;
        char c = descr.charAt(0);
        descr.deleteCharAt(0);
        switch (c) {
        case Type.CH_ARRAY:
            t = new MEArray(processTypeDescriptor(clazz, descr));
            break;
        case Type.CH_CLASS_PRE:
            int end = descr.indexOf(Character.toString(Type.CH_CLASS_POST));
            String classname = descr.substring(0, end);
            classname = classname.replace('/', '.');
            descr.delete(0, end + 1);
            try {
                t = clazz.getResource().getContext().getMEClass(classname);
            } catch (ClassNotFoundException cnfe) {
                try {
                    if (superContext != null) {
                        t = superContext.getMEClass(classname);
                    } else {
                        throw cnfe;
                    }
                } catch (ClassNotFoundException cnfe2) {
                    // TODO: could not find the class in apis either.
                    // Workaround by specifying it as unknown, unsafe when doing ops on fields, but keep on running
                    t = new UnknownClass(classname, clazz.getResource());
                }
            }

            break;
        case Type.CH_BOOLEAN:
            t = new MEBoolean();
            break;
        case Type.CH_BYTE:
            t = new MEByte();
            break;
        case Type.CH_CHAR:
            t = new MEChar();
            break;
        case Type.CH_DOUBLE:
            t = new MEDouble();
            break;
        case Type.CH_FLOAT:
            t = new MEFloat();
            break;
        case Type.CH_INT:
            t = new MEInt();
            break;
        case Type.CH_LONG:
            t = new MELong();
            break;
        case Type.CH_SHORT:
            t = new MEShort();
            break;
        case Type.CH_VOID:
            t = new MEVoid();
            break;
        default:
            throw new IllegalArgumentException("Unknown class type " + t);
        }
        return t;
    }
}
