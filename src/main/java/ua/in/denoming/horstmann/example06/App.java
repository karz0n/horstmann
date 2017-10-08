package ua.in.denoming.horstmann.example06;

import ua.in.denoming.horstmann.shared.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

class App implements Runnable, AutoCloseable {
    private static App instance;

    private Database database;
    private Logger logger;

    static App getInstance() {
        if (App.instance == null) {
            App.instance = new App("ua.in.denoming.horstmann.example06");
        }
        return App.instance;
    }

    private App(String name) {
        this.logger = createLogger(name);
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

    /**
     * Sample using prepare statement
     */
    private String searchBooksByPublisherName(String... names) throws IOException, SQLException {
        final String publisherQuery = "SELECT Books.Price, Books.Title" +
            " FROM Books, Publishers" +
            " WHERE Books.Publisher_Id = Publishers.Publisher_Id AND Publishers.Name = ?";
        try (
            PreparedStatement statement = database.getConnection().prepareStatement(publisherQuery)
        ) {
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                statement.setString(1, name);
                ResultSet rs = statement.executeQuery();
                builder.append("By name: ").append(name).append(System.lineSeparator());
                builder.append(Database.printResultSet(rs));
            }
            return builder.toString();
        }
    }

    /**
     * Sample using scrollable result set
     */
    private void updateBooksPrice(double increase, String... names) throws IOException, SQLException {
        Connection connection = database.getConnection();

        if (!connection.getMetaData().supportsResultSetConcurrency(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            logger.log(Level.WARNING, "Database doest not support result set type");
            return;
        }

        try (
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
        ) {
            List<String> nameList = Arrays.asList(names);
            ResultSet rs = statement.executeQuery("SELECT * FROM Books");
            while (rs.next()) {
                String title = rs.getString("Title").trim();
                boolean isFound = nameList.contains(title);
                if (isFound) {
                    double price = rs.getDouble("Price");
                    rs.updateDouble("Price", price + increase);
                    rs.updateRow();
                }
            }
        }
    }

    /**
     *
     */
    private void doRun() throws IOException, SQLException {
        String output = searchBooksByPublisherName("MIT Press", "Oxford University Press", "Random House");
        logger.log(Level.FINE, output);

        updateBooksPrice(15, "Design Patterns", "The C++ Programming Language");
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
        if (database != null)
            database.close();
    }
}
