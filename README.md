# Village

A Werewolf inspired deduction gamemode in Minecraft.

## What is Village?

Village is a Minecraft server plugin that introduces a new gamemode based off <a href="https://en.wikipedia.org/wiki/Mafia_(party_game)">Werewolf</a>, a deduction game similar to Among Us.

## How does it work?

If you have ever played Werewolf, Mafia, Among Us, etc., the game loop is pretty similar. There is two opposing sides: Villagers and Mobs. The Villagers have to work together to find out who the Mobs are, and get them evicted. Individual Villagers can have a unique role (e.g. medic or detective) that give them buffs; however, Mobs are also able to have special abilities (e.g. cleaning up bodies, placing traps, etc.).

## How do I play Village?

All you need to do is download the latest release of the Village plugin (something like `village-1.x.x.jar`) and then placing it in your Spigot/Paper server's `/plugins` folder.

Once in-game, you are able to setup vents, a spawn and meeting location, and others. This allows you to create your own maps.

After everything is setup, you can get your friends together and play (or play by yourself, that's how I test it);

### TASKS?!

Similar to Among Us, Village does have tasks for the Villagers to do; however, these are not meant to be a win-condition and are pretty simple. Currently, if the Villagers finish all their tasks, they are given compasses that point towards the Mobs.

There are currently three different types of tasks: math (simple equations you need to answer correctly in a time limit), craft (given materials and must craft a specific item), trivia (both general and Minecraft related), and custom (you are able to create your own questions/answers).

## Features

Village is full of features to ensure that gameplay is as smooth as possible

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
