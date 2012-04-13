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

package jerl.blockformat;

public class BFParseException extends Exception {

    private static final long serialVersionUID = 3997294387566845603L;

    public BFParseException(Throwable cause) {
        super(cause);
    }

    public BFParseException(String message,
            Throwable cause) {
        super(message, cause);
    }

    public BFParseException(String message) {
        super(message);
    }
}
