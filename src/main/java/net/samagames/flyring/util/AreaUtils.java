package net.samagames.flyring.util;

import net.samagames.tools.Area;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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
public class AreaUtils
{
    private AreaUtils()
    {
    }

    public static Area str2area(String loc)
    {
        if (loc == null)
            return null;

        String[] location = loc.split(", ");

        if (location.length != 8)
            return null;
        return new Area(
                new Location(Bukkit.getServer().getWorld(location[0]), Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3])),
                new Location(Bukkit.getServer().getWorld(location[4]), Double.parseDouble(location[5]), Double.parseDouble(location[6]), Double.parseDouble(location[7]))
        );
    }
}
