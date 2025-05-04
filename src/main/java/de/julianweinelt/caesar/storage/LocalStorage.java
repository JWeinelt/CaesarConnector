package de.julianweinelt.caesar.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.julianweinelt.caesar.CaesarConnector;
import lombok.Getter;

import java.io.*;
import java.util.logging.Logger;

public class LocalStorage {
    @Getter
    private Configuration data = new Configuration();
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configFile = new File(CaesarConnector.getInstance().getDataFolder(), "config.json");

    private final Logger log = CaesarConnector.getInstance().getLogger();


    public static LocalStorage getInstance() {
        return CaesarConnector.getInstance().getStorage();
    }

    public LocalStorage() {
        CaesarConnector.getInstance().getDataFolder().mkdir();
    }

    public void loadData() {
        log.info("Loading local storage...");
        if (!configFile.exists()) saveData();
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            data = GSON.fromJson(jsonStringBuilder.toString(), new TypeToken<Configuration>(){}.getType());
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }


    public void saveData() {
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(GSON.toJson(data));
        } catch (IOException e) {
            log.severe("Failed to save object: " + e.getMessage());
        }
        log.info("Local storage saved.");
    }
}