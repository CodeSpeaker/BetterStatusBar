package betterstatusbar.status.util;

import betterstatusbar.status.enums.CalBorder;
import com.intellij.openapi.util.Pair;

import javax.swing.border.Border;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

public class BorderUtil {
    private static final Map<String, Border> STATUS_MAP = new HashMap<>();
    private static final Map<Pair<Boolean, Boolean>, Border> BORDER_RULE_MAP = new HashMap<>();

    static {
        STATUS_MAP.put("1", CalBorder.REST_DAY.border);
        STATUS_MAP.put("2", CalBorder.WORK_WEEKEND.border);

        BORDER_RULE_MAP.put(Pair.pair(true, false), CalBorder.WEEKDAY_LIGHT.border);
        BORDER_RULE_MAP.put(Pair.pair(true, true), CalBorder.WEEKEND_LIGHT.border);
        BORDER_RULE_MAP.put(Pair.pair(false, false), CalBorder.WEEKDAY_DARK.border);
        BORDER_RULE_MAP.put(Pair.pair(false, true), CalBorder.WEEKEND_DARK.border);
    }

    /**
     * 规则，优先级由高到低：
     * 1、节假日，红色加粗
     * 2、周末上班，绿色
     * 3、当月，蓝色，周末加粗
     * 4、非当月，周末加粗
     * 5、其他，灰色
     */
    public static Border getBorder(LocalDate now, LocalDate curDate, String status) {
        boolean isCurrentMonth = now.getMonth().equals(curDate.getMonth());
        boolean isCurrentYear = now.getYear() == curDate.getYear();
        boolean isWeekend = curDate.get(ChronoField.DAY_OF_WEEK) == 6 || curDate.get(ChronoField.DAY_OF_WEEK) == 7;
        return STATUS_MAP.getOrDefault(status, BORDER_RULE_MAP.get(Pair.create(isCurrentMonth && isCurrentYear, isWeekend)));
    }
}
