package net.samagames.skyfall;

import net.samagames.api.games.GamePlayer;
import org.bukkit.entity.Player;

public class SFPlayer extends GamePlayer
{
    private int score;
    private boolean onGround;
    private boolean eliminated;

    public SFPlayer(Player player)
    {
        super(player);
        this.onGround = false;
        this.score = 0;
    }

    public boolean isOnGround()
    {
        return onGround;
    }

    public int getScore()
    {
        return score;
    }

    public void setOnGround(boolean onGround)
    {
        this.onGround = onGround;
    }

    public void eliminate()
    {
        this.score = 0;
        this.onGround = true;
        this.eliminated = true;
    }

    public boolean isEliminitated()
    {
        return eliminated;
    }

    public void setScore(int score)
    {
        this.score = score;
    }
}
