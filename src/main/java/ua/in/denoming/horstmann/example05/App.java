package ua.in.denoming.horstmann.example05;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App implements Consumer<String[]>, AutoCloseable {
    private static App instance;

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
        for (int i = 1; i < columnCount; i++) {
            if (i > 1)
                builder.append(", ");
            builder.append(metaData.getColumnLabel(i));
        }
        builder.append(System.lineSeparator());

        while(rs.next()) {
            for (int i = 1; i < columnCount; i++) {
                if (i > 1)
                    builder.append(", ");
                builder.append(rs.getString(i));
            }
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    @Override
    public void accept(String[] args) {
        boolean fromFile = (args.length != 0);
        try {
            InputStream stream = this.getClass().getResourceAsStream(args[0]);
            if (stream == null) {
                throw new FileNotFoundException("Script file doesn't exists");
            }

            try (
                Scanner in = fromFile
                    ? new Scanner(stream, "UTF-8")
                    : new Scanner(System.in);
                Statement statement = connection.createStatement()
            ) {
                while(true) {
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
                            String value = printResultSet(rs);
                            logger.log(Level.FINE, value);
                        }
                    } else
                        logger.log(Level.FINE, statement.getUpdateCount() + " rows updated");
                }
            }
            catch (SQLException e) {
                for (Throwable t: e) {
                    logger.log(Level.WARNING, "Execute command", t);
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Read file", e);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
