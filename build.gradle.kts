import org.jooq.meta.jaxb.Logging
import java.time.Instant

plugins {
    `java-library`

    id("com.github.johnrengelman.shadow") version "8.1.1" // Shades and relocates dependencies, See https://imperceptiblethoughts.com/shadow/introduction/
    id("xyz.jpenilla.run-paper") version "2.2.4" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Automatic plugin.yml generation
    id("io.papermc.paperweight.userdev") version "1.6.2" // Used to develop internal plugins using Mojang mappings, See https://github.com/PaperMC/paperweight
    id("org.flywaydb.flyway") version "10.12.0" // Database migrations
    id("org.jooq.jooq-codegen-gradle") version "3.19.7"

    eclipse
    idea
}

group = "com.github.alathra"
version = "3.0.0"
description = ""
val mainPackage = "${project.group}.${rootProject.name.lowercase()}"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17)) // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
//    withJavadocJar() // Enable Javadoc generation
}

repositories {
    mavenCentral()

    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://mvn-repo.arim.space/lesser-gpl3/")

    maven("https://repo.glaremasters.me/repository/towny/") {
        content { includeGroup("com.palmergames.bukkit.towny") }
    }

    maven("https://jitpack.io/") {
        content {
            includeGroup("com.github.milkdrinkers")
            includeGroup("com.github.MilkBowl")
            includeGroup("com.palmergames.bukkit.towny")
            includeGroup("com.github.Gecolay.GSit")
        }
    }

    maven("https://repo.kryptonmc.org/releases") {
        content { includeGroup("me.neznamy") }
    }

    maven("https://repo.codemc.org/repository/maven-public/") {
        content { includeGroup("dev.jorel") }
    }

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content { includeGroup("me.clip") }
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    annotationProcessor("org.jetbrains:annotations:24.1.0")

    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    implementation("space.arim.morepaperlib:morepaperlib:latest.release")

    implementation("com.github.milkdrinkers:crate:1.1.0")
    implementation("com.github.milkdrinkers:colorparser:2.0.0") {
        exclude("net.kyori")
    }

    implementation("dev.jorel:commandapi-bukkit-shade:9.4.2")

    compileOnly("me.clip:placeholderapi:2.11.5") {
        exclude("me.clip.placeholderapi.libs", "kyori")
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.palmergames.bukkit.towny:towny:0.100.2.6") {
        exclude("com.palmergames.adventure")
    }
    compileOnly("me.neznamy:tab-api:4.0.2")
    compileOnly(files("lib/Graves-4.9.jar"))
    compileOnly("com.github.Gecolay.GSit:core:1.9.0")
    compileOnly(files("lib/HeadsPlus-7.0.14.jar"))
    compileOnly(files("lib/Skulls.jar"))

    // Database Dependencies
    implementation("com.zaxxer:HikariCP:5.1.0")
    library("org.flywaydb:flyway-core:10.12.0")
    library("org.flywaydb:flyway-mysql:10.12.0")
    library("org.flywaydb:flyway-database-hsqldb:10.12.0")
    library("org.jooq:jooq:3.19.7")
    jooqCodegen("com.h2database:h2:2.2.224")

    // JDBC Drivers
    library("org.hsqldb:hsqldb:2.7.2")
    library("com.h2database:h2:2.2.224")
    library("com.mysql:mysql-connector-j:8.3.0")
    library("org.mariadb.jdbc:mariadb-java-client:3.3.3")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    build {
        dependsOn(shadowJar)
    }

    jooqCodegen {
        dependsOn(flywayMigrate)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
        options.compilerArgs.addAll(arrayListOf("-Xlint:all", "-Xlint:-processing", "-Xdiags:verbose"))

        dependsOn(jooqCodegen) // Generate jOOQ sources before compilation
    }

    javadoc {
        isFailOnError = false
        exclude("${mainPackage.replace(".", "/")}/db/schema/**") // Exclude generated jOOQ sources from javadocs
        val options = options as StandardJavadocDocletOptions
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.overview = "src/main/javadoc/overview.html"
        options.tags("apiNote:a:API Note:", "implNote:a:Implementation Note:", "implSpec:a:Implementation Requirements:")
        options.use()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        // Shadow classes
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${mainPackage}.lib.${targetPkg}")

        reloc("space.arim.morepaperlib", "morepaperlib")
        reloc("com.github.milkdrinkers.Crate", "crate")
        reloc("com.github.milkdrinkers.colorparser", "colorparser")
        reloc("dev.jorel.commandapi", "commandapi")
        reloc("com.zaxxer.hikari", "hikaricp")

        mergeServiceFiles {
            setPath("META-INF/services/org.flywaydb.core.extensibility.Plugin") // Fix Flyway overriding its own files
        }

        minimize()
    }

    runServer {
        // Configure the Minecraft version for our task.
        minecraftVersion("1.20.1")

        // IntelliJ IDEA debugger setup: https://docs.papermc.io/paper/dev/debugging#using-a-remote-debugger
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true", "-DIReallyKnowWhatIAmDoingISwear")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)

        // Automatically install dependencies
        downloadPlugins {
            url("https://ci.dmulloy2.net/job/ProtocolLib/lastStableBuild/artifact/build/libs/ProtocolLib.jar")
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
            url("https://download.luckperms.net/1521/bukkit/loader/LuckPerms-Bukkit-5.4.108.jar")
            modrinth("tab-was-taken", "4.1.2")
            github("PlaceholderAPI", "PlaceholderAPI", "2.11.4", "PlaceholderAPI-2.11.4.jar")
//            url("https://www.spigotmc.org/resources/skulls-the-ultimate-head-database.90098/download?version=520217/Skulls.jar")
        }
    }
}

