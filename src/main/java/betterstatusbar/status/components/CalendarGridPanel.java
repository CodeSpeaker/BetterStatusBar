package betterstatusbar.status.components;

import betterstatusbar.status.data.BaiduCalendarData;
import betterstatusbar.status.util.GridConstraintsUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.ui.JBUI;
import com.intellij.util.xml.ui.TextPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class CalendarGridPanel extends OpaquePanel implements Disposable {

    private static final Map<String, Border> STATUS_MAP = new HashMap<>();
    private static final Map<Pair<Boolean, Boolean>, Border> BORDER_RULE_MAP = new HashMap<>();
    private static final String[] WEEKDAYS = {"日", "一", "二", "三", "四", "五", "六"};
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final String CALENDAR_URL_FORMAT = "https://sp1.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?tn=wisetpl&resource_id=39043&query=%d年%d月";
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    static {
        STATUS_MAP.put("1", CalBorder.REST_DAY.border);
        STATUS_MAP.put("2", CalBorder.WORK_WEEKEND.border);

        BORDER_RULE_MAP.put(Pair.pair(true, false), CalBorder.WEEKDAY_LIGHT.border);
        BORDER_RULE_MAP.put(Pair.pair(true, true), CalBorder.WEEKEND_LIGHT.border);
        BORDER_RULE_MAP.put(Pair.pair(false, false), CalBorder.WEEKDAY_DARK.border);
        BORDER_RULE_MAP.put(Pair.pair(false, true), CalBorder.WEEKEND_DARK.border);

        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    CalendarGridPanel(){
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID).truncatedTo(ChronoUnit.DAYS);
        Future<Map<ZonedDateTime, BaiduCalendarData>> dateMapFuture = getDateMapFuture(now.getYear(), now.getMonth().getValue());
        ZonedDateTime curDate = now.withDayOfMonth(1).minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);

        setLayout(new GridLayoutManager(8, 7, JBUI.insets(1), 2, 2));

        DateTimePanel dateTimePanel = new DateTimePanel();
        Disposer.register(this, dateTimePanel);
        add(dateTimePanel, GridConstraintsUtil.getPositionGridConstraints(0, 0, 7, 100, 160));

        for (int i = 0; i < 7; i++) {
            WeekPanel l = new WeekPanel(WEEKDAYS[i]);
            add(l, GridConstraintsUtil.getPositionGridConstraints(1, i % 7, -1, 0, 50));
        }

        Map<ZonedDateTime, BaiduCalendarData> dateMap;
        try {
            dateMap = dateMapFuture.get();
        } catch (Exception e) {
            dateMap = new HashMap<>();
        }

        for(int i = 0; i < 42; ++i) {
            BaiduCalendarData tempData = dateMap.getOrDefault(curDate, BaiduCalendarData.DEFAULT);
            DateNumPanel l = new DateNumPanel(String.valueOf(curDate.getDayOfMonth()), tempData.lMonth, tempData.lDate);

            l.setBorder(getBorder(now, curDate, tempData.status));
            if (now.equals(curDate)) {
                l.setBackground(new JBColor(0XFF6400, 0XFF6400));
                l.setLabelForeground(new JBColor(0, 0));
                dateTimePanel.setInfoText(tempData.suit, tempData.avoid);
            }
            add(l, GridConstraintsUtil.getPositionGridConstraints(i / 7 + 2, i % 7, -1, 100, 100));
            curDate = curDate.plusDays(1);
        }

        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    /**
     * 规则，优先级由高到低：
     * 1、节假日，红色加粗
     * 2、周末上班，绿色
     * 3、当月，蓝色，周末加粗
     * 4、非当月，周末加粗
     * 5、其他，灰色
     */
    private Border getBorder(ZonedDateTime now, ZonedDateTime curDate, String status) {
        boolean isCurrentMonth = now.getMonth().equals(curDate.getMonth());
        boolean isWeekend = curDate.get(ChronoField.DAY_OF_WEEK) == 6 || curDate.get(ChronoField.DAY_OF_WEEK) == 7;
        return STATUS_MAP.getOrDefault(status, BORDER_RULE_MAP.get(Pair.create(isCurrentMonth, isWeekend)));
    }

    @NotNull
    private Future<Map<ZonedDateTime, BaiduCalendarData>> getDateMapFuture(int year, int month) {
        return EXECUTOR_SERVICE.submit(() -> {
            Map<ZonedDateTime, BaiduCalendarData> dateMap;
            try {
                String url = String.format(CALENDAR_URL_FORMAT, year, month);
                String response = HttpRequests.request(url).readString();
                int start = response.indexOf("{");
                int end = response.lastIndexOf("}");
                String json = response.substring(start, end + 1);
                BaiduCalendarData tempData = JSON_MAPPER.readValue(json, BaiduCalendarData.class);
                dateMap = tempData.data.get(0).almanac.parallelStream().collect(Collectors.toMap(td -> Instant.parse(td.oDate).atZone(ZONE_ID), td -> td));
            } catch (Exception e) {
                e.printStackTrace();
                dateMap = new HashMap<>();
            }
            return dateMap;
        });

    }

    @Override
    public void dispose() {

    }

    private static class WeekPanel extends com.intellij.openapi.wm.impl.status.TextPanel {
        private WeekPanel(String text) {
            setText(text);
        }
    }

    private static class DateNumPanel extends TextPanel {
        private JBLabel label = new JBLabel();
        private JBLabel lunarLabel = new JBLabel();

        private DateNumPanel(String monthDay, String lunarMonth, String lunarDate) {
            label.setText(monthDay);
            setLayout(new GridLayoutManager(2, 1, JBUI.insets(1), 2, 2));

            label.setHorizontalAlignment(JBLabel.CENTER);
            label.setVerticalTextPosition(JBLabel.TOP);
            add(label, GridConstraintsUtil.getPositionGridConstraints(0, 0));

            lunarLabel.setText(lunarMonth + " 月 " + lunarDate);
            add(lunarLabel, GridConstraintsUtil.getPositionGridConstraints(1, 0));
        }

        private void setLabelForeground(JBColor fg) {
            label.setForeground(fg);
            lunarLabel.setForeground(fg);
        }
    }

    private enum CalBorder {
        REST_DAY(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.RED)),
        WORK_WEEKEND(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GREEN)),
        WEEKEND_LIGHT(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.BLUE)),
        WEEKDAY_LIGHT(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.BLUE)),
        WEEKEND_DARK(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.GRAY)),
        WEEKDAY_DARK(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GRAY)),
        ;

        private final Border border;

        CalBorder(Border border){
            this.border = border;
        }
    }
}
