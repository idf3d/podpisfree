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
import java.awt.Color;
import java.awt.Frame;
import java.awt.TextArea;

public class ErrorWindow extends Frame {

  public static void show(Throwable error) {
    show(error.getMessage());
  }

  public static void show(String message) {
    ErrorWindow window = new ErrorWindow(message);
    window.setVisible(true);
  }

  ErrorWindow(String message) {
    setSize(500, 200);
    setTitle("podpisFree: Error.");
    setLayout(null);
    setResizable(false);
    setAlwaysOnTop(true);

    TextArea textArea = new TextArea();
    textArea.setBounds(50, 50, 400, 100);
    textArea.setText(message);
    textArea.setEditable(false);
    textArea.setForeground(Color.RED);
    add(textArea);

    Button button = new Button();
    button.setLabel("OK");
    button.setBounds(250, 160, 50, 30);
    button.addActionListener(e -> setVisible(false));
    add(button);
  }
}
