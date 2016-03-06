package net.samagames.skyfall.util;

import net.samagames.tools.Area;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class AreaUtils
{
    private AreaUtils(){

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
