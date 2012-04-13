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

package jerl.bcm.inj;

import java.util.List;

public abstract class Injection {
    public static final int METHOD_ENTRY_INJECTION = 1;

    public static final int METHOD_OFFSET_INJECTION = 2;

    public static final int METHOD_CALL_INJECTION = 3;

    public static final int METHOD_EXIT_INJECTION = 4;

    public static final int FIELD_ADD_INJECTION = 5;

    public static final int METHOD_ADD_INJECTION = 6;

    public static final int METHOD_EXCEPTION_HANDLER_INJECTION = 7;

    public static final int METHOD_LOCAL_ACCESS_INJECTION = 8;
    public static final int METHOD_FIELD_ACCESS_INJECTION = 9;
    public boolean performed = false;

    /**
     * Returns the injection type of this injection
     * 
     * @return injection type
     */
    public abstract int getInjectionType();

    public abstract List<String> getInstanceData();
}
