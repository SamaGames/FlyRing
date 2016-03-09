package net.samagames.skyfall;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.util.Vector;

import static java.lang.Math.*;
import static java.lang.Double.*;

public class Ring
{
    private static final int BULLETS_NUMBER = 4;

    private Location center;
    private double radius;
    private double angleX;
    private double angleY;
    private double offset;
    private ShulkerBullet[] bullets;

    public Ring(Location center, double radius, double angleX, double angleY)
    {
        this.center = center;
        this.radius = radius;
        this.angleX = angleX;
        this.angleY = angleY;
        this.offset = 0;
        this.bullets = null;
    }

    public void display(SFPlugin plugin)
    {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::update, 1, 1);
    }

    private void update()
    {
        if (bullets == null)
            bullets = new ShulkerBullet[BULLETS_NUMBER];
        double off = offset;
        for (int i = 0; i < BULLETS_NUMBER; i++)
        {
            Location newLocation = center.clone().add(cos(off) * radius, sin(off) * radius, 0);
            if (bullets[i] == null || bullets[i].isDead())
                bullets[i] = (ShulkerBullet)center.getWorld().spawnEntity(newLocation, EntityType.SHULKER_BULLET);
            bullets[i].teleport(newLocation);
            bullets[i].setVelocity(new Vector(0, 0.03, 0));
            off += 2 * Math.PI / BULLETS_NUMBER;
        }
        offset += 2 * Math.PI / 80;
    }

    public static Ring str2ring(String str)
    {
        if (str == null)
            return null;

        String[] location = str.split(", ");

        if (location.length != 7)
            return null;
        return new Ring(new Location(Bukkit.getWorld(location[0]), parseDouble(location[1]), parseDouble(location[2]), parseDouble(location[3])),
                parseDouble(location[4]), parseDouble(location[5]), parseDouble(location[6]));
    }


    public boolean isPlayerInRing(Location location)
    {
        Location tmp = location.clone().subtract(center);
        if (tmp.getZ() > 2 || tmp.getZ() < -2)
            return (false);
        double distance = tmp.distanceSquared(new Location(tmp.getWorld(), 0, 0, 0));
        if (distance > radius * radius)
            return (false);
        return (true);
    }
}
