package net.samagames.skyfall.listener;

import net.samagames.skyfall.SFPlayer;
import net.samagames.skyfall.SFPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

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
                || sfPlayer.isOnGround())
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
            /*case FLY_ON_WALL:
                eliminatePlayer(sfPlayer, player, "s'est écrasé contre un mur");
                event.setDamage(3 - player.getHealth());
                break ;//*/
            default:
                event.setCancelled(true);
        }
    }
}
