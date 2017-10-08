package ua.in.denoming.horstmann.shared;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Database implements AutoCloseable {
    private Connection connection;

    public static String printResultSet(ResultSet rs) throws SQLException {
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

        rs.first();
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1)
                    builder.append(", ");
                builder.append(rs.getString(i).trim());
            }
            builder.append(System.lineSeparator());
        }
        builder.append("===================================").append(System.lineSeparator());

        return builder.toString();
    }

    private Properties getProperties() throws IOException {
        InputStream stream = getClass().getResourceAsStream("database.properties");
        if (stream == null) {
            throw new FileNotFoundException("Properties file not found");
        }
        Properties props = new Properties();
        props.load(stream);
        return props;
    }

    private Connection createConnection(Properties props) throws SQLException {
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

    private void executeResourceScriptFile(String fileName) throws IOException, SQLException {
        InputStream fileStream = getClass().getResourceAsStream(fileName);
        if (fileStream == null) {
            throw new FileNotFoundException("Script file not found");
        }

        Connection connection = getConnection();
        boolean isAutoCommit = connection.getAutoCommit();
        boolean isBatchUpdatesSupport = connection.getMetaData().supportsBatchUpdates();
        if (isBatchUpdatesSupport) {
            connection.setAutoCommit(false);
        }

        try (
            Statement statement = connection.createStatement();
            Scanner scanner = new Scanner(fileStream, "UTF-8")
        ) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim();
                if (command.endsWith(";"))
                    command = command.substring(0, command.length() - 1);
                if (isBatchUpdatesSupport) {
                    statement.addBatch(command);
                } else {
                    statement.execute(command);
                }
            }

            if (isBatchUpdatesSupport) {
                statement.executeBatch();
                try {
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
        }

        if (isBatchUpdatesSupport) {
            connection.setAutoCommit(isAutoCommit);
        }
    }

    public Connection getConnection() throws IOException, SQLException {
        if (connection == null) {
            Properties properties = getProperties();
            connection = createConnection(properties);
        }
        return connection;
    }

    public RowSet getRowSet(String command) throws IOException, SQLException {
        Properties props = getProperties();

        RowSetFactory factory = RowSetProvider.newFactory();
        CachedRowSet crs = factory.createCachedRowSet();

        crs.setUrl("jdbc:postgresql://192.168.1.20/sqlcmd");
        crs.setUsername(props.getProperty("jdbc.user"));
        crs.setPassword(props.getProperty("jdbc.password"));

        crs.setCommand(command);

        return crs;
    }

    public void prepare() throws IOException, SQLException {
        executeResourceScriptFile("prepare.sql");
    }

    public void populate() throws IOException, SQLException {
        executeResourceScriptFile("populate.sql");
    }

    public void cleanup() throws IOException, SQLException {
        executeResourceScriptFile("cleanup.sql");
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}
