package be4rjp.parallel.gui;

import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import be4rjp.parallel.Parallel;
import be4rjp.parallel.ParallelAPI;
import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.player.ParallelPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class UniverseGUI {

    public static void openUniverseGUI(Player player){

        ParallelPlayer parallelPlayer = ParallelPlayer.getParallelPlayer(player);
        if(parallelPlayer == null) return;

        Artist artist = new Artist(() -> {

            ArtButton V = null;

            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());

            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name("&rNext page &7[{NextPage}/{MaxPage}]").build());

            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name("&rPrevious page &7[{PreviousPage}/{MaxPage}]").build());

            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name("&7Back to &r{PreviousName}").build());

            ArtButton L = new ArtButton(new ItemBuilder(Material.BARRIER).name("&b&nLeave from current universe").build())
                    .listener((inventoryClickEvent, menu) -> {
                        parallelPlayer.setUniverse(null);
                        player.closeInventory();
                    });

            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, G,
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, G,
                    V, V, V, V, V, V, V, G, L,
                    V, V, V, V, V, V, V, G, B,
            };
        });

        ArtMenu artMenu = artist.createMenu(Parallel.getPlugin().getArtGUI(), "&nUniverse list");
        artMenu.asyncCreate(menu -> {

            for(ParallelUniverse universe : ParallelAPI.getInstance().getAllUniverse()){
                menu.addButton(new ArtButton(new ItemBuilder(Material.END_PORTAL_FRAME).name(universe.getName()).lore("&7Click to join.").build()).listener((inventoryClickEvent, menu1) -> {
                    parallelPlayer.setUniverse(universe);
                    player.closeInventory();
                    player.sendMessage("§7Switched to §r" + universe.getName());
                }));
            }

        });

        artMenu.open(player);

    }

}
