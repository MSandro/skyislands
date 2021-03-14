# Tatters Skyblock

## Table of Contents
* [Overview](#about)
* [Single Player](#solo)
* [Server Mode](#server)
* [Teams](#teams)
* [Config](#config)
* [Skyblocks](#skyblocks)
* [Commands](#commands)
* [Translations](#translations)
* [License](LICENSE)
* [Minecraft EULA](https://www.minecraft.net/en-us/eula/)
* [Fabric License](https://github.com/FabricMC/fabric-loader/blob/master/LICENSE)
* [Contributors](CONTRIBUTORS)

## About
A skyblock mod for Fabric/Minecraft 1.16.5. With support for single player, server and team play using user defined skyblocks.

The name "Tatters" is a bad pun on the word "Fabric".

Tatters uses the notion of a lobby and satellite skyblocks. The skyblocks are on a 1000x1000 grid by default.

When the first person logs into a world, the lobby will be created at roughly 0,0. This is just a normal skyblock. The world spawn is set to the lobby's location.

NOTE: The lobby is actually created at 8,8. Like all skyblocks, Tatters tries to place them in the centre of a chunk. 

This mod technically doesn't need the fabric-api to run. But if you don't use it, you will have to use a resource pack to get the translations, including the default en-us.

## Solo
To create a skyblock world in single player mode, use the "more options" button in the create new world screen and select world type "Sky Block".

You use the lobby as your skyblock in single player.

## Server
To create a skyblock world for a server, change the "level-type" to "tatters" in server.properties.

If you are just collaborating with friends, you can use the lobby as your skyblock.

For a more public server, it is recommended to add a command block with a button to the lobby that runs the command "tatters home @p".
<br> This will allow non-op users to create new islands and be teleported to them automatically.

## Teams
There is rudimentary support for teams. You first need to create your minecraft teams, e.g. "/team add teamName". 

Then you can use the command "tatters team @p teamName" in a command block (one per team) to have players create and move to their team's skyblock.

## Config
The configuration file tatters.json can be found at mod-pack-root/config/tatters/tatters.json

* spacing - this is the size of the skyblock grid, default 1000
* defaultY - this is the height of the bottom block of the skyblocks, default 80
* skyblock - the name of the skyblock definition file to use in mod-pack-root/config/tatters/skyblocks, default is "default.json"
* lobby - the skyblock definition for the lobby, no value means to use the normal skyblock

## Skyblocks

The skyblock definition files in mod-pack-root/config/tatters/skyblocks allow you to define your own skyblocks. 

Tatters has some predefined skyblocks:
* default.json - a skyblock that should be familiar to modded players
* traditional.json - more like the original vanilla skyblock

The skyblock configuration options are:
* enabled - whether the configuration can be used
* name - a user readable name - not currently used by the code
* layers - this is the layout of the blocks starting from the bottom layer. Each character is a block defined in the mappings, except for space which is an air block and ! marks the spawn point (also an air block)
* mappings - maps a character to a block definition

The layers are a three dimensional array, e.g. 3x3 bedrock below 3x3 dirt from the default.json:

```
  "layers": [
    [
      "bbb",
      "bbb",
      "bbb"
    ],
    [
      "ddd",
      "ddd",
      "ddd"
    ],
etc.
```

The mappings have three elements, only the first is required for simple blocks
* block - the minecraft block id
* properties - key, value pairs that optionally defines the block's state, e.g. the facing direction
* nbt - this defines the internal state of block/tile entities such as the contents of a chest

This is the example chest configuration from traditional.json. The properties say it faces north, it's a single chest and it's not waterlogged. The nbt says it contains a lava bucket in slot 0 and 2 ice blocks in slot 1. 

```
    "c": {
      "block": "minecraft:chest",
      "properties": {
        "facing": "north",
        "type": "single",
        "waterlogged": false
      },
      "nbt": "{Items:[{Slot:0b,id:\"minecraft:lava_bucket\",Count:1b},{Slot:1b,id:\"minecraft:ice\",Count:2b}]}"
    }
```

Some technical notes:

* An easy way to get the properties and nbt for a block is to setup what you want in game and then press F3+i. You can paste the result into a text file.
* Any double quote " in the nbt value must be escaped as \"
* When you define the layers, you don't have to define all the air blocks around the edges. Tatters will try to centre the layers you define when they are smaller than other layers. For this reason is it useful to define your layers as an odd number of blocks to clearly state the centre.
* You will need to define more air blocks around the edge if your design is not symmetrical, e.g. see traditional.json
* An easy way to try your designs in game is to modify the file then use "/tatters regen @p" to force a new skyblock. Any errors will be in mod-pack-root/logs/latest.log
* When the spawn point is not defined for a skyblock (using the ! character) it is calculated as the centre of the skyblock and 1 block above the top layer 

## Commands
Type the command "/tatters help" in game, to see the available commands. Or you can see the help in the translations file below.

The commands are mainly meant for server play, you will need permssion level 2 to use them. You can give access to other players via command blocks.

## Translations
To make your own translation, add a resource pack with an assets/tatters/lang/xx_yy.json
<br>Please feel free to contribute back any translations you make.

[English](src/main/resources/assets/tatters/lang/en_us.json)