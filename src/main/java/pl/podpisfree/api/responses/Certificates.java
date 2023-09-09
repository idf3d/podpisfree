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

import eu.europa.esig.dss.utils.Utils;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.UUID;
import org.json.JSONObject;

public class Certificates extends AbstractResponse {

  private final String tokenId;

  private final String certificate;

  private final String keyId;

  public Certificates(X509Certificate certificate) throws CertificateEncodingException {
    this.tokenId = UUID.randomUUID().toString();
    this.certificate = Utils.toBase64(certificate.getEncoded());
    this.keyId = certificate.getSerialNumber().toString(16);
  }

  JSONObject getResponse() {
    JSONObject result = new JSONObject();

    result.put("tokenId", tokenId);
    result.put("certificate", certificate);
    result.put("keyId", keyId);
    result.put("certificateChain", new ArrayList<>());

    return result;
  }
}
