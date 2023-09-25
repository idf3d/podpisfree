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
        String method = req.requestMethod();
        if (method.equals("GET") || method.equals("POST")) {
          res.header("Access-Control-Allow-Origin", "*");
          res.type("application/json");
        }
      });

      options("/*", (req, res) -> {
        res.status(204);
        res.header("Access-Control-Allow-Origin", "*");
        res.header("Access-Control-Allow-Headers", "*");
        return "";
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
          Certificates response = new Certificates(card.getCertificate());
          card.close();
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

        boolean havePIN = savedPIN.get() != null;
        PinWindow pinWindow = PinWindow.getPinWindowForDocument(request.getData(), havePIN);
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
  }

  public void stop() {
    Spark.stop();
  }
}
