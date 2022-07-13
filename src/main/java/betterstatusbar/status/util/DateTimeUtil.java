package betterstatusbar.status.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static final LocalDate MAX_DATE = LocalDate.of(2050, 11, 30);
    public static final LocalDate MIN_DATE = LocalDate.of(1900, 2, 1);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final Clock CLOCK = Clock.system(ZONE_ID);

    public static String getDateTimeString(ZonedDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

    public static String getDateTimeString(LocalDateTime dateTime) {
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
