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

import eu.europa.esig.dss.definition.xmldsig.XMLDSigAttribute;
import eu.europa.esig.dss.definition.xmldsig.XMLDSigElement;
import eu.europa.esig.dss.enumerations.CommitmentType;
import eu.europa.esig.dss.enumerations.CommitmentTypeEnum;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.BLevelParameters;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.DigestDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.reference.DSSReference;
import eu.europa.esig.dss.xades.reference.DSSTransform;
import eu.europa.esig.dss.xades.reference.XPathTransform;
import eu.europa.esig.dss.xades.signature.XAdESService;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.transforms.Transforms;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLSigner {
  private final Document inputDocument;

  public XMLSigner(byte[] data) throws XMLSignerException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      inputDocument = builder.parse(new ByteArrayInputStream(data));
    } catch (ParserConfigurationException e) {
      throw new XMLSignerException("Can not create document builder.", e);
    } catch (Throwable parseException) {
      throw new XMLSignerException("Can not parse document.", parseException);
    }
  }

  public byte[] sign(CryptoCard card) throws XMLSignerException {
    try {
      DSSDocument emptyDocument = new InMemoryDocument("<empty/>".getBytes());
      XAdESService service = new XAdESService(new CommonCertificateVerifier());
      XAdESSignatureParameters parameters = getParameters();

      parameters.setSigningCertificate(card.getCertificateToken());
      parameters.setCertificateChain(card.getCertificateTokenChain());
      parameters.setEncryptionAlgorithm(card.getEncryptionAlgorithm());

      ToBeSigned toBeSigned = service.getDataToSign(emptyDocument, parameters);
      SignatureValue signatureValue = card.sign(toBeSigned, parameters.getDigestAlgorithm());

      DSSDocument signedDocument = service.signDocument(
          emptyDocument,
          parameters,
          signatureValue
      );

      return DSSUtils.toByteArray(signedDocument);
    } catch (DSSException dssException) {
      throw new XMLSignerException("Unable to sign", dssException);
    } catch (Throwable throwable) {
      throw new XMLSignerException("Unexpected exception", throwable);
    }
  }

  private XAdESSignatureParameters getParameters() throws XMLSignerException {
    XAdESSignatureParameters parameters = new XAdESSignatureParameters();
    parameters.setReferences(createDSSReferences());

    parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
    parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
    parameters.setSigningCertificateDigestMethod(DigestAlgorithm.SHA256);

    parameters.setDigestAlgorithm(getDigestAlgorithm());
    parameters.setEn319132(false);

    BLevelParameters blevelParameters = parameters.bLevel();
    blevelParameters.setSigningDate(new Date());
    blevelParameters.setCommitmentTypeIndications(getCommitment());

    return parameters;
  }

  private List<CommitmentType> getCommitment() {
    List<CommitmentType> list = new ArrayList<>();
    list.add(CommitmentTypeEnum.ProofOfApproval);
    return list;
  }

  private DigestAlgorithm getDigestAlgorithm() throws XMLSignerException {
    try {
      Element digestMethodDom = (Element) inputDocument
          .getElementsByTagName("ds:" + XMLDSigElement.DIGEST_METHOD.getTagName())
          .item(0);
      String algorithm = digestMethodDom
          .getAttribute(XMLDSigAttribute.ALGORITHM.getAttributeName());
      return DigestAlgorithm.forXML(algorithm);
    } catch (Exception e) {
      throw new XMLSignerException("Can not retrieve algorithm.", e);
    }
  }

  private List<DSSReference> createDSSReferences() throws XMLSignerException {
    List<DSSReference> result = new ArrayList<>();

    NodeList references = inputDocument
        .getElementsByTagName("ds:" + XMLDSigElement.REFERENCE.getTagName());

    for (int i = 0; i < references.getLength(); ++i) {
      result.add(DSSReferenceFactory.produce(
          references.item(i)
      ));
    }

    return result;
  }

  private record DSSReferenceFactory(Element sourceElement) {
    static DSSReference produce(Node input) throws XMLSignerException {
      DSSReferenceFactory instance = new DSSReferenceFactory((Element) input);
      return instance.produce();
    }

    DSSReference produce() throws XMLSignerException {
      DigestDocument digestDocument = getDigestDocument();

      String id = sourceElement.getAttribute(XMLDSigAttribute.ID.getAttributeName());
      String uri = sourceElement.getAttribute(XMLDSigAttribute.URI.getAttributeName());

      DSSReference dssReference = new DSSReference();
      dssReference.setContents(digestDocument);
      dssReference.setId(id);
      dssReference.setUri(uri);
      dssReference.setDigestMethodAlgorithm(getDigestAlgorithm());

      DSSTransform dssTransform = getTransform();

      if (dssTransform == null) {
        digestDocument.setMimeType(MimeTypeEnum.BINARY);
        return dssReference;
      }

      List<DSSTransform> transforms = new ArrayList<>();
      transforms.add(dssTransform);
      dssReference.setTransforms(transforms);

      return dssReference;
    }

    private Element getElement(XMLDSigElement dsElement) {
      return (Element) getNode(dsElement);
    }

    private Node getNode(XMLDSigElement dsElement) {
      return sourceElement.getElementsByTagName("ds:" + dsElement.getTagName()).item(0);
    }

    private DigestAlgorithm getDigestAlgorithm() {
      Element digestMethod = getElement(XMLDSigElement.DIGEST_METHOD);
      return DigestAlgorithm.forXML(
          digestMethod.getAttribute(XMLDSigAttribute.ALGORITHM.getAttributeName())
      );
    }

    private DigestDocument getDigestDocument() {
      Node digestValue = getNode(XMLDSigElement.DIGEST_VALUE);

      DigestDocument digestDocument = new DigestDocument();
      digestDocument.addDigest(getDigestAlgorithm(), digestValue.getTextContent());

      return digestDocument;
    }

    private DSSTransform getTransform() throws XMLSignerException {
      Element transformElement = getElement(XMLDSigElement.TRANSFORM);

      if (transformElement == null) {
        return null;
      }

      checkTransformAlgo(transformElement);

      Node xpath = getNode(XMLDSigElement.XPATH);
      if (xpath == null) {
        throw new XMLSignerException("Transformation content not found");
      }

      String xpathContent = xpath.getTextContent();
      if (xpathContent == null) {
        throw new XMLSignerException("Transformation content found, but is null.");
      }

      return new XPathTransform(xpathContent);
    }

    private void checkTransformAlgo(Element transform) throws XMLSignerException {
      String transformAlgo = transform
          .getAttribute(XMLDSigAttribute.ALGORITHM.getAttributeName());
      if (!transformAlgo.equals(Transforms.TRANSFORM_XPATH)) {
        throw new XMLSignerException(
            "Unexpected Transform algorithm (" + transformAlgo + ")"
        );
      }
    }
  }

  public static class XMLSignerException extends Exception {
    XMLSignerException(String message, Throwable cause) {
      super(message, cause);
    }

    XMLSignerException(String message) {
      super(message);
    }
  }
}
