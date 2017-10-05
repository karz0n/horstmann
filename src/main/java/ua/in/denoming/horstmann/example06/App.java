package ua.in.denoming.horstmann.example06;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

class App implements Runnable, AutoCloseable {
    private static App instance;

    private static final String publisherQuery = "SELECT Books.Price, Books.Title" +
        " FROM Books, Publishers" +
        " WHERE Books.Publisher_Id = Publishers.Publisher_Id AND Publishers.Name = ?";

    private Properties properties;
    private Logger logger;
    private Connection connection;

    @SuppressWarnings("SameParameterValue")
    static App getInstance(String name) throws Exception {
        if (App.instance == null) {
            App.instance = new App(name);
        }
        return App.instance;
    }

    private App(String name) throws Exception {
        logger = createLogger(name);
        properties = getProperties();
        connection = getConnection(properties);
    }

    private String executeScript(String fileName) throws SQLException, FileNotFoundException {
        InputStream fileStream = this.getClass().getResourceAsStream(fileName);
        if (fileStream == null) {
            throw new FileNotFoundException("Script file not found");
        }

        StringBuilder builder = new StringBuilder();
        try (
            Scanner in = new Scanner(fileStream, "UTF-8");
            Statement statement = connection.createStatement()
        ) {
            builder.append(String.format("Execute file %s:%n", fileName));
            while (in.hasNextLine()) {
                String command = in.nextLine().trim();
                if (command.endsWith(";"))
                    command = command.substring(0, command.length() - 1);
                boolean hasResult = statement.execute(command);
                if (hasResult) {
                    try (ResultSet rs = statement.getResultSet()) {
                        builder.append(printResultSet(rs));
                    }
                } else {
                    builder.append(statement.getUpdateCount()).append(" rows updated");
                }
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    private Properties getProperties() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("database.properties");
        if (in == null) {
            throw new FileNotFoundException("Properties file not found");
        }
        Properties props = new Properties();
        props.load(in);
        return props;
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

    private Connection getConnection(Properties props) throws SQLException {
        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null) {
            System.setProperty("jdbc.drivers", drivers);
        }
        return DriverManager.getConnection(
            props.getProperty("jdbc.url"),
            props.getProperty("jdbc.user"),
            props.getProperty("jdbc.password")
        );
    }

    private String printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder builder = new StringBuilder();
        builder.append("===================================").append(System.lineSeparator());
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1)
                builder.append(", ");
            builder.append(metaData.getColumnLabel(i));
        }
        builder.append(System.lineSeparator());

        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1)
                    builder.append(", ");
                builder.append(rs.getString(i));
            }
            builder.append(System.lineSeparator());
        }
        builder.append("===================================").append(System.lineSeparator());

        return builder.toString();
    }

    /**
     * Sample using prepare statement
     */
    private String searchBooksByPublisherName(String... names) throws SQLException {
        try (
            PreparedStatement statement = connection.prepareStatement(App.publisherQuery)
        ) {
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                statement.setString(1, name);
                ResultSet rs = statement.executeQuery();
                builder.append("By name: ").append(name).append(System.lineSeparator());
                builder.append(printResultSet(rs));
            }
            return builder.toString();
        }
    }

    private void updateBooksPrice(double increase, String... names) throws SQLException {
        if (!connection.getMetaData().supportsResultSetConcurrency(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            logger.log(Level.WARNING, "Database doest not support result set type");
            return;
        }
        try (
            Statement statement = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
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

    @Override
    public void run() {
        try {
            String output;

            output = executeScript("prepare.sql");
            logger.log(Level.FINE, output);

            output = executeScript("populate.sql");
            logger.log(Level.FINE, output);

            output = searchBooksByPublisherName("MIT Press", "Oxford University Press", "Random House");
            logger.log(Level.FINE, output);

            updateBooksPrice(15, "Design Patterns", "The C++ Programming Language");

            output = executeScript("cleanup.sql");
            logger.log(Level.FINE, output);

        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Open file script", e);
        } catch (SQLException e) {
            for (Throwable t : e) {
                logger.log(Level.WARNING, "Execute file script", t);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
