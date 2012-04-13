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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

public class TextDialog extends JDialog {
    private static final long serialVersionUID = -7992139455449834926L;
    TextBuilder tPanel;
    JScrollPane scrollPane;

    public TextDialog(Frame owner, String title, LineBuilder lb, JButton[] buttons)
            throws HeadlessException {
        super(owner, title);
        tPanel = new TextBuilder(lb);
        init(buttons);
    }

    public TextDialog(Frame owner, String title, String text, JButton[] buttons)
            throws HeadlessException {
        super(owner, title);
        tPanel = new TextBuilder(text);
        init(buttons);
    }

    public TextDialog(Frame owner, String title, LineBuilder lb)
            throws HeadlessException {
        super(owner, title);
        tPanel = new TextBuilder(lb);
        init(null);
    }

    public TextDialog(Frame owner, String title, String text)
            throws HeadlessException {
        super(owner, title);
        tPanel = new TextBuilder(text);
        init(null);
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    private void init(JButton[] buttons) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        scrollPane = new JScrollPane(tPanel.getTextPane());
        tPanel.setScrollPane(scrollPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                buttonPanel.add(buttons[i]);
            }
        }
        buttonPanel.add(close);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void registerPopup(Object o, JPopupMenu popup) {
        TextBuilder.registerPopup(o, popup);
    }

    public static Object getPopupReference() {
        return TextBuilder.getPopupReference();
    }

    public static JPopupMenu getPopup(Object o) {
        return TextBuilder.getPopup(o);
    }

    public int getCurrentLine() {
        return tPanel.getCurrentLine();
    }

    public void updateDocument() {
        tPanel.updateDocument();
    }

    public LineBuilder getLineBuilder() {
        return tPanel.getLineBuilder();
    }

    public void setEditable(boolean editable) {
        tPanel.setEditable(editable);
    }

    public void setText(String text) {
        tPanel.setText(text);
    }

    public int getCaretPosition() {
        return tPanel.getCaretPosition();
    }

    public void setCaretPosition(int pos) {
        tPanel.setCaretPosition(pos);
    }

    public void highlightCurrentRow(JTextComponent textComp) {
        tPanel.highlightCurrentRow(textComp);
    }

    public void removeHighlights(JTextComponent textComp) {
        tPanel.removeHighlights(textComp);
    }

    public void findNext(String search) {
        tPanel.findNext(search);
    }

    public void findPrev(String search) {
        tPanel.findPrev(search);
    }

    public Object getOwnerData() {
        return tPanel.getOwnerData();
    }

    public void setOwnerData(Object ownerData) {
        tPanel.setOwnerData(ownerData);
    }
}