/* KookyConsole
Copyright (C) 2022, zly2006 & contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

@file:OptIn(DelicateCoroutinesApi::class)

package io.github.kookybot.console

import io.github.kookybot.JavaBaseClass
import io.github.kookybot.client.Client
import io.github.kookybot.contract.Self
import io.github.kookybot.message.SelfMessage
import io.github.kookybot.plugin.Plugin
import kotlinx.coroutines.DelicateCoroutinesApi
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.URLClassLoader

const val version = "0.1"

class Config {

}

class PluginInfo (
    val name: String,
    val main: Class<Plugin>,
    val instance: Plugin,
)

class PluginLoader (
) {
    val plugins = mutableListOf<PluginInfo>()
    internal val yaml = Yaml()
    internal val logger = LoggerFactory.getLogger(this::class.java)
    var client: Client
        internal set
    var self: Self
        internal set

    init {
        if (!File("data/").exists())
            File("data/").mkdir()
        if (!File("plugins/").exists())
            File("plugins/").mkdir()
        if (!File("data/token.txt").isFile) {
            File("data/token.txt").createNewFile()
            println("please fill your token in data/token.txt")
            throw Error("please fill your token in data/token.txt")
        }



        File("plugins/").listFiles()?.forEach {
            try {
                if (it.name.endsWith(".jar")) run {
                    val loader = URLClassLoader(arrayOf(it.toURI().toURL()))
                    val map = yaml.loadAs(loader.getResource("plugin.yml")!!.openStream(), Map::class.java)
                    @Suppress("UNCHECKED_CAST")
                    val main = loader.loadClass(map["main"] as String) as Class<Plugin>
                    val name = map["name"] as String
                    val instance = main.getConstructor(PluginLoader::class.java).newInstance(this@PluginLoader)
                    instance.onLoad()
                    plugins.add(PluginInfo(
                        name = name,
                        main = main,
                        instance = instance
                    ))
                }
                if (it.name.endsWith(".class")) run {
                    val name = it.name
                    val loader = URLClassLoader(arrayOf(it.parentFile.toURI().toURL()))
                    @Suppress("UNCHECKED_CAST")
                    val main = loader.loadClass(it.nameWithoutExtension) as Class<Plugin>
                    val instance = main.getConstructor(PluginLoader::class.java).newInstance(this@PluginLoader)
                    instance.onLoad()
                    plugins.add(PluginInfo(
                        name = name,
                        main = main,
                        instance = instance
                    ))
                }
            }
            catch (e: Exception) {
                logger.error("Error while loading a plugin. ", e)
            }
        }

        logger.info("Loaded plugins(${plugins.size}): ${plugins.joinToString(", ")}")
        logger.info("Enabling plugins...")
        plugins.forEach {
            it.instance.onEnable()
        }
        logger.info("Starting client...")
        client = Client(File("data/token.txt").readText()) {
            withDefaultCommands()
        }
        // onDisable
        self = JavaBaseClass.utils.connectWebsocket(client)
    }

    internal fun run() {
        while (true) {
            val cmd = readln()

            client.eventManager.parseCommand(cmd)
        }
    }
}

fun main(args: Array<String>) {
    println("KookyConsole v${version}")
    println("Copyright (c) 2022, zly2006 & contributors.")
    println("This software is a *FREE* software under AGPL v3 or later license.")
    println("You can find the source code at <https://github.com/KookyBot/KookyConsole>.")
    println("")
    PluginLoader().run()
}