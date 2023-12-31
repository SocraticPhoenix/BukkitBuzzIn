package com.socraticphoenix.mc.bukkitbuzz;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.socraticphoenix.mc.bukkitbuzz.commands.BuzzCommandExecutor;
import com.socraticphoenix.mc.bukkitbuzz.commands.GameCommandExecutor;
import com.socraticphoenix.mc.bukkitbuzz.commands.TeamCommandExecutor;
import com.socraticphoenix.mc.bukkitbuzz.listener.BuzzBlockListener;
import com.socraticphoenix.mc.bukkitbuzz.manager.BuzzBlockManager;
import com.socraticphoenix.mc.bukkitbuzz.manager.GameManager;
import com.socraticphoenix.mc.bukkitbuzz.manager.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BukkitBuzzPlugin extends JavaPlugin {
    public static final UUID CONSOLE_UUID = new UUID(0, 0);

    private static final Gson gson = new Gson();
    private TeamManager teamManager;
    private GameManager gameManager;
    private BuzzBlockManager buzzBlockManager;

    private final Object fileLock = new Object();

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();

        this.teamManager = new TeamManager(this);
        this.gameManager = new GameManager(this);
        this.buzzBlockManager = new BuzzBlockManager();

        this.load();

        this.getCommand("team").setExecutor(new TeamCommandExecutor(this));
        this.getCommand("game").setExecutor(new GameCommandExecutor(this));
        this.getCommand("buzz").setExecutor(new BuzzCommandExecutor(this));
        this.getServer().getPluginManager().registerEvents(new BuzzBlockListener(this), this);

        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    @Override
    public void onDisable() {
        this.save();
    }

    public void load() {
        synchronized (this.fileLock) {
            try {
                File teamConf = new File(this.getDataFolder(), "teams.json");
                if (teamConf.exists()) {
                    JsonObject object = gson.fromJson(new FileReader(teamConf), JsonObject.class);
                    this.teamManager.load(object);
                } else {
                    Files.writeString(teamConf.toPath(), gson.toJson(this.teamManager.save()));
                }
            } catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "FAILED TO load teams.json", ex);
            }

            try {
                File gamesConf = new File(this.getDataFolder(), "games.json");
                if (gamesConf.exists()) {
                    JsonObject object = gson.fromJson(new FileReader(gamesConf), JsonObject.class);
                    this.gameManager.load(object);
                } else {
                    Files.writeString(gamesConf.toPath(), gson.toJson(this.gameManager.save()));
                }
            } catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "FAILED TO load games.json", ex);
            }

            try {
                File buzzBlocksConf = new File(this.getDataFolder(), "buzz_blocks.json");
                if (buzzBlocksConf.exists()) {
                    JsonObject object = gson.fromJson(new FileReader(buzzBlocksConf), JsonObject.class);
                    this.buzzBlockManager.load(object);
                } else {
                    Files.writeString(buzzBlocksConf.toPath(), gson.toJson(this.buzzBlockManager.save()));
                }
            } catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "FAILED TO load buzz_blocks.json", ex);
            }
        }
    }

    public void save() {
        synchronized (this.fileLock) {
            try {
                File teamConf = new File(this.getDataFolder(), "teams.json");
                Files.writeString(teamConf.toPath(), gson.toJson(this.teamManager.save()));
            } catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "FAILED TO SAVE teams.json", ex);
            }

            try {
                File gamesConf = new File(this.getDataFolder(), "games.json");
                Files.writeString(gamesConf.toPath(), gson.toJson(this.gameManager.save()));
            } catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "FAILED TO SAVE games.json", ex);
            }

            try {
                File buzzBlocksConf = new File(this.getDataFolder(), "buzz_blocks.json");
                Files.writeString(buzzBlocksConf.toPath(), gson.toJson(this.buzzBlockManager.save()));
            } catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "FAILED TO SAVE buzz_blocks.json", ex);
            }
        }
    }

    public TeamManager teamManager() {
        return this.teamManager;
    }

    public GameManager gameManager() {
        return this.gameManager;
    }

    public BuzzBlockManager buzzBlockManager() {
        return this.buzzBlockManager;
    }

    public UUID getUuid(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        } else {
            return CONSOLE_UUID;
        }
    }

    public void sendTitleMessage(CommandSender sender, String text, String subtitle) {
        if (sender instanceof Player) {
            ((Player) sender).sendTitle(text, subtitle, 0, 20, 20);
        } else {
            sender.sendMessage(text);
        }
    }

    public String buildLastBuzzedMessage(GameManager.BuzzState state) {
        if (state.buzzIns().isEmpty()) {
            return "No one has buzzed in yet.";
        } else if (state.buzzIns().size() == 1) {
            return tryGetFrom(state.buzzIns().get(0));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("(First) ");
            for (int i = 0; i < state.buzzIns().size(); i++) {
                if (i == state.buzzIns().size() - 1) {
                    sb.append("(Last) ");
                }
                sb.append(this.tryGetFrom(state.buzzIns().get(i)));
                if (i < state.buzzIns().size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }

    private String tryGetFrom(UUID id) {
        Player buzzedPlayer = this.getServer().getPlayer(id);
        TeamManager.Team team = this.teamManager().getPlayerTeam(id);

        if (buzzedPlayer != null && team != null) {
            return buzzedPlayer.getName() + " on team " + team.chatName() + ChatColor.WHITE;
        } else if (buzzedPlayer != null) {
            return buzzedPlayer.getName();
        } else if (team != null) {
            return "Team " + team.chatName() + ChatColor.WHITE;
        } else {
            return "Offline player with no team (buzz-ins may need to be reset)";
        }
    }

    public synchronized void registerBuzz(Player player) {
        long timestamp = System.currentTimeMillis();

        UUID uuid = player.getUniqueId();
        GameManager.Game game = this.gameManager().getPlayerGame(uuid);
        TeamManager.Team team = this.teamManager().getPlayerTeam(uuid);
        if (game != null && team != null) {
            GameManager.BuzzState state = game.buzzState();

            if(state.hasCooldown()) {
                long dif = timestamp - team.buzzTimestamp();
                if (dif < state.cooldownMillis()) {
                    player.sendMessage(ChatColor.RED + "Your team's buzz-in is cooling down! " + ChatColor.GREEN +
                            (TimeUnit.MILLISECONDS.toSeconds(state.cooldownMillis() - dif) + 1) + ChatColor.RED + " second(s) left!");
                    return;
                } else {
                    team.setBuzzTimestamp(timestamp);
                }
            }

            if (state.hasCountdown()) {
                long dif = timestamp - state.startTimestamp();
                if (dif < TimeUnit.SECONDS.toMillis(state.countdownSeconds())) {
                    player.sendMessage(ChatColor.RED + "Countdown to buzz-in is not complete!");
                    return;
                }
            }

            if (state.buzzOn()) {
                if (!state.buzzIns().contains(player.getUniqueId())) {
                    if (team.players().stream().noneMatch(teamMate -> state.buzzIns().contains(teamMate))) {
                        buzz(player, game, team, state);
                    } else {
                        player.sendMessage(ChatColor.RED + "Your team has already buzzed in!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You have already buzzed in!");
                }
            } else if (!state.buzzIns().isEmpty()) {
                player.sendMessage(ChatColor.RED + buildLastBuzzedMessage(state) + " already buzzed in!");
            } else if (state.buzzStarted()) {
                buzz(player, game, team, state);
            } else {
                player.sendMessage(ChatColor.RED + "Buzzing in is not open!");
            }
        } else {
            player.sendMessage(ChatColor.RED + (game == null ? "You are not in a game." : "You are not on a team."));
        }
    }

    private void buzz(Player player, GameManager.Game game, TeamManager.Team team, GameManager.BuzzState state) {
        state.setBuzzStarted(false);
        state.buzzIns().add(player.getUniqueId());

        String message = team.color() + player.getName();
        String subTitle = ChatColor.WHITE + "on team " + team.chatName() + ChatColor.WHITE + " buzzed in!";
        this.gameManager().forEachPlayerExcept(game, game.gameMaster(), p -> this.sendTitleMessage(p, message, subTitle));

        Player gameMaster = this.getServer().getPlayer(game.gameMaster());
        if (gameMaster != null) {
            this.sendTitleMessage(gameMaster, message, subTitle);
        }
    }
}
