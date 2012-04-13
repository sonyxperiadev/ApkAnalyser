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

package analyser.io;

public interface TraceProtocol {
    /* 11111000 */
    public static final byte PROTOCOL_HEAD_ID = (byte) 0xf8;

    /* 11111000 idididid idididid idididid */
    public static final byte THREAD = (byte) 0xf8;
    /* 11111001 idididid idididid idididid */
    public static final byte METHOD_ENTER = (byte) 0xf9;
    /* 11111010 idididid idididid idididid */
    public static final byte METHOD_EXIT = (byte) 0xfa;
    /* 11111011 idididid idididid idididid */
    public static final byte METHOD_EXCEPTION = (byte) 0xfb;
    /* 11111100 idididid idididid idididid pntpntpn pntpntpn */
    public static final byte METHOD_JUMP_POINT = (byte) 0xfc;
    /* 11111101 idididid idididid idididid nxtidnxt nxtidnxt nxtidnxt */
    public static final byte METHOD_INVOKATION = (byte) 0xfd;
    /* 11111110 [0 enter | 1 exit] nnnnnnn nnnnnnnn */
    public static final byte MONITOR = (byte) 0xfe;
}