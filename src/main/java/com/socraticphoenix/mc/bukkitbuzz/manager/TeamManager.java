package com.socraticphoenix.mc.bukkitbuzz.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.socraticphoenix.mc.bukkitbuzz.AbstractPluginService;
import com.socraticphoenix.mc.bukkitbuzz.BukkitBuzzPlugin;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeamManager extends AbstractPluginService {
    private Map<String, Team> teams = new LinkedHashMap<>();
    private Map<UUID, Team> playerTeams = new HashMap<>();

    public TeamManager(BukkitBuzzPlugin plugin) {
        super(plugin);
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        this.teams.forEach((name, team) -> {
            JsonObject teamObj = new JsonObject();
            JsonArray players = new JsonArray();

            teamObj.addProperty("name", name);
            teamObj.addProperty("color", team.color().name());
            teamObj.addProperty("buzzTimestamp", team.buzzTimestamp());

            team.players().forEach(uuid -> players.add(uuid.toString()));
            teamObj.add("players", players);

            object.add(name, teamObj);
        });
        return object;
    }

    public void load(JsonObject object) {
        this.teams.clear();
        this.playerTeams.clear();

        object.entrySet().forEach(e -> {
            if (e.getValue() instanceof JsonObject) {
                JsonObject teamObj = (JsonObject) e.getValue();
                if (teamObj.has("name") && teamObj.has("color") && teamObj.has("players")) {
                    Team team = new Team(teamObj.get("name").getAsString(), ChatColor.valueOf(teamObj.get("color").getAsString()), new LinkedHashSet<>(),
                            teamObj.get("buzzTimestamp").getAsLong());
                    JsonElement playersElem = teamObj.get("players");
                    if (playersElem instanceof JsonArray) {
                        ((JsonArray) playersElem).forEach(elem -> {
                            UUID id = UUID.fromString(elem.getAsString());
                            team.players().add(id);
                            playerTeams.put(id, team);
                        });
                    }
                    this.teams.put(team.name, team);
                }
            }
        });
    }

    public Collection<Team> getTeams() {
        return this.teams.values();
    }

    public Team getTeam(String name) {
        return this.teams.get(name);
    }

    public Team getPlayerTeam(UUID player) {
        return this.playerTeams.get(player);
    }

    public boolean createTeam(String name, ChatColor color) {
        if (this.teams.containsKey(name)) return false;
        this.teams.put(name, new Team(name, color, new LinkedHashSet<>(), 0));
        return true;
    }

    public boolean removeTeam(String name) {
        Team team = this.teams.remove(name);
        if (team == null) return false;

        this.playerTeams.entrySet().removeIf(e -> e.getValue().name().equals(name));
        this.plugin.gameManager().getGames().forEach(g -> g.teams().remove(team));
        return true;
    }

    public void assignTeam(UUID player, Team team) {
        this.teams.forEach((name, t) -> {
            if (t != team) {
                t.players().remove(player);
            }
        });
        team.players().add(player);
        this.playerTeams.put(player, team);
    }

    public void removeFromTeam(UUID player) {
        this.teams.forEach((k, t) -> t.players.remove(player));
        this.playerTeams.remove(player);
    }

    public static class Team {
        private String name;
        private ChatColor color;
        private Set<UUID> players;
        private long buzzTimestamp;

        public Team(String name, ChatColor color, Set<UUID> players, long buzzTimestamp) {
            this.name = name;
            this.color = color;
            this.players = players;
            this.buzzTimestamp = buzzTimestamp;
        }

        public long buzzTimestamp() {
            return this.buzzTimestamp;
        }

        public Team setBuzzTimestamp(long buzzTimestamp) {
            this.buzzTimestamp = buzzTimestamp;
            return this;
        }

        public String chatName() {
            return this.color + this.name;
        }

        public String name() {
            return this.name;
        }

        public Team setName(String name) {
            this.name = name;
            return this;
        }

        public ChatColor color() {
            return this.color;
        }

        public Team setColor(ChatColor color) {
            this.color = color;
            return this;
        }

        public Set<UUID> players() {
            return this.players;
        }

        public Team setPlayers(Set<UUID> players) {
            this.players = players;
            return this;
        }
    }

}
