package net.samagames.flyring.util;

import net.samagames.flyring.SFPlayer;

import java.util.Comparator;

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
