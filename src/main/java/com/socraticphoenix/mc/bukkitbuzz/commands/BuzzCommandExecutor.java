package com.socraticphoenix.mc.bukkitbuzz.commands;

import com.socraticphoenix.mc.bukkitbuzz.AbstractPluginService;
import com.socraticphoenix.mc.bukkitbuzz.BukkitBuzzPlugin;
import com.socraticphoenix.mc.bukkitbuzz.manager.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BuzzCommandExecutor extends AbstractPluginService implements CommandExecutor {

    public BuzzCommandExecutor(BukkitBuzzPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("bukkitbuzz.player")) {
            if (args.length >= 1) {
                String subCommand = args[0];
                if (subCommand.equals("in")) {
                    if (sender instanceof Player) {
                        this.plugin.registerBuzz((Player) sender);
                        return true;
                    } else {
                        sender.sendMessage("Only Players can buzz in.");
                        return false;
                    }
                } else if (subCommand.equals("block")) {
                    if (args.length == 2) {
                        String mode = args[1];
                        if (mode.equals("list")) {
                            sender.sendMessage("Registered buzz-in blocks are: " + this.plugin.buzzBlockManager().getBuzzBlocks()
                                    .stream().map(l -> l.world + "(" + l.x + ", " + l.y + ", " + l.z + ")").collect(Collectors.joining(", ")));
                        } else if (sender instanceof Player) {
                            if (sender.hasPermission("bukkitbuzz.admin")) {
                                Player player = (Player) sender;
                                Block block = player.getTargetBlockExact(20);
                                if (block != null && !block.getType().isAir()) {
                                    String blockStr = block.getType() + " at " + block.getX() + ", " + block.getY() + ", " + block.getZ();

                                    if (mode.equals("create")) {
                                        if (this.plugin.buzzBlockManager().contains(block)) {
                                            sender.sendMessage(blockStr + " is already a buzz-in block.");
                                        } else {
                                            this.plugin.buzzBlockManager().add(block);
                                            sender.sendMessage("Added " + blockStr + " as a buzz-in block.");
                                        }
                                    } else if (mode.equals("remove")) {
                                        if (this.plugin.buzzBlockManager().contains(block)) {
                                            this.plugin.buzzBlockManager().remove(block);
                                            sender.sendMessage("Removed " + blockStr + "as a buzz-in block.");
                                        } else {
                                            sender.sendMessage(blockStr + " is already not a buzz-in block.");
                                        }
                                    } else if (mode.equals("test")) {
                                        if (this.plugin.buzzBlockManager().contains(block)) {
                                            sender.sendMessage(blockStr + " is a buzz-in block.");
                                        } else {
                                            sender.sendMessage(blockStr + " is not a buzz-in block.");
                                        }
                                    } else {
                                        return false;
                                    }
                                    return true;
                                } else {
                                    sender.sendMessage("You are not looking at a block.");
                                }
                                return true;
                            } else {
                                sender.sendMessage("You do not have permission to use this command.");
                                return false;
                            }
                        } else {
                            sender.sendMessage("Only players can use /buzz block <create|remove|test>");
                            return false;
                        }
                    }
                } else {
                    GameManager.Game targetGame = null;
                    boolean exactGame = false;
                    if (args.length >= 2) {
                        String gameName = args[1];
                        targetGame = this.plugin.gameManager().getGame(gameName);
                        exactGame = targetGame != null;
                    }

                    if (targetGame == null) {
                        UUID id;
                        if (sender instanceof Player) {
                            id = ((Player) sender).getUniqueId();
                        } else {
                            id = BukkitBuzzPlugin.CONSOLE_UUID;
                        }

                        GameManager.Game joined = this.plugin.gameManager().getPlayerGame(id);
                        if (joined == null) {
                            Set<GameManager.Game> games = this.plugin.gameManager().getGameMasterGames(id);
                            if (games.isEmpty()) {
                                sender.sendMessage("You are not a game master of any active games.");
                                return false;
                            } else if (games.size() > 1) {
                                sender.sendMessage("You are a game master of more than one active games, so a game name must be specified.");
                                return false;
                            } else {
                                targetGame = games.iterator().next();
                            }
                        } else {
                            targetGame = joined;
                        }
                    }

                    GameManager.Game finalTargetGame = targetGame;
                    if (sender.hasPermission("bukkitbuzz.admin")) {
                        if (subCommand.equals("on")) {
                            targetGame.buzzState().setLastBuzz(null);
                            targetGame.buzzState().setBuzzOn(true);
                            sender.sendMessage("Enabled buzzing in at anytime for game " + targetGame.name());
                            this.plugin.gameManager().forEachPlayer(targetGame, player -> player.sendMessage(ChatColor.GREEN + "Buzzing in at any time is enabled for game " + finalTargetGame.name()));
                            return true;
                        } else if (subCommand.equals("off")) {
                            targetGame.buzzState().setBuzzOn(false);
                            sender.sendMessage("Disabled buzzing in at anytime for game " + targetGame.name());
                            this.plugin.gameManager().forEachPlayer(targetGame, player -> player.sendMessage(ChatColor.RED + "Buzzing in at any time is disabled for game " + finalTargetGame.name()));
                            return true;
                        } else if (subCommand.equals("start")) {
                            targetGame.buzzState().setLastBuzz(null);
                            targetGame.buzzState().setBuzzStarted(true);
                            targetGame.buzzState().setHasCountdown(false);
                            sender.sendMessage("Buzzing in started for game " + targetGame.name());
                            this.plugin.gameManager().forEachPlayer(targetGame, player -> player.sendMessage(ChatColor.GREEN + "Buzzing has started for game " + finalTargetGame.name()));
                            return true;
                        } else if (subCommand.equals("stop")) {
                            targetGame.buzzState().setBuzzStarted(false);
                            sender.sendMessage("Buzzing in stopped for game " + targetGame.name());
                            this.plugin.gameManager().forEachPlayer(targetGame, player -> player.sendMessage(ChatColor.RED + "Buzzing in has stopped for game " + finalTargetGame.name()));
                            return true;
                        } else if (subCommand.equals("countdown")) {
                            if ((args.length >= 2 && !exactGame) || args.length >= 3) {
                                targetGame.buzzState().setLastBuzz(null);
                                targetGame.buzzState().setBuzzStarted(true);
                                targetGame.buzzState().setHasCountdown(true);

                                String secondsStr = exactGame ? args[2] : args[1];
                                int seconds;
                                try {
                                    seconds = Integer.parseInt(secondsStr);
                                } catch (NumberFormatException ex) {
                                    return false;
                                }

                                boolean display;
                                if (exactGame) {
                                    if (args.length >= 4) {
                                        display = "true".equals(args[3]);
                                    } else {
                                        display = true;
                                    }
                                } else {
                                    if (args.length >= 3) {
                                        display = "true".equals(args[2]);
                                    } else {
                                        display = true;
                                    }
                                }

                                targetGame.buzzState().setCountdownSeconds(seconds);
                                targetGame.buzzState().setStartTimestamp(System.currentTimeMillis());

                                sender.sendMessage("Started countdown buzz for game " + targetGame.name() + " lasting " + seconds + " second(s)");

                                if (display) {
                                    delayBuzzMessage(targetGame, seconds);
                                } else {
                                    this.plugin.gameManager().forEachPlayer(targetGame, p -> p.sendMessage(ChatColor.GREEN + "Buzzing in opens in " + seconds + " second(s)"));
                                }
                                return true;
                            }
                        } else if (subCommand.equals("reset")) {
                            targetGame.buzzState().setLastBuzz(null);
                            sender.sendMessage("Reset buzzing for game " + targetGame.name());
                            return true;
                        } else if (subCommand.equals("display")) {
                            UUID buzzedIn = targetGame.buzzState().lastBuzz();
                            if (buzzedIn != null) {
                                Player player = this.plugin.getServer().getPlayer(buzzedIn);
                                String message;
                                if (player != null) {
                                    message = player.getName() + " on team " + this.plugin.teamManager().getPlayerTeam(buzzedIn).chatName() + ChatColor.WHITE + " buzzed in last!";
                                } else {
                                    message = "Team " + this.plugin.teamManager().getPlayerTeam(buzzedIn).chatName() + ChatColor.WHITE + " buzzed in last!";
                                }
                                this.plugin.gameManager().forEachPlayer(targetGame, p -> p.sendMessage(message));
                            } else {
                                sender.sendMessage("No last buzzed in team recorded for " + targetGame.name());
                            }
                            return true;
                        }
                    }

                    if (sender.hasPermission("bukkitbuzz.player")) {
                        if (subCommand.equals("buzzed")) {
                            UUID buzzedIn = targetGame.buzzState().lastBuzz();
                            if (buzzedIn != null) {
                                Player player = this.plugin.getServer().getPlayer(buzzedIn);
                                String message;
                                if (player != null) {
                                    message = player.getName() + " on team " + this.plugin.teamManager().getPlayerTeam(buzzedIn).chatName() + ChatColor.WHITE + " buzzed in last!";
                                } else {
                                    message = "Team " + this.plugin.teamManager().getPlayerTeam(buzzedIn).chatName() + ChatColor.WHITE + " buzzed in last!";
                                }
                                sender.sendMessage(message);
                            } else {
                                sender.sendMessage("No last buzzed in team recorded for " + targetGame.name());
                            }
                            return true;
                        }
                    } else {
                        sender.sendMessage("You do not have permission to use this command.");
                    }
                }
            }
        }
        return false;
    }

    private void delayBuzzMessage(GameManager.Game game, int countdown) {
        long startTimestamp = System.currentTimeMillis();
        long countdownMillis = TimeUnit.SECONDS.toMillis(countdown);
        new BukkitRunnable() {
            long lastCountdown = countdown + 1;

            @Override
            public void run() {
                long millis = countdownMillis - (System.currentTimeMillis() - startTimestamp);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

                if (millis <= 0) {
                    plugin.gameManager().forEachPlayer(game, p -> p.sendMessage(ChatColor.GREEN + "Buzzing in " + ChatColor.GOLD + "is open!"));
                    this.cancel();
                } else if (seconds != lastCountdown) {
                    lastCountdown = seconds;
                    plugin.gameManager().forEachPlayer(game, p -> p.sendMessage(ChatColor.GREEN + "Buzzing in opens in " + (seconds + 1) + "..."));
                }

            }
        }.runTaskTimer(plugin, 0, countdown);
    }
}
