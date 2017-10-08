package ua.in.denoming.horstmann.example05;

import ua.in.denoming.horstmann.shared.Database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App implements Consumer<String[]>, AutoCloseable {
    private static App instance;

    private Database database;
    private Logger logger;

    static App getInstance() throws Exception {
        if (App.instance == null) {
            App.instance = new App("ua.in.denoming.horstmann.example05");
        }
        return App.instance;
    }

    private App(String name) throws Exception {
        logger = createLogger(name);
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

    private void doAccept(String[] args) {
        boolean fromFile = (args.length != 0);
        try {
            InputStream stream = this.getClass().getResourceAsStream(args[0]);
            if (stream == null) {
                throw new FileNotFoundException("Script file doesn't exists");
            }

            try (
                Statement statement = database.getConnection().createStatement();
                Scanner in = fromFile
                    ? new Scanner(stream, "UTF-8")
                    : new Scanner(System.in)
            ) {
                while (true) {
                    if (!fromFile)
                        System.out.println("Enter command or EXIT to exit:");
                    if (!in.hasNextLine())
                        return;

                    String command = in.nextLine().trim();
                    if (command.equalsIgnoreCase("EXIT"))
                        return;
                    if (command.endsWith(";"))
                        command = command.substring(0, command.length() - 1);

                    boolean hasResult = statement.execute(command);
                    if (hasResult) {
                        try (ResultSet rs = statement.getResultSet()) {
                            String value = Database.printResultSet(rs);
                            logger.log(Level.FINE, value);
                        }
                    } else
                        logger.log(Level.FINE, statement.getUpdateCount() + " rows updated");
                }
            } catch (SQLException e) {
                for (Throwable t : e) {
                    logger.log(Level.WARNING, "Execute command", t);
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Read file", e);
        }
    }

    @Override
    public void accept(String... args) {
        database = new Database();
        doAccept(args);
    }

    @Override
    public void close() throws Exception {
        if (database != null)
            database.close();
    }
}
