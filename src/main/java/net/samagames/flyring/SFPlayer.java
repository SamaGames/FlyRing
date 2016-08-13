package net.samagames.flyring;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamePlayer;
import net.samagames.tools.scoreboards.ObjectiveSign;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SFPlayer extends GamePlayer
{
    private int score;
    private boolean onGround;
    private List<Ring> rings;
    private Location spawn;
    private ObjectiveSign objective;
    private SFGame game;
    private long time;

    public SFPlayer(Player player)
    {
        super(player);
        this.onGround = false;
        this.score = 0;
        this.rings = new ArrayList<>();
        this.spawn = null;
        this.objective = new ObjectiveSign("flyring", ChatColor.AQUA + "FlyRing" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");

        this.game = (SFGame)SamaGamesAPI.get().getGameManager().getGame();
        this.updateScoreboard();
    }

    private void updateScoreboard()
    {
        Collection<SFPlayer> players = this.game.getInGamePlayers().values();
        this.objective.setLine(0, " ");
        this.objective.setLine(1, ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + players.size());
        this.objective.setLine(2, ChatColor.GRAY + "Score : " + ChatColor.WHITE + this.score);
        this.objective.setLine(3, "   ");
        this.objective.updateLines();
    }

    void setScoreboard()
    {
        this.objective.addReceiver(this.getOfflinePlayer());
    }

    void setScoreboardTime(String time)
    {
        this.objective.setDisplayName(ChatColor.AQUA + "FlyRing" + ChatColor.WHITE + " | " + ChatColor.AQUA + time);
        this.updateScoreboard();
    }

    public boolean isOnGround()
    {
        return this.onGround;
    }

    public int getScore()
    {
        return this.score;
    }

    void setOnGround(boolean onGround)
    {
        this.onGround = onGround;
    }

    public void eliminate()
    {
        this.score = 0;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public boolean hasCrossedRing(Ring ring)
    {
        return this.rings.contains(ring);
    }

    public void crossRing(Ring ring)
    {
        this.rings.add(ring);
    }

    void setSpawn(Location location)
    {
        this.spawn = location;
    }

    public Location getSpawn()
    {
        return this.spawn;
    }

    public void setEndTime(long time)
    {
        this.time = time;
    }

    public long getTime()
    {
        return this.time;
    }
}
