package io.github.shevkomore.smmatter;

import io.github.shevkomore.smmatter.management.PlayerStatManager;
import io.github.shevkomore.smmatter.stats.created.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class SMMatter extends JavaPlugin {
    PlayerStatManager PlayerStatManager;
    FileConfiguration data;
    @Override
    public void onEnable(){
        //find (or create) a file where player stats will be stored
        File f = getDataFolder();
        //create a PlayerStatManager that updates player stats from file
        //(it automatically registers EventHandlers and commands when needed)
        PlayerStatManager = new PlayerStatManager(this, f);

        //Adding all the stats
        PlayerStatManager.addStat(new HealthStat(PlayerStatManager));
        PlayerStatManager.addStat(new SaturationStat(PlayerStatManager));
        PlayerStatManager.addStat(new SpeedStat(PlayerStatManager));
        PlayerStatManager.addStat(new DamageStat(PlayerStatManager));
        PlayerStatManager.addStat(new AirStat(PlayerStatManager));
    }
    @Override
    public void onDisable(){

    }
}
