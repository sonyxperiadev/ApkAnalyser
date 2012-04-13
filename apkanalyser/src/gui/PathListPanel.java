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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class PathListPanel extends JPanel {
    private static final long serialVersionUID = -5965025765910494761L;
    protected JList list;
    protected AbstractMainFrame mainFrame;
    protected JButton addButton;
    protected JButton editButton;
    protected JButton removeButton;
    protected JButton upButton;
    protected JButton downButton;
    protected DefaultListModel listModel;
    protected boolean ordered = false;

    public PathListPanel(AbstractMainFrame mainFrame) {
        super();
        initGui(mainFrame, false);
    }

    /**
     * @param isDoubleBuffered
     */
    public PathListPanel(AbstractMainFrame mainFrame, boolean orderSignificant) {
        super();
        initGui(mainFrame, orderSignificant);
    }

    /**
     * @param layout
     */
    public PathListPanel(AbstractMainFrame mainFrame, LayoutManager layout) {
        super(layout);
        initGui(mainFrame, false);
    }

    /**
     * @param layout
     * @param isDoubleBuffered
     */
    public PathListPanel(AbstractMainFrame mainFrame, LayoutManager layout, boolean orderSignificant) {
        super(layout);
        initGui(mainFrame, orderSignificant);
    }

    JPanel selectablePanel = new JPanel();
    JCheckBox checkBox = new JCheckBox();

    public void initGui(AbstractMainFrame mainFrame, boolean orderSignificant) {
        this.mainFrame = mainFrame;
        ordered = orderSignificant;
        listModel = new DefaultListModel();

        list = new JList();
        list.setModel(listModel);
        addButton = new JButton(getAddButtonText());
        editButton = new JButton(getEditButtonText());
        removeButton = new JButton(getRemoveButtonText());
        if (ordered) {
            upButton = new JButton(getUpButtonText());
            downButton = new JButton(getDownButtonText());
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] s = list.getSelectedIndices();
                boolean oneSelected = s != null && s.length == 1;
                boolean oneOrMoreSelected = s != null && s.length > 0;

                editButton.setEnabled(oneSelected);
                removeButton.setEnabled(oneOrMoreSelected);

                if (ordered) {
                    upButton.setEnabled(oneOrMoreSelected);
                    downButton.setEnabled(oneOrMoreSelected);
                }
            }
        });

        checkBox.setBackground(list.getBackground());
        selectablePanel.setLayout(new BorderLayout());
        selectablePanel.add(checkBox, BorderLayout.WEST);

        list.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = -1659697029079591990L;

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                if (value instanceof Selectable) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    checkBox.setSelected(((Selectable) value).isSelected());
                    selectablePanel.add(c, BorderLayout.CENTER);
                    return selectablePanel;
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        editEntry();
                    } else {
                        if (e.getX() < checkBox.getWidth()) {
                            toggleSelected();
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel innerButtonPanel = new JPanel();
        innerButtonPanel.setLayout(new GridLayout(ordered ? 6 : 3, 1));
        innerButtonPanel.add(addButton);
        innerButtonPanel.add(editButton);
        innerButtonPanel.add(removeButton);
        if (ordered) {
            innerButtonPanel.add(new JLabel(""));
            innerButtonPanel.add(upButton);
            innerButtonPanel.add(downButton);
        }
        buttonPanel.add(innerButtonPanel);
        editButton.setEnabled(false);
        removeButton.setEnabled(false);

        setLayout(new BorderLayout());
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);

        setAddButtonListener(addButton);
        editButton.addActionListener(getEditActionListener());
        setRemoveButtonListener(removeButton);
        if (ordered) {
            setUpButtonListener(upButton);
            setDownButtonListener(downButton);
        }
    }

    public void setPaths(File[] paths) {
        listModel = new DefaultListModel();
        for (int i = 0; i < paths.length; i++) {
            listModel.addElement(paths[i]);
        }
        list.setModel(listModel);
    }

    public Object[] getPaths() {
        return listModel.toArray();
    }

    protected String getAddButtonText() {
        return "Add...";
    }

    protected String getEditButtonText() {
        return "Edit";
    }

    protected String getRemoveButtonText() {
        return "Remove";
    }

    protected String getUpButtonText() {
        return "Move up";
    }

    protected String getDownButtonText() {
        return "Move down";
    }

    protected String getFileListTitleText() {
        return "Add path";
    }

    protected String getFileListButtonText() {
        return "Add";
    }

    protected String getFilterSuffix() {
        return "";
    }

    protected String getFilterDescription() {
        return "";
    }

    protected boolean isFilesOnly() {
        return false;
    }

    protected boolean isDirectoriesOnly() {
        return false;
    }

    protected void addFileToList(File f) {
        SelectableFile sf = new SelectableFile(f.getAbsolutePath());
        sf.setSelected(true);
        listModel.addElement(sf);
    }

    protected void setAddButtonListener(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File[] f = mainFrame.selectFiles(getFileListTitleText(), getFileListButtonText(), getFilterSuffix(),
                        getFilterDescription(), isFilesOnly(), isDirectoriesOnly());
                if (f != null && f.length > 0) {
                    for (int i = 0; i < f.length; i++) {
                        addFileToList(f[i]);
                    }
                }
            }
        });
    }

    protected ActionListener getEditActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editEntry();
            }
        };
    }

    protected void editEntry() {
        int selInd = list.getSelectedIndex();
        if (selInd == -1) {
            return;
        }
        File f = (File) listModel.get(selInd);
        String s = (String) JOptionPane.showInputDialog(mainFrame, "", "Edit path",
                JOptionPane.NO_OPTION, null, null, f.getAbsoluteFile());
        if (s != null) {
            f = new SelectableFile(s);
            listModel.set(selInd, f);
        }
    }

    protected void setRemoveButtonListener(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedIndices = null;
                do {
                    selectedIndices = list.getSelectedIndices();
                    if (selectedIndices != null && selectedIndices.length > 0) {
                        listModel.remove(selectedIndices[0]);
                    }
                } while (selectedIndices != null && selectedIndices.length > 0);
            }
        });
    }

    protected void setUpButtonListener(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedIndices = list.getSelectedIndices();
                if (selectedIndices[0] == 0) {
                    return;
                }
                for (int i = 0; i < selectedIndices.length; i++) {
                    int index = selectedIndices[i];
                    if (index > 0) {
                        Object over = listModel.elementAt(index - 1);
                        Object at = listModel.elementAt(index);
                        listModel.set(index - 1, at);
                        listModel.set(index, over);
                    }
                    selectedIndices[i] = index - 1;
                }
                list.setSelectedIndices(selectedIndices);
            }
        });
    }

    protected void toggleSelected() {
        int[] selectedIndices = list.getSelectedIndices();
        if (selectedIndices.length != 1) {
            return;
        }
        Object at = listModel.elementAt(selectedIndices[0]);
        if (at instanceof SelectableFile) {
            ((SelectableFile) at).setSelected(!((SelectableFile) at).isSelected());
            repaint();
        }
    }

    protected void setDownButtonListener(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedIndices = list.getSelectedIndices();
                if (selectedIndices[selectedIndices.length - 1] == listModel.size() - 1) {
                    return;
                }
                for (int i = selectedIndices.length - 1; i >= 0; i--) {
                    int index = selectedIndices[i];
                    if (index < listModel.size() - 1) {
                        Object at = listModel.elementAt(index);
                        Object under = listModel.elementAt(index + 1);
                        listModel.set(index, under);
                        listModel.set(index + 1, at);
                    }
                    selectedIndices[i] = index + 1;
                }
                list.setSelectedIndices(selectedIndices);
            }
        });
    }
}
