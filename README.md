# BukkitBuzzIn
BukkitBuzzIn is a simple plugin for managing players "buzzing in," for things like
gameshows.

## Permissions

| Permission             | Default     | Purpose                                                                   |
|------------------------|-------------|---------------------------------------------------------------------------|
| bukkitbuzz.admin       | op-only     | Allows usage of commands for creating and managing teams and games.       |
| bukkitbuzz.player      | all players | Allows usage of commands for viewing teams and games, and for buzzing in. |
| bukkitbuzz.player.team | all players | Allows usage of the /team join command to join teams.                     |

## Commands

### Teams
Teams are collections of players with a team name. Players can only be in one team at a time.

#### Creating & Removing
| Command                       | Permission       | Purpose                                       |
|-------------------------------|------------------|-----------------------------------------------|
| `/team create <name> <color>` | bukkitbuzz.admin | Creates a team with the given name and color. |
| `/team remove <name>`         | bukkitbuzz.admin | Removes the team with the given name.         |

#### Adding Players to Teams
| Command                     | Permission             | Purpose                                                                                                            |
|-----------------------------|------------------------|--------------------------------------------------------------------------------------------------------------------|
| `/team set <Player> <name>` | bukkitbuzz.admin       | Manually sets a specific player's team. Can also pass "none" as the team name to remove the player from all teams. |
| `/team join <name>`         | bukkitbuzz.player.team | Allows a player to join the specified team. Can also pass "none" as the team name to be removed from all teams.    |


#### Listing Teams
| Command               | Permission        | Purpose          |
|-----------------------|-------------------|------------------|
| `/team list`          | bukkitbuzz.player | Lists all teams. |

### Games
Games are collections of teams with a game name. Teams can only be in one game at a time.

#### Creating & Removing
| Command                          | Permission       | Purpose                                                                                                                                                                       |
|----------------------------------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/game create <name> [teams...]` | bukkitbuzz.admin | Creates a game with the given name, and optionally set the teams to include in the game. If no teams are provided, every team not currently attached to a game will be added. |
| `/game remove <name>`            | bukkitbuzz.admin | Removes the game with the given name.                                                                                                                                         |


#### Listing Games & Teams
| Command              | Permission        | Purpose                                          |
|----------------------|-------------------|--------------------------------------------------|
| `/game teams <name>` | bukkitbuzz.player | Lists the teams that are part of the given game. |
| `/game list`         | bukkitbuzz.player | List all games.                                  |

### Buzzing
There are two modes for buzzing in, as well as two ways to buzz in. To buzz in, a player can either use the `/buzz in` command,
or click a buzz-in block. 

The buzz-in modes define when players can buzz in. 

The On/Off mode, when turned on, allows a player to buzz-in at any point as long as their team is not buzzed in
yet. The list of buzzed-in players can be displayed with `/buzz display`, and will be displayed in order. Use `/buzz reset`
to reset the list of buzzed-in players. You can also start a countdown at any point with `/buzz countdown`, which will only
allow players to buzz-in after a delay. `/buzz countdown` will also reset the list of currently buzzed in players, so
new players can buzz in after the delay.

The Start/Stop mode, when turned on, allows a player to buzz-in only after buzzing in starts, and only if no one else is buzzed in. 
Starting buzzing in can either be triggered with the `/buzz start` command, or it can be started after a delay with the `/buzz countdown` command. 
Both `/buzz start` and `/buzz countdown` reset the currently buzzed in player so a new player can buzz in.

Finally, both modes support cooldowns, which can be set with the `/buzz cooldown` command. A cooldown will prevent a team from buzzing
in again after their last buzz-in. Note that this delay applies even if the last buzz-in was not accepted (because someone already buzzed
in, or because a countdown was not finished). This can help prevent teams simply spamming the buzz-in command/block while
a countdown is active, or before a game master has used `/buzz start`.

#### Buzzing in
One option for buzzing in is to use the command.

| Command                | Permission        | Purpose                                                                                                           |
|------------------------|-------------------|-------------------------------------------------------------------------------------------------------------------|
| `/buzz in`             | bukkitbuzz.player | Buzzes in.                                                                                                        |
| `/buzz buzzed [game]`  | bukkitbuzz.player | Shows you the last player(s) that buzzed in in the current game, or the game specified.                           |
| `/buss display [game]` | bukkitbuzz.admin  | Displays to all players in the game the last player(s) that buzzed in in the current game, or the game specified. |
| `/buzz reset`          | bukkitbuzz.admin  | Resets the currently buzzed in player to be no one.                                                               |


#### Countdowns & Cooldowns

| Command                                      | Permission       | Purpose                                                                                                                                                                          |
|----------------------------------------------|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/buzz countdown [game] <seconds> [display]` | bukkitbuzz.admin | Starts a countdown until buzz-ins are open, for the current game or the game specified. If display is false, the countdown messages will not be displayed to the players.        |
| `/buzz cooldown [game] <milliseconds>`       | bukkitbuzz.admin | Sets the buzz-in cooldown for the current game, or the game specified. Teams can only buzz-in once every X milliseconds. Note this applies even if the buzz-in was not accepted. |

#### Buzz-in On/Off Mode
On/Off mode allows buzzing in at any time, even if someone else has already buzzed in. On/Off mode being enabled overrides Start/Stop mode.

| Command                                      | Permission       | Purpose                                                                                                                                                                                         |
|----------------------------------------------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/buzz on [game]`                            | bukkitbuzz.admin | Enables on/off mode for the current game, or the game specified.                                                                                                                                |
| `/buzz off [game]`                           | bukkitbuzz.admin | Disables on/off mode for the current game, or the game specified.                                                                                                                               |

#### Buzz-in Start/Stop Mode
Start/Stop mode only allows buzzing in after buzzing in has started, and only if no one else is currently buzzed in.

| Command                                      | Permission       | Purpose                                                                                                                                                                                         |
|----------------------------------------------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/buzz start [game]`                         | bukkitbuzz.admin | Starts accepting buzz-ins for the current game, or the game specified.                                                                                                                          |
| `/buzz stop [game]`                          | bukkitbuzz.admin | Stops accepting buzz-ins for the current game, or the game specified.                                                                                                                           |

#### Buzz-in Blocks
Another system for buzzing in is clicking on special buzz-in blocks.

| Command              | Permission        | Purpose                                                       |
|----------------------|-------------------|---------------------------------------------------------------|
| `/buzz block create` | bukkitbuzz.admin  | Designates the block you are looking at as a buzz-in block.   |
| `/buzz block remove` | bukkitbuzz.admin  | Removes the block you are looking at from the buzz-in blocks. |
| `/buzz block test`   | bukkitbuzz.admin  | Tests if the block you are looking at is a buzz-in block.     |
| `/buzz block list`   | bukkitbuzz.player | Lists all buzz-in blocks.                                     |
