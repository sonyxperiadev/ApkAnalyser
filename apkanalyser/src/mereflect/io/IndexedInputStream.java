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

package mereflect.io;

import java.io.IOException;
import java.io.InputStream;

public class IndexedInputStream extends InputStream
{
    int index = 0;
    protected InputStream m_is;

    public IndexedInputStream(InputStream is)
    {
        m_is = is;
    }

    public int getIndex()
    {
        return index;
    }

    @Override
    public int read() throws IOException
    {
        index++;
        return m_is.read();
    }

    @Override
    public int available() throws IOException
    {
        return m_is.available();
    }

    @Override
    public void close() throws IOException
    {
        m_is.close();
    }

    @Override
    public void mark(int readlimit)
    {
        m_is.mark(readlimit);
    }

    @Override
    public boolean markSupported()
    {
        return m_is.markSupported();
    }

    @Override
    public void reset() throws IOException
    {
        m_is.reset();
    }

    @Override
    public long skip(long n) throws IOException
    {
        return m_is.skip(n);
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        int len = m_is.read(b);
        index += len;
        return len;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int leng = m_is.read(b, off, len);
        index += leng;
        return leng;
    }
}
