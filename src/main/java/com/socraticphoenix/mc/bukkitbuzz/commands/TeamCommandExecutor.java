package com.socraticphoenix.mc.bukkitbuzz.commands;

import com.socraticphoenix.mc.bukkitbuzz.AbstractPluginService;
import com.socraticphoenix.mc.bukkitbuzz.BukkitBuzzPlugin;
import com.socraticphoenix.mc.bukkitbuzz.manager.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TeamCommandExecutor extends AbstractPluginService implements CommandExecutor {

    public TeamCommandExecutor(BukkitBuzzPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            String subCommand = args[0];
            if (subCommand.equals("join")) {
                if (sender.hasPermission("bukkitbuzz.player.join")) {
                    if (args.length == 2) {
                        String teamName = args[1];
                        if (sender instanceof Player) {
                            if (teamName.equals("none")) {
                                this.plugin.teamManager().removeFromTeam(((Player) sender).getUniqueId());
                                sender.sendMessage("You have been removed from any teams.");
                            } else {
                                TeamManager.Team team = this.plugin.teamManager().getTeam(teamName);
                                if (team != null) {
                                    this.plugin.teamManager().assignTeam(((Player) sender).getUniqueId(), team);
                                    sender.sendMessage("You joined team " + team.chatName());
                                } else {
                                    sender.sendMessage("Team " + teamName + " does not exist.");
                                }
                            }
                            return true;
                        } else {
                            sender.sendMessage("Only a player can join a team.");
                        }
                    }
                } else {
                    sender.sendMessage("You do not have permission to use that command");
                }
            } else if (subCommand.equals("list")) {
                if (sender.hasPermission("bukkitbuzz.player")) {
                    sender.sendMessage("Current teams are: " + this.plugin.teamManager().getTeams().stream().map(TeamManager.Team::chatName).collect(Collectors.joining(ChatColor.WHITE + ", ")));
                } else {
                    sender.sendMessage("You do not have permission to use that command");
                }
                return true;
            } else if (subCommand.equals("players")) {
                if (args.length == 2) {
                    if (sender.hasPermission("bukkitbuzz.player")) {
                        String teamName = args[1];
                        TeamManager.Team team = this.plugin.teamManager().getTeam(teamName);
                        if (team == null) {
                            sender.sendMessage("Team " + teamName + " does not exist.");
                        } else {
                            List<String> playerNames = team.players().stream().map(u -> this.plugin.getServer().getPlayer(u)).filter(Objects::nonNull).map(Player::getName).collect(Collectors.toList());
                            sender.sendMessage(playerNames.isEmpty() ? "No online players for team " + team.chatName() :
                                    "Online players on " + team.chatName() + ChatColor.WHITE + " are: " + String.join(", ", playerNames));
                        }
                    } else {
                        sender.sendMessage("You do not have permission to use that command");
                    }
                    return true;
                }
            } else if (sender.hasPermission("bukkitbuzz.admin")) {
                if (subCommand.equals("set")) {
                    if (args.length == 3) {
                        String playerName = args[1];
                        String teamName = args[2];

                        Player player = this.plugin.getServer().getPlayer(playerName);
                        if (player != null) {
                            if (teamName.equals("none")) {
                                this.plugin.teamManager().removeFromTeam(player.getUniqueId());
                                sender.sendMessage(playerName + " was removed from any teams.");
                                player.sendMessage("You were removed from any teams.");
                            } else {
                                TeamManager.Team team = this.plugin.teamManager().getTeam(teamName);
                                if (team != null) {
                                    this.plugin.teamManager().assignTeam(player.getUniqueId(), team);
                                    sender.sendMessage(playerName + " has joined team " + team.chatName());
                                    player.sendMessage("You were added to team " + team.chatName());
                                } else {
                                    sender.sendMessage("Team " + teamName + " does not exist.");
                                }
                            }
                        } else {
                            sender.sendMessage("Player " + playerName + " does not exist or is offline.");
                        }
                        return true;
                    }
                } else if (subCommand.equals("create")) {
                    if (args.length == 3) {
                        String teamName = args[1];
                        String colorName = args[2];

                        if (teamName.equals("none")) {
                            sender.sendMessage("none is not a valid team name");
                        } else {
                            ChatColor color;
                            try {
                                color = ChatColor.valueOf(colorName.toUpperCase());
                            } catch (IllegalArgumentException ex) {
                                sender.sendMessage(colorName + " is not recognized as a color, valid colors are: " + Arrays.toString(ChatColor.values()));
                                return false;
                            }

                            if (this.plugin.teamManager().getTeam(teamName) != null) {
                                sender.sendMessage("Team " + teamName + " already exists.");
                            } else {
                                this.plugin.teamManager().createTeam(teamName, color);
                                sender.sendMessage("Created team " + color + teamName + ChatColor.WHITE + ".");
                            }
                            return true;
                        }
                    }
                } else if (subCommand.equals("remove")) {
                    if (args.length == 2) {
                        String teamName = args[1];
                        boolean removed = this.plugin.teamManager().removeTeam(teamName);
                        if (removed) {
                            sender.sendMessage("Removed team " + teamName);
                        } else {
                            sender.sendMessage("Team " + teamName + " does not exist.");
                        }
                        return true;
                    }
                }
            } else {
                sender.sendMessage("You do not have permission to use that command.");
            }
        }
        return false;
    }

}
