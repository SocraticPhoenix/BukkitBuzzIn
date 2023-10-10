package com.socraticphoenix.mc.bukkitbuzz.commands;

import com.socraticphoenix.mc.bukkitbuzz.AbstractPluginService;
import com.socraticphoenix.mc.bukkitbuzz.BukkitBuzzPlugin;
import com.socraticphoenix.mc.bukkitbuzz.manager.GameManager;
import com.socraticphoenix.mc.bukkitbuzz.manager.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Collectors;

public class GameCommandExecutor extends AbstractPluginService implements CommandExecutor {

    public GameCommandExecutor(BukkitBuzzPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            String subCommand = args[0];
            if (args.length >= 2) {
                if (subCommand.equals("teams")) {
                    if (sender.hasPermission("bukkitbuzz.player")) {
                        if (args.length == 2) {
                            String gameName = args[1];
                            GameManager.Game game = this.plugin.gameManager().getGame(gameName);
                            if (game != null) {
                                sender.sendMessage("Game " + gameName + " has the following teams: " +
                                        game.teams().stream().map(TeamManager.Team::chatName).collect(Collectors.joining(ChatColor.WHITE + ", ")));
                            } else {
                                sender.sendMessage("Game " + gameName + " does not exist.");
                            }
                            return true;
                        }
                    } else {
                        sender.sendMessage("You do not have permission to use that command.");
                    }
                } else if (sender.hasPermission("bukkitbuzz.admin")) {
                    if (subCommand.equals("create")) {
                        String gameName = args[1];
                        UUID gameMaster;
                        if (sender instanceof Player) {
                            gameMaster = ((Player) sender).getUniqueId();
                        } else {
                            gameMaster = BukkitBuzzPlugin.CONSOLE_UUID;
                        }

                        boolean created = this.plugin.gameManager().createGame(gameName, gameMaster);
                        if (created) {
                            GameManager.Game game = this.plugin.gameManager().getGame(gameName);
                            if (args.length > 2) {
                                for (int i = 2; i < args.length; i++) {
                                    String teamName = args[i];
                                    TeamManager.Team team = this.plugin.teamManager().getTeam(teamName);
                                    if (team == null) {
                                        sender.sendMessage("Team " + teamName + " does not exist, creating game without it.");
                                    } else {
                                        GameManager.Game existing = this.plugin.gameManager().getGame(team);
                                        if (existing != null) {
                                            sender.sendMessage("Team " + teamName + " is already part of game " + existing.name() + ", creating game without it.");
                                        } else {
                                            this.plugin.gameManager().addTeam(game, team);
                                        }
                                    }
                                }
                            } else {
                                sender.sendMessage("No teams specified, creating game with all available teams.");
                                this.plugin.teamManager().getTeams().forEach(team -> {
                                    if (this.plugin.gameManager().getGame(team) == null) {
                                        this.plugin.gameManager().addTeam(game, team);
                                    }
                                });
                            }
                            sender.sendMessage("Created game " + gameName + " with " + game.teams().size() + " team(s)");
                        } else {
                            sender.sendMessage("Game " + gameName + " already exists.");
                        }
                        return true;
                    } else if (subCommand.equals("remove")) {
                        if (args.length == 2) {
                            String gameName = args[1];
                            boolean removed = this.plugin.gameManager().removeGame(gameName);
                            if (removed) {
                                sender.sendMessage("Game " + gameName + " was removed.");
                            } else {
                                sender.sendMessage("Game " + gameName + " does not exist.");
                            }
                            return true;
                        }
                    }
                } else {
                    sender.sendMessage("You do not have permission to use that command.");
                }
            } else if (subCommand.equals("list")) {
                if (sender.hasPermission("bukkitbuzz.player")) {
                    sender.sendMessage("Current games are: " +
                            this.plugin.gameManager().getGames().stream().map(GameManager.Game::name).collect(Collectors.joining(", ")));
                    return true;
                } else {
                    sender.sendMessage("You do not have permission to use that command.");
                }
            }
        }
        return false;
    }

}
