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
        this.api = SamaGamesAPI.get();

        this.game = new SFGame(this);
        this.api.getGameManager().registerGame(this.game);

        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.api.getGameManager().setKeepPlayerCache(true);
    }

    public SamaGamesAPI getApi()
    {
        return this.api;
    }

    public SFGame getGame()
    {
        return this.game;
    }
}
