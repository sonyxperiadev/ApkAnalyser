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

import gui.AbstractMainFrame;
import gui.Canceable;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mereflect.MEClass;
import mereflect.MEField;

public class FieldDialog extends JDialog {
    private static final long serialVersionUID = 3596837179732660909L;
    public static final int INSTANCE = 0;
    public static final int ELEMENTS = 1;
    public static final int ALL_ELEMENTS = 2;

    protected static Map<MEClass, Integer> selectedIndexPerClass = new HashMap<MEClass, Integer>();
    AbstractMainFrame mainFrame;
    JList list;
    JTextField prefixTextField;
    boolean acked = false;
    Object ACK_LOCK = new Object();
    boolean awaitingAckRequest = true;
    String locationPrefix;
    ButtonGroup rbGroup;
    JRadioButton oInstance;
    JRadioButton oElements;
    JRadioButton oAllElements;
    JTextField indexTextField;

    MEClass clazz;
    MEField chosenField = null;
    String output = null;
    int arraySpec = ALL_ELEMENTS;
    int[] specArrayElements;
    Canceable canceable;

    /**
     * @param owner
     * @param title
     * @throws java.awt.HeadlessException
     */
    public FieldDialog(AbstractMainFrame owner, MEClass clazz, String title, String prefix, Vector<MEField> fields,
            Canceable canceable) throws HeadlessException {
        super(owner, title);
        mainFrame = owner;
        this.clazz = clazz;
        this.canceable = canceable;
        initGui(prefix, fields);
    }

    protected void initGui(String prefix, Vector<MEField> fields) {
        locationPrefix = prefix;
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        mainPanel.setLayout(new BorderLayout());

        // Prefix textfield
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setLayout(new BorderLayout());
        prefixTextField = new JTextField(locationPrefix);
        textFieldPanel.add(prefixTextField, BorderLayout.CENTER);
        textFieldPanel.setBorder(BorderFactory.createTitledBorder("Field output prefix"));

        // Array radiobuttons
        JPanel radioPanel = new JPanel(new GridLayout(0, 2));
        rbGroup = new ButtonGroup();
        oInstance = new JRadioButton("Array instance");
        oElements = new JRadioButton("Specified elements (comma separated)");
        oAllElements = new JRadioButton("All elements");
        indexTextField = new JTextField("0");
        rbGroup.add(oInstance);
        rbGroup.add(oElements);
        rbGroup.add(oAllElements);
        oAllElements.setEnabled(false);
        radioPanel.add(oElements);
        radioPanel.add(indexTextField);
        radioPanel.add(oAllElements);
        radioPanel.add(new JLabel());
        radioPanel.add(oInstance);
        radioPanel.add(new JLabel());
        radioPanel.setBorder(BorderFactory.createTitledBorder("Array field output"));
        ActionListener ugly = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultButtonModel model = (DefaultButtonModel) oElements.getModel();
                boolean enabled = model.getGroup().isSelected(model);
                indexTextField.setEnabled(enabled);
            }
        };
        Enumeration<AbstractButton> rbe = rbGroup.getElements();
        while (rbe.hasMoreElements()) {
            ((JRadioButton) rbe.nextElement()).addActionListener(ugly);
        }
        oElements.setSelected(true);
        indexTextField.setEnabled(true);

        // Field list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Fields"));
        list = new JList(fields);
        listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        int sel = 0;
        if (selectedIndexPerClass.get(clazz) != null) {
            sel = (selectedIndexPerClass.get(clazz)).intValue();
        }
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent l) {
                String text = prefixTextField.getText();
                MEField sField = (MEField) list.getSelectedValue();
                boolean enableArrays = sField != null && sField.getType().isArray();
                oInstance.setEnabled(enableArrays);
                oElements.setEnabled(enableArrays);
                //oAllElements.setEnabled(enableArrays);  // TODO enable choice when implemented
                indexTextField.setEditable(enableArrays);
                if (sField != null && (text == null || text.trim().length() == 0 || text.startsWith(locationPrefix))) {
                    prefixTextField.setText(locationPrefix + " " + sField.getType().toString() + " " + sField.getName() + "=");
                }
            }
        });
        list.setSelectedIndex(sel);

        mainPanel.add(textFieldPanel, BorderLayout.NORTH);
        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(radioPanel, BorderLayout.SOUTH);

        // Buttons
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apply();
                close();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        awaitingAckRequest = true;
        chosenField = null;
        output = null;
    }

    protected void apply() {
        selectedIndexPerClass.put(clazz, new Integer(list.getSelectedIndex()));
        output = prefixTextField.getText();
        chosenField = (MEField) list.getSelectedValue();
        DefaultButtonModel modelAllElements = (DefaultButtonModel) oAllElements.getModel();
        DefaultButtonModel modelElements = (DefaultButtonModel) oElements.getModel();
        DefaultButtonModel modelInstance = (DefaultButtonModel) oInstance.getModel();
        if (modelAllElements.getGroup().isSelected(modelAllElements)) {
            arraySpec = ALL_ELEMENTS;
        } else if (modelElements.getGroup().isSelected(modelElements)) {
            arraySpec = ELEMENTS;
        } else if (modelInstance.getGroup().isSelected(modelInstance)) {
            arraySpec = INSTANCE;
        }
        if (arraySpec == ELEMENTS) {
            String[] indicesStr = Settings.breakString(indexTextField.getText(), ",");
            List<Integer> indicesList = new ArrayList<Integer>();
            for (int i = 0; i < indicesStr.length; i++) {
                try {
                    indicesList.add(Integer.parseInt(indicesStr[i].trim()));
                } catch (Throwable ignore) {
                }
            }
            specArrayElements = new int[indicesList.size()];
            for (int i = 0; i < specArrayElements.length; i++) {
                specArrayElements[i] = (indicesList.get(i)).intValue();
            }
        }
    }

    protected void close() {
        synchronized (ACK_LOCK) {
            ACK_LOCK.notify();
            awaitingAckRequest = false;
        }
        setVisible(false);
        dispose();
    }

    /**
     * @return
     */
    public MEField getChosenField() {
        return chosenField;
    }

    /**
     * @return
     */
    public String getOutput() {
        return output;
    }

    public int getArraySpecification() {
        return arraySpec;
    }

    public int[] getArrayIndicesSpecification() {
        return specArrayElements;
    }

    public void awaitAcknowledge() {
        synchronized (ACK_LOCK) {
            while (awaitingAckRequest && canceable.isRunning()) {
                try {
                    ACK_LOCK.wait(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }
}
