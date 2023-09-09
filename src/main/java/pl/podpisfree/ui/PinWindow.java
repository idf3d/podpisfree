/*
 *   Copyright (C) 2023 <https://github.com/idf3d>
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.podpisfree.ui;

import java.awt.Button;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.security.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinWindow extends Frame {

  Logger logger = LoggerFactory.getLogger(PinWindow.class);

  boolean click = false;

  public KeyStore.PasswordProtection password;

  public boolean isConfirmed;

  public PinWindow(String confirmationMessage) {

    final int widowWidth = 600;

    setSize(widowWidth, 300);
    setTitle("podpisFree: PIN Request.");
    setLayout(null);
    setVisible(true);
    setResizable(false);
    setAlwaysOnTop(true);

    int currentY = 30;
    TextArea textArea = new TextArea();
    textArea.setBounds(0, currentY, widowWidth, 200);
    textArea.setText(confirmationMessage);
    textArea.setEditable(false);

    currentY += textArea.getHeight() + 10;
    add(textArea);

    int currentX = 150; // left margin

    Label label = new Label();
    label.setText("PIN:");
    label.setBounds(currentX, currentY, 40, 30);
    add(label);

    currentX += (int) label.getBounds().getWidth();
    currentX += 5;

    TextField textField = new TextField();
    textField.setEchoChar('*');
    textField.setBounds(currentX, currentY, 100, 30);
    add(textField);

    currentX += (int) textField.getBounds().getWidth();
    currentX += 10; // spacing

    Button confirmButton = new Button("Confirm");
    confirmButton.setBounds(currentX, currentY, 80, 30);
    confirmButton.addActionListener(e -> {
      password = new KeyStore.PasswordProtection(textField.getText().toCharArray());
      isConfirmed = true;
      setVisible(false);
      click = true;
      logger.info("Confirmed.");
    });

    add(confirmButton);
    currentX += (int) confirmButton.getBounds().getWidth();

    Button cancelButton = new Button("Cancel");
    cancelButton.setBounds(currentX, currentY, 80, 30);
    cancelButton.addActionListener(e -> {
      isConfirmed = false;
      setVisible(false);
      click = true;
      logger.info("Cancelled.");
    });
    add(cancelButton);

    logger.info("Window is shown");
  }

  public void waitForInput() {
    while (!click) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        logger.warn("Interrupted");
      }
    }
  }

  public static PinWindow getPinWindowForCertificates() {
    PinWindow window = new PinWindow(
        """
            Remote side requested certificates list.\s
            It is safe operation, but remote side will have your data (like name, surname, etc)\s\s
                                    
            Once retrieved, subsequent requests for certificates will not require confirmation. \s
            If you will want to use different card - restart application.
            """
    );
    window.waitForInput();
    return window;
  }

  public static PinWindow getPinWindowForDocument(byte[] data) {
    String documentToSign;

    try {
      documentToSign = XMLPrettifier.prettifyXML(data);
    } catch (Exception e) {
      documentToSign = """
          <<< !!! UNABLE TO DECODE DOCUMENT !!! >>>
          Document malformed or contains binary data.
          Although sometimes it is safe to proceed, you don't know what you sign.
          """;
    }

    PinWindow window = new PinWindow(
        "Remote side asks you to sign following document. \n"
            + "Please review carefully. Signature may be equivalent to wet, handwritten signature."
            + " \n \n"
            + "------------------------------------------------------------- \n"
            + documentToSign
    );
    window.waitForInput();
    return window;
  }
}
