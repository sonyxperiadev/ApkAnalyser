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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessHandler {
    String cmd;
    String[] args;
    String runPath;
    Process process;
    List<ProcessListener> listeners;
    StreamListener stdinListener;
    StreamListener stderrListener;
    volatile boolean isRunning;

    public ProcessHandler(String cmd, String[] args, String runPath) {
        this.cmd = cmd;
        this.args = args;
        this.runPath = runPath;
        listeners = new ArrayList<ProcessListener>();
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public void setRunPath(String runPath) {
        this.runPath = runPath;
    }

    public void start() throws IOException {
        String cmdString = cmd;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                cmdString += " " + args[i];
            }
        }
        process = Runtime.getRuntime().exec(cmdString, null, runPath == null ? null : new File(runPath));
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                try {
                    ret = process.waitFor();
                    isRunning = false;
                } catch (InterruptedException ignore) {
                }
                for (int i = 0; i < listeners.size(); i++) {
                    (listeners.get(i)).died(ret);
                }
            }
        }).start();
        stdinListener = new StreamListener(process.getInputStream(), false);
        stderrListener = new StreamListener(process.getErrorStream(), true);
        new Thread(stdinListener).start();
        new Thread(stderrListener).start();
        for (int i = 0; i < listeners.size(); i++) {
            (listeners.get(i)).started();
        }
    }

    public void addProcessListener(ProcessListener pl) {
        listeners.add(pl);
    }

    public void kill() {
        process.destroy();
        stdinListener.stop();
        stderrListener.stop();
        isRunning = false;
    }

    public void restart() throws IOException {
        kill();
        start();
    }

    public boolean isRunning() {
        return isRunning;
    }

    class StreamListener implements Runnable {
        InputStream is;
        boolean errStream;
        volatile boolean running = true;

        public StreamListener(InputStream is, boolean err) {
            this.is = is;
            errStream = err;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    int ch = is.read();
                    if (ch == -1) {
                        stop();
                    } else {
                        for (int i = 0; i < listeners.size(); i++) {
                            ProcessListener pl = listeners.get(i);
                            if (errStream) {
                                pl.stderr((char) ch);
                            } else {
                                pl.stdout((char) ch);
                            }
                        }
                    }
                } catch (Exception e) {
                    stop();
                }
            }
        }

        public void stop() {
            try {
                is.close();
            } catch (IOException e) {
            }
            running = false;
        }
    }
}
