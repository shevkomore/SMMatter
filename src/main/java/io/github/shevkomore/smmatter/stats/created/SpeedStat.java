package io.github.shevkomore.smmatter.stats.created;

import io.github.shevkomore.smmatter.management.PlayerStatManager;
import io.github.shevkomore.smmatter.stats.Stat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpeedStat extends Stat {
    static String Name = "Speed";
    protected static BaseComponent[] DisplayText;
    static{
        DisplayText = new ComponentBuilder("Speed")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text("Determines the speed of on-ground movement.\n"),
                        new Text(ChatColor.DARK_PURPLE+""+ChatColor.UNDERLINE+""+ChatColor.BOLD+
                                "Negative values indicate reverse directions.")))
                .create();
    }

    public SpeedStat(PlayerStatManager manager) {
        super(manager);
    }

    @Override
    protected void apply(Player player, int previous_value) {
        player.setWalkSpeed((float) Math.tanh(getValue(player) / 100.0));
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
        return 20;
    }

    @Override
    public BaseComponent[] getDisplayText() {
        return DisplayText;
    }
}
