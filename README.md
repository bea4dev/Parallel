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

### maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.Be4rJP</groupId>
    <artifactId>Parallel</artifactId>
    <version>v1.0.2</version>
</dependency>
```