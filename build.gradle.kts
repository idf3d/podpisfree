plugins {
    id("java")
    id("application")
    id("checkstyle")
}

group = "pl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("eu.europa.ec.joinup.sd-dss:dss-utils-apache-commons:5.12.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-token:5.12.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-utils:5.12.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-xades:5.12.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-model:5.12.1")
    implementation("org.json:json:20230618")
    implementation("org.w3c:dom:2.3.0-jaxb-1.0.6")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "pl.podpisfree.Main"
}

checkstyle {
    configFile = rootProject.file("config/checkstyle.xml")
    configDirectory = rootProject.file("config")
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
