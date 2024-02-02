# PodpisFree [![Build and test](https://github.com/idf3d/podpisfree/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/idf3d/podpisfree/actions/workflows/main.yml)

> Free software means that the users have the freedom to run, edit, contribute to, and share the software.
> 
> [fsf.org](https://www.fsf.org)

## Introduction

Idea behind this project is to provide free (open source) alternative for PodpisGov application.

## Contents
1. [API](#api)
2. [Limitations](#limitations)
3. [Vendors](#qalified-signature-vendors)
    * [KIR Szafir](#kir-szafir)
    * [Other vendors](#other-vendors)
4. [Usage](#usage)
    * [Configure and run](#configure-and-run)
    * [Slot number](#finding-a-slot-number)
5. [Contribution](#contributing)
    * [TODO list](#todo-list)

## API

Implementation is based on available sources of information from the internet, it is not guaranteed that it is 100%
compatible with PodpisGOV application and suitable for all the cases. Use with caution. 

## Limitations

* PodpisFree is free application for advanced users. Use on your own risk.
* `eu.europa.esig.dss` package is used to communicate with HSM. It uses Sun PKCS11 implementation, so as of now 
Sun / Oracle JDK shall be used to run the application. It would be great to migrate to any kind of open PKCS11 implementation
to remove this limitation (please feel free to submit Pull Request)
* PodpisFree requires PKCS11 library to work properly - please contact your Qalified Signature vendor to obtain such library.
* `eu.europa.esig.dss` doesn't have possibility to find all available slots, so you need to provide slot number manually.

## Qalified Signature Vendors
### KIR Szafir

* PKCS11 library is available on [official web site](https://www.elektronicznypodpis.pl/informacje/aplikacje/).
* If only one HSM is connected to system, Qualified Signature is available under slot with index `1` for most cases.

### Other vendors

_Please add description for other vendors if you will test application with it - submit PR or create issue._

## Usage
### Configure and run
1. Obtain PKCS11 library from your QES vendor - you can put it into `files` directory for convenience.
2. Open `src/main/java/pl/podpisfree/Config.java`, adjust paths for the library and slot index.
3. In project folder, run `./gradlew run`
4. When compilation completes, you should see "Started" message in logs.
5. Open web browser and navigate to <https://localhost:8641>
6. Browser will notify you about insecure connection - accept and proceed to the web page. Depending on browser and OS 
you may need to add certificate of this page as trusted certificate.
7. You shall see `PodpisFree is ready` message in your browser.

### Finding a slot number

If during usage of the application you see PIN or Smart Card related errors, please try to change slot index in
configuration and restart application. 

_Please note_, that usually Smart Card allows only 3 tries for PIN, so if slot was not
selected properly and have different PIN, you can lock the PIN if too many attempts to use the slot is performed.

## Contributing

* Use only standard, cross-platform components for UI (awt should be fine).
* Run `./gradlew check` before submitting PR.
* It is recommended to sign commits with GPG key.
* Do not commit proprietary libraries, non-free components and sensitive information to the repository.
* Write unit tests when possible.

### TODO list
1. Test application with different QES vendors and add information to this file.
2. Use non-sun implementation of PKCS11. If `eu.europa.esig.dss` does not currently support it, consider to 
submit PR to theirs repository or create fork - it is open-source library.
3. Automatically detect slot index.
4. Create UI for configuration.
