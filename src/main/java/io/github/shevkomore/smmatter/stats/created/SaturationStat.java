package io.github.shevkomore.smmatter.stats.created;

import io.github.shevkomore.smmatter.PlayerData;
import io.github.shevkomore.smmatter.management.PlayerStatManager;
import io.github.shevkomore.smmatter.stats.Stat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class SaturationStat extends Stat {
    protected static String Name = "Saturation";
    protected static BaseComponent[] displayText;
    static{
        displayText = new ComponentBuilder("Saturation")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text("Positive values improve food gain;\n"),
                new Text("Negative values increase food loss.\n"),
                new Text(ChatColor.DARK_PURPLE+"Changes are incredibly drastic. Beware!")))
                .create();
    }
    public SaturationStat(PlayerStatManager manager) {
        super(manager);
    }

    @Override
    public int getValue(Player player){
        if(player.getWorld().isUltraWarm()) //see Stat.getValue
            return getRawValue(player) - 1;
        return getRawValue(player);
    }

    @Override
    public int getDefault() {
        return 0;
    }
    @Override
    public String getTitle(){return Name;}

    @Override
    public String getName() {
        return Name.toLowerCase();
    }

    @Override
    public BaseComponent[] getDisplayText() {
        return displayText;
    }

    @EventHandler
    public void onSaturationDecrease(FoodLevelChangeEvent e){
        if(e.getEntityType() != EntityType.PLAYER) return;
        Player player = (Player) e.getEntity();
        if(getPlayers().contains(player)){
            int delta = e.getFoodLevel() - player.getFoodLevel();
            //if opposite signs, then no change; otherwise multiply delta by value
            delta += Math.copySign(Math.max(delta*getValue(player), 0), getValue(player));
            e.setFoodLevel(player.getFoodLevel() + delta);
        }
    }
}
