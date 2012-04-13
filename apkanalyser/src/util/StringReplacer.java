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

public class StringReplacer {
    public static String replaceFirst(String text, String pattern, String replacement) {
        StringBuffer sb = new StringBuffer(text);
        int i = sb.indexOf(pattern);
        if (i >= 0) {
            sb.delete(i, i + pattern.length());
            sb.insert(i, replacement);
        }
        return sb.toString();
    }

    public static String replaceAll(String text, String pattern, String replacement) {
        int i = 0;
        StringBuffer sb = new StringBuffer(text);
        while (i >= 0) {
            i = sb.indexOf(pattern, i);
            if (i >= 0) {
                sb.delete(i, i + pattern.length());
                sb.insert(i, replacement);
                i += replacement.length();
            }
        }
        return sb.toString();
    }

    public static String replaceAllStatements(String text, String pattern, String replacement) {
        int i = 0;
        StringBuffer sb = new StringBuffer(text);
        while (i >= 0) {
            i = sb.indexOf(pattern, i);
            if (i >= 2) {
                char pre1 = sb.charAt(i - 1);
                char pre2 = sb.charAt(i - 2);

                if (pre1 == ':' || pre1 == ' ' && pre2 == ':')
                {
                    sb.delete(i, i + pattern.length());
                    sb.insert(i, replacement);
                    i += replacement.length();
                }
                else
                {
                    i++;
                }
            }
        }
        return sb.toString();
    }
}
