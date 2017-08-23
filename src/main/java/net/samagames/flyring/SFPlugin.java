package net.samagames.flyring;

import net.samagames.api.SamaGamesAPI;
import net.samagames.flyring.listener.PlayerListener;
import net.samagames.flyring.listener.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;

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
