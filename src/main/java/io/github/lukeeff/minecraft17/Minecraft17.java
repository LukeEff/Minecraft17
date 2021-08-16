package io.github.lukeeff.minecraft17;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.lukeeff.minecraft17.packetlistener.OnEntityMetaData;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import static io.github.lukeeff.minecraft17.Logger.log;

@Getter
public class Minecraft17 extends JavaPlugin {

    private ProtocolManager manager;

    @Override
    public void onEnable() {
        Config.setDefaults();
        saveConfig();
        manager = ProtocolLibrary.getProtocolManager();
        new OnEntityMetaData(this);
        log("Successfully enabled Minecraft17.");
    }
}
