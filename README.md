## Parallel-v2
プレイヤーごとに表示するブロックを変更するためのプラグイン

このプラグインは他のプラグインからの呼び出しを前提としています


### Supported version
```
1.15.2, 1.16.5
```

### maven
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://raw.github.com/Be4rJP/Parallel/mvn-repo/</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>be4rjp</groupId>
    <artifactId>parallel_api</artifactId>
    <version>2.0.2</version>
    <scope>provided</scope>
</dependency>
```

### 使用例

* 別プラグインからブロックを設置
```java
Player player = ...;
Block block = ...;

//APIのインスタンスを取得
ParallelAPI api = ParallelAPI.getInstance();

//Universeを作成
ParallelUniverse universe = api.createUniverse("TestUniverse");

//ParallelPlayerを取得
ParallelPlayer parallelPlayer = api.getParallelPlayer(player);
if(parallelPlayer == null) return;

//作成したuniverseにプレイヤーを参加させる
universe.addPlayer(parallelPlayer);

//作成したuniverse内のParallelWorldを取得
ParallelWorld parallelWorld = universe.getWorld(player.getWorld().getName());

//指定された座標にブロックをセット
parallelWorld.setType(block.getX(), block.getY(), block.getZ(), Material.REDSTONE_BLOCK);

//ブロックの変更をプレイヤーに通知
parallelWorld.sendBlockChange(block.getX(), block.getY(), block.getZ());
```
