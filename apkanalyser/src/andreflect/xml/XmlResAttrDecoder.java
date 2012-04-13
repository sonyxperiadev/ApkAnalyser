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

package andreflect.xml;

import mereflect.CollaborateClassContext;
import mereflect.MEClassContext;
import analyser.gui.MainFrame;
import andreflect.DexReferenceCache;
import android.util.TypedValue;
import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResID;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.data.value.ResReferenceValue;
import brut.androlib.res.data.value.ResScalarValue;
import brut.androlib.res.decoder.ResAttrDecoder;

public class XmlResAttrDecoder extends ResAttrDecoder {

    private final ResTable mResTable;

    public XmlResAttrDecoder(ResTable resTable) {
        mResTable = resTable;
    }

    public static final int ANDROID_PACKAGE_ID = 0x1;

    @Override
    public String decode(int type, int value, String rawValue, int attrResId) {
        String decoded = (rawValue == null ? TypedValue.coerceToString(type, value) : rawValue);
        try {
            if (mResTable != null) {
                ResScalarValue resValue = mResTable.listMainPackages().iterator().next().getValueFactory().factory(type, value, rawValue);
                if (resValue instanceof ResReferenceValue) {
                    decoded = getResReferenceValue((ResReferenceValue) resValue,
                            mResTable,
                            type == TypedValue.TYPE_ATTRIBUTE);
                } else {
                    decoded = resValue.encodeAsResXmlAttr();
                }
            }

        } catch (AndrolibException e1) {
            e1.printStackTrace();
        }

        return decoded;
    }

    public static String getResReferenceValue(ResReferenceValue resReferenceValue, ResTable resTable, boolean isAttribute) {
        int id = resReferenceValue.getValue();
        if (resReferenceValue.isNull()) {
            return "@null";
        }

        try {
            if (resTable.getResSpec(id) != null) {
                return resReferenceValue.encodeAsResXmlAttr();
            }
        } catch (AndrolibException e) {
        }

        CollaborateClassContext ctx = MainFrame.getInstance().getResolver().getReferenceContext();
        MEClassContext[] contexts = ctx.getContexts();
        for (MEClassContext context : contexts) {
            ResTable resFrameworkTable = null;
            DexReferenceCache dexResCache = context.getDexReferenceCache();
            if (dexResCache != null) {
                resFrameworkTable = dexResCache.getResTable();
            }

            try {
                if (resFrameworkTable != null
                        && resFrameworkTable.getResSpec(id) != null) {
                    ResReferenceValueX x = new ResReferenceValueX(resReferenceValue, resFrameworkTable);
                    return x.encodeAsResXmlAttr();
                }
            } catch (AndrolibException e) {
                //ignore because not define in this jar file
            }

        }

        return String.format("%s%s%08X",
                isAttribute ? "?" : "@",
                new ResID(id).package_ == ANDROID_PACKAGE_ID ? "android:" : "",
                resReferenceValue.getValue());

    }

    private static class ResReferenceValueX extends ResReferenceValue {
        private final ResTable m_resTable;

        public ResReferenceValueX(ResReferenceValue resReferenceValue, ResTable resTable) {
            super(resReferenceValue.mPackage, resReferenceValue.getValue(), resReferenceValue.mRawValue, resReferenceValue.mTheme);
            m_resTable = resTable;
        }

        @Override
        public ResResSpec getReferent() throws AndrolibException {
            return m_resTable.getResSpec(getValue());
        }
    }
}
