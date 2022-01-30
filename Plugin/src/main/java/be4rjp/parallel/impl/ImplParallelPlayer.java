package be4rjp.parallel.impl;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.player.ParallelPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ImplParallelPlayer extends ParallelPlayer {

    public static ParallelPlayer onPlayerJoin(Player player){
        return playerMap.computeIfAbsent(player, ImplParallelPlayer::new);
    }

    public static void onPlayerQuit(Player player){
        ParallelPlayer parallelPlayer = playerMap.get(player);
        parallelPlayer.setUniverse(null);
        playerMap.remove(player);
    }



    private ImplParallelPlayer(Player player) {
        super(player);
    }

    @Override
    public synchronized @Nullable ParallelUniverse getUniverse() {return currentUniverse;}

    @Override
    public synchronized void setUniverse(@Nullable ParallelUniverse parallelUniverse) {
        if(currentUniverse == parallelUniverse) return;

        if(currentUniverse != null){
            ((ImplParallelUniverse) currentUniverse).getPlayers().remove(this);

            ParallelWorld currentWorld = currentUniverse.getWorld(player.getWorld().getName());
            this.currentUniverse = parallelUniverse;

            int range = Bukkit.getViewDistance();

            int chunkX = player.getLocation().getBlockX() >> 4;
            int chunkZ = player.getLocation().getBlockZ() >> 4;

            for(int x = -range; x < range; x++){
                for(int z = -range; z < range; z++){
                    ParallelChunk chunk = currentWorld.getChunk(chunkX + x, chunkZ + z);
                    if(chunk == null) continue;

                    ((ImplParallelChunk) chunk).sendClearPacket(player);
                }
            }
        }
        if(parallelUniverse != null){
            ((ImplParallelUniverse) parallelUniverse).getPlayers().add(this);

            ParallelWorld nextWorld = parallelUniverse.getWorld(player.getWorld().getName());
            this.currentUniverse = parallelUniverse;

            int range = Bukkit.getViewDistance();

            int chunkX = player.getLocation().getBlockX() >> 4;
            int chunkZ = player.getLocation().getBlockZ() >> 4;

            for(int x = -range; x < range; x++){
                for(int z = -range; z < range; z++){
                    ParallelChunk chunk = nextWorld.getChunk(chunkX + x, chunkZ + z);
                    if(chunk == null) continue;

                    chunk.sendUpdate(player);
                }
            }
        }

        this.currentUniverse = parallelUniverse;
    }
    
    public void setUniverseRaw(ParallelUniverse universe){this.currentUniverse = universe;}

}
