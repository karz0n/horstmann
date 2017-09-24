package ua.in.denoming.horstmann.example04;

import java.io.IOException;
import java.util.logging.*;

public class Main {
    public static void main(String[] args) {
        try {
            Logger logger = createLogger("ua.in.denoming");
            logger.logp(
                Level.WARNING, "MY CLASS", "Main", "Some message");
            logger.logp(
                Level.WARNING, "NOT MY CLASS", "Main", "Some message");
            logger.logp(
                Level.FINE, "MY CLASS", "Main", "Some message");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Logger createLogger(String name) throws Exception {
        return Main.createLogger(name, Level.WARNING, Level.FINE);
    }

    private static Logger createLogger(String name, Level consoleLevel, Level fileLevel) throws Exception {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);

        Formatter formatter = getFormatter(
            "======= BEGIN =======\n", "\n======= END =======");
        Filter filter = getFilter();

        logger.addHandler(
            Main.getConsoleHandler(consoleLevel, filter, formatter));
        logger.addHandler(
            Main.getFileHandler(fileLevel, filter, formatter));

        return logger;
    }

    private static Handler getConsoleHandler(Level level, Filter filter, Formatter formatter) {
        Handler handler = new ConsoleHandler();
        handler.setFilter(filter);
        handler.setFormatter(formatter);
        handler.setLevel(level);
        return handler;
    }

    private static Handler getFileHandler(Level level, Filter filter, Formatter formatter) throws Exception {
        final String LOG_FILE_PATTERN = "%h/app.%u.log";
        final int LOG_SIZE_LIMIT = 5 * 1024 * 1024;
        final int LOG_ROTATION_COUNT = 3;
        try {
            Handler handler = new FileHandler(LOG_FILE_PATTERN, LOG_SIZE_LIMIT, LOG_ROTATION_COUNT);
            handler.setFilter(filter);
            handler.setFormatter(formatter);
            handler.setLevel(level);
            return handler;
        } catch (IOException e) {
            throw new Exception("Create file handler", e);
        }
    }

    private static Formatter getFormatter(String head, String footer) {
        return new Formatter() {
            @Override
            public String format(LogRecord record) {
                return super.formatMessage(record);
            }

            @Override
            public String getHead(Handler h) {
                return head;
            }

            @Override
            public String getTail(Handler h) {
                return footer;
            }
        };
    }

    private static Filter getFilter() {
        return record -> record.getSourceClassName().equals("MY CLASS");
    }
}
