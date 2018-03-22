package com.biokey.client.views.panels.challenges;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TextMessageChallengePanelView implements ChallengePanelView {

    private JButton sendButton;
    private JButton resendButton;
    private JButton altButton;
    private JLabel informationLabel;
    private JPanel textArray;
    private JTextField[] fields;

    @Getter private JPanel challengePanel;

    public TextMessageChallengePanelView() {
        super();
        KeyListener onlyNumbers = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if(c < '0' || c > '9')
                    e.consume();
            }
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        };
        fields = new JTextField[6];
        textArray.setLayout(new GridLayout(1,fields.length,5,5));
        for (int i = 0; i < fields.length; i++) {
            final int index = i;
            fields[index] = new JTextField(1);
            fields[index].setHorizontalAlignment(JTextField.CENTER);
            fields[index].addKeyListener(onlyNumbers);
            if (i > 0) {
                fields[index].setEnabled(false);
                fields[index-1].addKeyListener(new KeyListener() {
                    public void keyTyped(KeyEvent e) {}
                    public void keyPressed(KeyEvent e) {}
                    public void keyReleased(KeyEvent e) {
                        if (fields[index-1].getText().length() > 0) {
                            fields[index].setEnabled(true);
                            fields[index].requestFocusInWindow();
                            fields[index-1].setEnabled(false);
                        }
                    }
                });
                fields[index].addKeyListener(new KeyListener() {
                    public void keyTyped(KeyEvent e) {}
                    public void keyPressed(KeyEvent e) {}
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                            fields[index-1].setEnabled(true);
                            fields[index-1].requestFocusInWindow();
                            fields[index].setEnabled(false);
                        }
                    }
                });
            }
            textArray.add(fields[index]);

        }

    }

    public String getCode() {
        // return code.getText();
        String code  = "";
        for (int i = 0; i < fields.length; i++) {
            code += fields[i].getText();
        }
        return code;
    }

    public void addSendAction(ActionListener l) {
        sendButton.addActionListener(l);
    }

    public void addSubmitAction(ActionListener l) {
        // Do Nothing
    }

    public void addResendAction(ActionListener l) {
        resendButton.addActionListener(l);
    }

    public void addAltAction(ActionListener l) {
        altButton.addActionListener(l);
    }

    public void addKeyAction(ActionListener l) {
        for (int i = 0; i < fields.length; i++) {
            fields[i].addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                    l.actionPerformed(new ActionEvent(this, e.getID(), getCode()));
                }
            });
        }
    }


    public void setEnableSend(boolean enable) {
        sendButton.setEnabled(enable);
        sendButton.setVisible(enable);
    }

    public void setEnableSubmit(boolean enable) {
        // Do nothing
    }

    public void setEnableResend(boolean enable) {
        resendButton.setEnabled(enable);
        resendButton.setVisible(enable);
    }

    public void setEnableAlt(boolean enable) {
        altButton.setEnabled(enable);
    }

    public void setInformationText(String newInfo) {
        informationLabel.setText("<html><div style='text-align: center;'>" + newInfo.replace("\n", "<br/>") + "</div></html>");
    }

    public void drawFocus() {
        fields[0].setEnabled(true);
        fields[0].requestFocusInWindow();
    }

    public void clearCode() {
        for (int i = 0; i < fields.length; i++) {
            fields[i].setText("");
            fields[i].setEnabled(false);
        }
        fields[0].setEnabled(true);
        fields[0].requestFocusInWindow();
    }
}
