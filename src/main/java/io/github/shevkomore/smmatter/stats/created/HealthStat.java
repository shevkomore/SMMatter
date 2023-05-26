package io.github.shevkomore.smmatter.stats.created;

import io.github.shevkomore.smmatter.management.PlayerStatManager;
import io.github.shevkomore.smmatter.stats.UniqueNegativeBehaviorStat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealthStat extends UniqueNegativeBehaviorStat {
    protected static String Name = "Health";
    protected static BaseComponent[] DisplayText;
    static{
        DisplayText = new ComponentBuilder("Health")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text("Determines the amount of red hearts.\n"),
                new Text("Lose your body when below 0\n"),
                new Text(ChatColor.DARK_PURPLE+""+ChatColor.UNDERLINE+""+ChatColor.BOLD+"Do not go below -40")))
                .create();
    }
    public HealthStat(PlayerStatManager manager) {
        super(manager);
    }

    @Override
    protected void applyPositive(Player player, int previous_value) {
        if(player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        player.setHealthScale(getValue(player));
    }

    @Override
    protected void applyNegative(Player player, int previous_value) {
        applyZero(player);
        if(getValue(player) <= -40)
            player.kickPlayer(ChatColor.ITALIC+""+ ChatColor.LIGHT_PURPLE +
                    "Your health has become so low, even your soul cannot exist and dissolves into nothingness.");
    }

    @Override
    protected void applyZero(Player player) {
        player.setHealthScale(0.1);
        player.setGameMode(GameMode.SPECTATOR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 20));
    }

    @Override
    public int getDefault() {
        return 20;
    }
    @Override
    public String getTitle(){
        return Name;
    }
    @Override
    public String getName(){return Name.toLowerCase(); }
    @Override
    public BaseComponent[] getDisplayText() {
        return DisplayText;
    }
}
