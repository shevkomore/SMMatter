package io.github.shevkomore.smmatter.management.trading;

import org.bukkit.entity.Player;

import java.util.Objects;

class OfferUsers{
    public final Player sender, receiver;
    public OfferUsers(Player sender, Player receiver){
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfferUsers that = (OfferUsers) o;
        return Objects.equals(sender, that.sender) && receiver.equals(that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, receiver);
    }

    @Override
    public String toString() {
        return "OfferUsers{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}
