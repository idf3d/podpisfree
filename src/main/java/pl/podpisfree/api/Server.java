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

package pl.podpisfree.api;

import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.ipAddress;
import static spark.Spark.options;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.secure;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.podpisfree.Config;
import pl.podpisfree.api.requests.SignRequest;
import pl.podpisfree.api.responses.Certificates;
import pl.podpisfree.api.responses.Signature;
import pl.podpisfree.api.responses.Version;
import pl.podpisfree.crypto.CryptoCard;
import pl.podpisfree.crypto.XMLSigner;
import pl.podpisfree.ui.ErrorWindow;
import pl.podpisfree.ui.PinWindow;
import spark.Spark;

public class Server {
  private final Logger logger = LoggerFactory.getLogger(Server.class);

  private final AtomicReference<Certificates> certificates = new AtomicReference<>();

  private final AtomicReference<KeyStore.PasswordProtection> savedPIN = new AtomicReference<>();

  private final AtomicReference<Date> lastKnownExpirationDate = new AtomicReference<>();

  public void run() {
    ipAddress("127.0.0.1");
    port(8641);
    secure(
        Config.SERVER_KEYSTORE_PATH.getAbsolutePath(),
        Config.SERVER_KEYSTORE_PASSWORD.getString(),
        null,
        null
    );
    get("/", (req, res) -> "<h2>PodpisFree is ready.</h2>");
    path("/rest", () -> {
      before("/*", (req, res) -> {
        String origin = req.headers("Origin");
        if (origin != null) {
          res.header("Access-Control-Allow-Origin", origin);
        }

        String method = req.requestMethod();
        if (!"GET".equals(method) && !"POST".equals(method)) {
          return;
        }

        res.header("Access-Control-Allow-Headers", String.join(", ", req.headers()));
        res.header("Access-Control-Allow-Credentials", "true");
        res.type("application/json");
      });

      options("/*", (req, res) -> {
        res.status(204);

        String acHeaders = req.headers("Access-Control-Request-Headers");
        if (acHeaders != null) {
          res.header("Access-Control-Allow-Headers", acHeaders);
        }

        String acMethod = req.headers("Access-Control-Request-Method");
        if (acMethod != null) {
          res.header("Access-Control-Allow-Methods", acMethod);
        }

        return "OK";
      });

      get("/version", (req, res) -> new Version());

      get("/certificates", (req, res) -> {
        if (certificates.get() != null) {
          return certificates.get();
        }

        PinWindow pinWindow = PinWindow.getPinWindowForCertificates();
        if (!pinWindow.isConfirmed) {
          halt(422, "{}");
        }
        logger.info("Certificates - confirmation received.");

        try {
          CryptoCard card = CryptoCard.getInstance(pinWindow.pin);
          X509Certificate certificate = card.getCertificate();
          card.close();

          lastKnownExpirationDate.set(certificate.getNotAfter());
          Certificates response = new Certificates(certificate);
          if (pinWindow.savePIN) {
            savedPIN.set(pinWindow.pin);
          }
          certificates.set(response);
          return response;
        } catch (Exception e) {
          logger.error("Can not process 'certificates' api request", e);
          ErrorWindow.show(e);
          halt(500);
          return "{}";
        }
      });
      post("/sign", (req, res) -> {
        SignRequest request = new SignRequest(req.body());

        if (!"SHA256".equals(request.getAlgorithm())) {
          ErrorWindow.show(
              "Unexpected digest algorithm, expected SHA256, but "
                  + request.getAlgorithm() + "found"
          );
          halt(500);
          return "";
        }

        boolean havePIN = savedPIN.get() != null;
        PinWindow pinWindow = PinWindow.getPinWindowForDocument(
            request.getData(),
            havePIN,
            lastKnownExpirationDate.get()
        );
        if (!pinWindow.isConfirmed) {
          halt(422, "{}");
        }

        try {
          XMLSigner signer = new XMLSigner(request.getData());
          CryptoCard card;
          if (havePIN && pinWindow.pin == null) {
            card = CryptoCard.getInstance(savedPIN.get());
          } else {
            card = CryptoCard.getInstance(pinWindow.pin);
          }

          Signature response = new Signature(
              card.getCertificate(),
              request.getAlgorithm(),
              signer.sign(card)
          );

          card.close();

          if (!pinWindow.savePIN) {
            savedPIN.set(null);
          } else if (pinWindow.pin != null) {
            savedPIN.set(pinWindow.pin);
          }

          return response;
        } catch (Exception e) {
          logger.error("Can not process 'sign' api request.", e);
          ErrorWindow.show(e);
          halt(500);
          return "";
        }
      });
    });

    awaitInitialization();
    printInfo();
  }

  public void stop() {
    Spark.stop();
  }

  private void printInfo() {
    logger.info("=".repeat(48));
    logger.info("Please open https://localhost:8641/ in browser,");
    logger.info("message \"PodpisFree is ready.\" should be shown.");
    logger.info("If this is first run or locally generated certificate was recreated,");
    logger.info("you may need to allow self-signed certificate in your browser.");
    logger.info("=".repeat(48));
  }
}
