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

package util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class JadFileModifier {
    File origJad;
    String newName;

    public JadFileModifier(File originalJad, String newName, String postfix) {
        origJad = originalJad;
        this.newName = newName;
        if (newName == null || newName.trim().length() == 0)
        {
            String name = origJad.getName();
            if (name.toLowerCase().endsWith(".jad"))
            {
                name = name.substring(0, name.length() - 4);
            }
            newName = name + "_" + postfix + ".jad";
        }
    }

    public File modify(String newJar, String midletNamePostfix, long newSize) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(origJad));
        props.put("MIDlet-Jar-URL", newJar);
        props.put("MIDlet-Jar-Size", Long.toString(newSize));
        props.put("MIDlet-Name", props.get("MIDlet-Name") + midletNamePostfix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos, null);
        String newProps = StringReplacer.replaceAll(baos.toString(), "=", ": ");
        newProps = StringReplacer.replaceAll(newProps, "#", "Mod-date: ");
        File outFile = new File(newName);
        if (!outFile.exists()) {
            outFile.createNewFile();
            FileWriter out = new FileWriter(outFile);
            out.write(newProps);
            out.close();
        } else {
            throw new IOException();
        }
        return outFile;
    }
}
