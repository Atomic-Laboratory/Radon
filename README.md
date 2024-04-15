[![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Atomic-Laboratory/Radon/total)](https://github.com/Atomic-Laboratory/Radon/releases/latest)
[![GitHub Release](https://img.shields.io/github/v/release/Atomic-Laboratory/Radon)](https://github.com/Atomic-Laboratory/Radon/releases/latest)
[![Discord](https://img.shields.io/discord/1158888581964779530)](https://discord.gg/U6hGBbT87D)

### Overview
Radon is a Mindustry HQL client that simplifies the usage of HQL/SQL.

### Example
[View the file](https://github.com/Atomic-Laboratory/Radon/tree/master/src/example/Example.java)

```java
package example;

import arc.Events;
import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;
import Radon.Radon;
import Radon.RegisterRadonEntities;

public class Example extends Plugin {
    public static void main(String[] args) {
        Vars.modDirectory = new Fi("");
        var r = new Radon();
        r.init();
        Events.on(RegisterRadonEntities.class, event -> event.configuration.addAnnotatedClass(UserData_DBO.class));
        Events.on(EventType.ServerLoadEvent.class, event -> {
            var ud = new UserData_DBO("123123123", "asdajhbdka");
            Radon.saveOrUpdate(ud);
            Log.info("saved!");
            var list = Radon.run("FROM UserData_DBO").getMultiple(UserData_DBO.class);
            for (var a : list)
                Log.info("@: @", a.getUuid(), a.getNickname());
        });
        Events.fire(new EventType.ServerLoadEvent());
    }

    @Override
    public void init() {
        Events.on(PlayerJoin.class, event -> {
            var p = event.player;
            var ud = Radon.run("FROM UserData_DBO WHERE uuid = :uuid").set("uuid", p.uuid()).getSingle(UserData_DBO.class);
            if (ud == null) {
                ud = new UserData_DBO(event.player.uuid(), event.player.plainName());
                Radon.save(ud);
                Log.info("saved!");
            }
            p.name(ud.getNickname());
        });

        Events.on(RegisterRadonEntities.class, event -> {
            //register entity classes used for SQL ORM
            event.configuration.addAnnotatedClass(UserData_DBO.class);
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("allnicknames", "all nicknames in database", args -> {
            var list = Radon.run("FROM UserData_DBO").getMultiple(UserData_DBO.class);
            for (var a : list)
                Log.info("@: @", a.getUuid(), a.getNickname());
        });
        handler.register("resetonlineusernames", "reset usernames of players currently on the server", args -> {
            Integer r = Radon.run("UPDATE UserData_DBO SET nickname = :name WHERE uuid IN (:list)").set("name", "player").setList("list", Groups.player.copy().list()).update();
            Log.info("@ Entries updated.", r);
        });
    }
}
```

### Setup

#### MySql Server
Google how to install a MySql server, or chatGPT it. The following is a basic, not all-encompassing guide for linux.
1) Install MySql on your server.  
    `sudo apt install -y mysql-server`
2) Go through basic configuration of the MySql server  
    `sudo mysql_secure_installation`
3) Create a SQL User  
    `CREATE USER 'mindustry'@'%' IDENTIFIED BY 'password'`
4) Grant the user external access (so it can talk outside localhost)  
    `GRANT ALL PRIVILEGES ON *.* TO 'mindustry'@'%' WITH GRANT OPTION;`
5) Reload user privileges  
    `FLUSH PRIVILEGES;`

#### Game Server
1) Go to [Relases](https://github.com/Atomic-Laboratory/Radon/releases/latest), download `Radon.jar`, and place it inside your mindustry server's `mods` folder.
2) Find `./config/mods/Radon/config.json` and set the proper SQL url, port, username and password.
> [!WARNING]
> If you are not using MySql, remember to change the driver class and SQL dialect. You would also need a separate plugin to load your SQL Driver if you are not using one of the integrated drivers[^1].

### Development
In order to use this plugin with your custom plugin, you will need to add Radon as dependency.

In gradle, add the following:
```groovy
repositories {
   //other repositories like maven central
   maven{ url 'https://www.jitpack.io' }
}
dependencies {
   //other dependencies
   compileOnly "com.github.Atomic-Laboratory:Radon:1.0.0"
}
```
In your plugins.json, append the following:
```json
"dependencies": [
"Radon"
]
```


### Building a Jar

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.

[^1]: Radon.jar comes packaged with MySQL, Microsoft SQL, Oracle DB, PostgreSQL, SQLite, and MariaDB.
