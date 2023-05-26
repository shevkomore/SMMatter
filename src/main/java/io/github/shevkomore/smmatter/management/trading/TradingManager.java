package io.github.shevkomore.smmatter.management.trading;

import io.github.shevkomore.smmatter.PlayerData;
import io.github.shevkomore.smmatter.management.PlayerStatManager;
import io.github.shevkomore.smmatter.stats.Stat;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TradingManager implements CommandExecutor {

    public PlayerStatManager statManager;
    Map<OfferUsers, Offer> sentOffers = new HashMap<>();
    Map<Player, Offer> editedOffers = new HashMap<>();
    public TradingManager(PlayerStatManager manager) {
        this.statManager = manager;
        manager.plugin.getCommand("offer").setExecutor(this);
        manager.plugin.getCommand("editoffer").setExecutor(this);
    }
    public void sendOfferMessage(OfferUsers users){
        String senderName = users.sender.toString();
        ComponentBuilder message = new ComponentBuilder(users.sender+" has sent you a new offer.\n")
                .append(ChatColor.UNDERLINE+"Click here")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/offer view "+senderName))
                .append(ChatColor.RESET+" or type " + "/offer view " + senderName +" to see it.");
        users.receiver.getPlayer().spigot().sendMessage(message.create());
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equals("offer")) {
                if (args.length == 1) {
                    if (args[0].equals("send")) {
                        player.getPlayer().openBook(getReceiverListBookView(player));
                    }
                    if (args[0].equals("view")) {
                        player.getPlayer().openBook(getOfferListBookView(player));
                    }
                    return true;
                }
                if (args.length > 1) {
                    //commands for creating offers
                    Player other = statManager.plugin.getServer().getPlayer(args[1]);
                    if (!statManager.getActivePlayers().contains(other)) {
                        sender.sendMessage("Cannot find player " + other);
                        return true;
                    }
                    OfferUsers users;
                    if (args[0].equals("send")) {
                        users = new OfferUsers(player, other);
                        finishOffer(users, editedOffers.remove(player));
                        editedOffers.put(player, new Offer(this));
                        sendOfferMessage(users);
                        sender.sendMessage("Offer sent to " + other);
                        return true;
                    }
                    //commands for received offers
                    users = new OfferUsers(other, player);
                    if (!sentOffers.containsKey(users)) {
                        sender.sendMessage(ChatColor.DARK_RED+"No offers found from " + other);
                        return true;
                    }
                    if (args[0].equals("view")) {
                        openSentOfferToReceiver(users);
                        return true;
                    }
                    if (args[0].equals("accept")) {
                        sentOffers.remove(users).resolve(users);
                        return true;
                    }
                }

            }
            if (command.getName().equals("editoffer")) {
                if (args.length < 2) {
                    openEditedOfferToSender(player);
                    return true;
                }
                if (args.length >= 3){
                    Offer offer = editedOffers.get(player);
                    if(offer == null){
                        editedOffers.put(player, new Offer(this));
                        offer = editedOffers.get(player);
                    }
                    Stat stat = statManager.getStat(args[1]);
                    if(stat == null){
                        sender.sendMessage("Cannot find stat with ID \""+args[1]+"\"");
                        return false;
                    }
                    int value;
                    try {
                        value = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e){
                        sender.sendMessage("\""+args[2]+"\" is not a number.");
                        return false;
                    }
                    if(args[0].equals("set")){
                        offer.set(stat, value);
                        openEditedOfferToSender(player);
                        return true;
                    }
                    if(args[0].equals("change")){
                        offer.set(stat, offer.get(stat)+value);
                        openEditedOfferToSender(player);
                        return true;
                    }
                }
            }
        } else sender.sendMessage("Offer creation commands don't work with non-player entities");
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        editedOffers.put(e.getPlayer(), new Offer(this));
    }
    @EventHandler
    public void onPayerLeave(PlayerQuitEvent e){
        editedOffers.remove(e.getPlayer());
    }
    protected void finishOffer(OfferUsers users, Offer offer){
        sentOffers.put(users, offer);
    }
    protected void openSentOfferToReceiver(OfferUsers users){
        Offer offer = sentOffers.get(users);
        if(offer == null) return;
        users.receiver.getPlayer().openBook(offer.getReceiverBookView(users));
    }
    protected void openEditedOfferToSender(Player sender){
        Offer offer = editedOffers.get(sender);
        if(offer == null) {
            editedOffers.put(sender, new Offer(this));
            offer = editedOffers.get(sender);
        }
        sender.getPlayer().openBook(offer.getSenderBookView(sender));
    }

    protected ItemStack getOfferListBookView(Player player) {
        TextUIBuilder ui = new TextUIBuilder("Offer list", "server");
        ui.addElement(new ComponentBuilder("List of offers sent to you:\n\n")
                .color(ChatColor.DARK_GRAY.asBungee())
                .create());
        for(Player p: statManager.getActivePlayers()){
            OfferUsers users = new OfferUsers(p, player);
            if(sentOffers.containsKey(users)){
                ui.addElement(new ComponentBuilder("Offer from "+p+"\n")
                        .underlined(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/offer view "+p)))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/offer view "+p))
                        .create());
            }
        }
        return ui.finishBook();
    }

    protected ItemStack getReceiverListBookView(Player player) {
        TextUIBuilder ui = new TextUIBuilder("Player list", "server");
        ui.addElement(new ComponentBuilder("Choose a player to send the offer to:\n\n")
                .color(ChatColor.GRAY.asBungee())
                .create());
        for(Player p: statManager.getActivePlayers()){
            if(p.equals(player)) continue;
            ui.addElement(new ComponentBuilder(p+"\n")
                    .underlined(true)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/offer send "+p)))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/offer send "+p))
                    .create());
        }
        return ui.finishBook();
    }
}
