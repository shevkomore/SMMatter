package io.github.shevkomore.smmatter.stats;

import io.github.shevkomore.smmatter.management.PlayerStatManager;
import org.bukkit.entity.Player;

public abstract class UniqueNegativeBehaviorStat extends Stat{
    public UniqueNegativeBehaviorStat(PlayerStatManager manager) {
        super(manager);
    }
    @Override
    public void apply(Player player, int old_value){
        if(getValue(player) > 0) {
            applyPositive(player, old_value);
            return;
        }
        if(getValue(player) < 0) {
            applyNegative(player, old_value);
            return;
        }
        applyZero(player);
    }
    protected abstract void applyPositive(Player player, int value);
    protected abstract void applyNegative(Player player, int value);
    protected abstract void applyZero(Player player);
}
