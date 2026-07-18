package org.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.openlauncherlib.*;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.*;
import fr.flowarg.flowupdater.*;
import fr.theshark34.openlauncherlib.util.explorer.Explorer;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        GameInfos infos = new GameInfos(
                "MyMinecraft",
                new GameVersion("1.20.4", GameType.V1_13_HIGHER_VANILLA),
                new GameTweak[0]
        );

        Path gameDir = infos.getGameDir();

        // --- Téléchargement (FlowUpdater) ---
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder()
                .withName("1.20.4")
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(version)
                .build();

        updater.update(gameDir);

        // --- Extraction des natives (Mojang ne les extrait pas, on le fait nous-mêmes) ---
        extractNatives(gameDir);

        // --- Lancement (OpenLauncherLib) ---
        GameFolder folder = new GameFolder("assets", "libraries", "natives", "client.jar");
        AuthInfos authInfos = new AuthInfos("Player", "0", "00000000-0000-0000-0000-000000000000");

        ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(infos, folder, authInfos);
        ExternalLauncher launcher = new ExternalLauncher(profile);
        launcher.launch();
    }

    /**
     * Extrait les .dll/.so/.dylib des jars "natives-<plateforme>" de libraries/
     * vers le dossier natives/, en ignorant les variantes x86/arm64 (on vise x64 standard).
     */
    private static void extractNatives(Path gameDir) throws IOException {
        Path librariesDir = gameDir.resolve("libraries");
        Path nativesDir = gameDir.resolve("natives");
        Files.createDirectories(nativesDir);

        String os = System.getProperty("os.name").toLowerCase();
        String platformTag = os.contains("win") ? "windows" : os.contains("mac") ? "macos" : "linux";

        try (Stream<Path> stream = Files.walk(librariesDir)) {
            stream.filter(p -> p.toString().endsWith(".jar"))
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.contains("natives-" + platformTag)
                                && !name.contains("natives-" + platformTag + "-x86")
                                && !name.contains("natives-" + platformTag + "-arm64");
                    })
                    .forEach(jar -> extractJarInto(jar, nativesDir));
        }

        System.out.println("Natives extraites : " + java.util.Arrays.toString(nativesDir.toFile().list()));
    }

    private static void extractJarInto(Path jar, Path destDir) {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().startsWith("META-INF")) continue;

                Path outPath = destDir.resolve(Paths.get(entry.getName()).getFileName().toString());
                try (InputStream in = jarFile.getInputStream(entry)) {
                    Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur d'extraction des natives : " + jar, e);
        }
    }
}