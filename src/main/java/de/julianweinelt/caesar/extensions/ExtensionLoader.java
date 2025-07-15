package de.julianweinelt.caesar.extensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.event.CaesarExtensionLoadEvent;
import de.julianweinelt.caesar.exceptions.PluginInvalidException;
import de.julianweinelt.caesar.feature.Registry;
import de.julianweinelt.caesar.plugin.CPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class ExtensionLoader {
    private final File base = CaesarConnector.getInstance().getDataFolder();
    private final File extensionFolder = new File(base, "extensions");
    private final Logger log = CaesarConnector.getInstance().getLogger();

    private final Registry registry = CaesarConnector.getInstance().getFeatureRegistry();

    private final HashMap<String, URL> extensionURLs = new HashMap<>();
    private URLClassLoader sharedLoader;

    public void prepareLoading() {
        log.info("Preparing to load extensions...");
        if (extensionFolder.mkdir()) log.info("Created folder: " + extensionFolder.getAbsolutePath());
        File[] extensions = extensionFolder.listFiles();
        if (extensions == null) return;
        for (File f : extensions) {
            if (f.getName().endsWith(".jar")) {
                log.info("Found extension: " + f.getName());

                try {
                    Path jarPath = Path.of(f.getAbsolutePath());

                    try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                        ZipEntry entry = jarFile.getEntry("plugin.json");
                        if (entry == null) {
                            log.warning("No plugin.json found in extension: " + f.getName());
                            log.warning("Skipping...");
                            return;
                        }

                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            String jsonString  = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

                            URL jarURL = jarPath.toUri().toURL();
                            extensionURLs.put(json.get("pluginName").getAsString(), jarURL);
                        }
                    }
                } catch (Exception e) {
                    log.severe("Failed to load extension: " + f.getName() + " - " + e.getMessage());
                }
            }
        }

        sharedLoader = new URLClassLoader(extensionURLs.values().toArray(URL[]::new), getClass().getClassLoader());
    }

    public void loadExtensions() {
        log.info("Loading extensions...");
        for (String name : extensionURLs.keySet()) {
            loadExtension(name);
        }
    }

    public void loadExtension(String name) {
        log.info("Loading " + name);
        name = name.replace(".jar", "");
        try {
            Path jarPath = Path.of(extensionFolder + "/" + name + ".jar");

            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                ZipEntry jsonEntry = jarFile.getEntry("plugin.json");
                if (jsonEntry == null) {
                    throw new PluginInvalidException("The loaded file " + name + ".jar does not contain a plugin.json file.");
                }


                try(InputStream inputStream = jarFile.getInputStream(jsonEntry)) {
                    String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
                    List<String> authors = new ArrayList<>();
                    for (JsonElement e : json.get("authors").getAsJsonArray()) authors.add(e.getAsString());
                    StringBuilder autorString = new StringBuilder();
                    for (String s : authors) autorString.append(s).append(",");
                    log.info("Detected plugin with name " + json.get("pluginName").getAsString() + " created by " + autorString);
                    log.info("Version: " + json.get("version").getAsString());

                    String mainClassName = json.get("mainClass").getAsString();
                    URLClassLoader classLoader = sharedLoader;

                    log.info("Loading " + mainClassName);

                    Class<?> mainClass = Class.forName(mainClassName, true, classLoader);

                    if (!CPlugin.class.isAssignableFrom(mainClass)) {
                        throw new PluginInvalidException("Main class must implement CPlugin interface");
                    }

                    CPlugin extensionInstance = (CPlugin) mainClass.getDeclaredConstructor().newInstance();
                    log.info("Extension Classloader: " + extensionInstance.getClass().getClassLoader());

                    extensionInstance.onBukkitLoad();

                    File dataFolder = new File(extensionFolder, extensionInstance.getName());
                    if (dataFolder.mkdir()) log.info("Created new data folder for " +  extensionInstance.getName());

                    extensionInstance.onBukkitEnable();

                    registry.registerExtension(extensionInstance);
                    Bukkit.getPluginManager().callEvent(new CaesarExtensionLoadEvent(
                            extensionInstance.getName(),
                            extensionInstance.getVersion(),
                            extensionInstance.getAuthors(),
                            extensionInstance.getDescription()
                    ));
                }

            }
        } catch (Exception e) {
            log.severe("Error while loading extension " + name);
            log.severe(e.getMessage());
            printStacktrace(e);
        }
    }

    public void disableExtension(String name) {
        log.info("Disabling extension " + name);
        registry.getPlugins().removeIf(p -> p.getName().equals(name));

    }

    public void disableExtensions() {
        log.info("Disabling extensions...");
        for (CPlugin p : registry.getPlugins()) {
            p.onBukkitDisable();
            disableExtension(p.getName());
        }
    }


    private void printStacktrace(Exception e) {
        for (StackTraceElement a : e.getStackTrace()) {
            log.severe(a.toString());
        }
    }
}