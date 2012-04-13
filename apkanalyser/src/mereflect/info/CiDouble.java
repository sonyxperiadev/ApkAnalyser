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

package mereflect.info;

public class CiDouble extends AbstractClassInfo
{
    protected static final int ID_DOUBLE = 10;

    public CiDouble(double d)
    {
        setTag(CONSTANT_Double);
        setValue(ID_DOUBLE, new Double(d));
    }

    public double getDouble()
    {
        return ((Double) getValue(ID_DOUBLE)).doubleValue();
    }

    @Override
    public int getLength()
    {
        return 9;
    }
}
