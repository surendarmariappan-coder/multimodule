# LLM Script: Generate a Multi‑Module Spring Boot Assembly (Root BootJar + Service Modules + Common)

Copy and paste this entire script into your LLM/chat assistant. Provide the inputs in the “Your Inputs” section. The assistant should produce a working multi‑module Spring Boot project where:
- The root module is a Spring Boot application (bootJar) that assembles and runs all services in a single JVM.
- Service modules (e.g., department-service, employee-service) are built as plain library JARs and included by the root.
- A common module provides shared code (exceptions, error handling, utilities) and is consumed by services and root.

This script is aligned with the current reference project and Gradle configuration (Spring Boot 3.5.x, Java 21, Gradle wrapper, JdbcClient + H2).

---

## Your Inputs
Fill these before running the instructions:
- ROOT_NAME: <your-root-name> (e.g., workspace)
- JAVA_VERSION: <21>
- SPRING_BOOT_VERSION: <3.5.6>
- MODULES:
  - COMMON_MODULE: <common>
  - SERVICE_A: <department-service>
  - SERVICE_B: <employee-service>
  - (Optional more services)
- PACKAGE BASES:
  - COMMON_BASE: <org.example.common>
  - SERVICE_A_BASE: <org.example.departmentservice>
  - SERVICE_B_BASE: <org.example.employeeservice>
- RUNTIME SCAN PACKAGES: list of packages to scan in the root app (typically service subpackages for controllers/repos + common), e.g.:
  - SCAN_PACKAGES: [
      "org.example.departmentservice.departments",
      "org.example.employeeservice.employees",
      "org.example.common"
    ]
- SERVER_PORT: <8080>
- Leave the property files within the service modules only and remove the server port within the service modules (e.g., department-service/src/main/resources/application.properties)

---

## Expected Final Layout
- Root project: ROOT_NAME
- Modules: COMMON_MODULE (java-library), SERVICE_A (library), SERVICE_B (library)
- Root assembles all modules into one executable Spring Boot JAR.
- Ensure the root project builds and runs successfully.
- The dependencies of the root project should be the same as the dependencies of the service modules.
- Remove any duplicate dependencies.
- Ensure BOM is visible to subprojects for version alignment
- Ensure the root project can run the assembled JAR.

---

## Steps for the LLM to Perform

1. Create settings.gradle at root
```
rootProject.name = 'ROOT_NAME'
include 'COMMON_MODULE', 'SERVICE_A', 'SERVICE_B'
```

2. Create root build.gradle
- Plugins: base, java, org.springframework.boot (apply true at root), io.spring.dependency-management (apply true at root)
- dependencyManagement imports Spring Boot BOM
- subprojects: apply java + dependency-management, set group/version, configure repositories, toolchain (JAVA_VERSION), and tests
- Shared dependencies for service modules via configure block
- Root dependencies include project(':COMMON_MODULE') and all services so they are packaged into the bootJar
- Define springBoot.mainClass
- Add a convenient Gradle run task

Template:
```
plugins {
    id 'base'
    id 'org.springframework.boot' version 'SPRING_BOOT_VERSION' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

apply plugin: 'io.spring.dependency-management'
apply plugin: 'java'
apply plugin: 'org.springframework.boot'

dependencyManagement {
    imports {
        mavenBom "org.springframework.boot:spring-boot-dependencies:SPRING_BOOT_VERSION"
    }
}

subprojects {
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'java'

    group = 'org.example'
    version = '0.0.1-SNAPSHOT'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(JAVA_VERSION)
        }
    }
    tasks.named('test') {
        useJUnitPlatform()
    }
}

configure([project(":SERVICE_A"), project(":SERVICE_B")]) {
    dependencies {
        implementation project(':COMMON_MODULE')

        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'org.springframework.boot:spring-boot-starter-jdbc'
        implementation 'org.springframework.boot:spring-boot-starter-validation'

        runtimeOnly 'com.h2database:h2'

        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }
}

dependencies {
    implementation project(':COMMON_MODULE')
    implementation project(':SERVICE_A')
    implementation project(':SERVICE_B')

    // Root needs these to resolve versions via BOM for bootJar/bootRun
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    runtimeOnly 'com.h2database:h2'
}

springBoot {
    mainClass = 'org.example.AssemblyApplication'
}

tasks.named("build") {
    group = "build"
    description = "Builds all subprojects"
    dependsOn(subprojects.collect { it.tasks.named("build") })
}

tasks.register("run") {
    group = "application"
    description = "Runs the root Spring Boot application"
    dependsOn("bootRun")
}
```

