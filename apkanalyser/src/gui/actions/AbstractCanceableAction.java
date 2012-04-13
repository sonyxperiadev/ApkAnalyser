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

package gui.actions;

import gui.AbstractMainFrame;
import gui.Canceable;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

public abstract class AbstractCanceableAction extends AbstractAction implements
        Canceable
{
    private static final long serialVersionUID = 2470138122897194302L;

    protected volatile boolean m_running = false;

    protected volatile Thread m_actionRunner;
    protected AbstractMainFrame m_mainFrame;

    protected AbstractCanceableAction(String actionName, Icon actionIcon)
    {
        super(actionName, actionIcon);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        m_actionRunner = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                m_running = true;
                m_mainFrame.actionStarted(AbstractCanceableAction.this);
                try
                {
                    AbstractCanceableAction.this.run(e);
                }
                catch (Throwable t)
                {
                    handleThrowable(t);
                }
                finally
                {
                    m_running = false;
                    m_mainFrame.actionFinished(AbstractCanceableAction.this);
                    m_actionRunner = null;
                }
            }
        });
        m_actionRunner.start();
    }

    public void setMainFrame(AbstractMainFrame mainFrame)
    {
        m_mainFrame = mainFrame;
    }

    public AbstractMainFrame getMainFrame()
    {
        return m_mainFrame;
    }

    public void cancel()
    {
        m_running = false;
        if (m_actionRunner != null)
        {
            //m_actionRunner.interrupt();
        }
    }

    @Override
    public boolean isRunning()
    {
        return m_running;
    }

    public void setRunning(boolean b)
    {
        m_running = b;
    }

    public abstract void handleThrowable(Throwable t);

    public abstract void run(ActionEvent e) throws Throwable;

    public abstract String getWorkDescription();
}