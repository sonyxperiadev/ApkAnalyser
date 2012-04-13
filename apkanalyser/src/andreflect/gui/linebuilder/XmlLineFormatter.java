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

package andreflect.gui.linebuilder;

import java.io.IOException;
import java.io.InputStream;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;


public class XmlLineFormatter extends LineBuilderFormatter {
    public LineBuilder lb;
    boolean isRaw;
    int caret = -1;

    public static final int COLOR_TAG = COLOR_HEX;
    public static final int COLOR_ATTRIB = 0x00880000;
    public static final int COLOR_EQ = 0x00000000;
    public static final int COLOR_VALUE = COLOR_COMMENT;

    public LineBuilder getLineBuilder() {
        return lb;
    }

    public XmlLineFormatter() {
        lb = new LineBuilder();
        lb.newLine();
        isRaw = false;
    }

    public XmlLineFormatter(InputStream in) {
        lb = new LineBuilder();
        lb.newLine();
        try {
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1;) {
                lb.append(new String(b, 0, n), COLOR_TEXT);
            }
        } catch (IOException e) {
            e.printStackTrace();
            lb = null;
        }
        isRaw = true;
    }

    public boolean isRaw() {
        return isRaw;
    }

    public int getCaret() {
        return caret;
    }

    public void appendEQ() {
        lb.append("=", COLOR_EQ);
    }

    public void appendLF() {
        lb.append("\n", COLOR_TEXT);
        //lb.newLine();
    }

    public void appendSpace() {
        lb.append(" ", COLOR_TEXT);
    }

    //<?xml version=\"1.0\" encoding=\"utf-8\"?>
    public void appendXMLHeader() {
        lb.append("<?xml ", COLOR_TAG);
        lb.append("version", COLOR_ATTRIB);
        lb.append("=", COLOR_EQ);
        lb.append("\"1.0\" ", COLOR_VALUE);
        lb.append("encoding", COLOR_ATTRIB);
        lb.append("=", COLOR_EQ);
        lb.append("\"utf-8\"", COLOR_VALUE);
        lb.append("?>\n", COLOR_TAG);
    }

    public void appendText(String text) {
        char[] chararray = text.toCharArray();
        boolean inquote = false;
        for (char c : chararray) {

            if (c == '\"') {
                inquote = !inquote;
            }
            if ((c != '\t' && c != '\n' && c != '\r') || inquote) {
                lb.append(c, COLOR_TEXT);
            }
        }

        //lb.append(text, COLOR_TEXT);
    }

    public void appendValue(String text) {
        lb.append("\"", COLOR_VALUE);
        lb.append(text, COLOR_VALUE);
        lb.append("\"", COLOR_VALUE);
    }

    public void appendBeginTagBegin(String namespacePrefix, String name) {
        lb.append("<", COLOR_TAG);
        if (namespacePrefix != null) {
            lb.append(namespacePrefix, COLOR_TAG);
        }
        lb.append(name, COLOR_TAG);
        appendSpace();//appendLF();
    }

    public void appendBeginTagEnd() {
        lb.append(">", COLOR_TAG);
        //appendLF();
    }

    public void appendAttrib(String text) {
        lb.append(text, COLOR_ATTRIB);
    }

    public void setCurrentLine() {
        caret = lb.length();
    }

    public void appendEndTag(String namespacePrefix, String name) {
        lb.append("</", COLOR_TAG);
        if (namespacePrefix != null) {
            lb.append(namespacePrefix, COLOR_TAG);
        }
        lb.append(name, COLOR_TAG);
        lb.append(">", COLOR_TAG);
        appendLF();
    }

}
