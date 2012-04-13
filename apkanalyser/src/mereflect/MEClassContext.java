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

import andreflect.DexReferenceCache;

/**
 * A context from where classes can be loaded. Conceptually same as a ClassLoader.
 */
public interface MEClassContext
{
    /**
     * Sets the parent context
     * @param ctx   the parent context
     */
    public void setParentContext(MEClassContext ctx);

    /**
     * Returns the parent context
     * @return   the parent context
     */
    public MEClassContext getParentContext();

    /**
     * Returns all children context of this context
     * @return children contexts
     */
    public MEClassContext[] getContexts();

    /**
     * Adds a child context
     * @param ctx the child context
     */
    public void addContext(MEClassContext ctx);

    /**
     * Removes a child context
     * @param ctx the child context
     */
    public void removeContext(MEClassContext ctx);

    /**
     * Returns name of this context
     * @return context name
     */
    public String getContextName();

    /**
     * Returns a description of this context type
     * @return context type description
     */
    public String getContextDescription();

    /**
     * Returns all class resources of this context
     * @return the class resources
     * @throws IOException if resources cannot be found
     */
    public MEClassResource[] getClassResources() throws IOException;

    /**
     * Returns specified class resource of this context
     * @param the class resource name
     * @return the class resource or null if resource cannot be found
     * @throws IOException if resources cannot be found
     */
    public MEClassResource getClassResource(String name) throws IOException;

    /**
     * Returns all identified classes in this context
     * @return Array of classnames identified by this context
     * @throws IOException if classresources cannot be read
     */
    public String[] getClassnames() throws IOException;

    /**
     * Returns the specified class
     * @param classname the class to return
     * @return a class
     * @throws IOException if class cannot be loaded
     * @throws ClassNotFoundException if specified class is not defined in this context
     */
    public MEClass getMEClass(String classname) throws IOException, ClassNotFoundException;

    /**
     * Returns if this context belongs to the midlet classpath
     * @return true if this context belongs to midlet classpath, false otherwise
     */
    public boolean isMidlet();

    public DexReferenceCache getDexReferenceCache();
}