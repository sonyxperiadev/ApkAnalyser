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

import java.util.ArrayList;
import java.util.List;

public class Parser
{
    List<Identifier> m_identifiers = new ArrayList<Identifier>();

    public void add(Identifier id)
    {
        m_identifiers.add(id);
    }

    public void parse(ParserReporter report, StringBuffer sb)
    {
        int index = 0;
        int len = sb.length();
        while (index >= 0 && index < len)
        {
            //System.out.print(">>> INDEX " + index + "/" + len);
            index = parseRecurse(report, sb, m_identifiers, index, len);
        }
    }

    protected int parseRecurse(ParserReporter report, StringBuffer sb,
            List<Identifier> identifiers, int index, int end)
    {
        //int len = sb.length();
        boolean nomatch = true;
        for (int i = 0; i < identifiers.size(); i++)
        {
            Identifier id = identifiers.get(i);
            //System.out.println("\t\t\tPROCESSING " + id + " " + index + "-" + end);
            int curIndex = index;
            boolean cont = true;
            while (cont && curIndex < end)
            {
                //System.out.print(">>> CURINDEX " + curIndex + "/" + end);
                String pre = id.getPre();
                String post = id.getPost();

                if (pre != null) {
                    curIndex = sb.indexOf(pre, curIndex);
                }
                if (curIndex == -1 || curIndex >= end)
                {
                    //System.out.println("\t\t\t\t" + id + "\tfail pre " + index + "-" + end);
                    cont = false;
                }
                if (cont)
                {
                    if (pre != null) {
                        curIndex += pre.length();
                    }
                    int start = curIndex;

                    if (post != null) {
                        curIndex = sb.indexOf(post, curIndex);
                    } else {
                        curIndex = end - 1;
                    }
                    int stop = curIndex;
                    if (curIndex == -1 || curIndex >= end || start == stop)
                    {
                        //System.out.println("\t\t\t\t" + id + "\tfail post " + index + "-" + end);
                        cont = false;
                    }
                    if (cont)
                    {
                        nomatch = false;
                        if (post != null) {
                            curIndex += post.length();
                        }
                        //System.out.print("\t\t\t\t" + id + "\t match at " + start + "-" + stop + " " + index + "-" + end);
                        //System.out.println(" [" + sb.substring(start, stop).replace('\n', ' ') + "]");
                        if (id.hasSubIdentifiers())
                        {
                            int nextIndex = parseRecurse(report, sb, id.getSubIdentifiers(), start, stop);
                            if (pre == null && post == null) {
                                nomatch = nextIndex == (end - 1);
                            }
                            curIndex = nextIndex;
                        }
                        else
                        {
                            report.match(id, sb, start, stop);
                        }
                        cont &= id.isMultiple();
                        index = curIndex;
                    }
                }
            }
            if (nomatch) {
                index = end;
            }
        }
        return index;
    }

    public static class Identifier
    {
        String m_pre;
        String m_post;
        String m_name;
        boolean m_multiple;
        List<Identifier> m_subIdentifiers = new ArrayList<Identifier>();

        public Identifier(boolean multiple)
        {
            this(null, null, multiple, "[SCOPE]");
        }

        public Identifier(boolean multiple, String name)
        {
            this(null, null, multiple, name);
        }

        public Identifier(String pre, String post, boolean multiple)
        {
            this(pre, post, multiple, "[" + pre + " - " + post + "]");
        }

        public Identifier(String pre, String post, boolean multiple, String name)
        {
            m_pre = pre;
            m_post = post;
            m_multiple = multiple;
            m_name = name;
        }

        public String getPre()
        {
            return m_pre;
        }

        public String getPost()
        {
            return m_post;
        }

        public void add(Identifier id)
        {
            m_subIdentifiers.add(id);
        }

        public List<Identifier> getSubIdentifiers()
        {
            return m_subIdentifiers;
        }

        public boolean hasSubIdentifiers()
        {
            return !m_subIdentifiers.isEmpty();
        }

        public boolean isMultiple()
        {
            return m_multiple;
        }

        @Override
        public String toString()
        {
            return m_name;
        }
    }
}