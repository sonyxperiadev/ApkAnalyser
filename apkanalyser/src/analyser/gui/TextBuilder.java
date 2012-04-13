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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

public class TextBuilder {
    protected JTextPane pane = new JTextPane();
    protected JScrollPane scrollPane;
    protected int curSearchPos = 0;
    protected static String curSearchString = null;
    protected int curLine = 0;
    protected LineBuilder lineBuilder = null;
    protected JPopupMenu popup = null; // TODO not thread safe
    protected Highlighter.HighlightPainter lineHighlightPainter =
            new LineHighlightPainter(new Color(0, 255, 255, 48));
    Object ownerData;
    // ugly bugly
    protected static Map<Object, JPopupMenu> popups = new HashMap<Object, JPopupMenu>();
    protected static Object popupReference = null;

    public JTextPane getTextPane() {
        return pane;
    }

    public TextBuilder(LineBuilder lb) {
        super();
        initListener();
        init(lb);
    }

    public TextBuilder(String text) {
        super();
        initListener();
        init(text);
    }

    public void init(LineBuilder lb) {
        curSearchPos = 0;
        lineBuilder = lb;
        updateDocument();
        setCaretPosition(0);
    }

    public void init(String text) {
        curSearchPos = 0;
        pane.setText(text);
        setCaretPosition(0);
    }

    private void initListener() {
        pane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent ce) {
                int dot = ce.getDot();
                curLine = pane.getDocument().getDefaultRootElement().getElementIndex(dot);
                highlightCurrentRow(pane);
            }
        });
        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (popup != null) { // TODO not thread safe
                    popup.setVisible(false);
                    popup = null;
                }
                int offset = pane.viewToModel(SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), pane));
                setCaretPosition(offset);
                highlightCurrentRow(pane);
                if (lineBuilder != null) {
                    int line = pane.getDocument().getDefaultRootElement().getElementIndex(offset);
                    Object ref = null;
                    ref = lineBuilder.getReference(line);
                    Selection.setSelectedObject(TextBuilder.this, ref);
                    if (SwingUtilities.isRightMouseButton(me)) {
                        popupReference = ref;
                        if (ref == null) {
                            ref = void.class;
                        } else {
                            ref = ref.getClass();
                        }
                        JPopupMenu pu = getPopup(ref);
                        if (pu == null) {
                            pu = getPopup(void.class);
                        }
                        if (pu != null) {
                            pu.show(me.getComponent(), me.getX(), me.getY());
                            popup = pu;
                        }
                    } else if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount() == 1) {
                        if (ref != null && ref instanceof LineBuilderFormatter.QuickLink) {
                            LineBuilderFormatter.Link link = (LineBuilderFormatter.Link) ref;
                            link.getLinkedAction().actionPerformed(new ActionEvent(link, 0, null));
                        }
                    } else if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount() == 2) {
                        if (ref != null && ref instanceof LineBuilderFormatter.Link) {
                            LineBuilderFormatter.Link link = (LineBuilderFormatter.Link) ref;
                            link.getLinkedAction().actionPerformed(new ActionEvent(link, 0, null));
                        }
                    }
                }
            }
        });

        // Add keylistener for Search dialog
        pane.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getModifiers() & KeyEvent.CTRL_MASK) > 0
                        && e.getKeyCode() == KeyEvent.VK_F) {
                    String s = (String) JOptionPane.showInputDialog(
                            pane.getParent(), "", "Search",
                            JOptionPane.QUESTION_MESSAGE, null, null, (curSearchString == null ? "" : curSearchString));
                    if (s != null) {
                        curSearchString = s;
                        curSearchPos = 0;
                        findNext(curSearchString);
                        pane.repaint();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_F3) {
                    if (curSearchString != null) {
                        if ((e.getModifiers() & KeyEvent.SHIFT_MASK) > 0) {
                            findPrev(curSearchString);
                        } else {
                            findNext(curSearchString);
                        }
                        pane.repaint();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        pane.setEditable(false);
    }

    public static void registerPopup(Object o, JPopupMenu popup) {
        popups.put(o, popup);
    }

    /**
     * @return
     */
    public static Object getPopupReference() {
        return popupReference;
    }

    public static JPopupMenu getPopup(Object o) {
        return popups.get(o);
    }

    public int getCurrentLine() {
        return curLine;
    }

    public void updateDocument() {
        DefaultStyledDocument newDoc = new DefaultStyledDocument();
        lineBuilder.toDocument(newDoc);
        pane.setDocument(newDoc);
    }

    /**
     * @return
     */
    public LineBuilder getLineBuilder() {
        return lineBuilder;
    }

    public void setEditable(boolean editable) {
        pane.setEditable(editable);
    }

    public void setText(String text) {
        pane.setText(text);
    }

    public int getCaretPosition() {
        return pane.getCaretPosition();
    }

    public void setCaretPosition(int pos) {
        pane.setCaretPosition(pos);
        curLine = pane.getDocument().getDefaultRootElement().getElementIndex(
                pos);
        highlightCurrentRow(pane);
    }

    public void highlightCurrentRow(JTextComponent textComp) {
        // First remove all old highlights
        removeHighlights(textComp);

        try {
            Highlighter hilite = textComp.getHighlighter();

            // Find start index to highlight
            Element elem = Utilities.getParagraphElement(pane, pane.getCaretPosition());
            int start = elem.getStartOffset();
            int end = elem.getEndOffset();
            hilite.addHighlight(start, end, lineHighlightPainter);
        } catch (Throwable ignore) {
        }
    }

    // Removes only our private highlights
    public void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter() instanceof LineHighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    // Search functions

    public void findNext(String search) {
        String text = pane.getText();
        curSearchPos = text.indexOf(search, curSearchPos + 1);
        int selPos = curSearchPos;
        if (curSearchPos >= 0) {
            for (int i = 0; i < curSearchPos; i++) {
                if (text.charAt(i) == '\n') {
                    selPos--;
                }
            }
            setCaretPosition(selPos);
            pane.setSelectionStart(selPos);
            pane.setSelectionEnd(selPos + search.length());
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            pane.setSelectionStart(0);
            pane.setSelectionEnd(0);
            curSearchPos = 0;
            removeHighlights(pane);
        }
    }

    public void findPrev(String search) {
        String text = pane.getText();
        int br = curSearchPos - 1;
        if (br < 0) {
            br = 0;
        }
        curSearchPos = text.substring(0, br).lastIndexOf(search);
        int selPos = curSearchPos;
        if (curSearchPos >= 0) {
            for (int i = 0; i < curSearchPos; i++) {
                if (text.charAt(i) == '\n') {
                    selPos--;
                }
            }
            setCaretPosition(selPos);
            pane.setSelectionStart(selPos);
            pane.setSelectionEnd(selPos + search.length());
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            pane.setSelectionStart(text.length() - 1);
            pane.setSelectionEnd(text.length() - 1);
            curSearchPos = text.length() - 1;
            removeHighlights(pane);
        }
    }

    class LineHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public LineHighlightPainter(Color color) {
            super(color);
        }
    }

    /**
     * @return
     */
    public Object getOwnerData() {
        return ownerData;
    }

    /**
     * @param ownerData
     */
    public void setOwnerData(Object ownerData) {
        this.ownerData = ownerData;
    }

    /**
     * @param scrollPane
     */
    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    /**
     * @return
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}
