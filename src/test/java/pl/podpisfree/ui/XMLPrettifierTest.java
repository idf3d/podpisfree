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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class XMLPrettifierTest {

  @Test
  void testWithOneTagDocument() throws XMLPrettifier.PrettifierException {
    String output = XMLPrettifier.prettifyXML("<empty/>".getBytes(StandardCharsets.UTF_8));
    assertEquals(
        """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <empty/>
            """,
        output
    );
  }

  @Test
  void testThatItPrettifiesSimpleDocument() throws XMLPrettifier.PrettifierException {
    String input = "<node><element>value</element><element2>value2</element2></node>";
    String expected = """
        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <node>
          <element>value</element>
          <element2>value2</element2>
        </node>
        """;

    String actual = XMLPrettifier.prettifyXML(input.getBytes(StandardCharsets.UTF_8));
    assertEquals(expected, actual);
  }

  @Test
  void testThatItExpectsProperRootStructure() {
    String input = "<node><element1>velue</element1></node><node/>";
    try {
      XMLPrettifier.prettifyXML(input.getBytes(StandardCharsets.UTF_8));
      fail("Exception was not thrown");
    } catch (XMLPrettifier.PrettifierException e) {
      assertEquals("Can not decode XML", e.getMessage());
    }
  }

  @Test
  void testThatItThrowsExceptionForUnparseableData() {
    try {
      XMLPrettifier.prettifyXML(new byte[]{50, 10, 10, 20, 50});
      fail("Exception was not thrown");
    } catch (XMLPrettifier.PrettifierException e) {
      assertEquals("Can not decode XML", e.getMessage());
    }
  }
}