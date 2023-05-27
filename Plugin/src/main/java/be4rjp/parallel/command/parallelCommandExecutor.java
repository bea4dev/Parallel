package be4rjp.parallel.command;

import be4rjp.parallel.ParallelAPI;
import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.gui.UniverseGUI;
import be4rjp.parallel.player.ParallelPlayer;
import be4rjp.parallel.structure.ParallelStructure;
import be4rjp.parallel.structure.ImplStructureData;
import be4rjp.parallel.structure.StructureData;
import be4rjp.parallel.util.RegionBlocks;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class parallelCommandExecutor implements CommandExecutor, TabExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args == null) return false;
        if (args.length == 0) return false;
    
        if(args[0].equals("structure")){
            if(args.length < 3){
                return false;
            }
        
            //parallel structure set-data [structure-name] [data-name] [player]
            if(args[1].equals("set-data")) {
                if(args.length < 5){
                    return false;
                }
            
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は存在しません。");
                    return true;
                }
            
                ImplStructureData implStructureData = (ImplStructureData) ImplStructureData.getStructureData(args[3]);
                if (implStructureData == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造データは存在しません。");
                    return true;
                }
            
                Player player = Bukkit.getPlayer(args[4]);
                if(player == null){
                    sender.sendMessage(ChatColor.RED + "指定されたプレイヤーが見つかりませんでした。");
                    return true;
                }
            
                parallelStructure.setStructureData(player, implStructureData);
                sender.sendMessage(ChatColor.GREEN + "適用しました。");
                return true;
            }
        
            //parallel structure remove-data [structure-name] [player]
            if(args[1].equals("remove-data")) {
                if (args.length < 4) {
                    return false;
                }
            
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は存在しません。");
                    return true;
                }
            
                Player player = Bukkit.getPlayer(args[3]);
                if(player == null){
                    sender.sendMessage(ChatColor.RED + "指定されたプレイヤーが見つかりませんでした。");
                    return true;
                }
            
                parallelStructure.clearStructureData(player, true);
                sender.sendMessage(ChatColor.GREEN + "適用しました。");
                return true;
            }

            //parallel structure remove [structure-name]
            if(args[1].equals("remove")) {
                if (args.length < 3) {
                    return false;
                }

                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は存在しません。");
                    return true;
                }

                ParallelStructure.getStructureMap().remove(args[2]);
                File file = new File("plugins/Parallel/structures", args[2] + ".yml");
                file.delete();

                sender.sendMessage(ChatColor.GREEN + "削除しました。");
                return true;
            }
        }
    
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはコンソールから実行できません。");
            return true;
        }

        if(args[0].equals("menu")){
            Player player = (Player) sender;
            UniverseGUI.openUniverseGUI(player);

            return true;
        }
    
        if(args[0].equals("join-universe")){
            if(args.length != 2) return false;
            
            String universeName = args[1];
            ParallelUniverse universe = ParallelAPI.getInstance().getUniverse(universeName);
            
            if(universe == null){
                sender.sendMessage("§aThe universe is not found.");
                return true;
            }
    
            ParallelPlayer parallelPlayer = ParallelPlayer.getParallelPlayer((Player) sender);
            if(parallelPlayer == null) return false;
            
            parallelPlayer.setUniverse(universe);
            parallelPlayer.getBukkitPlayer().sendMessage("§7Switched to §r" + universe.getName());
        }
    
        if(args[0].equals("leave-universe")){
            ParallelPlayer parallelPlayer = ParallelPlayer.getParallelPlayer((Player) sender);
            if(parallelPlayer == null) return false;
        
            parallelPlayer.setUniverse(null);
        }
        
        //parallel structure-data create [name]
        if(args[0].equals("structure-data")){
            if(args.length < 3){
                return false;
            }
            if(args[1].equals("create")) {
                Player player = (Player) sender;
                com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
                SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
                LocalSession localSession = sessionManager.get(wePlayer);
    
                com.sk89q.worldedit.world.World selectionWorld = localSession.getSelectionWorld();
                Region region;
                try {
                    if (selectionWorld == null) throw new IncompleteRegionException();
                    region = localSession.getSelection(selectionWorld);
                } catch (IncompleteRegionException ex) {
                    sender.sendMessage(ChatColor.GREEN + "範囲が選択されていません。");
                    return true;
                }
    
                BlockVector3 max = region.getMaximumPoint();
                BlockVector3 min = region.getMinimumPoint();
    
                World world = BukkitAdapter.adapt(region.getWorld());
    
                Vector maxLocation = new Vector(max.getX(), max.getY(), max.getZ());
                Vector minLocation = new Vector(min.getX(), min.getY(), min.getZ());
    
                RegionBlocks regionBlocks = new RegionBlocks(minLocation.toLocation(world), maxLocation.toLocation(world));
    
                ImplStructureData implStructureData = (ImplStructureData) StructureData.getStructureData(args[2]);
                if (implStructureData != null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造データは既に存在しています。");
                    return true;
                }
                implStructureData = new ImplStructureData(args[2]);
    
                implStructureData.setBlockData(minLocation.toLocation(world), regionBlocks.getBlocks());
                sender.sendMessage(ChatColor.GREEN + "作成しました。");
                return true;
            }
    
            if(args[1].equals("save")) {
                ImplStructureData implStructureData = (ImplStructureData) ImplStructureData.getStructureData(args[2]);
                if (implStructureData == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造データは存在しません。");
                    return true;
                }
                
                implStructureData.saveData();
                sender.sendMessage(ChatColor.GREEN + "保存しました。");
                return true;
            }

            if(args[1].equals("remove")) {
                ImplStructureData implStructureData = (ImplStructureData) ImplStructureData.getStructureData(args[2]);
                if (implStructureData == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造データは存在しません。");
                    return true;
                }

                ImplStructureData.getStructureDataMap().remove(args[2]);
                File file = new File("plugins/Parallel/structure_data", args[2] + ".yml");
                file.delete();

                sender.sendMessage(ChatColor.GREEN + "削除しました。");
                return true;
            }
        }
    
        
        if(args[0].equals("structure")){
            if(args.length < 3){
                return false;
            }
            //parallel structure create [name]
            if(args[1].equals("create")) {
                Player player = (Player) sender;
                com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
                SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
                LocalSession localSession = sessionManager.get(wePlayer);
            
                com.sk89q.worldedit.world.World selectionWorld = localSession.getSelectionWorld();
                Region region;
                try {
                    if (selectionWorld == null) throw new IncompleteRegionException();
                    region = localSession.getSelection(selectionWorld);
                } catch (IncompleteRegionException ex) {
                    sender.sendMessage(ChatColor.GREEN + "範囲が選択されていません。");
                    return true;
                }
                
                BlockVector3 min = region.getMinimumPoint();
            
                World world = BukkitAdapter.adapt(region.getWorld());
                
                Vector minLocation = new Vector(min.getX(), min.getY(), min.getZ());
            
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure != null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は既に存在しています。");
                    return true;
                }
                parallelStructure = new ParallelStructure(args[2]);
                parallelStructure.setBaseLocation(minLocation.toLocation(world));
                
                sender.sendMessage(ChatColor.GREEN + "作成しました。");
                return true;
            }
    
            //parallel structure save [name]
            if(args[1].equals("save")) {
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は存在しません。");
                    return true;
                }
    
                parallelStructure.saveData();
                sender.sendMessage(ChatColor.GREEN + "保存しました。");
                return true;
            }
        }
        
        return true;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    
        List<String> list = new ArrayList<>();
    
        if (args.length == 1) {
            list.add("structure-data");
            list.add("structure");
            list.add("menu");
            list.add("join-universe");
            list.add("leave-universe");
        
            return list;
        }
    
        if (args.length == 2) {
            if(args[0].equals("structure")){
                list.add("set-data");
                list.add("remove-data");
                list.add("create");
                list.add("save");
                list.add("remove");
            }
            
            if(args[0].equals("structure-data")){
                list.add("create");
                list.add("save");
                list.add("remove");
            }
            
            if(args[0].equals("join-universe")){
                list.addAll(ParallelAPI.getInstance().getAllUniverseName());
            }
        
            return list;
        }
    
        if (args.length == 3) {
            if(args[0].equals("structure")){
                if(args[1].equals("create")) {
                    list.add("[structure-name]");
                }else{
                    list = new ArrayList<>(ParallelStructure.getStructureMap().keySet());
                }
            }else{
                if(args[1].equals("create")) {
                    list.add("[data-name]");
                }else{
                    list = new ArrayList<>(ImplStructureData.getStructureDataMap().keySet());
                }
            }
        
            return list;
        }
    
        if (args.length == 4) {
            if(args[1].equals("set-data")){
                list = new ArrayList<>(ImplStructureData.getStructureDataMap().keySet());
                return list;
            }
        }
        
        return null;
    }
    
}
