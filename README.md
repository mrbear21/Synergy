# Synergy [Velocity-Spigot]
Basic tools and server messaging plugin for minecraft servers. The plugin can be used both in proxy and standalone servers.

> The purpose of the plugin is to create synergy between servers and unite them into a solid and seamless project

# TODO List
☑️Proxy messaging system
☑️Localization system
☑️Chat manager
☑️Discord integration
☑️Votifier (only handling for now)
☑️Convenient API
⏹️Player data manager
⏹️Patreon integration (maybe)
⏹️Convenient web manager
⏹️Security and caching features

# Maven Dependency
```
<dependency>
  <groupId>archi.quest</groupId>
  <artifactId>synergy</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

# Permissions
https://github.com/mrbear21/Synergy/wiki/Permissions

# Convenient data synchronization between servers
```
@EventHandler
    public void onMessage(SynergyPluginMessage e) {
        if (!e.getIdentifier().equals("broadcast-message")) {
            return;
        }
        Bukkit.broadcastMessage(String.join(" ", e.getArgs()));
    }

@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		SynergyPluginMessage spm = new SynergyPluginMessage(plugin);
		spm.setArguments(args);
		spm.send("broadcast-message");      
        return true;
    }
```

# Convenient localization system (including third-party plugins and system messages)

Synergy's locales.yml
```
login-command-usage:
    en: "&cUsage: /login <password>"
    uk: "&cВикористання: /login <пароль>"
login-wrong-password:
    en: "&cWrong password!"
    uk: "&cНевірний пароль!"
localized-unknown-command-message:
    en: "Unknown command. Type '/help' for help."
    uk: "Невідома команда. Введіть '/help' для допомоги"

```
Authme's messages_en.yml
```
login:
  command_usage: 'login-command-usage'
  wrong_password: 'login-wrong-password'
...
```
Spigot.yml
```
messages:
  unknown-command: 'localized-unknown-command-message'
...
```

# Convenient storage of player data

```
BreadMaker bread = plugin.getBread("player");
//Get player data
bread.getData("level").getAsInt()
bread.getData("language").getAsString()

//Save data locally (temporarily)
bread.setData("key", "value");

//Save data permanently
bread.setData("key", "value").save();
```
