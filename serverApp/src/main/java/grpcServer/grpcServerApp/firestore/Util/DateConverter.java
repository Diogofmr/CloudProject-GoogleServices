package grpcServer.grpcServerApp.firestore.Util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class to convert a string date to a timestamp
 */
public class DateConverter {

    private static final String pattern = "yyyy/MM/dd";

    /**
     * Convert a string date to a timestamp
     * @param dateStr string date
     * @return timestamp
     */
    public static Timestamp stringToTimestamp(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate date = LocalDate.parse(dateStr, formatter);
        LocalDateTime dateTime = date.atStartOfDay();
        return Timestamp.valueOf(dateTime);
    }
}
