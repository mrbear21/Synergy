# Synergy [Velocity-Spigot]
Basic tools and server messaging plugin for minecraft servers. The plugin can be used both in proxy and standalone servers.

# Convenient data synchronization between servers
```
@EventHandler
    public void getMessage(SynergyPluginMessage e) {
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
    	}	
        return true;
    }
```

# Convenient localization system (including third-party plugins and system messages)

Synergy's locales.yml
```
login_command_usage:
    en: "&cUsage: /login <password>"
    uk: "&cВикористання: /login <пароль>"
login_wrong_password:
    en: "&cWrong password!"
    uk: "&cНевірний пароль!"
localized-unknown-command-message:
    en: "Unknown command. Type '/help' for help."
    uk: "Невідома команда. Введіть '/help' для допомоги"

```
Authme's messages_en.yml
```
...
login:
  command_usage: 'login_command_usage'
  wrong_password: 'login_wrong_password'
...
```
Spigot.yml
```
...
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
