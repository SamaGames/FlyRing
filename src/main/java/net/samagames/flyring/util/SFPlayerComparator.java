package net.samagames.flyring.util;

import net.samagames.flyring.SFPlayer;

import java.util.Comparator;

public class SFPlayerComparator implements Comparator<SFPlayer>
{
    @Override
    public int compare(SFPlayer o1, SFPlayer o2)
    {
        if (o2.getScore() != o1.getScore())
            return (o2.getScore() - o1.getScore());
        return (int)(o1.getTime() - o2.getTime());
    }
}
