## Parallel
プレイヤーごとに表示するブロックを変更するためのプラグイン

このプラグインは他のプラグインからの呼び出しを前提としています


### Tested version
```
1.15.x, 1.16.x
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
    <version>v1.1.0</version>
    <scope>provided</scope>
</dependency>
```

### 使用例

* 別プラグインからブロックを設置
```java
Player player ...;
Block block ...;
        
ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(player);
parallelWorld.setBlock(block, Material.STONE);
```

* 建築物の状態を変更する

最初にコマンドで建築物と建築物の状態のデータを登録してやる必要があります。

1. まずは別の場所に建築をする


2. 建築物をプラグインから設置したい場所をWorldEditで範囲選択した後「test-structure」という名前をつけて建築物の位置を登録する
```
/parallel structure create test-structure
```

3. 1で作った建築物をWorldEditで選択した後「test-data」という名前をつけて建築物のデータを作成する
```
/parallel structure-data create test-data
```

4. 3を建築物を変化させたい状態のパターンの数だけ繰り返す


5. コマンドで試しに建築物を設置してみる　([player]は建築物の変化を適用したいプレイヤー)
```
/parallel structure set-data test-structure test-data [player]
```

6. 作成した建築物と建築物データの保存(建築物の状態は保存されません)
```
/parallel structure save test-structure

/parallel structure-data save test-data
```

7. プラグインから設置
```java
//建築物の設置を適用するプレイヤー
Player player...;

//「test-structure」という名前の登録された建築物の取得
ParallelStructure parallelStructure = ParallelStructure.getParallelStructure("test-structure");

//「test-data」という名前の建築物データを取得
StructureData structureData = StructureData.getStructureData("test-data");

//設置する
parallelStructure.setStructureData(player, structureData);
```