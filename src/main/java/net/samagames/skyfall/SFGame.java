package net.samagames.skyfall;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.samagames.api.games.Game;
import net.samagames.api.games.IGameProperties;
import net.samagames.api.games.Status;
import net.samagames.skyfall.util.AreaUtils;
import net.samagames.skyfall.util.SFPlayerComparator;
import net.samagames.tools.Area;
import net.samagames.tools.LocationUtils;
import net.samagames.tools.Titles;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SFGame extends Game<SFPlayer>
{
    private SFPlugin plugin;
    private Location lobby;
    private List<Location> spawns;
    private List<Area> walls;
    private List<Ring> rings;
    private int delay;
    private BukkitTask removeTask;

    public SFGame(SFPlugin plugin)
    {
        super("skyfall", "Skyfall", "Gare à la chute", SFPlayer.class);
        this.plugin = plugin;

        IGameProperties properties = this.gameManager.getGameProperties();
        this.spawns = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.rings = new ArrayList<>();
        this.lobby = LocationUtils.str2loc(properties.getConfig("lobby", new JsonPrimitive("world, 0, 64, 0")).getAsString());

        JsonArray array = properties.getConfig("spawns", new JsonArray()).getAsJsonArray();
        array.forEach(json -> this.spawns.add(LocationUtils.str2loc(json.getAsString())));

        array = properties.getConfig("walls", new JsonArray()).getAsJsonArray();
        array.forEach(json -> this.walls.add(AreaUtils.str2area(json.getAsString())));

        array = properties.getConfig("rings", new JsonArray()).getAsJsonArray();
        array.forEach(json -> this.rings.add(Ring.str2ring(json.getAsString())));
    }

    @Override
    public void handleLogin(Player player)
    {
        super.handleLogin(player);
        player.teleport(this.lobby);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        ItemStack wings = new ItemStack(Material.ELYTRA);
        wings.getItemMeta().spigot().setUnbreakable(true);
        player.getInventory().setChestplate(wings);
        player.setMaxHealth(4);
        player.setHealth(4);
    }

    @Override
    public void handleLogout(Player player)
    {
        super.handleLogout(player);
        SFPlayer sfPlayer = this.getPlayer(player.getUniqueId());
        if (sfPlayer != null)
            sfPlayer.eliminate();
    }

    @Override
    public void startGame()
    {
        super.startGame();
        Iterator<Location> it = this.spawns.iterator();
        for (SFPlayer sfplayer : this.getInGamePlayers().values())
        {
            Player player = sfplayer.getPlayerIfOnline();
            if (player == null)
                continue ;
            if (!it.hasNext())
                this.gameManager.kickPlayer(player, "Plus de place dans la partie. Contactez un administrateur.");
            else
            {
                Location location = it.next();
                player.teleport(location);
                sfplayer.setSpawn(location);
                sfplayer.setScoreboard();
            }
            player.setHealth(4);
        }
        this.delay = 10;
        this.removeTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::removeWalls, 20L, 20L);
        this.rings.forEach(ring -> ring.display(plugin));
        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable()
        {
            private int time = 0;

            @Override
            public void run()
            {
                this.time++;
                for (SFPlayer arena : gamePlayers.values())
                    arena.setScoreboardTime(this.formatTime(time));
            }

            public String formatTime(int time)
            {
                int mins = time / 60;
                int secs = time - mins * 60;

                String secsSTR = (secs < 10) ? "0" + Integer.toString(secs) : Integer.toString(secs);

                return mins + ":" + secsSTR;
            }
        }, 0L, 20L);
    }

    public void removeWalls()
    {
        delay--;
        if (delay == 0)
        {
            for (Area wall : walls)
            {
                Location min = wall.getMin();
                for (int i = 0; i <= wall.getSizeX(); i++)
                    for (int j = 0; j <= wall.getSizeY(); j++)
                        for (int k = 0; k <= wall.getSizeZ(); k++)
                            min.clone().add(i, j, k).getBlock().setType(Material.AIR);//Je crois que Sonar aime pas ça
            }
            this.removeTask.cancel();
        }
        if (delay <= 5)
            for (Player player : plugin.getServer().getOnlinePlayers())
            {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                Titles.sendTitle(player, 0, 30, 0, "", ChatColor.GOLD + (delay == 0 ? "Sautez et volez !" : "Suppression des murs dans " + delay + " secondes"));
            }
    }

    public List<Ring> getRings()
    {
        return rings;
    }

    public void checkPlayers()
    {
        if (this.status == Status.FINISHED)
            return ;
        List<SFPlayer> players = new ArrayList<>();
        this.getInGamePlayers().values().forEach(sfPlayer -> {
            if (!sfPlayer.isEliminated() && !sfPlayer.isOnGround())
                return ;
            if (!sfPlayer.isEliminated() && sfPlayer.isOnline())
                players.add(sfPlayer);
        });
        if (players.isEmpty())
        {
            this.coherenceMachine.getTemplateManager().getBasicMessageTemplate().execute(Arrays.asList("Tout le monde est éliminé.", "Il n'y a aucun gagnant."));
        }
        else
        {
            Collections.sort(players, new SFPlayerComparator());
            this.coherenceMachine.getTemplateManager().getPlayerWinTemplate().execute(players.get(0).getPlayerIfOnline(), players.get(0).getScore());
        }
        this.handleGameEnd();
    }
}
