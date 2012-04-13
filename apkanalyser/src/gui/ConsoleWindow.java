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

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import util.ProcessHandler;
import util.ProcessListener;
import andreflect.adb.ConsoleWindowListener;

public class ConsoleWindow implements ProcessListener {
    static ConsoleWindow inst = null;
    ProcessHandler process;

    JTextArea text;

    JFrame console;

    ConsoleWindowListener listener;

    protected int curSearchPos = 0;

    protected String curSearchString = null;

    protected JButton EButton = null;
    protected JButton IButton = null;
    protected JButton WButton = null;
    protected JButton FButton = null;
    protected JButton SButton = null;
    protected JButton DButton = null;
    protected JButton VButton = null;

    private ConsoleWindow() {
    }

    public synchronized static ConsoleWindow getInstance() {
        if (inst == null) {
            inst = new ConsoleWindow();
        }
        return inst;
    }

    synchronized void init(String title) {
        if (console == null) {
            console = new JFrame(title);
            text = new JTextArea();
            text.setFont(new Font("lucida console", Font.PLAIN, 10));
            text.setBackground(Color.black);
            text.setForeground(new Color(192, 255, 192));
            text.setEditable(false);
            text.setWrapStyleWord(true);

            text.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.getModifiers() & KeyEvent.CTRL_MASK) > 0
                            && e.getKeyCode() == KeyEvent.VK_F) {
                        String s = (String) JOptionPane.showInputDialog(text.getParent(),
                                "", "Search", JOptionPane.QUESTION_MESSAGE, null, null,
                                (curSearchString == null ? "" : curSearchString));
                        if (s != null) {
                            curSearchString = s;
                            curSearchPos = 0;
                            findNext(curSearchString);
                            text.repaint();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F3) {
                        if (curSearchString != null) {
                            if ((e.getModifiers() & KeyEvent.SHIFT_MASK) > 0) {
                                findPrev(curSearchString);
                            } else {
                                findNext(curSearchString);
                            }
                            text.repaint();
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

            console.getContentPane().setLayout(new BorderLayout());
            console.getContentPane().add(new JScrollPane(text), BorderLayout.CENTER);
            console.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent w) {
                    synchronized (ConsoleWindow.this) {
                        console = null;
                        process.kill();
                    }
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            JButton exitButton = new JButton("Close");
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });
            JButton clsButton = new JButton("Clear");
            clsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    text.setText("");
                    if (listener != null) {
                        listener.onClear(process);
                    }
                }
            });
            JButton restartButton = new JButton("Relaunch");
            restartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });

            IButton = new JButton("I");
            IButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text.setText("");
                                listener.onI(process);
                                EButton.setEnabled(true);
                                DButton.setEnabled(true);
                                VButton.setEnabled(true);
                                SButton.setEnabled(true);
                                FButton.setEnabled(true);
                                WButton.setEnabled(true);
                                IButton.setEnabled(false);
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });
            VButton = new JButton("V");
            VButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text.setText("");
                                listener.onV(process);
                                EButton.setEnabled(true);
                                DButton.setEnabled(true);
                                VButton.setEnabled(false);
                                SButton.setEnabled(true);
                                FButton.setEnabled(true);
                                WButton.setEnabled(true);
                                IButton.setEnabled(true);
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });

            DButton = new JButton("D");
            DButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text.setText("");
                                listener.onD(process);
                                EButton.setEnabled(true);
                                DButton.setEnabled(false);
                                VButton.setEnabled(true);
                                SButton.setEnabled(true);
                                FButton.setEnabled(true);
                                WButton.setEnabled(true);
                                IButton.setEnabled(true);
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });

            SButton = new JButton("S");
            SButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text.setText("");
                                listener.onS(process);
                                EButton.setEnabled(true);
                                DButton.setEnabled(true);
                                VButton.setEnabled(true);
                                SButton.setEnabled(false);
                                FButton.setEnabled(true);
                                WButton.setEnabled(true);
                                IButton.setEnabled(true);
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });

            WButton = new JButton("W");
            WButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text.setText("");
                                listener.onW(process);
                                EButton.setEnabled(true);
                                DButton.setEnabled(true);
                                VButton.setEnabled(true);
                                SButton.setEnabled(true);
                                FButton.setEnabled(true);
                                WButton.setEnabled(false);
                                IButton.setEnabled(true);
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });
            FButton = new JButton("F");
            FButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text.setText("");
                                listener.onF(process);
                                EButton.setEnabled(true);
                                DButton.setEnabled(true);
                                VButton.setEnabled(true);
                                SButton.setEnabled(true);
                                FButton.setEnabled(false);
                                WButton.setEnabled(true);
                                IButton.setEnabled(true);
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });

            EButton = new JButton("E");
            EButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                text.setText("");
                                listener.onE(process);
                                EButton.setEnabled(false);
                                DButton.setEnabled(true);
                                VButton.setEnabled(true);
                                SButton.setEnabled(true);
                                FButton.setEnabled(true);
                                WButton.setEnabled(true);
                                IButton.setEnabled(true);
                                process.restart();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });

            buttonPanel.add(VButton);
            buttonPanel.add(DButton);
            buttonPanel.add(IButton);
            buttonPanel.add(WButton);
            buttonPanel.add(EButton);
            buttonPanel.add(FButton);
            buttonPanel.add(SButton);
            buttonPanel.add(clsButton);
            buttonPanel.add(restartButton);
            buttonPanel.add(exitButton);

            console.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int inset = 200;
            int x = inset;
            int y = inset;
            int width = Math.min(800, screenSize.width - inset * 2);
            int height = Math.min(600, screenSize.height - inset * 2);
            console.setBounds(x, y, width, height);
            console.setVisible(true);
        }
    }

    public synchronized void start(String title, ProcessHandler p, ConsoleWindowListener listener)
            throws IOException {
        init(title);
        this.listener = listener;
        EButton.setEnabled(true);
        DButton.setEnabled(false);
        VButton.setEnabled(true);
        SButton.setEnabled(true);
        FButton.setEnabled(true);
        WButton.setEnabled(true);
        IButton.setEnabled(true);
        text.setText("");
        if (process != null) {
            process.kill();
        }
        process = p;
        process.addProcessListener(this);
        p.start();
    }

    public synchronized void start(String title, ProcessHandler p)
            throws IOException {
        init(title);
        listener = null;
        EButton.setEnabled(false);
        DButton.setEnabled(false);
        VButton.setEnabled(false);
        SButton.setEnabled(false);
        FButton.setEnabled(false);
        WButton.setEnabled(false);
        IButton.setEnabled(false);
        text.setText("");
        if (process != null) {
            process.kill();
        }
        process = p;
        process.addProcessListener(this);
        p.start();
    }

    public synchronized void close() {
        console.dispose();
        console = null;
        process.kill();
    }

    public void findNext(String search) {
        String t = text.getText();
        curSearchPos = t.indexOf(search, curSearchPos + 1);
        int selPos = curSearchPos;
        if (curSearchPos >= 0) {
            text.setCaretPosition(selPos);
            text.setSelectionStart(selPos);
            text.setSelectionEnd(selPos + search.length());
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            text.setSelectionStart(0);
            text.setSelectionEnd(0);
            curSearchPos = 0;
        }
    }

    public void findPrev(String search) {
        String t = text.getText();
        int br = curSearchPos - 1;
        if (br < 0) {
            br = 0;
        }
        curSearchPos = t.substring(0, br).lastIndexOf(search);
        int selPos = curSearchPos;
        if (curSearchPos >= 0) {
            text.setCaretPosition(selPos);
            text.setSelectionStart(selPos);
            text.setSelectionEnd(selPos + search.length());
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            text.setSelectionStart(t.length() - 1);
            text.setSelectionEnd(t.length() - 1);
            curSearchPos = t.length() - 1;
        }
    }

    // ProcessListener impl
    @Override
    public void started() {
    }

    @Override
    public void died(int ret) {
        //text.append("\n\n[PROCESS DIED, RETURN CODE " + ret + "]\n\n");
    }

    @Override
    public void stderr(char c) {
    }

    @Override
    public void stdout(char c) {
        text.append(Character.toString(c));
        if (c == '\n') {
            text.setCaretPosition(text.getText().length());
        }
    }
}
