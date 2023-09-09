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

package pl.podpisfree.crypto;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.PrefilledPasswordCallback;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import pl.podpisfree.Config;


public class CryptoCard {

  private final Pkcs11SignatureToken token;

  private final DSSPrivateKeyEntry key;

  public static CryptoCard getInstance(KeyStore.PasswordProtection pin) {
    return new CryptoCard(
        Config.CARD_DRIVER_PATH.getAbsolutePath(),
        Config.CARD_DRIVER_SLOT_INDEX.getInt(),
        pin
    );
  }

  CryptoCard(String path, int slotIndex, KeyStore.PasswordProtection pin) {
    PrefilledPasswordCallback callback = new PrefilledPasswordCallback(pin);
    token = new Pkcs11SignatureToken(
        path,
        callback,
        -1,
        slotIndex,
        "");
    key = token.getKeys().get(0);
  }

  public X509Certificate getCertificate() {
    return key.getCertificate().getCertificate();
  }

  public CertificateToken getCertificateToken() {
    return key.getCertificate();
  }

  public CertificateToken[] getCertificateTokenChain() {
    return key.getCertificateChain();
  }

  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return key.getEncryptionAlgorithm();
  }

  public SignatureValue sign(ToBeSigned toBeSigned, DigestAlgorithm digestAlgorithm) {
    return token.sign(toBeSigned, digestAlgorithm, key);
  }

  public void close() {
    token.close();
  }
}
