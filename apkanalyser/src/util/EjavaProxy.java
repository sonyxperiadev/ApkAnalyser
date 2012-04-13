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

import gui.Canceable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class EjavaProxy {
    String ejavaPath;

    public EjavaProxy(String ejavaPath) {
        this.ejavaPath = ejavaPath;
    }

    public List<Midlet> getMidlets(Canceable c) throws Exception {
        String[] args = { "-xml", "midlets" };
        String res = runEjavaSync(args, c);
        List<Midlet> midlets = new ArrayList<Midlet>();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new ByteArrayInputStream(res.getBytes()), new MidletParserHandler(midlets));
        return midlets;
    }

    public Midlet installMidlet(File midletPath, Canceable c) throws Exception {
        String[] args = { "-xml", "install", "\"" + midletPath.getAbsolutePath() + "\"" };
        String res = "<?xml version=\"1.0\"?>\r\n<midlets>" + runEjavaSync(args, c) + "</midlets>";
        List<Midlet> midlets = new ArrayList<Midlet>();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new ByteArrayInputStream(res.getBytes()), new MidletParserHandler(midlets));
        if (midlets.size() > 0) {
            return midlets.get(0);
        } else {
            System.out.println("No midlets parsed, returnstring from ejava:");
            System.out.println(res);
            return null;
        }
    }

    public String startMidlet(Midlet midlet, Canceable c) throws Exception {
        String[] args = { "start", Integer.toString(midlet.id), Integer.toString(midlet.entry) };
        String res = runEjavaSync(args, c);
        return res;
    }

    public ProcessHandler openStdout() throws Exception {
        String[] args = { "open" };
        ProcessHandler ph = new ProcessHandler(ejavaPath, args, null);
        return ph;
    }

    String runEjavaSync(String[] args, Canceable c) throws IOException {
        final Object LOCK = new Object();
        final StringBuffer res = new StringBuffer();
        final ProcessHandler ph = new ProcessHandler(ejavaPath, args, null);
        ph.addProcessListener(new ProcessListener() {
            @Override
            public void started() {
            }

            @Override
            public void died(int ret) {
                synchronized (LOCK) {
                    LOCK.notify();
                }
            }

            @Override
            public void stderr(char c) {
            }

            @Override
            public void stdout(char c) {
                res.append(c);
            }
        });
        synchronized (LOCK) {
            ph.start();
            while (ph.isRunning() && (c != null && c.isRunning() || c == null)) {
                try {
                    LOCK.wait(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
        return res.toString();
    }

    public class Midlet {
        public int id;
        public int entry;
        public String name;
        public String version;
        public String vendor;
    } // Midlet

    class MidletParserHandler extends DefaultHandler {
        Midlet curMidlet = null;
        List<Midlet> midlets;

        public MidletParserHandler(List<Midlet> midlets) {
            this.midlets = midlets;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) {
            if (qName.equals("midlet")) {
                curMidlet = new Midlet();
                curMidlet.id = Integer.parseInt(attributes.getValue("id"));
                curMidlet.name = attributes.getValue("name");
            } else if (curMidlet != null) {
                if (qName.equals("vendor")) {
                    curMidlet.vendor = attributes.getValue("name");
                }
                if (qName.equals("version")) {
                    curMidlet.version = attributes.getValue("name");
                }
                if (qName.equals("entry")) {
                    curMidlet.entry = Integer.parseInt(attributes.getValue("id"));
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equals("midlet")) {
                midlets.add(curMidlet);
                curMidlet = null;
            }
        }
    } // MidletParserHandler
}
