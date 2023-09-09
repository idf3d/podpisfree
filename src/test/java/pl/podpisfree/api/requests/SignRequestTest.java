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

package pl.podpisfree.api.requests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SignRequestTest {
  @Test
  void testSignatureRequest() {
    String input = "{ "
        + "\"tokenId\":\"my-token-id\", "
        + "\"keyId\":\"my-key-id\", "
        + "\"toBeSigned\": \"bXkgdGVzdCBkYXRhIHRvIHNpZ24=\", "
        + "\"digestAlgorithm\":\"SHA256\""
        + "}";
    SignRequest request = new SignRequest(input);

    byte[] expectedData = new byte[]{
        109, 121, 32, 116, 101, 115, 116, 32, 100, 97, 116, 97, 32, 116, 111, 32, 115, 105, 103, 110
    };
    String expectedStringRepresentation = """
        [tokenId: my-token-id
         keyId: my-key-id
         digestAlgorithm: SHA256
         data: bXkgdGVzdCBkYXRhIHRvIHNpZ24=
         decodedData: my test data to sign ]""";

    assertArrayEquals(expectedData, request.getData());
    assertEquals(expectedStringRepresentation, request.toString());
    assertEquals("SHA256", request.getAlgorithm());
  }
}