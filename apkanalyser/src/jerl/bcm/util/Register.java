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

package jerl.bcm.util;

public class Register {
    private static int[] register_a = new int[80];
    private static long num_register = 0;
    private static int i_register = 0;

    public static void register(int i) {
        if (i_register == register_a.length) {
            i_register = 0;
        }
        register_a[i_register++] = i;
        num_register++;
    }

    public static void printTest() {
        System.out.println("RegisterprintTest");
    }

    public static void printRegistrations() {
        //System.out.println("i_reg="+i_register);
        System.out.println("======= PrintRegistrations =======");
        for (int i = i_register - 1; i >= 0; i--) {
            System.out.println(register_a[i]);
        }
        if (num_register > register_a.length) {
            //System.out.println("inter n="+num_register);
            for (int i = register_a.length - 1; i >= i_register; i--) {
                System.out.println(register_a[i]);
            }
        }
    }

    public static void printCharA(char[] a) {
        System.out.println("========= Begin =============");
        System.out.println(new String(a).toString());
        System.out.println("========= End =============");
        if (a.length > 10) {
            System.out.println("0: 0x" + Integer.toHexString(a[0]));
            System.out.println("1: 0x" + Integer.toHexString(a[1]));
            System.out.println("2: 0x" + Integer.toHexString(a[2]));
            System.out.println("end-3: 0x" + Integer.toHexString(a[a.length - 4]));
            System.out.println("end-2: 0x" + Integer.toHexString(a[a.length - 3]));
            System.out.println("end-1: 0x" + Integer.toHexString(a[a.length - 2]));
            System.out.println("end: 0x" + Integer.toHexString(a[a.length - 1]));
        }
    }
}
