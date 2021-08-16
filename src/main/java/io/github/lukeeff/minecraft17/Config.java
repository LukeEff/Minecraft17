package io.github.lukeeff.minecraft17;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

import static io.github.lukeeff.minecraft17.Minecraft17.*;

public enum Config {

    DEBUG_MODE("debug", true),
    COLOR_LOGS("color-logs", true);

    final String path;
    final boolean defaultBoolean;

    Config(final String path, final boolean defaultBoolean) {
        this.path = path;
        this.defaultBoolean = defaultBoolean;
    }

    public void setBooleanValue(final Config key, final boolean value) {
        getConfig().set(key.path, value);
    }

    public boolean getBoolean() {
        return getConfig().getBoolean(path);
    }

    private static FileConfiguration getConfig() {
        return getPlugin(Minecraft17.class).getConfig();
    }

    public static void setDefaults() {
        final FileConfiguration configuration = getConfig();
        Arrays.stream(values()).forEach(config -> configuration.set(config.path, config.defaultBoolean));
        configuration.options().copyDefaults(true);
    }
}
