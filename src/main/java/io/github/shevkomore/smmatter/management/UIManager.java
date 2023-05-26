package io.github.shevkomore.smmatter.management;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
//TODO replace all text with calls to this
public class UIManager {
    FileConfiguration data;
    public UIManager(File file){
        if(!file.exists()) throw new IllegalArgumentException("Missing file");
        data = YamlConfiguration.loadConfiguration(file);
    }
    public String getOrDefault(String path, String default_replacement){
        if(!data.contains(path)) return default_replacement;
        return (String) data.get(path);
    }
}
