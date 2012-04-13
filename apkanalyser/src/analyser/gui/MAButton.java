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

package analyser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class MAButton extends JButton {
    private static final long serialVersionUID = 4835076246277895053L;
    Action m_action;
    List<Object> m_relevantTypes = new ArrayList<Object>();

    public MAButton(Action a, ImageIcon icon, Object[] relevantTypes) {
        super(icon);

        m_action = a;

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_action.actionPerformed(e);
            }
        });

        if (relevantTypes != null) {
            for (int i = 0; i < relevantTypes.length; i++) {
                m_relevantTypes.add(relevantTypes[i]);
            }
        }
    }

    public MAButton(Action a, ImageIcon icon, Object[] relevantTypes,
            String tooltip) {
        this(a, icon, relevantTypes);
        setToolTipText(tooltip);
    }

    public void activate(Class<?> type) {
        if (m_relevantTypes.size() == 0) {
            setEnabled(true);
        } else {
            boolean enable = false;
            for (int i = 0; i < m_relevantTypes.size(); i++) {
                if (m_relevantTypes.get(i).equals(type)) {
                    enable = true;
                    break;
                }
            }
            setEnabled(enable);
        }
    }
}
