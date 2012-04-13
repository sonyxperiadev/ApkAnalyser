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

package jerl.blockformat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Stack;
import java.util.StringTokenizer;

public class BFReader {
    private static final String BEGIN_HEADER = "\\begin{";

    private static final String END_HEADER = "\\end{";

    private final Stack<String> blockStack = new Stack<String>();

    private final LineNumberReader reader;

    public BFReader(InputStream is) {
        reader = new LineNumberReader(new InputStreamReader(is));
    }

    public void accept(BFVisitor visitor) throws IOException, BFParseException {
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String lineTrim = line.trim();
                if (lineTrim.startsWith("#")) {
                    continue;
                }
                else if (line.indexOf(BEGIN_HEADER) != -1) {
                    String blockName = getBlockNameBegin(line);
                    String[] args = getBlockArgumentsBegin(line);
                    blockStack.push(blockName);
                    visitor.visitBeginBlock(blockName, args);
                } else if (line.indexOf(END_HEADER) != -1) {
                    String blockName = getBlockNameEnd(line);
                    String fromStack = blockStack.pop();
                    if (!blockName.equals(fromStack)) {
                        throw new BFParseException(
                                "ERROR: expected end of block '" + fromStack
                                        + "' got '" + blockName + "'. ("
                                        + reader.getLineNumber() + "), line='"
                                        + line + "'");
                    }
                    visitor.visitEndBlock(blockName);
                } else if (line.indexOf("=") != -1) {
                    String key = getPropertyKey(line);
                    String value = getPropertyValue(line);
                    visitor.visitProperty(key, value);
                }
            }
        } catch (Exception e) {
            throw new BFParseException("ERROR: " + e.getClass().getName() + ": " + e.getMessage() + ". ("
                    + reader.getLineNumber() + "), line='" + line + "'");
        }
        if (blockStack.size() != 0) {
            throw new BFParseException(
                    "ERROR: found more block begin than block end");
        }
    }

    private String getPropertyKey(String line) throws BFParseException {
        int i = line.indexOf('=');
        if (i == -1) {
            throw new BFParseException(
                    "ERROR: expecting property 'key=value'. ("
                            + reader.getLineNumber() + "), line='" + line + "'");
        }
        return line.substring(0, i).trim();
    }

    private String getPropertyValue(String line) throws BFParseException {
        int i = line.indexOf('=');
        if (i == -1 || line.length() < i + 2) {
            throw new BFParseException(
                    "ERROR: expecting property 'key=value'. ("
                            + reader.getLineNumber() + "), line='" + line + "'");
        }
        return line.substring(i + 1).trim();
    }

    private String[] getBlockArgumentsBegin(String line)
            throws BFParseException {
        int startI = line.indexOf(BEGIN_HEADER);
        if (startI == -1) {
            throw new BFParseException("ERROR: missing block header. ("
                    + reader.getLineNumber() + "), line='" + line + "'");
        }
        startI = line.indexOf('[', startI);
        if (startI == -1) {
            return new String[0];
        }
        int endI = line.indexOf(']', startI);
        if (startI == -1) {
            throw new BFParseException(
                    "ERROR: missing ']' at end of argument list. ("
                            + reader.getLineNumber() + "), line='" + line + "'");
        }
        String args = line.substring(startI + 1, endI).trim();
        StringTokenizer st = new StringTokenizer(args, ",");
        String[] ret = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            ret[i++] = st.nextToken().trim();
        }
        return ret;
    }

    private String getBlockNameBegin(String line) throws BFParseException {
        int startI;
        startI = line.indexOf(BEGIN_HEADER);
        if (startI == -1) {
            throw new BFParseException("ERROR: missing block header. ("
                    + reader.getLineNumber() + "), line='" + line + "'");
        }
        int endI = line.indexOf('}', startI);
        if (endI == -1) {
            throw new BFParseException("ERROR: missing '}'. ("
                    + reader.getLineNumber() + "), line='" + line + "'");
        }
        return line.substring(startI + BEGIN_HEADER.length(), endI).trim();
    }

    private String getBlockNameEnd(String line) throws BFParseException {
        int startI;
        startI = line.indexOf(END_HEADER);
        if (startI == -1) {
            throw new BFParseException("ERROR: missing block header. ("
                    + reader.getLineNumber() + "), line='" + line + "'");
        }
        int endI = line.indexOf('}', startI);
        if (endI == -1) {
            throw new BFParseException("ERROR: missing '}'. ("
                    + reader.getLineNumber() + "), line='" + line + "'");
        }
        return line.substring(startI + END_HEADER.length(), endI).trim();
    }

    public static void main(String[] args) throws IOException, BFParseException {
        BFReader pr = new BFReader(new FileInputStream("res/ex1"));

        pr.accept(new BFVisitor() {
            @Override
            public void visitBeginBlock(String blockName, String[] args) {
                System.out.println("start block, name='" + blockName + "'");
                for (int i = 0; i < args.length; i++) {
                    System.out.println("\t'" + args[i] + "'");
                }
            }

            @Override
            public void visitEndBlock(String blockName) {
                System.out.println("end block, name='" + blockName + "'");
            }

            @Override
            public void visitProperty(String key, String value) {
                System.out.println("key='" + key + "', value='" + value + "'");
            }
        });
        /*
         * System.out.println("'"+getBlockNameBegin("\\begin{ blockname
         * }[ab]")+"'");
         * System.out.println("'"+getBlockNameEnd("\\end{blockname}")+"'");
         * System.out.println("'"+getBlockArgumentsBegin("\\begin{ blockname
         * }")+"'"); System.out.println("'"+getBlockArgumentsBegin("\\begin{
         * blockname }[arg1=2, arg5=34342]")+"'");
         */
    }
}
