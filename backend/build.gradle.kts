plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.qualitrace"
version = "0.0.1-SNAPSHOT"
description = "Pilotage de la conformité et traçabilité des flux industriels."

java {
	toolchain {
        languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Core & Web
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.session:spring-session-data-redis")

	// Data
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")


    // Documentation (Swagger UI)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

	// Outils
	developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Tests (Spring Boot centralise presque tout dans 'starter-test')
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}
tasks.processResources {
    filesMatching("application.yml") {
        expand(project.properties)
    }
}

tasks.register<Exec>("seedDb") {
    group = "database"
    description = "Vide les tables et injecte les jeux de données de seed dans PostgreSQL."

    // Configuration du conteneur Docker et BDD
    val dbContainer = "qualitrace-db"
    val dbUser = "root"
    val dbName = "qualitrace"

    val seedDir = file("src/main/resources/db/seed")

    doFirst {
        if (!seedDir.exists()) {
            throw GradleException("Le dossier de seed ${seedDir.path} n'existe pas.")
        }
    }

    // Exécution sous Windows CMD
    val scriptPath = seedDir.absolutePath.replace('/', '\\')
    commandLine("cmd", "/c", "for %f in (\"$scriptPath\\*.sql\") do (echo Exécution de %f... && docker exec -i $dbContainer psql -U $dbUser -d $dbName < \"%f\")")
}