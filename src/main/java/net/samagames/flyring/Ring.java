package net.samagames.flyring;

import net.samagames.api.SamaGamesAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.util.Vector;

import static java.lang.Math.*;
import static java.lang.Double.*;

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
public class Ring
{
    private static final int BULLETS_NUMBER = 4;

    private Location center;
    private double radius;
    private double angleX;
    private double angleY;
    private double offset;
    private ShulkerBullet[] bullets;

    private Ring(Location center, double radius, double angleX, double angleY)
    {
        this.center = center;
        this.radius = radius;
        this.angleX = angleX * PI / 180;
        this.angleY = angleY * PI / 180;
        this.offset = 0;
        this.bullets = null;
    }

    void display(SFPlugin plugin)
    {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::update, 1, 1);
    }

    private void update()
    {
        if (this.bullets == null)
            this.bullets = new ShulkerBullet[BULLETS_NUMBER];
        double off = this.offset;
        for (int i = 0; i < Ring.BULLETS_NUMBER; i++)
        {
            Location tmp = new Location(this.center.getWorld(), cos(off) * this.radius, sin(off) * this.radius, 0);
            tmp = new Location(this.center.getWorld(), tmp.getX() * cos(this.angleY), tmp.getY(), tmp.getZ() * cos(this.angleY) + tmp.getX() * sin(this.angleY));
            tmp = new Location(this.center.getWorld(), tmp.getX(), tmp.getY() * cos(this.angleX) + tmp.getZ() * sin(this.angleX), tmp.getZ() * cos(this.angleX) - tmp.getY() * sin(this.angleX));
            Location newLocation = this.center.clone().add(tmp);
            if (this.bullets[i] == null || this.bullets[i].isDead())
                this.bullets[i] = (ShulkerBullet)this.center.getWorld().spawnEntity(newLocation, EntityType.SHULKER_BULLET);
            this.bullets[i].teleport(newLocation);
            for (SFPlayer sfPlayer : ((SFGame)SamaGamesAPI.get().getGameManager().getGame()).getInGamePlayers().values())
            {
                Player bukkitPlayer = sfPlayer.getPlayerIfOnline();
                if (bukkitPlayer != null)
                        bukkitPlayer.spawnParticle(sfPlayer.hasCrossedRing(this) ? Particle.VILLAGER_HAPPY : Particle.END_ROD, newLocation, 1, 0, 0, 0, 0.04D);
            }
            this.bullets[i].setVelocity(new Vector(0, 0.03, 0));
            off += 2 * PI / BULLETS_NUMBER;
        }
        this.offset += 2 * PI / 80;
    }

    static Ring str2ring(String str)
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
        Location tmp = location.clone().subtract(this.center);
        tmp = new Location(this.center.getWorld(), tmp.getX() * cos(-this.angleY) - tmp.getZ() * sin(-this.angleY), tmp.getY(), tmp.getZ() * cos(-this.angleY) + tmp.getX() * sin(-this.angleY));
        tmp = new Location(this.center.getWorld(), tmp.getX(), tmp.getY() * cos(-this.angleX) + tmp.getZ() * sin(-this.angleX), tmp.getZ() * cos(-this.angleX) - tmp.getY() * sin(-this.angleX));
        if (tmp.getZ() > 2 || tmp.getZ() < -2)
            return (false);
        tmp = tmp.multiply(1D / this.radius);
        double distance = tmp.distanceSquared(new Location(tmp.getWorld(), 0, 0, 0));
        return (distance <= 1);
    }
}
