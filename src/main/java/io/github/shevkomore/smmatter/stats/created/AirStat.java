package io.github.shevkomore.smmatter.stats.created;

import io.github.shevkomore.smmatter.management.PlayerStatManager;
import io.github.shevkomore.smmatter.stats.Stat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class AirStat extends Stat {
    /**
     * The maximum amount of air a player can have
     */
    static int MaxAir = 300;
    /**
     * amount of air at which the air amount is set to 0 (and the player gets suffocation damage)
     */
    static int MinAir = -20;
    /**
     * At which delta value the player starts steadily lose air (the player wouldn't lose air quicker than that)
     */
    static int MaxDelta = -45;
    /**
     * Minimum amount of air that would be shown as max air
     */
    static int MinMaxAir = MaxAir + MaxDelta;
    /**
     * If amount of air must be shown as max air, the delta is saved here to use in the next loop
     */
    protected Map<Player, Integer> airBuffer = new HashMap<>();
    /**
     * The scale by which the height (relative to {@linkplain AirStat#AverageHeight}) is divided to get air delta
     */
    static int HeightAirEffectScaleInverted = 1;
    /**
     * The height relative to which the air delta is calculated (at this height, the air delta should be 0)
     */
    static int AverageHeight = 64;
    /**
     * The scale by which the stat value is multiplied to get air delta
     */
    static int StatAirEffectScale = 2;
    private final BukkitTask airUpdate =
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(Player p: getPlayers()){
                        int delta = calculateAirChange(p.getEyeLocation().getBlockY(), p) + airBuffer.get(p);
                        airBuffer.put(p, 0);
                        int value = p.getRemainingAir() + delta;
                        //if you should suffocate
                        if (value < MinAir){
                            if(p.getGameMode() == GameMode.SURVIVAL
                                    || p.getGameMode() == GameMode.ADVENTURE)
                            {
                                p.damage(1);
                            }
                            p.setRemainingAir(0);
                        }
                        //if you get over saturated
                        else if(value > MaxAir)
                            p.setRemainingAir(MaxAir);
                        //if the air bar would just blink
                        else if(value > MinMaxAir)
                            airBuffer.put(p, delta);
                        //if you lose air
                        else
                            p.setRemainingAir(value);
                        p.setPlayerListFooter("Air: "+p.getRemainingAir()+" | Delta: "+ calculateAirChange(p.getEyeLocation().getBlockY(), p));
                    }
                }
            }.runTaskTimer(manager.plugin, 10, 10);

    protected static String Name = "Air";
    protected static BaseComponent[] DisplayText;
    static{
        DisplayText = new ComponentBuilder("Air")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text("Determines your ability to not lose air.\n"),
                        new Text(ChatColor.DARK_PURPLE+""+ChatColor.UNDERLINE+""+ChatColor.BOLD
                                +"It is harder to breath deep below")))
                .create();
    }
    public AirStat(PlayerStatManager manager) {
        super(manager);
    }
    protected int calculateAirChange(int y_level, Player p){
        int r = (y_level - AverageHeight) / HeightAirEffectScaleInverted + getValue(p) * StatAirEffectScale;
        return Math.max(r, MaxDelta);
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
        return 10;
    }

    @Override
    public BaseComponent[] getDisplayText() {
        return DisplayText;
    }

    @Override
    public void apply(Player player, int previous_value){
        airBuffer.put(player, 0);
    }
}
