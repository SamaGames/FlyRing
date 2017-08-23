package net.samagames.flyring.listener;

import net.minecraft.server.v1_9_R2.DamageSource;
import net.samagames.flyring.SFPlayer;
import net.samagames.flyring.SFPlugin;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

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
public class PlayerListener implements Listener
{
    private SFPlugin plugin;

    public PlayerListener(SFPlugin plugin)
    {
        this.plugin = plugin;
    }

    private void eliminatePlayer(SFPlayer sfPlayer, Player player, String reason)
    {
        this.plugin.getApi().getGameManager().getCoherenceMachine().getMessageManager().writeCustomMessage(
                player.getDisplayName() + " est mort" + (reason == null ? "." : " (" + reason + ")."), true);
        sfPlayer.eliminate();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event)
    {
        SFPlayer sfPlayer;
        if (!(event.getEntity() instanceof Player) || !this.plugin.getGame().isGameStarted()
                || (sfPlayer = this.plugin.getGame().getPlayer(event.getEntity().getUniqueId())) == null
                || sfPlayer.isOnGround())
        {
            event.setCancelled(true);
            return ;
        }
        Player player = (Player)event.getEntity();
        switch (event.getCause())
        {
            case PROJECTILE:
                event.setDamage(400D);
                eliminatePlayer(sfPlayer, player, "tué par un autre joueur");
                break ;
            case FALL:
                eliminatePlayer(sfPlayer, player, "écrasé au sol");
                event.setDamage(400D);
                break ;
            case FLY_INTO_WALL:
                eliminatePlayer(sfPlayer, player, "écrasé contre un mur");
                event.setDamage(400D);
                break ;
            default:
                event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerMove(PlayerMoveEvent event)
    {
        SFPlayer sfPlayer;
        if (!this.plugin.getGame().isGameStarted()
                || (sfPlayer = this.plugin.getGame().getPlayer(event.getPlayer().getUniqueId())) == null
                || sfPlayer.isOnGround())
            return ;
        if (event.getPlayer().isOnGround() && sfPlayer.getSpawn() != null)
        {
            if (this.plugin.getGame().getEndArea().isInArea(event.getPlayer().getLocation()))
            {
                int time = this.plugin.getGame().getTime();
                this.plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(event.getPlayer().getDisplayName() + " a atterri (" + (time < 600 ? "0" : "") + (time / 60) + ":" + (time % 60 < 10 ? "0" : "") + (time % 60) + ").", true);
                this.plugin.getGame().win(sfPlayer, event.getPlayer());
                sfPlayer.addCoins(2, "Atterrissage réussi");
                sfPlayer.setScore(sfPlayer.getScore() + 1);
                sfPlayer.setEndTime(System.currentTimeMillis());
            }
            else if (event.getPlayer().getLocation().distanceSquared(this.plugin.getGame().getSpawn()) > 25)
                ((CraftPlayer)event.getPlayer()).getHandle().damageEntity(DamageSource.FALL, 400F);
        }
        else if (sfPlayer.getSpawn() != null)
            this.plugin.getGame().getRings().stream().filter(ring -> !sfPlayer.hasCrossedRing(ring)).forEach(ring ->
            {
                if (ring.isPlayerInRing(event.getFrom()))
                {
                    sfPlayer.crossRing(ring);
                    sfPlayer.addCoins(2, "Anneau passé");
                    sfPlayer.setScore(sfPlayer.getScore() + 1);
                    event.getPlayer().playSound(event.getTo(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                    event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().normalize().multiply(event.getPlayer().getVelocity().length()).multiply(1.1F));
                }
            });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        event.getEntity().spigot().respawn();
        event.getEntity().teleport(this.plugin.getGame().getSpawn());
    }

    @EventHandler
    public void onSecondHand(PlayerSwapHandItemsEvent ev)
    {
        ev.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event)
    {
        event.setCancelled(true);
    }
}
