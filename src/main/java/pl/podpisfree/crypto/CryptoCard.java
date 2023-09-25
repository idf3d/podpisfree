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
import java.security.KeyStoreException;
import java.security.ProviderException;
import java.security.cert.X509Certificate;
import javax.security.auth.login.LoginException;
import pl.podpisfree.Config;

public class CryptoCard {

  private final Pkcs11SignatureToken token;

  private final DSSPrivateKeyEntry key;

  public static CryptoCard getInstance(KeyStore.PasswordProtection pin) throws CryptoCardException {
    return new CryptoCard(
        Config.CARD_DRIVER_PATH.getAbsolutePath(),
        Config.CARD_DRIVER_SLOT_INDEX.getInt(),
        pin
    );
  }

  CryptoCard(
      String path,
      int slotIndex,
      KeyStore.PasswordProtection pin
  ) throws CryptoCardException {
    try {
      PrefilledPasswordCallback callback = new PrefilledPasswordCallback(pin);
      token = new Pkcs11SignatureToken(
          path,
          callback,
          -1,
          slotIndex,
          "");
      key = token.getKeys().get(0);
    } catch (Exception e) {
      throw parseException(e, 0);
    }
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

  private CryptoCardException parseException(Throwable exception, int depth) {
    if (exception instanceof LoginException) {
      if ("CKR_PIN_LOCKED".equalsIgnoreCase(exception.getCause().getMessage())) {
        return new CryptoCardException(CryptoCardException.ErrorType.PIN_LOCKED, exception);
      } else {
        return new CryptoCardException(CryptoCardException.ErrorType.PIN_INCORRECT, exception);
      }
    }
    if (exception instanceof KeyStoreException) {
      String message = exception.getMessage();
      if (message != null) {
        if (message.toLowerCase().contains("not found")) {
          return new CryptoCardException(CryptoCardException.ErrorType.NOT_FOUND, exception);
        }
      }
    }
    if (exception instanceof ProviderException) {
      String message = exception.getCause().getMessage();
      if (message != null) {
        if (message.toLowerCase().contains("slotlistindex")) {
          return new CryptoCardException(CryptoCardException.ErrorType.NOT_FOUND, exception);
        }
      }
    }
    if (depth > 3) {
      return new CryptoCardException(CryptoCardException.ErrorType.UNKNOWN, exception);
    }
    return parseException(exception.getCause(), depth + 1);
  }

  public static class CryptoCardException extends Exception {
    public enum ErrorType {
      PIN_INCORRECT, PIN_LOCKED, NOT_FOUND, UNKNOWN;

      String asString() {
        switch (this) {
          case PIN_INCORRECT -> {
            return "Incorrect PIN.";
          }
          case PIN_LOCKED -> {
            return "PIN is locked. Try to use vendor application to unlock PIN.";
          }
          case NOT_FOUND -> {
            return "Card not found. Card not connected or incorrect slot?";
          }
          default -> {
            return "Unknown error.";
          }
        }
      }
    }

    public final ErrorType type;

    CryptoCardException(ErrorType type, Throwable cause) {
      super(type.asString(), cause);
      this.type = type;
    }
  }
}
