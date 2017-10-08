package ua.in.denoming.horstmann.example07;

import ua.in.denoming.horstmann.shared.Database;

import javax.sql.RowSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

class App implements Runnable, AutoCloseable {
    private static App instance;

    private Database database;
    private Logger logger;

    private App(String name) {
        logger = createLogger(name);
    }

    static App getInstance() {
        if (App.instance == null) {
            App.instance = new App("ua.in.denoming.horstmann.example07");
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

    private void doRun() throws IOException, SQLException {
        RowSet rs = database.getRowSet("SELECT * FROM Books WHERE Title LIKE ?");
        rs.setString(1, "The Art%");
        rs.execute();

        String output = Database.printResultSet(rs);
        logger.log(Level.FINE, output);
    }

    @Override
    public void run() {
        try {
            database = new Database();
            database.prepare();
            database.populate();
            doRun();
            database.cleanup();
            database.close();
        } catch (Exception e) {
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
