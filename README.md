## Parallel
プレイヤーごとに表示するブロックを変更するためのプラグイン

現在はAPI機能のみ利用可能です


### Tested version
```
1.15.x
```

### How to use
```java
Player player ...;
Block block ...;
        
ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(player);
parallelWorld.setBlock(block, Material.STONE);
```