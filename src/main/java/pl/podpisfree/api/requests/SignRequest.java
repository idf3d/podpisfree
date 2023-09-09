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

import eu.europa.esig.dss.utils.Utils;
import java.util.Base64;
import org.json.JSONObject;

public class SignRequest {
  private final String tokenId;

  private final String keyId;

  private final String toBeSigned;

  private final String digestAlgorithm;

  public SignRequest(String input) {
    JSONObject object = new JSONObject(input);
    tokenId = (String) object.get("tokenId");
    keyId = (String) object.get("keyId");
    toBeSigned = (String) object.get("toBeSigned");
    digestAlgorithm = (String) object.get("digestAlgorithm");
  }

  public byte[] getData() {
    return Base64.getDecoder().decode(toBeSigned);
  }

  public String getAlgorithm() {
    return digestAlgorithm;
  }

  @Override
  public String toString() {
    return "[tokenId: " + tokenId
        + "\n keyId: " + keyId
        + "\n digestAlgorithm: " + digestAlgorithm
        + "\n data: " + toBeSigned
        + "\n decodedData: " + new String(Utils.fromBase64(toBeSigned))
        + " ]";
  }
}
