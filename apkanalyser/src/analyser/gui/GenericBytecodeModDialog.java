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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;

import jerl.bcm.config.Config;
import jerl.bcm.inj.Injection;
import jerl.bcm.inj.InjectionClass;
import jerl.bcm.inj.InjectionMethod;
import mereflect.MEClass;
import mereflect.MEMethod;
import mereflect.primitives.AbstractPrimitive;

public class GenericBytecodeModDialog extends JDialog {
    private static final long serialVersionUID = -8890042430518040330L;
    AbstractMainFrame mainFrame;
    JComboBox combo;
    JLabel classType = new JLabel();
    JPanel mainPanel;
    JPanel ctorPanel;
    MEClass clazz;
    MEMethod method;
    Class<?> injectionClass;
    List<JTextField> textFields = new ArrayList<JTextField>();
    Object[] argValues;
    Object ACK_LOCK = new Object();
    boolean awaitingAckRequest = true;
    Canceable canceable;

    public GenericBytecodeModDialog(AbstractMainFrame owner, String title,
            MEClass clazz, MEMethod method, Canceable canceable)
            throws HeadlessException {
        super(owner, title);
        mainFrame = owner;
        this.clazz = clazz;
        this.method = method;
        this.canceable = canceable;
        initGui();
    }

    Vector<String> predefinedClassStrings = new Vector<String>();

    protected void initGui() {
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        mainPanel.setLayout(new BorderLayout());

        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BorderLayout());
        comboPanel.add(new JLabel("Class"), BorderLayout.NORTH);
        Class<?>[] predefinedClasses = Config.getAvailableInjections();
        for (int i = 0; i < predefinedClasses.length; i++) {
            predefinedClassStrings.addElement(predefinedClasses[i].getName());
        }
        String[] userdefinedClasses = Settings.getUserDefinedInjections();
        for (int i = 0; i < userdefinedClasses.length; i++) {
            predefinedClassStrings.addElement(userdefinedClasses[i]);
        }
        combo = new JComboBox(predefinedClassStrings);
        combo.setEditable(true);
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {
                    String item = (String) ((JComboBox) e.getSource()).getSelectedItem();
                    Class<?> newClass = null;
                    try {
                        newClass = Class.forName(item);
                        if (!(Injection.class.isAssignableFrom(newClass))) {
                            mainFrame.showError(GenericBytecodeModDialog.this, "Invalid class", "Class " + item
                                    + " is not a bytecode injection class");
                            combo.setSelectedItem(injectionClass.getName());
                            return;
                        }
                        if (newClass.getConstructors().length != 1) {
                            mainFrame.showError(GenericBytecodeModDialog.this, "Invalid class", "Class " + item
                                    + " does not define one constructor only");
                            combo.setSelectedItem(injectionClass.getName());
                            return;
                        }
                    } catch (ClassNotFoundException e1) {
                        mainFrame.showError(GenericBytecodeModDialog.this, "Invalid class", "Class " + item
                                + " not found");
                        combo.setSelectedItem(injectionClass.getName());
                        return;
                    }
                    if (!predefinedClassStrings.contains(item)) {
                        Settings.addUserDefinedInjection(item);
                    }
                    if (newClass != null) {
                        initNewClass(newClass);
                    }
                }
            }
        });
        comboPanel.add(combo, BorderLayout.CENTER);
        comboPanel.add(classType, BorderLayout.SOUTH);
        mainPanel.add(comboPanel, BorderLayout.NORTH);
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (apply()) {
                    close();
                }
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

        initNewClass(predefinedClasses[0]);
    }

    protected void initNewClass(final Class<?> c) {
        injectionClass = c;
        textFields.clear();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (InjectionMethod.class.isAssignableFrom(c)) {
                    classType.setText("Method injection");
                } else if (InjectionClass.class.isAssignableFrom(c)) {
                    classType.setText("Class injection");
                }
                if (ctorPanel != null) {
                    mainPanel.remove(ctorPanel);
                }
                ctorPanel = new JPanel();
                ctorPanel.setLayout(new BorderLayout());
                JPanel contPanel = new JPanel();
                contPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 16, 0));
                Constructor<?>[] ctors = c.getConstructors();
                Class<?>[] args = ctors[0].getParameterTypes();
                contPanel.setLayout(new GridLayout(args.length, 2, 4, 4));
                for (int i = 0; i < args.length; i++) {
                    JTextField tField = new JTextField();
                    contPanel.add(new JLabel(args[i].getName()));
                    switch (i) {
                    case 0:
                        if (method == null) {
                            if (clazz != null) {
                                tField.setText("All methods of " + clazz.getName());
                            } else {
                                tField.setText("All methods");
                            }
                        } else {
                            tField.setText((AbstractTreeBytecodeModAction.getMethodSignature(method)));
                        }
                        tField.setEditable(false);
                        break;
                    default:
                        textFields.add(tField);
                        break;
                    }
                    contPanel.add(tField);
                }
                ctorPanel.add(contPanel, BorderLayout.NORTH);
                mainPanel.add(ctorPanel, BorderLayout.CENTER);
                SwingUtilities.updateComponentTreeUI(GenericBytecodeModDialog.this);
            }
        });
    }

    protected boolean apply() {
        Constructor<?>[] ctors = injectionClass.getConstructors();
        Class<?>[] args = ctors[0].getParameterTypes();
        Class<?>[] params = { String.class };
        argValues = new Object[args.length];
        for (int i = 1; i < args.length; i++) {
            String argString = (textFields.get(i - 1)).getText();
            Object arg = null;
            try {
                Class<?> argType = args[i];
                if (argType.isPrimitive()) {
                    argType = AbstractPrimitive.convertJavaPrimitiveToEnclosingClass(argType);
                }
                Constructor<?> argCtor = argType.getConstructor(params);
                String[] argCtorArg = { argString };
                arg = argCtor.newInstance((Object[]) argCtorArg);
            } catch (Exception e) {
                mainFrame.showError(this, "Instantiation error",
                        "Could not create argument for " + injectionClass.getName() + ", argument #" + (i + 1) +
                                " when calling constructor " + args[i].getName() + "(\"" +
                                argString + "\")");
                return false;
            }
            argValues[i] = arg;
        }
        return true;
    }

    protected void close() {
        synchronized (ACK_LOCK) {
            ACK_LOCK.notify();
            awaitingAckRequest = false;
        }
        setVisible(false);
        dispose();
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

    /**
     * @return
     */
    public Class<?> getInjectionClass() {
        return injectionClass;
    }

    public Object[] getArguments() {
        return argValues;
    }
}
