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

package codegen;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.TreeMap;

import codegen.Parser.Identifier;

public class BytecodeTableGenerator
{
    static String[] REFERENCE =
    {
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc1.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc2.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc3.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc4.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc5.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc6.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc7.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc8.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc9.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc10.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc11.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc12.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc13.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc14.html",
            "http://java.sun.com/docs/books/vmspec/2nd-edition/html/Instructions2.doc15.html"
    };

    static final String FORMAT_ID_PRE = "<p><b>Format</b><br>";
    static final String FORMAT_ID_IDX_PRE = "<i>";
    static final String FORMAT_ID_IDX_POST = "</i>";
    static final String FORMAT_ID_POST = "</Table>";

    static final String FORMS_ID_PRE = "<p><b>Forms</b><br>";
    static final String FORMS_ID_IDX_NAME_PRE = "<i>";
    static final String FORMS_ID_IDX_NAME_POST = "</i>";
    static final String FORMS_ID_IDX_CODE_PRE = "=";
    static final String FORMS_ID_IDX_CODE_POST = "(0x";
    static final String FORMS_ID_POST = "<p><b>Operand Stack</b><br>";

    static final Parser.Identifier FORMAT =
            new Parser.Identifier(FORMAT_ID_PRE, FORMAT_ID_POST, false, "FORMAT");
    static final Parser.Identifier FORMAT_IDX =
            new Parser.Identifier(FORMAT_ID_IDX_PRE, FORMAT_ID_IDX_POST, true, "FORMAT_IDX");
    static final Parser.Identifier FORMS =
            new Parser.Identifier(FORMS_ID_PRE, FORMS_ID_POST, false, "FORMS");
    static final Parser.Identifier FORMS_NC_MULT =
            new Parser.Identifier(true, "FORMS_NC_MULT");
    static final Parser.Identifier FORMS_NAME =
            new Parser.Identifier(FORMS_ID_IDX_NAME_PRE, FORMS_ID_IDX_NAME_POST, false, "FORMS_NAME");
    static final Parser.Identifier FORMS_CODE =
            new Parser.Identifier(FORMS_ID_IDX_CODE_PRE, FORMS_ID_IDX_CODE_POST, false, "FORMS_CODE");

    static Parser m_parser;
    static ParserReporter m_reporter = new ByteCodeReporter();
    static Map<Integer, ByteCode> m_byteCodeMap = new TreeMap<Integer, ByteCode>();

    public static void main(String[] args) throws Exception
    {
        FORMAT.add(FORMAT_IDX);
        FORMS.add(FORMS_NC_MULT);
        FORMS_NC_MULT.add(FORMS_NAME);
        FORMS_NC_MULT.add(FORMS_CODE);
        m_parser = new Parser();
        m_parser.add(FORMAT);
        m_parser.add(FORMS);

        for (int i = 0; i < REFERENCE.length; i++)
        {
            URL url = new URL(REFERENCE[i]);
            URLConnection urlconn = url.openConnection();
            urlconn.connect();
            InputStream is = (InputStream) urlconn.getContent();
            parse(read(is));
        }
        printByteCodes();
    }

    public static void printByteCodes()
    {
        System.out.println("  public static final String OP_UNDEFINED = \"N/A\";");
        System.out.println("  public static final int OP_UNDEFINED_LEN = 1;");
        System.out.println();
        System.out.println("  public static final String[] BC_OPCODES = {");
        for (int i = 0; i < 256; i++)
        {
            ByteCode bc = m_byteCodeMap.get(new Integer(i));
            if (bc == null) {
                System.out.print("    OP_UNDEFINED + \"[" + Integer.toHexString(i) + "]\"");
            } else {
                System.out.print("    \"" + bc.instr + "\"");
            }
            if (i < 255) {
                System.out.print(",");
            }
            if (bc != null)
            {
                System.out.print("\t\t");
                System.out.print("// " + bc.length);
            }
            System.out.println();
        }
        System.out.println("  };");

        System.out.println("  public static final int[] BC_LENGTHS = {");
        for (int i = 0; i < 256; i++)
        {
            ByteCode bc = m_byteCodeMap.get(new Integer(i));
            if (bc == null) {
                System.out.print("    OP_UNDEFINED_LEN");
            } else {
                System.out.print("    " + bc.length);
            }
            if (i < 255) {
                System.out.print(",");
            }
            if (bc != null)
            {
                System.out.print("\t\t");
                System.out.print("// " + bc.instr);
            }
            System.out.println();
        }
        System.out.println("  };");
    }

    public static void parse(final StringBuffer sb)
    {
        m_parser.parse(m_reporter, sb);
    }

    public static StringBuffer read(InputStream is) throws Exception
    {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        do
        {
            i = is.read();
            if (i != -1)
            {
                sb.append((char) i);
            }
        } while (i != -1);
        return sb;
    }

    static class ByteCodeReporter implements ParserReporter
    {
        boolean firstIndex = true;
        int index = 0;
        String name = null;
        String code = null;

        @Override
        public void match(Identifier id, StringBuffer sb, int startIndex, int endIndex)
        {
            if (id == FORMAT_IDX)
            {
                System.err.println("FORMAT_IX :\t" + sb.substring(startIndex, endIndex));
                if (firstIndex) {
                    index = 1;
                } else {
                    index++;
                }
                firstIndex = false;
            }
            else
            {
                firstIndex = true;
            }

            if (id == FORMS_NAME)
            {
                System.err.println("FORMS_NAME:\t" + sb.substring(startIndex, endIndex));
                name = sb.substring(startIndex, endIndex).trim();
            }
            else if (id == FORMS_CODE)
            {
                System.err.println("FORMS_CODE:\t" + sb.substring(startIndex, endIndex));
                code = sb.substring(startIndex, endIndex);
                int iCode = Integer.parseInt(code.trim());
                m_byteCodeMap.put(new Integer(iCode), new ByteCode(name, iCode, index));
            }
        }
    }

    static class ByteCode
    {
        public String instr;
        public int code;
        public int length;

        public ByteCode(String instr, int code, int length)
        {
            this.instr = instr;
            this.code = code;
            this.length = length;
        }
    }
}
