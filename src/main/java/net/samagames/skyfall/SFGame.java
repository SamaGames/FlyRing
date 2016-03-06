package net.samagames.skyfall;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.samagames.api.games.Game;
import net.samagames.api.games.IGameProperties;
import net.samagames.skyfall.util.AreaUtils;
import net.samagames.tools.Area;
import net.samagames.tools.LocationUtils;
import net.samagames.tools.Titles;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        this.rings.forEach(ring -> ring.display(plugin));
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
                player.teleport(it.next());
            player.setHealth(4);
        }
        this.delay = 10;
        this.removeTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::removeWalls, 20L, 20L);
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
                Titles.sendTitle(player, 0, 30, 0, ChatColor.GOLD + (delay == 0 ? "Sautez et volez !" : "Suppression des murs dans " + delay + " secondes"), "");
            }
    }
}
