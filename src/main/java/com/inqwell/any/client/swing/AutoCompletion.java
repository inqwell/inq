/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any.client.swing;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.text.*;
import com.inqwell.any.client.AnyListModel;

/* This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit
 * http://creativecommons.org/licenses/publicdomain/
 */
public class AutoCompletion extends PlainDocument {
    JComboBox comboBox_;
    AnyListModel model_;
    JTextComponent editor_;
    // flag to indicate if setSelectedItem has been called
    // subsequent calls to remove/insertString should be ignored
    boolean selecting_=false;
    boolean hidePopupOnFocusLoss_;
    boolean hitBackspace_=false;
    boolean hitBackspaceOnSelection_;

    KeyListener editorKeyListener_;
    FocusListener editorFocusListener_;
    
    // Keep these so we can disable autocompletion later
    PropertyChangeListener propertyChangeListener_;
    ActionListener         actionListener_;
    Document               document_;

    public AutoCompletion(final JComboBox comboBox) {
        this.comboBox_ = comboBox;
        model_ = (AnyListModel)comboBox.getModel();
        comboBox.addActionListener(actionListener_ = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!selecting_) highlightCompletedText(0);
            }
        });
        comboBox.addPropertyChangeListener(propertyChangeListener_ = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("editor")) configureEditor((ComboBoxEditor) e.getNewValue());
                if (e.getPropertyName().equals("model")) model_ = (AnyListModel) e.getNewValue();
            }
        });
        editorKeyListener_ = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (comboBox.isDisplayable()) comboBox.setPopupVisible(true);
                hitBackspace_=false;
                switch (e.getKeyCode()) {
                    // determine if the pressed key is backspace (needed by the remove method)
                    case KeyEvent.VK_BACK_SPACE : hitBackspace_=true;
                    hitBackspaceOnSelection_=editor_.getSelectionStart()!=editor_.getSelectionEnd();
                    break;
                    // ignore delete key
                    case KeyEvent.VK_DELETE : e.consume();
                    comboBox.getToolkit().beep();
                    break;
                }
            }
        };
        // Bug 5100422 on Java 1.5: Editable JComboBox won't hide popup when tabbing out
        hidePopupOnFocusLoss_=System.getProperty("java.version").startsWith("1.5");
        // Highlight whole text when gaining focus
        editorFocusListener_ = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                highlightCompletedText(0);
            }
            public void focusLost(FocusEvent e) {
                // Workaround for Bug 5100422 - Hide Popup on focus loss
                if (hidePopupOnFocusLoss_ || e.isTemporary())
                  comboBox.setPopupVisible(false);
            }
        };
        configureEditor(comboBox.getEditor());
        // Handle initially selected object
        Object selected = comboBox.getSelectedItem();
        if (selected!=null) setText(selected.toString());
        highlightCompletedText(0);
    }

    public static void enable(JComboBox comboBox) {
        // has to be editable
        comboBox.setEditable(true);
        // change the editor's document
        new AutoCompletion(comboBox);
    }

    void configureEditor(ComboBoxEditor newEditor) {
        if (editor_ != null) {
            editor_.removeKeyListener(editorKeyListener_);
            editor_.removeFocusListener(editorFocusListener_);
        }

        if (newEditor != null) {
            editor_ = (JTextComponent) newEditor.getEditorComponent();
            editor_.addKeyListener(editorKeyListener_);
            editor_.addFocusListener(editorFocusListener_);
            editor_.setDocument(this);
        }
    }

    public void remove(int offs, int len) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting_) return;
        if (hitBackspace_) {
            // user hit backspace => move the selection backwards
            // old item keeps being selected
            if (offs>0) {
                if (hitBackspaceOnSelection_) offs--;
            } else {
                // User hit backspace with the cursor positioned on the start => beep
                comboBox_.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox_);
            }
            highlightCompletedText(offs);
        } else {
            super.remove(offs, len);
        }
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting_) return;
        // insert the string into the document
        super.insertString(offs, str, a);
        // lookup and select a matching item
        Object item = lookupItem(getText(0, getLength()));
        if (item != null) {
            setSelectedItem(item);
        } else {
            // keep old item selected if there is no match
            item = comboBox_.getSelectedItem();
            // imitate no insert (later on offs will be incremented by str.length(): selection won't move forward)
            offs = offs-str.length();
            // provide feedback to the user that his input has been received but can not be accepted
            comboBox_.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox_);
        }
        setText(model_.getExternalForItem(item).toString());
        // select the completed part
        highlightCompletedText(offs+str.length());
    }

    private void setText(String text) {
        try {
            // remove all text and insert the completed string
            super.remove(0, getLength());
            super.insertString(0, text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private void highlightCompletedText(int start) {
        editor_.setCaretPosition(getLength());
        editor_.moveCaretPosition(start);
    }

    private void setSelectedItem(Object item) {
        selecting_ = true;
        //System.out.println("Selecting " + item);
        model_.setSelectedItem(item);
        selecting_ = false;
    }

    private Object lookupItem(String pattern) {
        Object selectedItem = model_.getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(model_.getExternalForItem(selectedItem).toString(), pattern)) {
            return selectedItem;
        } else {
            // iterate over all items
            for (int i=0, n=model_.getRealSize(); i < n; i++) {
                Object currentItem = model_.getElementAt(i);
                // current item starts with the pattern?
                if (currentItem != null && startsWithIgnoreCase(model_.getExternalForItem(currentItem).toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }

    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }

    /*
    private static void createAndShowGUI() {
        // the combo box (add/modify items if you like to)
        final JComboBox comboBox = new JComboBox(new Object[] {"Ester", "Jordi", "Jordina", "Jorge", "Sergi"});
        enable(comboBox);

        // create and show a window containing the combo box
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(comboBox);
        frame.pack(); frame.setVisible(true);
    }


    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    */
}
