plugins {
    id("java")
    id("application")
    id("checkstyle")
    id("com.github.ben-manes.versions") version("0.51.0")
}

group = "pl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val dssVersion = "6.0"
    val junitVersion = "5.10.2"

    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("eu.europa.ec.joinup.sd-dss:dss-utils-apache-commons:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-token:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-utils:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-xades:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-model:$dssVersion")
    implementation("org.json:json:20240303")
    implementation("org.w3c:dom:2.3.0-jaxb-1.0.6")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}

application {
    mainClass = "pl.podpisfree.Main"
}

checkstyle {
    configFile = rootProject.file("config/checkstyle.xml")
    configDirectory = rootProject.file("config")
}

tasks.create("genCert") {
    tasks["run"].dependsOn(this)
    doLast {
        val targetDir = rootProject.file("files")
        val keystoreFile = rootProject.file("files/server.jks")
        if (!targetDir.exists()) {
            mkdir(targetDir.path)
        }
        if (!keystoreFile.exists()) {
            exec {
                commandLine(
                    "keytool",
                        "-genkey",
                        "-noprompt",
                        "-dname", "CN=localhost",
                        "-keyalg", "RSA",
                        "-alias","selfsigned",
                        "-keystore", keystoreFile.absolutePath,
                        "-storepass", "password",
                        "-validity", "360",
                        "-keysize", "2048"
                )
            }
        }
    }
}

tasks.jar {
    manifest.attributes["Main-Class"] = "pl.podpisfree.Main"

    val dependencies = configurations
            .runtimeClasspath
            .get()
            .map(::zipTree)
    from(dependencies)
    exclude("META-INF/*.RSA")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


tasks.test {
    useJUnitPlatform()
}
