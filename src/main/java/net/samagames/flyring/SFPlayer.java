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

/*
 * This file is part of FlyRing.
 *
 * FlyRing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FlyRing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FlyRing.  If not, see <http://www.gnu.org/licenses/>.
 */
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
