<div align="center">
    <img src="https://i.imgur.com/xeirIck.png" width=250 height=250 />
</div>

A Werewolf inspired deduction gamemode in Minecraft.

## What is Village?

Village is a Minecraft server plugin that introduces a new gamemode based off <a href="https://en.wikipedia.org/wiki/Mafia_(party_game)">Werewolf</a>, a deduction game similar to Among Us.


## Features

Village is full of features to ensure that gameplay is as smooth as possible.

### The Game

Village follows a very similar flow to Among Us. Players will move around, do tasks, etc. until either a body is found or a meeting is called.
Once a meeting is called, players will be teleported to the meeting teleport to discuss (I recommend using Discord for voice, but chat works fine).
They are able to evict a player by majority vote, or just go onto the next day.

<div align="center">
    <img src="https://i.imgur.com/27i3ojV.png" width=450 height=auto />
</div>

#### Villagers

Villagers are the Crewmates of Village. They are given tasks and can have special roles like Medic (can revive dead bodies), Detective (can kill and check time of death).

<div align="center">
    <img src="https://i.imgur.com/tlSg6NU.png" width=450 height=auto />
</div>

#### Mobs

Mobs are the Impostors of Village. They are supposed to kill every Villager to win. Similar to the Villagers, Mobs are also able to have special roles like Sweeper (hides bodies temporarily) and Wizard (switches locations of people). 


<div align="center">
    <img src="https://i.imgur.com/Gs7MDHJ.png" width=450 height=auto />
</div>

### Editor

Before playing Village, you'll want to have a map you can play in. The Village Editor allows
server operators to place tasks, sabotages, and create vents and link them together.

These editors can be accessed via the `/village task`, `/village sabotage` and `/village vent` commands.

To exit the editor, simply use right click the exit tool or use the command `/village no-edit`.

<div align="center">
    <img src="https://i.imgur.com/35BCQQA.png" width=auto height=250 />
</div>

<div align="center">
    <img src="https://i.imgur.com/Sfvs07E.png" width=auto height=250 />
</div>

<div align="center">
    <img src="https://i.imgur.com/BSoCyMu.png" width=auto height=250 />
</div>


### Sabotages

Introduced in b1.2, Sabotages can be used by Mobs to distract the Villagers and or to win the game.
If a sabotage is left unfixed for 45s, it will end the game, giving the win to the Mobs.

Currently there is two sabotages: Reactor and Stabilizer

#### Reactor

To fix the Reactor, a player must enter a set of codes into it. This is represented by an inventory
with certain items the player must click.

<div align="center">
    <img src="https://i.imgur.com/5SvvTKG.png" width=auto height=250 />
</div>

#### Stabilizer

To fix the Stabilizer, two players must both prevent it from shifting to much. This is done by an inventory
with errors the player must click repeatedly to clear.

<div align="center">
    <img src="https://i.imgur.com/4rjyqmN.png" width=auto height=250 />
</div>

### Tasks

As mentioned before, there are tasks in Village. A lot of these tasks are based off of the tasks found in Among Us; however, there are ones unique to Village like a crafting,
doing simple math, etc. All of these tasks can be placed and removed by server operators to give whatever experience you want.

#### Math Task

A math task consists of two numbers, a and b, and a operand (e.g. +, -, *). The Villager will be
asked to solve a random equation made up of two numbers (-10, 10) and one of the three operators (division isn't included because rounding feels unfair).

Example: `What is -9 * 5?` The Villager will then type the answer in chat (not shown).

#### Trivia Task

A trivia task is a simple trivia question relating to general knowledge and Minecraft. General knowledge is a pretty subjective category, so I focused on Minecraft more, but hopefully it's fine.

Example: `Do pigs fly (yes/no)?` The Villager will then type the answer in chat (not shown).

#### Craft Task

A craft task requires the Villager to craft a specified item. They are given the required materials to craft the item.

Example: `Craft: DIAMOND HOE` The Villager will craft a diamond hoe in any crafting table.

#### Manifold Task

A manifold task requires a Villager to input the correct order of items (represented by lights in an inventory). They have to enter Light 1, Light 2, ..., all the way until Light 9.
If they fail to input the correct order, they fail the task.

#### Upload Task

Similar to Among Us' upload task, each map can have an upload task that every Villager has. It is a two part task where players must copy and upload for 20s total in two different locations.

#### Medical Scan Task

A Villager has to stand in the medical scanner for 10s. While scanning, other Villagers with the same task cannot use the scanner.

#### Clean Vent Task

A Villager has to clean up a vent by clicking on items in an inventory.

#### Custom Task

A custom task is something server operators can add to Village. They are just custom versions
of the three generic tasks but can be made to be whatever you want.

### Debugging

Although a "secondary" feature, Village does offer a developer mode and verbose logging within the global config. Developer mode will ignore certain checks and similar things that would otherwise interfere with testing, make sure this is disabled if you just want to create and play Village. Verbose logging logs a large majority of interactions and things relating to Village, can be useful for debugging and knowing what's going on in-game. 

### Configuration

There is two different configs in Village. The first one is the global config. This is responsible for things like developer mode, verbose logging, or other plugin wide changes.
The other config is individual world/build configs. Server operators are able load and save configs by using the `/village load (id: optional)` and `/village save (id: optional)` commands.
By default, the ID is just the world name, but you can load any config from any world as long as you know the ID (check the `worlds.yml` file). To see all possible configuration options, check `/village` and the generated Village folder.

<div align="center">
    <img src="https://i.imgur.com/RHjyUyJ.png" width=auto height=250 />
</div>


## How do I play Village?

All you need to do is download the latest release of the Village plugin (something like `village-1.x.x.jar`) and then placing it in your Spigot/Paper server's `/plugins` folder.

Once in-game, you are able to setup vents, a spawn and meeting location, tasks, and others. This allows you to create your own maps.

After everything is setup, you can get your friends together and play (or play by yourself, that's how I test it).
