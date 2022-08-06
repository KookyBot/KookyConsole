package io.github.kookybot.plugin;

import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.Map;

public interface Plugin {
    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
    void onEnable();
    void onDisable();
    void onLoad();
}
