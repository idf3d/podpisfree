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

package pl.podpisfree.api.responses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  void testResponse() {
    Version response = new Version();
    String expectedResponse = "{"
        + "\"environment\":{"
        + "\"jreVendor\":\"ORACLE\","
        + "\"osVersion\":\"10\","
        + "\"os\":\"Windows 10\","
        + "\"osArch\":\"x86_64\","
        + "\"arch\":\"AMD64\","
        + "\"osName\":\"Windows 10\"},"
        + "\"version\":\"1.5.3.0\""
        + "}";
    assertEquals(expectedResponse, response.toString());
  }
}