package net.samagames.flyring;

import net.samagames.api.SamaGamesAPI;
import net.samagames.flyring.listener.PlayerListener;
import net.samagames.flyring.listener.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;

public class SFPlugin extends JavaPlugin
{
    private SamaGamesAPI api;
    private SFGame game;

    @Override
    public void onEnable()
    {
        api = SamaGamesAPI.get();

        game = new SFGame(this);
        api.getGameManager().registerGame(game);

        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public SamaGamesAPI getApi()
    {
        return api;
    }

    public SFGame getGame()
    {
        return game;
    }
}
