package com.socraticphoenix.mc.bukkitbuzz.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.socraticphoenix.mc.bukkitbuzz.AbstractPluginService;
import com.socraticphoenix.mc.bukkitbuzz.BukkitBuzzPlugin;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class GameManager extends AbstractPluginService {
    private Map<String, Game> games = new LinkedHashMap<>();
    private Map<UUID, Set<Game>> gameMasters = new LinkedHashMap<>();
    private Map<UUID, Game> players = new LinkedHashMap<>();

    public GameManager(BukkitBuzzPlugin plugin) {
        super(plugin);
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        this.games.forEach((name, game) -> {
            JsonObject gameObj = new JsonObject();
            JsonArray teams = new JsonArray();

            gameObj.addProperty("name", game.name());
            gameObj.addProperty("gameMaster", game.gameMaster().toString());

            JsonObject buzzStateObj = new JsonObject();
            BuzzState buzzState = game.buzzState();

            JsonArray buzzIns = new JsonArray();
            buzzState.buzzIns.forEach(u -> buzzIns.add(u.toString()));
            buzzStateObj.add("buzzIns", buzzIns);

            buzzStateObj.addProperty("buzzOn", buzzState.buzzOn());
            buzzStateObj.addProperty("buzzStarted", buzzState.buzzStarted());
            buzzStateObj.addProperty("hasCountdown", buzzState.hasCountdown());
            buzzStateObj.addProperty("startTimestamp", buzzState.startTimestamp());
            buzzStateObj.addProperty("countdownSeconds", buzzState.countdownSeconds());
            gameObj.add("buzzState", buzzStateObj);

            game.teams().forEach(team -> teams.add(team.name()));
            gameObj.add("teams", teams);

            object.add(name, gameObj);
        });
        return object;
    }

    public void load(JsonObject object) {
        this.games.clear();
        this.gameMasters.clear();
        this.players.clear();

        object.entrySet().forEach(e -> {
            if (e.getValue() instanceof JsonObject) {
                JsonObject gameObj = (JsonObject) e.getValue();
                if (gameObj.has("name") && gameObj.has("gameMaster") && gameObj.has("buzzState") && gameObj.has("teams")) {

                    JsonObject buzzStateObj = gameObj.getAsJsonObject("buzzState");
                    BuzzState buzzState = new BuzzState(buzzStateObj.get("buzzOn").getAsBoolean(), buzzStateObj.get("buzzStarted").getAsBoolean(),
                            buzzStateObj.get("hasCountdown").getAsBoolean(), buzzStateObj.get("startTimestamp").getAsLong(), buzzStateObj.get("countdownSeconds").getAsInt());

                    JsonElement buzzInsElem = buzzStateObj.get("buzzIns");
                    if (buzzInsElem instanceof JsonArray) {
                        ((JsonArray) buzzInsElem).forEach(elem -> {
                            String uuid = elem.getAsString();
                            buzzState.buzzIns().add(UUID.fromString(uuid));
                        });
                    }

                    Game game = new Game(gameObj.get("name").getAsString(), UUID.fromString(gameObj.get("gameMaster").getAsString()), buzzState, new LinkedHashSet<>());
                    JsonElement teamsElem = gameObj.get("teams");
                    if (teamsElem instanceof JsonArray) {
                        ((JsonArray) teamsElem).forEach(elem -> {
                            String name = elem.getAsString();
                            TeamManager.Team team = this.plugin.teamManager().getTeam(name);
                            if (team != null) {
                                game.teams().add(team);
                                team.players().forEach(player -> this.players.put(player, game));
                            }
                        });
                    }
                    this.gameMasters.computeIfAbsent(game.gameMaster(), k -> new LinkedHashSet<>()).add(game);
                    this.games.put(game.name(), game);
                }
            }
        });
    }

    public Collection<Game> getGames() {
        return this.games.values();
    }

    public Game getGame(String name) {
        return this.games.get(name);
    }

    public Game getPlayerGame(UUID player) {
        return this.players.get(player);
    }

    public Set<Game> getGameMasterGames(UUID gameMaster) {
        return this.gameMasters.computeIfAbsent(gameMaster, k -> new LinkedHashSet<>());
    }

    public void forEachPlayer(Game game, Consumer<Player> consumer) {
        game.teams().forEach(team -> team.players().forEach(uuid -> {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null) {
                consumer.accept(player);
            }
        }));
    }

    public Game getGame(TeamManager.Team team) {
        return this.getGames().stream().filter(g -> g.teams().stream().anyMatch(t -> t.name().equals(team.name()))).findFirst().orElse(null);
    }

    public void addTeam(Game game, TeamManager.Team team) {
        game.teams().add(team);
        team.players().forEach(u -> players.put(u, game));
    }

    public boolean createGame(String name, UUID gameMaster) {
        if (this.games.containsKey(name)) return false;
        Game game = new Game(name, gameMaster, new BuzzState(false, false, false, 0, 3), new LinkedHashSet<>());
        this.games.put(name, game);
        this.gameMasters.computeIfAbsent(gameMaster, k -> new LinkedHashSet<>()).add(game);
        return true;
    }

    public boolean removeGame(String name) {
        Game remove = this.games.remove(name);
        if (remove == null) return false;

        this.gameMasters.computeIfAbsent(remove.gameMaster, k -> new LinkedHashSet<>()).remove(remove);
        this.players.entrySet().removeIf(e -> e.getValue().name().equals(remove.name()));
        return true;
    }

    public static class BuzzState {
        private List<UUID> buzzIns = new ArrayList<>();

        private boolean buzzOn;
        private boolean buzzStarted;

        private boolean hasCountdown;
        private long startTimestamp;
        private int countdownSeconds;

        public BuzzState(boolean buzzOn, boolean buzzStarted, boolean hasCountdown, long startTimestamp, int countdownSeconds) {
            this.buzzOn = buzzOn;
            this.buzzStarted = buzzStarted;
            this.hasCountdown = hasCountdown;
            this.startTimestamp = startTimestamp;
            this.countdownSeconds = countdownSeconds;
        }

        public List<UUID> buzzIns() {
            return buzzIns;
        }

        public void setBuzzIns(List<UUID> buzzIns) {
            this.buzzIns = buzzIns;
        }

        public boolean buzzOn() {
            return this.buzzOn;
        }

        public BuzzState setBuzzOn(boolean buzzOn) {
            this.buzzOn = buzzOn;
            return this;
        }

        public boolean buzzStarted() {
            return this.buzzStarted;
        }

        public BuzzState setBuzzStarted(boolean buzzStarted) {
            this.buzzStarted = buzzStarted;
            return this;
        }

        public boolean hasCountdown() {
            return this.hasCountdown;
        }

        public BuzzState setHasCountdown(boolean hasCountdown) {
            this.hasCountdown = hasCountdown;
            return this;
        }

        public long startTimestamp() {
            return this.startTimestamp;
        }

        public BuzzState setStartTimestamp(long startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        public int countdownSeconds() {
            return this.countdownSeconds;
        }

        public BuzzState setCountdownSeconds(int countdownSeconds) {
            this.countdownSeconds = countdownSeconds;
            return this;
        }
    }

    public static class Game {
        private BuzzState buzzState;
        private String name;
        private UUID gameMaster;
        private Set<TeamManager.Team> teams;

        public Game(String name, UUID gameMaster, BuzzState buzzState, Set<TeamManager.Team> teams) {
            this.name = name;
            this.gameMaster = gameMaster;
            this.teams = teams;
            this.buzzState = buzzState;
        }

        public BuzzState buzzState() {
            return this.buzzState;
        }

        public String name() {
            return this.name;
        }

        public Game setName(String name) {
            this.name = name;
            return this;
        }

        public UUID gameMaster() {
            return this.gameMaster;
        }

        public Game setGameMaster(UUID gameMaster) {
            this.gameMaster = gameMaster;
            return this;
        }

        public Set<TeamManager.Team> teams() {
            return this.teams;
        }

        public Game setTeams(Set<TeamManager.Team> teams) {
            this.teams = teams;
            return this;
        }
    }

}
