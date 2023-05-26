package io.github.shevkomore.smmatter.management;

import io.github.shevkomore.smmatter.PlayerData;
import io.github.shevkomore.smmatter.management.trading.TextUIBuilder;
import io.github.shevkomore.smmatter.management.trading.TradingManager;
import io.github.shevkomore.smmatter.stats.Stat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerStatManager implements Listener, CommandExecutor {
    static String PlayerStatsFolderName = "playerstats";
    TradingManager tradingManager;
    protected static Map<String, Stat> statMap = new HashMap<>();
    public JavaPlugin plugin;
    File statsFolder;
    Map<Player,FileConfiguration> playerStats = new HashMap<>();

    public PlayerStatManager(JavaPlugin plugin, File data_folder) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
        plugin.getCommand("loadstats").setExecutor(this);
        plugin.getCommand("stats").setExecutor(this);

        //setting up inventory GUI
        tradingManager = new TradingManager(this);
        //setting up data folder access
        this.statsFolder = new File(data_folder, PlayerStatsFolderName);
        statsFolder.mkdirs();
    }
    public int getPlayerStatValue(Stat stat, Player player){
        //if possible, use value changed by dimension
        if(player.getPlayer() != null)
            try{
                return stat.getValue(player);
            } catch (IllegalStateException ignored) {}
        FileConfiguration data = playerStats.get(player);
        if (data == null) data = getPlayerFileData(player);
        Object value = data.get(stat.getID());
        if (!(value instanceof Integer)) {
            setDataToDefault(player, stat);
            value = data.get(stat.getID());
        }
        return (int) value;
    }
    public void setPlayerStatValue(Player player, Stat stat, int value){
        FileConfiguration data = playerStats.get(player);
        if(data == null) data = getPlayerFileData(player);
        data.set(stat.getID(), value);
        Player p = player;
        if(p != null) stat.implement(p, value);
        setPlayerFileData(player, data);
    }
    public void changePlayerStatValue(Player player, Stat stat, int change){
        setPlayerStatValue(player, stat, getPlayerStatValue(stat, player)+change);
    }
    public Collection<Stat> getStats(){
        return statMap.values();
    }
    public Stat getStat(String id){
        return statMap.get(id);
    }
    public Set<Player> getActivePlayers(){
        return playerStats.keySet();
    }
    public void terminate(){
        //just in case
        for(Player player: playerStats.keySet())
            setPlayerFileData(player);
        playerStats.clear();
    }
    //EventHandlers and other automatically called methods
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        loadPlayerFileData(e.getPlayer());
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        //just in case; theoretically all player data is updated after each change
        setPlayerFileData(e.getPlayer(), playerStats.remove(e.getPlayer()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(command.getName().equals("loadstats")){
            for(Player p: playerStats.keySet())
                loadPlayerFileData(p);
            return true;
        }
        if(command.getName().equals("stats")){
            if(sender instanceof Player){
                Player p = (Player) sender;
                TextUIBuilder ui = new TextUIBuilder(sender.getName()+"'s stats", "server");
                ui.addElement(new ComponentBuilder(sender.getName()+"'s stats:\n\n")
                        .color(ChatColor.GRAY).underlined(true)
                        .create());
                for(Stat stat: statMap.values()){
                    ui.addElement(new ComponentBuilder().append(stat.getDisplayText())
                            .append(": ", ComponentBuilder.FormatRetention.NONE)
                            .append(String.format("%+d\n",getPlayerStatValue(stat, p)))
                            .create());
                }
                ((Player) sender).openBook(ui.finishBook());
            }
        }
        return false;
    }

    //in-class abstraction methods
    protected void setPlayerFileData(Player player){
        setPlayerFileData(player, playerStats.get(player));
    }
    protected void setPlayerFileData(Player player, FileConfiguration configuration){
        File file = new File(statsFolder, player.getName()+".yml");
        try {
            configuration.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Failed to save data for "+player.getName());
        }
    }
    public void addStat(Stat stat){
        statMap.put(stat.getID(), stat);
    }
    protected FileConfiguration getPlayerFileData(Player player){
        File file = new File(statsFolder, player.getName()+".yml");
        if(!file.exists()) generateDefaultData(player);
        return YamlConfiguration.loadConfiguration(file);
    }
    protected void loadPlayerFileData(Player player){
        playerStats.put(player,getPlayerFileData(player));
        loadPlayerFileData(player, playerStats.get(player));
    }
    protected void loadPlayerFileData(Player player, FileConfiguration data){
        for(Stat stat: statMap.values()) {
            Object value = data.get(stat.getID());
            if(!(value instanceof Integer)) {
                setDataToDefault(player, stat);
                value = data.getInt(stat.getID());
            }
            Player p = player.getPlayer();
            if(p == null)
                plugin.getLogger().severe(
                        "Failed to apply "+ stat.getTitle().toLowerCase()+" change for "+player);
            else stat.implement(player.getPlayer(), (int) data.get(stat.getID()));
        }
    }
    protected void generateDefaultData(Player player) {
        FileConfiguration player_data = new YamlConfiguration();
        for(Stat stat: statMap.values())
            player_data.set(stat.getID(), stat.getDefault());
        setPlayerFileData(player, player_data);
    }
    protected void setDataToDefault(Player player, Stat stat){
        FileConfiguration player_data = playerStats.get(player);
        if(player_data == null) player_data = getPlayerFileData(player);
        player_data.set(stat.getID(), stat.getDefault());
        setPlayerFileData(player, player_data);
    }
}
