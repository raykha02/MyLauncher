package org.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import fr.flowarg.flowupdater.download.json.CurseFileInfo;
import fr.flowarg.flowupdater.download.json.Mod;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersionBuilder;
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

        // --- GameType maison pour Fabric (OpenLauncherLib n'en a pas de prêt) ---
        GameType fabricType = new GameType() {
            @Override
            public String getName() {
                return "Fabric";
            }

            @Override
            public String getMainClass(GameInfos infos) {
                return "net.fabricmc.loader.impl.launch.knot.KnotClient";
            }

            @Override
            public ArrayList<String> getLaunchArgs(GameInfos infos, GameFolder folder, AuthInfos authInfos) {
                ArrayList<String> arguments = new ArrayList<>();
                arguments.add("--username"); arguments.add(authInfos.getUsername());
                arguments.add("--accessToken"); arguments.add(authInfos.getAccessToken());
                arguments.add("--uuid"); arguments.add(authInfos.getUuid());
                arguments.add("--version"); arguments.add(infos.getGameVersion().getName());
                arguments.add("--gameDir"); arguments.add(infos.getGameDir().toAbsolutePath().toString());
                arguments.add("--assetsDir"); arguments.add(infos.getGameDir().resolve(folder.getAssetsFolder()).toAbsolutePath().toString());
                arguments.add("--assetIndex"); arguments.add(infos.getGameVersion().getName());
                arguments.add("--userType"); arguments.add("legacy");
                return arguments;
            }
        };

        // --- Décrit le jeu qu'on veut lancer ---
        GameInfos infos = new GameInfos(
                "MyMinecraft",
                new GameVersion("1.20.4", fabricType),
                new GameTweak[0]
        );

        Path gameDir = infos.getGameDir();

        // --- Téléchargement (FlowUpdater : vanilla + Fabric) ---
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder()
                .withName("1.20.4")
                .build();

        List<CurseFileInfo> modInfos = new ArrayList<>();

        modInfos.add(new CurseFileInfo(
                394468, // Project ID
                5092261 // File ID
        ));
        // Fabric API
        modInfos.add(new CurseFileInfo(
                306612,
                6674317

        ));
        // Mode Menu
        modInfos.add(new CurseFileInfo(
                308702,
                5441535
        ));
        // GeckoLib Fabric 1.20.4
        modInfos.add(new CurseFileInfo(
                388172,
                5188390
        ));

        FabricVersion fabricVersion = new FabricVersionBuilder()
                .withFabricVersion("0.15.11") // optionnel : omis = dernière version stable
                .withCurseMods(modInfos)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(version)
                .withModLoaderVersion(fabricVersion)
                .build();

        updater.update(gameDir);

        // --- Extraction des natives LWJGL (Mojang ne les extrait pas, on le fait nous-mêmes) ---
        extractNatives(gameDir);

        // --- Lancement (OpenLauncherLib) ---
        // GameFolder.BASIC attend "libs"/"minecraft.jar" (vieille convention).
        // FlowUpdater télécharge en "libraries"/"client.jar" (convention Mojang moderne).
        GameFolder folder = new GameFolder("assets", "libraries", "natives", "client.jar");

        // Auth "offline" : suffisant pour juste tester que ça démarre.
        // Remplace par une vraie session MinecraftAuth (device code flow) si besoin.
        AuthInfos authInfos = new AuthInfos("Player", "0", "00000000-0000-0000-0000-000000000000");

        ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(infos, folder, authInfos);
        ExternalLauncher launcher = new ExternalLauncher(profile);
        launcher.launch();
    }

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