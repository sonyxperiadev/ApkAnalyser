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

package analyser.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class LineBuilder {
    ArrayList<LineEntry> lines = new ArrayList<LineEntry>();
    LineEntry line = new LineEntry();
    int curLine = 0;

    public void newLine() {
        line = new LineEntry();
        lines.add(line);
        curLine = lines.size() - 1;
    }

    public int lineCount() {
        return lines.size();
    }

    public void gotoLine(int lineNbr) {
        line = lines.get(lineNbr);
        curLine = lineNbr;
    }

    public void gotoLastLine() {
        gotoLine(lines.size() - 1);
    }

    public void insertLineBefore(int lineNbr) {
        line = new LineEntry();
        lines.add(lineNbr, line);
        curLine = lineNbr;
    }

    public void insertLineAfter(int lineNbr) {
        insertLineBefore(lineNbr + 1);
    }

    public void insertBefore(int lineNbr, LineBuilder lb) {
        for (int i = lb.lineCount() - 1; i >= 0; i--) {
            LineEntry entry = lb.lines.get(i);
            lines.add(lineNbr, entry);
        }
    }

    public void insertAfter(int lineNbr, LineBuilder lb) {
        // TODO
        insertBefore(lineNbr + 1, lb);
    }

    public void removeLine(int lineNbr) {
        lines.remove(lineNbr);
        if (curLine >= lineNbr) {
            curLine--;
        }
        if (curLine == lineNbr) {
            line = null;
        }
    }

    public String currentLineString() {
        return line.toString();
    }

    public int currentLine() {
        return curLine;
    }

    public String toString(String EOL) {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < lines.size(); i++) {
            res.append(lines.get(i));
            res.append(EOL);
        }
        return res.toString();
    }

    @Override
    public String toString() {
        return toString("\n");
    }

    public void toDocument(Document doc) {
        try {
            SimpleAttributeSet set = new SimpleAttributeSet();
            StyleConstants.setFontFamily(set, "Courier");
            for (int i = 0; i < lines.size(); i++) {
                LineEntry line = lines.get(i);
                if (line.colorDefs.isEmpty()) {
                    doc.insertString(doc.getLength(), line.toString(), set);
                } else {
                    int offset = 0;
                    for (int j = 0; j < line.colorDefs.size(); j++) {
                        LineEntry.ColorEntry colDef = line.colorDefs.get(j);
                        int nextPos;
                        if (j < line.colorDefs.size() - 1) {
                            nextPos = (line.colorDefs.get(j + 1)).pos;
                        } else {
                            nextPos = line.buffer.length();
                        }
                        int rgb = colDef.rgb;
                        StyleConstants.setForeground(set, new Color(rgb));
                        doc.insertString(doc.getLength(), line.buffer.substring(offset, nextPos), set);
                        offset = nextPos;
                    }
                }
                doc.insertString(doc.getLength(), "\n", set);
            }
        } catch (Throwable t) {
        }
    }

    public void setReference(int lineNbr, Object reference) {
        LineEntry le = lines.get(lineNbr);
        le.setReference(reference);
    }

    public void setReferenceToCurrent(Object reference) {
        setReference(curLine, reference);
    }

    public int getLine(Object reference) {
        for (int i = 0; i < lines.size(); i++) {
            LineEntry le = lines.get(i);
            if (reference.equals(le.getReference())) {
                return i;
            }
        }
        return -1;
    }

    public Object getReference(int line) {
        if (line >= lines.size()) {
            return null;
        }
        LineEntry le = lines.get(line);
        return le.getReference();
    }

    public void blendLines(int col, int percent) {
        int a = (percent << 7) / 100;
        int b = 128 - a;
        for (int i = 0; i < lines.size(); i++) {
            LineEntry line = lines.get(i);
            if (!line.colorDefs.isEmpty()) {
                for (int j = 0; j < line.colorDefs.size(); j++) {
                    LineEntry.ColorEntry colDef = line.colorDefs.get(j);
                    colDef.rgb =
                            (((((colDef.rgb & 0xff0000) * b) >> 7) + (((col & 0xff0000) * a) >> 7)) & 0xff0000) |
                                    (((((colDef.rgb & 0x00ff00) * b) >> 7) + (((col & 0x00ff00) * a) >> 7)) & 0x00ff00) |
                                    (((((colDef.rgb & 0x0000ff) * b) >> 7) + (((col & 0x0000ff) * a) >> 7)) & 0x0000ff);
                }
            }
        }
    }

    // LineEntry StringBuffer wrapper
    public static class LineEntry {
        StringBuffer buffer = new StringBuffer();
        Object ref = null;
        List<LineEntry.ColorEntry> colorDefs = new ArrayList<LineEntry.ColorEntry>();

        public void setReference(Object reference) {
            ref = reference;
        }

        public Object getReference() {
            return ref;
        }

        @Override
        public String toString() {
            return buffer.toString();
        }

        public static class ColorEntry {
            int pos;
            int rgb;

            public ColorEntry(int pos, int rgb) {
                this.pos = pos;
                this.rgb = rgb;
            }
        }
    }

    private void setCurrentColor(int rgb) {
        if (line.colorDefs.isEmpty()) {
            line.colorDefs.add(new LineEntry.ColorEntry(line.buffer.length(), rgb));
        } else {
            LineEntry.ColorEntry lastColEntry = line.colorDefs.get(line.colorDefs.size() - 1);
            if (lastColEntry.rgb != rgb) {
                line.colorDefs.add(new LineEntry.ColorEntry(line.buffer.length(), rgb));
            }
        }
    }

    public StringBuffer append(boolean arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(boolean arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(char arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(char arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(char[] arg0, int arg1, int arg2) {
        return line.buffer.append(arg0, arg1, arg2);
    }

    public StringBuffer append(char[] arg0, int arg1, int arg2, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0, arg1, arg2);
    }

    public StringBuffer append(char[] arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(char[] arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(double arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(double arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(float arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(float arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(int arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(int arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(long arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(long arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(Object arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(Object arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(String arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(String arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public StringBuffer append(StringBuffer arg0) {
        return line.buffer.append(arg0);
    }

    public StringBuffer append(StringBuffer arg0, int color) {
        setCurrentColor(color);
        return line.buffer.append(arg0);
    }

    public int capacity() {
        return line.buffer.capacity();
    }

    public char charAt(int arg0) {
        return line.buffer.charAt(arg0);
    }

    public StringBuffer delete(int arg0, int arg1) {
        return line.buffer.delete(arg0, arg1);
    }

    public StringBuffer deleteCharAt(int arg0) {
        return line.buffer.deleteCharAt(arg0);
    }

    public void ensureCapacity(int arg0) {
        line.buffer.ensureCapacity(arg0);
    }

    @Override
    public boolean equals(Object arg0) {
        return line.buffer.equals(arg0);
    }

    public void getChars(int arg0, int arg1, char[] arg2, int arg3) {
        line.buffer.getChars(arg0, arg1, arg2, arg3);
    }

    @Override
    public int hashCode() {
        return line.buffer.hashCode();
    }

    public int indexOf(String arg0, int arg1) {
        return line.buffer.indexOf(arg0, arg1);
    }

    public int indexOf(String arg0) {
        return line.buffer.indexOf(arg0);
    }

    public StringBuffer insert(int arg0, boolean arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, char arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, char[] arg1, int arg2, int arg3) {
        return line.buffer.insert(arg0, arg1, arg2, arg3);
    }

    public StringBuffer insert(int arg0, char[] arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, double arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, float arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, int arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, long arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, Object arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public StringBuffer insert(int arg0, String arg1) {
        return line.buffer.insert(arg0, arg1);
    }

    public int lastIndexOf(String arg0, int arg1) {
        return line.buffer.lastIndexOf(arg0, arg1);
    }

    public int lastIndexOf(String arg0) {
        return line.buffer.lastIndexOf(arg0);
    }

    public int length() {
        return line.buffer.length();
    }

    public StringBuffer replace(int arg0, int arg1, String arg2) {
        return line.buffer.replace(arg0, arg1, arg2);
    }

    public StringBuffer reverse() {
        return line.buffer.reverse();
    }

    public void setCharAt(int arg0, char arg1) {
        line.buffer.setCharAt(arg0, arg1);
    }

    public void setLength(int arg0) {
        line.buffer.setLength(arg0);
    }

    public CharSequence subSequence(int arg0, int arg1) {
        return line.buffer.subSequence(arg0, arg1);
    }

    public String substring(int arg0, int arg1) {
        return line.buffer.substring(arg0, arg1);
    }

    public String substring(int arg0) {
        return line.buffer.substring(arg0);
    }

}