3. Create COMMON_MODULE/build.gradle
```
description = 'Common'

base {
    archivesName = 'COMMON_MODULE'
}

// Common stays a plain library jar (no Boot plugin at module level)
```

4. Create SERVICE_A/build.gradle and SERVICE_B/build.gradle
- Make them plain library JARs (bootJar disabled, jar enabled)
- Keep them lean; dependency inheritance comes from the root

Template:
```
description = 'ServiceName'

base {
    archivesName = 'service-name'
}

bootJar { enabled = false }
jar { enabled = true }
```

5. Create root main application class
- Path: src/main/java/org/example/AssemblyApplication.java (or adjust package if you prefer)
- Use targeted component scanning to avoid conflicting @SpringBootApplication classes in modules

Template:
```
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        // Replace with your SCAN_PACKAGES
        "org.example.departmentservice.departments",
        "org.example.employeeservice.employees",
        "org.example.common"
})
public class AssemblyApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssemblyApplication.class, args);
    }
}
```

6. Create root application.properties
- Path: src/main/resources/application.properties
- Use one port and one datasource for the unified app

Template:
```
spring.application.name=AssemblyApp
server.port=SERVER_PORT

spring.datasource.url=JDBC_URL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=JDBC_USERNAME
spring.datasource.password=JDBC_PASSWORD

spring.sql.init.mode=always
spring.sql.init.platform=h2

spring.jpa.show-sql=false
logging.level.org.springframework.jdbc.core=INFO

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

7. Schema files
- Keep schema.sql in each service module under src/main/resources. Spring will execute all schema.sql files on the classpath when spring.sql.init.mode=always is set at root.

8. Service code structure guidance
- Place controllers, services, repositories under service-specific packages (e.g., SERVICE_A_BASE + ".departments", SERVICE_B_BASE + ".employees").
- Use JdbcClient (Spring Boot 3.2+) for simple JDBC access as in the reference.
- Put shared exceptions and @ControllerAdvice into COMMON_MODULE under COMMON_BASE.

9. Avoid multiple Boot apps
- Do not run @SpringBootApplication classes from modules when assembling at the root. Limit component scanning in the root AssemblyApplication to the needed subpackages.
- Disable bootJar in the service modules to ensure only the root creates an executable.

10. Build and run
- Build all: `./gradlew clean build`
- Run from root in IntelliJ: use Gradle task `run` (which depends on `bootRun`) or directly `./gradlew bootRun`
- Run the assembled JAR: `java -jar build/libs/ROOT_NAME-0.0.1-SNAPSHOT.jar`

11. Optional: Publish common locally for other repos
- Add to COMMON_MODULE/build.gradle if needed:
```
plugins {
    id 'maven-publish'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            groupId = 'org.example'
            artifactId = 'COMMON_MODULE'
            version = '0.0.1-SNAPSHOT'
        }
    }
}
```
- Publish: `./gradlew :COMMON_MODULE:publishToMavenLocal`

12. Validation checklist
- Root `build.gradle` applies java + spring-boot; modules do NOT apply Boot
- Subprojects inherit dependency management and Java toolchain
- Root depends on COMMON_MODULE + all services
- Service modules depend on COMMON_MODULE and rely on root-provided dependencies
- Root `AssemblyApplication` scans only subpackages for controllers/repos + common
- `schema.sql` exists in each service module as needed; Spring initializes them on startup
- `./gradlew clean build` succeeds; `./gradlew bootRun` starts the app on SERVER_PORT.

13. Edge cases and tips
- Port conflict: change `server.port` in root application.properties
- If a module still has `@SpringBootApplication`, it’s fine, but ensure the root scanning doesn’t include that package directly; target narrower subpackages (e.g., `.departments`, `.employees`).
- If you need per‑service DBs, remove the unified datasource and add separate DataSource beans/configuration (more complex; not covered by this assembly pattern).
- For separate microservices (multiple processes), instead keep each service as a Boot app with its own bootJar and do not assemble at root; this script focuses on single-process assembly.

---

## Example Outcome (from the reference project)
- Root `build.gradle` manages BOMs and shared dependencies, applies Boot only at root, and sets `mainClass = org.example.AssemblyApplication`.
- `department-service` and `employee-service` are plain JAR modules (bootJar disabled), depending on `common`.
- Root `AssemblyApplication` scans `org.example.departmentservice.departments`, `org.example.employeeservice.employees`, and `org.example.common`.
- Root properties unify H2 database and port.
- Build: `./gradlew clean build` — success.
- Run: `./gradlew bootRun` — exposes both departments and employees endpoints under one app.

Use this script as a template whenever you need to assemble multiple Spring Boot services into a single executable root application with a shared common module.
