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

package andreflect.sign;

import java.io.File;

import com.android.signapk.SignApk;

public class ApkSign {
    //	SignApk.jar is a tool included with the Android platform source bundle.
    //	testkey.pk8 is the private key that is compatible with the recovery image included in this zip file
    //	testkey.x509.pem is the corresponding certificate/public key
    //
    //	Usage:
    //	java -jar signapk.jar testkey.x509.pem testkey.pk8 update.zip update_signed.zip

    public String sign(File file) {
        String name = file.getAbsolutePath();
        if (file.getAbsolutePath().toLowerCase().endsWith(".ap_"))
        {
            name = name.substring(0, name.length() - 4);
            name += ".apk";
        } else if (file.getAbsolutePath().toLowerCase().endsWith(".apk")) {
            name = name.substring(0, name.length() - 4);
            name += "_sign.apk";
        }

        String[] args = { null, null, file.getAbsolutePath(), name };
        SignApk.main(args);

        return name;
    }

}