bukkit { // Options: https://github.com/Minecrell/plugin-yml#bukkit
    // Plugin main class (required)
    main = "${mainPackage}.${rootProject.name}"

    // Plugin Information
    name = project.name
    prefix = project.name
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("darksaid98", "ShermansWorld", "NinjaMandalorian", "AubriTheHuman")
    contributors = listOf()
    apiVersion = "1.20"

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf("Vault", "Towny")
    softDepend = listOf("PlaceholderAPI", "TAB", "Skulls", "HeadsPlus")
}

flyway {
    url = "jdbc:h2:${project.layout.buildDirectory.get()}/generated/flyway/db;AUTO_SERVER=TRUE;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;IGNORECASE=TRUE"
    user = "sa"
    password = ""
    schemas = listOf("PUBLIC").toTypedArray()
    placeholders = mapOf( // Substitute placeholders for flyway
        "tablePrefix" to "",
        "columnSuffix" to " VIRTUAL",
        "tableDefaults" to "",
        "uuidType" to "BINARY(16)",
        "inetType" to "VARBINARY(16)",
        "binaryType" to "BLOB",
        "alterViewStatement" to "ALTER VIEW",
    )
    validateMigrationNaming = true
    baselineOnMigrate = true
    cleanDisabled = false
    locations = arrayOf(
        "filesystem:src/main/resources/db/migration",
        "classpath:db/migration"
    )
}

jooq {
    configuration {
        logging = Logging.ERROR
        jdbc {
            driver = "org.h2.Driver"
            url = flyway.url
            user = flyway.user
            password = flyway.password
        }
        generator {
            database {
                name = "org.jooq.meta.h2.H2Database"
                includes = ".*"
                excludes = "(flyway_schema_history)|(?i:information_schema\\..*)|(?i:system_lobs\\..*)"  // Exclude db specific files
                inputSchema = "PUBLIC"
                schemaVersionProvider = "SELECT :schema_name || '_' || MAX(\"version\") FROM \"flyway_schema_history\"" // Grab version from Flyway
            }
            target {
                packageName = "${mainPackage}.db.schema"
                directory = layout.buildDirectory.dir("generated-src/jooq").get().toString()
                withClean(true)
            }
        }
    }
}

// Apply custom version arg
val versionArg = if (hasProperty("customVersion"))
    (properties["customVersion"] as String).uppercase() // Uppercase version string
else
    "${project.version}-SNAPSHOT-${Instant.now().epochSecond}" // Append snapshot to version

// Strip prefixed "v" from version tag
project.version = if (versionArg.first().equals('v', true))
    versionArg.substring(1)
else
    versionArg.uppercase()