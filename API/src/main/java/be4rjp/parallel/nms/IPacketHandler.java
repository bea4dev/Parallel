package be4rjp.parallel.nms;

import be4rjp.parallel.player.ParallelPlayer;

public interface IPacketHandler {

    Object rewrite(Object packet, ParallelPlayer parallelPlayer, boolean cacheSetting);

}
