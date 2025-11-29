# Village

A Werewolf inspired deduction gamemode in Minecraft.

## What is Village?

Village is a Minecraft server plugin that introduces a new gamemode based off <a href="https://en.wikipedia.org/wiki/Mafia_(party_game)">Werewolf</a>, a deduction game similar to Among Us.


## Features

Village is full of features to ensure that gameplay is as smooth as possible

### The Game

Village follows a very similar flow to Among Us. Players will move around, do tasks, etc. until either a body is found or a meeting is called.
Once a meeting is called, players will be teleported to the meeting teleport to discuss (I recommend using Discord for voice, but chat works fine).
They are able to evict a player by majority vote, or just go onto the next day.

#### Villagers

Villagers are the Crewmates of Village. They are given tasks and can have special roles like Medic (can revive dead bodies), Detective (can scan one player and check time of death).

#### Mobs

Mobs are the Impostors of Village. They are supposed to kill every Villager to win. Like

### Editor

Before playing Village, you'll want to have a map you can play in. The Village Editor allows
server operators to place tasks, create and link vents together, and similar actions.

These editors can be accessed via the `/village task` and `/village vent` commands.

To exit the editor, simply use right click the exit tool or use the command `/village no-edit`.

### Tasks

As mentioned before, there are tasks in Village. These are no necessary to gameplay and do not provide a win condition; however, if there are tasks and all of them are completed, the Villagers will receive compasses that point to closest Mob.

The generic tasks provided to are math (represented by a smithing table), trivia (represented by a lectern), and crafting (represented and uses a crafting table).

#### Math Task

A math task consists of two numbers, a and b, and a operand (e.g. +, -, *). The Villager will be
asked to solve a random equation made up of two numbers (-10, 10) and one of the three operators (division isn't included because rounding feels unfair).

Example: `What is -9 * 5?` The Villager will then type the answer in chat (not shown)

#### Trivia Task

A trivia task is a simple trivia question relating to general knowledge and Minecraft. General knowledge is a pretty subjective category, so I focused on Minecraft more, but hopefully it's fine.

Example: `Do pigs fly (yes/no)?` The Villager will then type the answer in chat (not shown)

#### Craft Task

A craft task requires the Villager to craft a specified item. They are given the required materials to craft the item.

Example: `Craft: DIAMOND HOE` The Villager will craft a diamond hoe in any crafting table

#### Custom Task

A custom task is something server operators can add to Village. They are just custom versions
of the three generic tasks but can be made to be whatever you want.

### Debugging

Although a "secondary" feature, Village does offer a developer mode and verbose loggin with it's config. Developer mode will ignore certain checks and similar things that would otherwise interfere with testing, make sure this is disabled if you just want to create and play Village. Verbose logging logs a large majority of interactions and things realting to Village, can be useful for debugging and knowing what's going on in-game. 


## How do I play Village?

All you need to do is download the latest release of the Village plugin (something like `village-1.x.x.jar`) and then placing it in your Spigot/Paper server's `/plugins` folder.

Once in-game, you are able to setup vents, a spawn and meeting location, tasks, and others. This allows you to create your own maps.

After everything is setup, you can get your friends together and play (or play by yourself, that's how I test it);