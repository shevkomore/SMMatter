package io.github.shevkomore.smmatter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
/**
 * Used as a replacement for {@linkplain Player} in objects that might use username.
 * {@linkplain PlayerData#getPlayer() getPlayer()} and {@linkplain PlayerData#hashCode() hashCode()} use player's name
 * Use {@linkplain PlayerData#getPlayer() getPlayer()} to get a {@linkplain Player} instance.
 * */
public class PlayerData {
    public final String name;
    /**
     * Creates a PlayerData instance using player's {@linkplain Player#getName() name}.
     * @param name A player name, not case-sensitive
     * */
    public PlayerData(String name){
        //small machinations to naturally fix uppercase/lowercase mismatches
        Player p = Bukkit.getPlayer(name);
        if(p == null) {this.name = name;return;}
        this.name = p.getName();
    }

    /**
     * Creates a PlayerData instance using a {@linkplain Player} object. Namely, it copies the player's {@linkplain Player#getName() name}.
     * @param object the {@linkplain Player} instance from which the name is derived.
     */
    public PlayerData(Player object) {
        this.name = object.getName();
    }

    /**
     * Equiivalent to {@linkplain Bukkit#getPlayer(String)}.
     * @return a {@linkplain Player} instance, or {@linkplain null} if a player is not available.
     */
    public Player getPlayer(){
        return Bukkit.getPlayer(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return that.name.equalsIgnoreCase(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name;
    }
}
