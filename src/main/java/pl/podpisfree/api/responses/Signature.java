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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import org.json.JSONObject;

public class Signature extends AbstractResponse {
  private final String certificate;

  private final String algorithm;

  private final String signature;

  public Signature(
      X509Certificate certificate,
      String algorithm,
      byte[] data
  ) throws CertificateEncodingException {
    this.signature = Base64.getEncoder().encodeToString(data);
    this.algorithm = algorithm;
    this.certificate = Base64.getEncoder().encodeToString(certificate.getEncoded());
  }

  JSONObject getResponse() {
    JSONObject result = new JSONObject();

    result.put("signatureValue", signature);
    result.put("signatureAlgorithm", algorithm);
    result.put("certificate", certificate);
    result.put("certificateChain", new ArrayList<String>());

    return result;
  }
}
