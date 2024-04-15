package Radon;

import arc.Events;
import arc.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Radon extends Plugin {
    public static SessionFactory sessionFactory;
    private static int BatchSize = 1000;
    
    @Override
    public void init() {
        Events.on(EventType.ServerLoadEvent.class, event -> {
            var configFolder = Vars.modDirectory.child("Radon/");
            configFolder.mkdirs();
            var configFi = configFolder.child("config.json");

            boolean bad = false;

            badExit:
            if (configFi.exists()) {
                var sqlDetails = new SQLDetails();
                //region read SQL connection settings
                try {
                    sqlDetails = new ObjectMapper().readValue(configFi.file(), SQLDetails.class);
                } catch (IOException e) {
                    //e.g. save bad/old config as config.json.123123123.old
                    if (!sqlDetails.equals(new SQLDetails())) {
                        var old = configFolder.child(configFi.name() + '.' + System.currentTimeMillis() + ".old");
                        configFi.copyTo(old);
                        SQLDetails.saveDefault();
                        Log.err("Radon: Bad config file copied to @ and replaced with a clean config file!", old.absolutePath());
                    }
                    Log.err(e);
                    bad = true;
                    break badExit;
                }

                try {
                    BatchSize = sqlDetails.getBatch_size();

                    Properties settings = new Properties();

                    settings.put(Environment.DRIVER, sqlDetails.getDriver());
                    settings.put(Environment.DIALECT, sqlDetails.getDialect());
                    //login stuff
                    settings.put(Environment.URL, sqlDetails.getUrl());
                    settings.put(Environment.USER, sqlDetails.getUser());
                    settings.put(Environment.PASS, sqlDetails.getPassword());
                    settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

                    settings.put(Environment.HBM2DDL_AUTO, sqlDetails.getHBM2DDL_AUTO());
                    //disable auto commit
                    settings.put(Environment.AUTOCOMMIT, false);
                    //batching
                    settings.put(Environment.STATEMENT_BATCH_SIZE, BatchSize);
                    settings.put(Environment.BATCH_VERSIONED_DATA, true);
                    settings.put(Environment.ORDER_INSERTS, true);
                    settings.put(Environment.ORDER_UPDATES, true);
                    //debug
                    settings.put(Environment.SHOW_SQL, Administration.Config.debug.bool());
                    //make config
                    Configuration configuration = new Configuration();
                    configuration.setProperties(settings);
                    //add annotated classes
                    Events.fire(new RegisterRadonEntities(configuration));
                    //build
                    sessionFactory = configuration.buildSessionFactory();
                } catch (Exception e) {
                    Log.err(e);
                    bad = true;
                }
            } else {
                SQLDetails.saveDefault();
                bad = true;
            }

            if (bad) {
                var h = "#".repeat(97);
                var t = "\t".repeat(5);
                Log.warn("\u001B[31m@\u001B[0m", h);
                Log.warn("\u001B[31m@\u001B[0m", h);
                Log.warn("");
                Log.warn("\u001B[31m@@\u001B[0m", t, "Radon config file needs to be configured!");
                Log.warn("\u001B[31m@@\u001B[0m", t, configFi.absolutePath());
                Log.warn("");
                Log.warn("\u001B[31m@\u001B[0m", h);
                Log.warn("\u001B[31m@\u001B[0m", h);
                return;
            }

            Log.info("Radon started successfully!");
        });
    }

    /**
     * Saves an entity to the SQL Database
     *
     * @param o The entity to be saved to the SQL Database
     * @return A Serializable, for auto increment keys or other returns.
     */
    public static Object save(Object o) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var s = session.save(o);
            transaction.commit();
            return s;
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        }
        return null;
    }

    /**
     * Saves each entity of an array to the SQL Database.
     *
     * @param o An array of entities to be saved to the SQL Database
     */
    public static void save(Object... o) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            System.out.println(session.getProperties().get("autocommit"));
            session.setProperty("autocommit", false);
            transaction = session.beginTransaction();
            for (int i = 0, i2 = 1; i < o.length; i++, i2++) {
                session.persist(o[i]);
                if (i2 % BatchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        }
    }

    /**
     * Saves an entity to the SQL Database, or updates the entity if already in the database.
     *
     * @param o The entity to be saved or updated to the SQL Database
     * @return A boolean, true unless there was an error
     */
    public static boolean saveOrUpdate(Object o) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(o);
            transaction.commit();
            return true;
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            return false;
        }
    }

    /**
     * Saves each entity of an array to the SQL Database, or updates the entity if already in the database.
     *
     * @param o An array of entities to be saved or updated to the SQL Database
     */
    public static void saveOrUpdate(Object... o) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            for (int i = 0, i2 = 1; i < o.length; i++, i2++) {
                session.save(o[i]);
                if (i2 % BatchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            Log.level = Log.LogLevel.debug;
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        }
    }

    /**
     * Saves each entity of a collection to the SQL Database, or updates the entity if already in the database.
     *
     * @param o A collection of entities to be saved or updated to the SQL Database
     */
    public static void saveOrUpdate(Collection<Object> o) {
        saveOrUpdate(o.toArray());
    }

    /**
     * Creates an SQLBuilder with the provided HQL Query
     *
     * @param query The HQL Query to be run
     * @return An SqlBuilder, where you can set variables, and execute the query.
     */
    public static SqlBuilder run(String query) {
        return new SqlBuilder(query);
    }

}
