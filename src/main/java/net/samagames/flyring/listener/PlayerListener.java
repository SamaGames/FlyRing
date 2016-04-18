package net.samagames.flyring.listener;

import net.samagames.flyring.SFPlayer;
import net.samagames.flyring.SFPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener
{
    private SFPlugin plugin;

    public PlayerListener(SFPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void eliminatePlayer(SFPlayer sfPlayer, Player player, String reason)
    {
        plugin.getApi().getGameManager().getCoherenceMachine().getMessageManager().writeCustomMessage(
                player.getDisplayName() + " est éliminé" + (reason == null ? "." : " (" + reason + ")."), true);
        sfPlayer.eliminate();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event)
    {
        SFPlayer sfPlayer;
        if (!(event.getEntity() instanceof Player) || !plugin.getGame().isGameStarted()
                || (sfPlayer = plugin.getGame().getPlayer(event.getEntity().getUniqueId())) == null
                || sfPlayer.isOnGround()
                || sfPlayer.isEliminated())
        {
            event.setCancelled(true);
            return ;
        }
        Player player = (Player)event.getEntity();
        switch (event.getCause())
        {
            case PROJECTILE:
                if (player.getHealth() <= 2)
                {
                    event.setDamage(3 - player.getHealth());
                    eliminatePlayer(sfPlayer, player, "tué par un autre joueur");
                }
                event.setDamage(2);
                break ;
            case FALL:
                eliminatePlayer(sfPlayer, player, "s'est écrasé au sol");
                event.setDamage(3 - player.getHealth());
                break ;
            case FLY_INTO_WALL:
                eliminatePlayer(sfPlayer, player, "s'est écrasé contre un mur");
                event.setDamage(3 - player.getHealth());
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
        if (!plugin.getGame().isGameStarted()
                || (sfPlayer = plugin.getGame().getPlayer(event.getPlayer().getUniqueId())) == null
                || sfPlayer.isOnGround()
                || sfPlayer.isEliminated())
            return ;
        if (event.getPlayer().isOnGround() && sfPlayer.getSpawn() != null && event.getPlayer().getLocation().distanceSquared(sfPlayer.getSpawn()) > 100)
        {
            sfPlayer.setOnGround(true);
            int time = plugin.getGame().getTime();
            plugin.getGame().getCoherenceMachine().getMessageManager().writeCustomMessage(event.getPlayer().getDisplayName() + " a atterri (" + (time < 600 ? "0" : "") + (time / 60) + ":" + (time < 10 ? "0" : "") + (time % 60) + ").", true);
            sfPlayer.addCoins(2, "Atterrissage réussi");
            sfPlayer.setScore(sfPlayer.getScore() + 1);
            sfPlayer.setEndTime(System.currentTimeMillis());
        }
        else if (sfPlayer.getSpawn() != null)
            plugin.getGame().getRings().stream().filter(ring -> !sfPlayer.hasCrossedRing(ring)).forEach(ring -> {
                if (ring.isPlayerInRing(event.getFrom()))
                {
                    sfPlayer.crossRing(ring);
                    sfPlayer.addCoins(2, "Anneau passé");
                    sfPlayer.setScore(sfPlayer.getScore() + 1);
                    event.getPlayer().playSound(event.getTo(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                }
            });
    }
}
