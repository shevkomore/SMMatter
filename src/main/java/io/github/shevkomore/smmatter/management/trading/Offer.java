package io.github.shevkomore.smmatter.management.trading;

import io.github.shevkomore.smmatter.PlayerData;
import io.github.shevkomore.smmatter.stats.Stat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

class Offer {
    TradingManager manager;
    Map<Stat, Integer> receiverChanges = new HashMap<>();
    protected Offer(TradingManager ui_manager) {
        this.manager = ui_manager;
        for(Stat stat: manager.statManager.getStats())
            set(stat, 0);
    }
    public void set(Stat stat, int new_value){
        receiverChanges.put(stat, new_value);
    }
    public int get(Stat stat){
        return receiverChanges.get(stat);
    }

    public ItemStack getReceiverBookView(OfferUsers users) {
        TextUIBuilder ui = new TextUIBuilder("Offer from " + users.sender, users.sender.toString());
        ui.addElement(new ComponentBuilder("Offer from " + users.sender + ":\n\n")
                .color(ChatColor.DARK_GRAY.asBungee())
                .create());
        ComponentBuilder element;
        for (Stat stat : receiverChanges.keySet()) {
            if(receiverChanges.get(stat) == 0) continue;
            ui.addElement(new ComponentBuilder().append(stat.getDisplayText())
                    .append(": ", ComponentBuilder.FormatRetention.NONE)
                    .append(getNumberDisplayReceiver(stat, users))
                    .append("\n")
                    .create());
        }
        ui.addElement(new ComponentBuilder("\nAccept").underlined(true).bold(true).color(ChatColor.GREEN.asBungee())
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/offer accept " + users.sender))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new Text("/offer accept " + users.sender
                                        + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC
                                        + "\nOffer will close and its terms will apply immediately")))
                        .create());
        ui.addElement(new ComponentBuilder("\nThis offer will be available until replaced")
                .color(ChatColor.GRAY.asBungee()).italic(true)
                .create());
        return ui.finishBook();
    }
    public ItemStack getSenderBookView(Player sender) {
        TextUIBuilder ui = new TextUIBuilder("Offer", sender.toString());
        ui.addElement(new ComponentBuilder("Offer Editor\n\n")
                .color(ChatColor.DARK_GRAY.asBungee())
                .create());
        ComponentBuilder element;
        for(Stat el: receiverChanges.keySet()){
            ui.addElement(new ComponentBuilder().append(el.getDisplayText())
                    .append(": ")
                    .append(new ComponentBuilder(" - ").bold(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new Text("/editoffer "+el.getID()+" change -1"
                                       + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC
                                       + (receiverChanges.get(el)<=0
                                            ?"\nYou will get 1 more "+el.getName()+" from this offer"
                                            :"\nYou will lose 1 less "+el.getName()+" from this offer"))))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/editoffer change "+el.getID()+" -1"))
                            .create())
                    .append(getNumberDisplaySender(el, sender))
                    .append(new ComponentBuilder(" + \n").bold(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text("/editoffer change "+el.getID()+" 1"
                                    + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC
                                    + (receiverChanges.get(el)>=0
                                            ?"\nYou will lose 1 more "+el.getName()+" from this offer"
                                            :"\nYou will get 1 less "+el.getName()+" from this offer"))))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/editoffer change "+el.getID()+" 1"))
                        .create())
                    .create());
        }
        ui.addElement(new ComponentBuilder("\n")
                .append(new ComponentBuilder("Send*")
                        .bold(true).color(ChatColor.GOLD.asBungee())
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new Text("/offer send")))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/offer send"))
                        .create())
                .create());
        ui.addElement(new ComponentBuilder("\n* You won't be able to change it afterwards!")
                .italic(true).color(ChatColor.DARK_RED.asBungee()).create());
        return ui.finishBook();
    }

    public void resolve(OfferUsers users) {
        for (Stat s: receiverChanges.keySet()) {
            takeFrom(s, users.sender);
            giveTo(s, users.receiver);
        }
        users.sender.getPlayer().sendMessage("Exchange successful with "+users.receiver+"!");
        users.sender.getPlayer().spigot().sendMessage(new ComponentBuilder("Type ")
                .append("/stats")
                .underlined(true)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats"))
                .append(" to see your stats.", ComponentBuilder.FormatRetention.NONE)
                .create());
        users.receiver.getPlayer().sendMessage("Exchange successful with "+users.sender+"!");
        users.receiver.getPlayer().spigot().sendMessage(new ComponentBuilder("Type ")
                .append("/stats")
                .underlined(true)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats"))
                .append(" to see your stats.", ComponentBuilder.FormatRetention.NONE)
                .create());
    }

    public BaseComponent[] getNumberDisplaySender(Stat stat, Player sender) {
        int curr_value = manager.statManager.getPlayerStatValue(stat, sender);
        int change = receiverChanges.get(stat);
        return new ComponentBuilder(String.format("%+d", change))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text("/editoffer "+stat.getID()+" set <number>"),
                        new Text(ChatColor.RESET + "\nCurrently: " + ChatColor.BOLD + curr_value),
                        new Text(ChatColor.RESET + "\nAfterwards: " + ChatColor.BOLD + (curr_value - change))))
                .create();
    }
    public BaseComponent[] getNumberDisplayReceiver(Stat stat, OfferUsers users){
        int curr_value = manager.statManager.getPlayerStatValue(stat, users.receiver);
        int change = receiverChanges.get(stat);
        return new ComponentBuilder(String.format("%+d", change))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text(ChatColor.RESET + "Currently: " + ChatColor.BOLD + curr_value),
                        new Text(ChatColor.RESET + "\nAfterwards: " + ChatColor.BOLD + (curr_value + change))))
                .create();
    }
    public void takeFrom(Stat stat, Player player) {
        manager.statManager.changePlayerStatValue(player, stat, -receiverChanges.get(stat));
    }
    public void giveTo(Stat stat, Player player) {
        manager.statManager.changePlayerStatValue(player, stat, receiverChanges.get(stat));
    }
}
