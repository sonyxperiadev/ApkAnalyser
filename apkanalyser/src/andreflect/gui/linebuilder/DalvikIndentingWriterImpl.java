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
import java.io.Writer;


import org.jf.dexlib.Code.Instruction;
import org.jf.util.IndentingWriter;

import analyser.gui.LineBuilder;
import analyser.gui.LineBuilderFormatter;
import andreflect.Util;
import andreflect.gui.linebuilder.DalvikByteCodeLineBuilder.DalvikBytecodeOffset;

public class DalvikIndentingWriterImpl extends IndentingWriter implements DalvikIndentingWriter {
    private final LineBuilder lb;
    private int indentLevel = 0;
    private boolean beginningOfLine;
    private final char[] buffer = new char[16];
    private int linenumber = -1;
    private int codeaddress = -1;
    private int color = 0;
    private Instruction instruction = null;

    protected DalvikIndentingWriterImpl(LineBuilder lb) {
        super(null);
        this.lb = lb;
    }

    @Override
    public void setColor(int c) {
        color = c;
    }

    @Override
    public void resetColor() {
        color = COLOR_TEXT;
    }

    @Override
    public void setLine(int line) {
        linenumber = line;
    }

    @Override
    public void setInsAddress(int address) {
        codeaddress = address;
    }

    @Override
    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public String getProtoString(String typeDescriptor) {
        return Util.getProtoString(typeDescriptor);
    }

    @Override
    public void write(int chr) throws IOException {
        //synchronized(lock) {
        if (chr == '\n') {
            lb.newLine();
            beginningOfLine = true;
        } else {
            if (beginningOfLine
                    && color != COLOR_LABEL) {

                if (codeaddress != -1
                        && color != LineBuilderFormatter.COLOR_KEYWORD) {
                    lb.append(String.format("%1$5X", codeaddress), COLOR_CODEADDRESS);
                    lb.setReferenceToCurrent(new DalvikBytecodeOffset(instruction, linenumber, codeaddress));
                } else {
                    lb.append("     ");
                }

                if (codeaddress != -1
                        && linenumber != -1
                        && color != LineBuilderFormatter.COLOR_KEYWORD) {
                    lb.append(String.format("%1$5d", linenumber), COLOR_SOURCELINE);
                } else {
                    lb.append("     ");
                }

                codeaddress = -1;

                for (int i = 0; i < indentLevel; i++) {
                    lb.append(' ');
                }
            }

            if (color == COLOR_LABEL) {
                lb.append(Character.toUpperCase((char) chr), color);
            } else {
                lb.append((char) chr, color);
            }
            beginningOfLine = false;
        }
        //}
    }

    @Override
    public void write(String s) throws IOException {
        //synchronized (lock) {
        boolean isKeyword = s.startsWith(".") && s.length() > 1;
        if (isKeyword) {
            setColor(LineBuilderFormatter.COLOR_KEYWORD);
        }
        for (int i = 0; i < s.length(); i++) {
            write(s.charAt(i));
        }
        if (isKeyword) {
            resetColor();
        }
        //}
    }

    @Override
    public void write(char[] chars) throws IOException {
        //synchronized(lock) {
        for (char chr : chars) {
            write(chr);
        }
        //}
    }

    @Override
    public void write(char[] chars, int start, int len) throws IOException {
        //synchronized(lock) {
        len = start + len;
        while (start < len) {
            write(chars[start++]);
        }
        //}
    }

    @Override
    public void write(String str, int start, int len) throws IOException {
        //synchronized(lock) {
        len = start + len;
        while (start < len) {
            write(str.charAt(start++));
        }
        //}
    }

    @Override
    public Writer append(CharSequence charSequence) throws IOException {
        write(charSequence.toString());
        return this;
    }

    @Override
    public Writer append(CharSequence charSequence, int start, int len) throws IOException {
        write(charSequence.subSequence(start, len).toString());
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    @Override
    public void flush() throws IOException {
        //synchronized(lock) {
        //          writer.flush();
        //}
    }

    @Override
    public void close() throws IOException {
        //synchronized(lock) {
        //          writer.close();
        //}
    }

    @Override
    public void indent(int indentAmount) {
        //synchronized(lock) {
        indentLevel += indentAmount;
        if (indentLevel < 0) {
            indentLevel = 0;
        }
        //}
    }

    @Override
    public void deindent(int indentAmount) {
        //synchronized(lock) {
        indentLevel -= indentAmount;
        if (indentLevel < 0) {
            indentLevel = 0;
        }
        //}
    }

    public void printLongAsHex(long value) throws IOException {
        int bufferIndex = 0;
        do {
            int digit = (int) (value & 15);
            if (digit < 10) {
                buffer[bufferIndex++] = (char) (digit + '0');
            } else {
                buffer[bufferIndex++] = (char) ((digit - 10) + 'a');
            }

            value >>>= 4;
        } while (value != 0);

        while (bufferIndex > 0) {
            write(buffer[--bufferIndex]);
        }
    }

    public void printIntAsDec(int value) throws IOException {
        int bufferIndex = 0;
        boolean negative = value < 0;

        do {
            int digit = value % 10;
            buffer[bufferIndex++] = (char) (digit + '0');

            value = value / 10;
        } while (value != 0);

        if (negative) {
            write('-');
        }

        while (bufferIndex > 0) {
            write(buffer[--bufferIndex]);
        }
    }
}
