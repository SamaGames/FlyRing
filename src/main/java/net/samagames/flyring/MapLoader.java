package net.samagames.flyring;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import net.samagames.api.games.Status;
import net.samagames.tools.LocationUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("deprecation")
class MapLoader
{
    private Cuboid start;
    private List<Cuboid> straightCuboids;
    private List<Cuboid> curvedCuboids;
    private Location end;

    MapLoader(SFPlugin plugin)
    {
        this.straightCuboids = new ArrayList<>();
        this.curvedCuboids = new ArrayList<>();
        File folder = plugin.getDataFolder();
        File[] files = folder.listFiles();
        JsonObject offsets = plugin.getApi().getGameManager().getGameProperties().getConfig("offsets", new JsonObject()).getAsJsonObject();
        this.end = LocationUtils.str2loc(plugin.getApi().getGameManager().getGameProperties().getConfig("end", new JsonPrimitive("world, 0, 10, 0")).getAsString());

        if (files != null)
            for (File file : files)
            {
                try
                {
                    Cuboid cuboid = new Cuboid();
                    cuboid.clipboard = CuboidClipboard.loadSchematic(file);
                    JsonElement jsonElement = offsets.get(file.getName().substring(0, file.getName().indexOf('.')));
                    String string = jsonElement == null ? null : jsonElement.getAsString();
                    String[] split = string == null ? null : string.split(", ");
                    cuboid.offset = split == null ? new Vector(cuboid.clipboard.getWidth(), cuboid.clipboard.getHeight(), cuboid.clipboard.getLength()) : new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));

                    if (file.getName().contains("straight"))
                        this.straightCuboids.add(cuboid);
                    else if (file.getName().contains("curved"))
                        this.curvedCuboids.add(cuboid);
                    else if (file.getName().contains("start"))
                        this.start = cuboid;

                }
                catch (Exception exception)
                {
                    plugin.getLogger().log(Level.SEVERE, exception.getMessage(), exception);
                    plugin.getServer().shutdown();
                }
            }
    }

    Location generate(SFPlugin plugin, World world)
    {
        int rot = 0;

        Collections.shuffle(this.straightCuboids);
        Collections.shuffle(this.curvedCuboids);

        Location tmp = this.end.clone();
        BukkitWorld bukkitWorld = new BukkitWorld(world);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bukkitWorld, -1);
        editSession.setFastMode(false);

        try
        {
            for (int i = 0; i < this.straightCuboids.size(); i++)
            {
                Cuboid cuboid2 = this.curvedCuboids.get(i);
                this.rotateVector(cuboid2.offset, rot);
                tmp.subtract(cuboid2.offset);
                cuboid2.clipboard.rotate2D(rot);
                cuboid2.clipboard.paste(editSession, new com.sk89q.worldedit.Vector(tmp.getX(), tmp.getY(), tmp.getZ()), false);

                Cuboid cuboid1 = this.straightCuboids.get(i);
                this.rotateVector(cuboid1.offset, rot);
                tmp.subtract(cuboid1.offset);
                cuboid1.clipboard.rotate2D(rot);
                cuboid1.clipboard.paste(editSession, new com.sk89q.worldedit.Vector(tmp.getX(), tmp.getY(), tmp.getZ()), false);

                rot += 90;
            }
            this.rotateVector(this.start.offset, rot);
            tmp.subtract(this.start.offset);
            this.start.clipboard.paste(editSession, new com.sk89q.worldedit.Vector(tmp.getX(), tmp.getY(), tmp.getZ()), false);

            plugin.getGame().setSpawn(tmp);
            plugin.getGame().setStatus(Status.WAITING_FOR_PLAYERS);

            return tmp;
        }
        catch (MaxChangedBlocksException exception)
        {
            plugin.getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            plugin.getServer().shutdown();
            return null;
        }
    }

    private void rotateVector(Vector vector, int angle)
    {
        double piAngle = angle * Math.PI / 180;
        double cos = Math.cos(piAngle);
        double sin = Math.sin(piAngle);
        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = vector.getX() * -sin + vector.getZ() * cos;
        vector.setX(x).setZ(z);
    }

    private class Cuboid
    {
        private Vector offset;
        private CuboidClipboard clipboard;
    }
}
