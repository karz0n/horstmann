package ua.in.denoming.horstmann.example08;

import ua.in.denoming.horstmann.shared.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App implements Runnable, AutoCloseable {
    private static App instance;

    private Database database;
    private Logger logger;

    private App(String name) {
        logger = createLogger(name);
    }

    static App getInstance() {
        if (App.instance == null) {
            App.instance = new App("ua.in.denoming.horstmann.example08");
        }
        return App.instance;
    }

    private Logger createLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);
        logger.addHandler(consoleHandler);

        return logger;
    }

    @Override
    public void run() {
        Connection connection = null;
        try {
            database = new Database();

            connection = database.getConnection();

            logger.log(Level.FINE, "Disable auto commit");
            connection.setAutoCommit(false);

            database.prepare();
            database.populate();
            database.cleanup();

            logger.log(Level.FINE, "Commit changes");
            connection.commit();

            database.close();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if (database != null) {
            database.close();
            database = null;
        }
    }
}
