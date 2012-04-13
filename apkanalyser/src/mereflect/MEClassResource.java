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

package mereflect;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a resource in a context, meaning a data resource to a class. The class data
 * is retreived by reading the inputstream provided by <code>getInputStream()</code>
 */
public interface MEClassResource
{
    /**
     * Returns the context to which this
     * resource belongs
     * @return the surrounding context
     */
    public MEClassContext getContext();

    /**
     * Returns an unique identifier for
     * this resource in this context
     * @return specification of this context and resource
     */
    public String getContextualSpecification();

    /**
     * Returns full package definition and classname
     * of the class this resource points to
     * @return The full classname
     */
    public String getClassName();

    /**
     * Returns name of the class this resource points to
     * @return The classname
     */
    public String getName();

    /**
     * Returns the package of the class this resource
     * points to
     * @return The package
     */
    public String getPackage();

    /**
     * Returns an input stream providing data of this
     * class resource
     * @return an input stream of class data
     * @throws IOException if input stream cannot be provided
     */
    public InputStream getInputStream() throws IOException;
}
