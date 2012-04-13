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

package analyser;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Bootstrap
{
    public static ResourceJarClassLoader CLASSLOADER;

    public static void main(String[] args)
    {
        try
        {
            // Get common paths
            ClassLoader base = ClassLoader.getSystemClassLoader();
            URL[] urls;
            if (base instanceof URLClassLoader)
            {
                urls = ((URLClassLoader) base).getURLs();
            }
            else
            {
                urls = new URL[]
                { new File(".").toURI().toURL() };
            }

            // load target class using custom class loader
            CLASSLOADER = new ResourceJarClassLoader(urls, base.getParent(), base);
            CLASSLOADER.addClasspath("lib/asm-all-3.0.jar");
            CLASSLOADER.arm();
            Class<?> cls = CLASSLOADER.loadClass("analyser.Analyser");

            // invoke "ctxmain" method of target class
            Class<?>[] ptypes = new Class[] { args.getClass() };
            Method main = cls.getDeclaredMethod("ctxmain", ptypes);
            String[] pargs = new String[args.length];
            System.arraycopy(args, 0, pargs, 0, pargs.length);
            Thread.currentThread().setContextClassLoader(CLASSLOADER);
            main.invoke(null, new Object[] { pargs });
        } catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
