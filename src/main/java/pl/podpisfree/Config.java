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

package pl.podpisfree;

import java.io.File;

public enum Config {

  CARD_DRIVER_PATH("files/libCCGraphiteP11.dylib"),
  CARD_DRIVER_SLOT_INDEX("1"),

  // If you use certificate generated by gradle, keep this two values unchanged.
  SERVER_KEYSTORE_PATH("files/server.jks"),
  SERVER_KEYSTORE_PASSWORD("password");

  private final String value;

  Config(String value) {
    if (value == null) {
      throw new NullPointerException("Configuration value can not be null");
    }
    this.value = value;
  }

  public String getString() {
    return value;
  }

  public int getInt() {
    return Integer.parseInt(value);
  }

  public String getAbsolutePath() {
    return new File(value).getAbsolutePath();
  }
}
