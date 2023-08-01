plugins {
    `java-library`

    id("com.github.johnrengelman.shadow") version "8.1.1" // Shades and relocates dependencies, See https://imperceptiblethoughts.com/shadow/introduction/
    id("xyz.jpenilla.run-paper") version "2.1.0" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Automatic plugin.yml generation
    id("io.papermc.paperweight.userdev") version "1.5.5" // Used to develop internal plugins using Mojang mappings
}

group = "com.github.alathra.AlathranWars"
version = "3.0.0-SNAPSHOT-3"
description = ""

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17)) // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
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

    maven("https://repo.kryptonmc.org/releases/") {
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
    compileOnly("org.jetbrains:annotations:24.0.1")

    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
    implementation("space.arim.morepaperlib:morepaperlib:0.4.3")

    implementation("com.github.milkdrinkers:simplixstorage:3.2.7")
    implementation("com.github.milkdrinkers:colorparser:2.0.0") {
        exclude(group = "net.kyori", module = "adventure-api")
    }

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    compileOnly("com.palmergames.bukkit.towny:towny:0.99.5.7") {
        exclude(group = "com.palmergames.adventure")
    }

    compileOnly("me.neznamy:tab-api:4.0.0")

    implementation("dev.jorel:commandapi-bukkit-shade:9.0.3") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    compileOnly("dev.jorel:commandapi-annotations:9.0.3")
    annotationProcessor("dev.jorel:commandapi-annotations:9.0.3")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4") {
        isTransitive = false
    }

    compileOnly("me.clip:placeholderapi:2.11.3") {
        exclude(group = "me.clip.placeholderapi.libs", module = "kyori")
    }
    compileOnly(files("lib/Graves-4.9.jar"))
    compileOnly("com.github.Gecolay.GSit:core:1.4.9")
    compileOnly(files("lib/HeadsPlus-7.0.14.jar"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
        options.compilerArgs.add("-Xlint:-deprecation")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        // Shadow classes
        // helper function to relocate a package into our package
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.group}.${targetPkg}")

        reloc("space.arim.morepaperlib", "morepaperlib")
        reloc("de.leonhard.storage", "storageapi")
        reloc("dev.jorel.commandapi", "commandapi")
        reloc("com.zaxxer.hikari", "hikaricp")
        reloc("org.mariadb.jdbc", "mariadb")
    }

    runServer {
        // Configure the Minecraft version for our task.
        minecraftVersion("1.19.4")
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
    }
}

bukkit {
    // Plugin main class (required)
    main = "${project.group}.Main"

    // Plugin Information
    name = project.name
    prefix = "AlathranWars"
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("darksaid98", "ShermansWorld", "NinjaMandalorian", "AubriTheHuman")
    contributors = listOf()
    apiVersion = "1.19"

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf("Vault", "Towny")
    softDepend = listOf("TAB")
}