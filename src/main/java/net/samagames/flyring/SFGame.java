package net.samagames.flyring;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.samagames.api.games.Game;
import net.samagames.api.games.IGameProperties;
import net.samagames.api.games.Status;
import net.samagames.flyring.util.AreaUtils;
import net.samagames.flyring.util.SFPlayerComparator;
import net.samagames.tools.Area;
import net.samagames.tools.Titles;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class SFGame extends Game<SFPlayer>
{
    private SFPlugin plugin;
    private Location spawn;
    private List<Area> walls;
    private List<Ring> rings;
    private Area end;
    private int delay;
    private BukkitTask removeTask;
    private int time;
    private List<SFPlayer> winners;

    SFGame(SFPlugin plugin)
    {
        super("flyring", "FlyRing", "Gare à la chute", SFPlayer.class);
        this.plugin = plugin;

        IGameProperties properties = this.gameManager.getGameProperties();
        this.walls = new ArrayList<>();
        this.rings = new ArrayList<>();
        this.winners = new ArrayList<>();
        this.end = Area.str2area(properties.getConfig("end", new JsonPrimitive("world, 100, 64, 100, 200, 64, 200")).getAsString());

        JsonArray array = properties.getConfig("walls", new JsonArray()).getAsJsonArray();
        array.forEach(json -> this.walls.add(AreaUtils.str2area(json.getAsString())));

        array = properties.getConfig("rings", new JsonArray()).getAsJsonArray();
        array.forEach(json -> this.rings.add(Ring.str2ring(json.getAsString())));

        this.status = Status.STARTING;
        MapLoader mapLoader = new MapLoader(this.plugin);
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.spawn = mapLoader.generate(this.plugin, this.plugin.getServer().getWorlds().get(0)), 1L);
    }

    @Override
    public void handleLogin(Player player)
    {
        super.handleLogin(player);
        player.teleport(this.spawn);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        ItemStack wings = new ItemStack(Material.ELYTRA);
        wings.getItemMeta().spigot().setUnbreakable(true);
        wings.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        player.getInventory().setChestplate(wings);
        player.setMaxHealth(4);
        player.setHealth(4);
    }

    @Override
    public void handleLogout(Player player)
    {
        super.handleLogout(player);
        if (this.status != Status.IN_GAME)
            return ;
        Map<UUID, SFPlayer> playerMap = this.getInGamePlayers();
        if (playerMap.size() == 0)
            this.end();
        else if (playerMap.size() == 1)
        {
            SFPlayer sfPlayer = playerMap.values().iterator().next();
            this.win(sfPlayer, sfPlayer.getPlayerIfOnline());
        }
    }

    @Override
    public void startGame()
    {
        super.startGame();

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta itemMeta = bow.getItemMeta();
        itemMeta.spigot().setUnbreakable(true);
        bow.setItemMeta(itemMeta);
        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        ItemStack arrow = new ItemStack(Material.ARROW);

        for (SFPlayer sfplayer : this.getInGamePlayers().values())
        {
            Player player = sfplayer.getPlayerIfOnline();
            if (player == null)
                continue ;
            else
            {
                player.teleport(this.spawn);
                sfplayer.setSpawn(this.spawn);
                sfplayer.setScoreboard();

                player.getInventory().setItem(0, bow);
                player.getInventory().setItem(35, arrow);
            }
            player.setHealth(4);
        }
        this.delay = 10;
        this.removeTask = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this::removeWalls, 20L, 20L);
        this.rings.forEach(ring -> ring.display(this.plugin));
        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable()
        {
            @Override
            public void run()
            {
                SFGame.this.time++;
                for (SFPlayer arena : SFGame.this.gamePlayers.values())
                    arena.setScoreboardTime(this.formatTime());
            }

            String formatTime()
            {
                int mins = SFGame.this.time / 60;
                int secs = SFGame.this.time - mins * 60;

                String secsSTR = (secs < 10) ? "0" + Integer.toString(secs) : Integer.toString(secs);

                return mins + ":" + secsSTR;
            }
        }, 0L, 20L);
    }

    private void removeWalls()
    {
        this.delay--;
        if (this.delay == 0)
        {
            for (Area wall : this.walls)
            {
                Location min = wall.getMin();
                for (int i = 0; i <= wall.getSizeX(); i++)
                    for (int j = 0; j <= wall.getSizeY(); j++)
                        for (int k = 0; k <= wall.getSizeZ(); k++)
                            min.clone().add(i, j, k).getBlock().setType(Material.AIR);
            }
            this.removeTask.cancel();
        }
        if (this.delay <= 5)
            for (Player player : plugin.getServer().getOnlinePlayers())
            {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                Titles.sendTitle(player, 0, 30, 0, "", ChatColor.GOLD + (this.delay == 0 ? "Sautez et volez !" : "Suppression des murs dans " + this.delay + " secondes"));
            }
    }

    public List<Ring> getRings()
    {
        return this.rings;
    }

    public void win(SFPlayer sfPlayer, Player player)
    {
        if (this.status == Status.FINISHED || this.winners.contains(sfPlayer))
            return ;
        sfPlayer.setOnGround(true);
        this.winners.add(sfPlayer);
        int where = this.winners.size();
        this.coherenceMachine.getMessageManager().writeCustomMessage(player.getDisplayName() + ChatColor.YELLOW + " est arrivé " + where + (where == 1 ? "er" : "e") + ".", true);
        List<SFPlayer> players = this.getInGamePlayers().values().stream().filter(sf -> !sf.isOnGround()).collect(Collectors.toList());
        if (players.size() == 0)
            end();
        else if (where == 1)
        {
            this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Il vous reste 1 minute avant la fin de la partie.", true);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, this::end, 120L);
        }
    }

    private void end()
    {
        if (this.status == Status.FINISHED)
            return ;
        this.status = Status.FINISHED;
        List<SFPlayer> players = this.winners.stream().filter(sf -> sf.getPlayerIfOnline() != null).collect(Collectors.toList());
        if (players.isEmpty())
            this.coherenceMachine.getTemplateManager().getBasicMessageTemplate().execute(Collections.singletonList("Il n'y a aucun gagnant.. :/"));
        else
        {
            Collections.sort(players, new SFPlayerComparator());
            if (players.size() > 2)
                this.coherenceMachine.getTemplateManager().getPlayerLeaderboardWinTemplate().execute(players.get(0).getPlayerIfOnline(), players.get(1).getPlayerIfOnline(), players.get(2).getPlayerIfOnline(), players.get(0).getScore(), players.get(1).getScore(), players.get(2).getScore());
            else if (players.size() > 1)
                this.coherenceMachine.getTemplateManager().getPlayerLeaderboardWinTemplate().execute(players.get(0).getPlayerIfOnline(), players.get(1).getPlayerIfOnline(), null, players.get(0).getScore(), players.get(1).getScore(), 0);
            else
                this.coherenceMachine.getTemplateManager().getPlayerWinTemplate().execute(players.get(0).getPlayerIfOnline(), players.get(0).getScore());
        }
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, this::handleGameEnd, 40L);
    }

    public int getTime()
    {
        return this.time;
    }

    public Location getSpawn()
    {
        return this.spawn;
    }

    public Area getEndArea()
    {
        return this.end;
    }

    void setSpawn(Location spawn)
    {
        this.spawn = spawn;
    }
}
