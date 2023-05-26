package io.github.shevkomore.smmatter.management.trading;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.map.MinecraftFont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextUIBuilder {
    static final MinecraftFont Font = new MinecraftFont();
    static final int MaxLineWidth = Font.getWidth("LLLLLLLLLLLLLLLLLLL");
    static final int MaxLinesInPage = 14;

    protected ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
    protected BookMeta meta = (BookMeta) book.getItemMeta();
    protected List<BaseComponent> currentPage = new ArrayList<>();
    protected String lastLine = "";
    protected int lineCounter = 0;


    public TextUIBuilder(){
        this("undefined", "undefined");
    }
    public TextUIBuilder(String title, String author){
        meta.setTitle(title);
        meta.setAuthor(author);
    }
    public void addElement(BaseComponent[] element){
        //combine into text
        StringBuilder builder = new StringBuilder();
        for(BaseComponent el: element)
            builder.append(el.toPlainText());
        String text = builder.toString();
        //count the lines
        int count = -1;
        String[] lines = (lastLine+text).split("\n", -1);
        for(String line: lines)
            count += IntRoundUp(Font.getWidth(line), MaxLineWidth);
        //construct page accordingly
        assert count <= MaxLinesInPage;
        if(lineCounter > MaxLinesInPage)
            finishPage();
        Collections.addAll(currentPage, element.clone());
        //prepare for next call
        lastLine = lines[lines.length - 1];
        lineCounter += count;
    }
    public void finishPage(){
        lineCounter = 0;
        BaseComponent[] cloned = new BaseComponent[ currentPage.size() ];
        int i = 0;
        for ( BaseComponent part : currentPage )
            cloned[i++] = part.duplicate();
        meta.spigot().addPage(cloned);
        currentPage.clear();
        lastLine = "";
    }
    public ItemStack finishBook() {
        finishPage();
        ItemStack finished_book = book;
        finished_book.setItemMeta(meta);
        book = new ItemStack(Material.WRITTEN_BOOK);
        meta = (BookMeta) book.getItemMeta();
        return finished_book;
    }
    public static int IntRoundUp(int num, int divisor){
        return (num + divisor - 1) / divisor;
    }
}
