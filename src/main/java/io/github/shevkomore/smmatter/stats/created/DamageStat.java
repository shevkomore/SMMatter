package io.github.shevkomore.smmatter.stats.created;

import io.github.shevkomore.smmatter.management.PlayerStatManager;
import io.github.shevkomore.smmatter.stats.Stat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class DamageStat extends Stat {
    protected static String Name = "Damage";
    protected static final int MinDamage = -1;
    protected static final int RadicalMinDamage = -20;
    protected static BaseComponent[] DisplayText;
    static{
        DisplayText = new ComponentBuilder("Damage")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text("Determines the base damage you deal.\n"),
                        new Text("On low values, randomly drop items.\n"),
                        new Text(ChatColor.DARK_PURPLE+""+ChatColor.UNDERLINE+""+ChatColor.BOLD
                                +"Negative values don't heal enemies")))
                .create();
    }
    public DamageStat(PlayerStatManager manager) {
        super(manager);
    }

    @Override
    protected void apply(Player player, int previous_value) {
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(getValue(player));
    }
    @EventHandler
    public void onHoldItem(PlayerItemHeldEvent e){
        Player p = e.getPlayer();
        int v = getValue(p);
        if(v < MinDamage) {
            if (Math.random() < -v / 20.0) {
                ItemStack item = p.getInventory().getItem(e.getNewSlot());
                if(item != null && item.getType() != Material.AIR) {
                    p.getWorld().dropItem(p.getEyeLocation(), item)
                            .setVelocity(Vector.getRandom().subtract(new Vector(0.5f,0.5f,0.5f)).multiply(0.3f));
                    p.getInventory().setItem(e.getNewSlot(), new ItemStack(Material.AIR));
                }
            }
            if(v < RadicalMinDamage){
                p.setFallDistance(-v);
            }
        }
    }

    @Override
    public String getTitle() {
        return Name;
    }

    @Override
    public String getName() {
        return Name.toLowerCase();
    }

    @Override
    public int getDefault() {
        return 1;
    }

    @Override
    public BaseComponent[] getDisplayText() {
        return DisplayText;
    }
}
