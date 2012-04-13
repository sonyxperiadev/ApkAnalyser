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

package mereflect;

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
 * Represents a java type, i.e a primitive or a class
 */
public interface Type
{
    /** Identifier character for array */
    public static final char CH_ARRAY = '[';
    /** Identifier character for boolean primitive */
    public static final char CH_BOOLEAN = 'Z';
    /** Identifier character for byte primitive */
    public static final char CH_BYTE = 'B';
    /** Identifier character for char primitive */
    public static final char CH_CHAR = 'C';
    /** Identifier character for double primitive */
    public static final char CH_DOUBLE = 'D';
    /** Identifier character for float primitive */
    public static final char CH_FLOAT = 'F';
    /** Identifier character for int primitive */
    public static final char CH_INT = 'I';
    /** Identifier character for long primitive */
    public static final char CH_LONG = 'J';
    /** Identifier character for short primitive */
    public static final char CH_SHORT = 'S';
    /** Identifier character for void */
    public static final char CH_VOID = 'V';
    /** Identifier character prefix for classes */
    public static final char CH_CLASS_PRE = 'L';
    /** Identifier character postfix for classes */
    public static final char CH_CLASS_POST = ';';

    public static final Type BYTE = new MEByte();
    public static final Type BOOLEAN = new MEBoolean();
    public static final Type CHAR = new MEChar();
    public static final Type DOUBLE = new MEDouble();
    public static final Type FLOAT = new MEFloat();
    public static final Type INT = new MEInt();
    public static final Type LONG = new MELong();
    public static final Type SHORT = new MEShort();
    public static final Type VOID = new MEVoid();
    public static final String STR_STRING = "java.lang.String";

    /**
     * Returns name of this type
     * @return the name, e.g. classname or primitive name
     */
    public String getName();

    /**
     * Returns true if this type is a primitive
     * @return if this type is primitive
     */
    public boolean isPrimitive();

    /**
     * Returns true if this type is an array
     * @return if this type is array
     */
    public boolean isArray();
}
