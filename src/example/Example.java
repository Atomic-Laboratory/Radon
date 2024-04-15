package example;

import Radon.Radon;
import Radon.RegisterRadonEntities;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;

public class Example extends Plugin {
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
            var r = Radon.run("UPDATE UserData_DBO SET nickname = :name WHERE uuid IN (:list)").set("name", "player").setList("list", Groups.player.copy().list()).update();
            Log.info("@ Entries updated.", r);
        });
    }
}
