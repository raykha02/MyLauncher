package org.launcher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder().withName("1.20.4").build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(version)
                .build();

        updater.update(Paths.get("your/path/"));

        GameInfos infos = new GameInfos("MyMinecraft", new GameVersion("1.7.2", GameType.V1_7_2_LOWER), new GameTweak[] {GameTweak.FORGE});
        AuthInfos authInfos = new AuthInfos("PlayerUsername", "token", "uuid");

        ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(infos, GameFolder.BASIC, authInfos);
        ExternalLauncher launcher = new ExternalLauncher(profile);

        launcher.launch();
    }
}