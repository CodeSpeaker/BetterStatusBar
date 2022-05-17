package betterstatusbar.status.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final Clock CLOCK = Clock.system(ZONE_ID);

    public static String getDateTimeString(ZonedDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

    public static String getNowDateTimeString() {
        return getDateTimeString(ZonedDateTime.now(CLOCK));
    }

    public static LocalDateTime now() {
        return ZonedDateTime.now(CLOCK).toLocalDateTime();
    }

    public static LocalDate today() {
        return now().toLocalDate();
    }
}
