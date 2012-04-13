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

package andreflect.adb;

import gui.Canceable;

import java.io.File;
import java.io.IOException;

import util.ProcessHandler;
import util.ProcessListener;

public class AdbProxy {

    public static final int LEVEL_V = 1;
    public static final int LEVEL_D = 2;
    public static final int LEVEL_I = 3;
    public static final int LEVEL_W = 4;
    public static final int LEVEL_E = 5;
    public static final int LEVEL_F = 6;
    public static final int LEVEL_S = 7;

    String m_adbPath;

    public AdbProxy(String adbPath) {
        m_adbPath = adbPath;
    }

    public String installApk(File midletPath, Canceable c) throws Exception {
        String[] args = {/*"-d",*/"install", "-r", midletPath.getAbsolutePath()};
        return runAdbSync(args, c);
    }

    public String uninstallApk(String packageName, Canceable c) throws Exception {
        String[] args = {/*"-d",*/"uninstall", packageName};
        return runAdbSync(args, c);
    }

    public String startApk(String launcher, Canceable c) throws Exception {
        String[] args = {/*"-d",*/"shell", "am", "start", "-n", launcher };
        String res = runAdbSync(args, c);
        return res;
    }

    public ProcessHandler openStdout() throws Exception {
        String[] args = {/*"-d", */"logcat", "-v", "time", "APKANALYSER:V", "*:D" };
        ProcessHandler ph = new ProcessHandler(m_adbPath, args, null);
        return ph;
    }

    public String clearStdout(Canceable c) throws Exception {
        String[] args = {/*"-d", */"logcat", "-c" };
        String res = runAdbSync(args, c);
        return res;
    }

    String runAdbSync(String[] args, Canceable c) throws IOException {
        final Object LOCK = new Object();
        final StringBuffer res = new StringBuffer();
        final ProcessHandler ph = new ProcessHandler(m_adbPath, args, null);
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

    public String[] getArgsLevel(int level) {
        String arg = null;
        switch (level) {
        case LEVEL_V:
            arg = new String("*:V");
            break;
        case LEVEL_D:
            arg = new String("*:D");
            break;
        case LEVEL_W:
            arg = new String("*:W");
            break;
        case LEVEL_F:
            arg = new String("*:F");
            break;
        case LEVEL_I:
            arg = new String("*:I");
            break;
        case LEVEL_S:
            arg = new String("*:S");
            break;
        case LEVEL_E:
            arg = new String("*:E");
            break;
        }
        String[] args = {/*"-d",*/"logcat", "-v", "time", "APKANALYSER:V", arg };
        return args;
    }
}
