package io.github.shevkomore.smmatter.stats;

import io.github.shevkomore.smmatter.PlayerData;
import io.github.shevkomore.smmatter.management.PlayerStatManager;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Describes a class that is used by {@linkplain io.github.shevkomore.smmatter this plugin}
 * to manipulate a player's "stat" (a general concept of something
 * about a player that can be calculated by an integer).
 * Only one instance of this object is created per plugin instance,
 * and said instance is passed to a {@linkplain PlayerStatManager}
 * that correlates it with player files.
 */
public abstract class Stat implements Listener {
    /**
     * A {@linkplain PlayerStatManager} instance this object is {@linkplain PlayerStatManager#addStat passed to}.
     * Used to determine the {@linkplain org.bukkit.plugin.Plugin plugin} instance.
     */
    public PlayerStatManager manager;
    /**
     * Holds a copy of stat values for each player that is online.
     */
    private Map<Player, Integer> playerValues = new HashMap<>();
    //TODO unique End behavior (probably randomly change/swap stats?)

    /**
     * Creates a Stat instance and
     * {@linkplain org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)
     * registers it as an event listener}.
     * @param manager the {@linkplain Stat#manager manager} variable is set to this.
     */
    public Stat(PlayerStatManager manager){
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, manager.plugin);
    }
    public final void implement(Player player, int value){
        int previous_value = getValue(player);
        playerValues.put(player, value);
        apply(player, previous_value); //this way the "value reads differently" stuff won't interfere
    }
    public int getRawValue(Player player){
        if(player.isOnline())
            return playerValues.getOrDefault(player, getDefault());
        throw new IllegalStateException("Cannot get value of offline player: "+player);
    }
    public int getValue(Player player){
        if(player.getWorld().isUltraWarm()) //well, it's just Nether but more generic, so why not?
            return getRawValue(player)
                - getDefault() / 3 - getDefault()%3>0?1:0;
        return getRawValue(player);
    }
    public Set<Player> getPlayers(){ return playerValues.keySet();}
    @EventHandler
    public void update(PlayerChangedWorldEvent e)
    {
        implement(e.getPlayer(), getValue(e.getPlayer()));
    }
    @EventHandler
    public final void removePlayer(PlayerQuitEvent e){
        removePlayer(e.getPlayer());
    }
    public final void removePlayer(Player p){
        playerValues.remove(p);
    }
    protected void apply(Player player, int previous_value) {}
    public String getID(){
        return getName().strip().replaceAll("\\s+", "_");
    }
    public abstract String getTitle();
    public abstract String getName();
    public abstract int getDefault();
    public abstract BaseComponent[] getDisplayText();
}
