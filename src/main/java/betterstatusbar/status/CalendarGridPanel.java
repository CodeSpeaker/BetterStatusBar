package betterstatusbar.status;

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
import java.awt.*;
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

    private static Map<String, Border> statusMap = new HashMap<>();
    private static Map<Pair<Boolean, Boolean>, Border> borderRuleMap = new HashMap<>();

    static {
        statusMap.put("1", CalBorder.REST_DAY.border);
        statusMap.put("2", CalBorder.WORK_WEEKEND.border);

        borderRuleMap.put(Pair.pair(true, false), CalBorder.WEEKDAY_LIGHT.border);
        borderRuleMap.put(Pair.pair(true, true), CalBorder.WEEKEND_LIGHT.border);
        borderRuleMap.put(Pair.pair(false, false), CalBorder.WEEKDAY_DARK.border);
        borderRuleMap.put(Pair.pair(false, true), CalBorder.WEEKEND_DARK.border);
    }

    private final ZoneId zoneId = ZoneId.of("Asia/Shanghai");
    private DateNumPanel[] labels = new DateNumPanel[42];
    private DateTimePanel dateTimePanel = new DateTimePanel();
    private String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    CalendarGridPanel(){
        Disposer.register(this, dateTimePanel);

        ZonedDateTime now = ZonedDateTime.now(zoneId).truncatedTo(ChronoUnit.DAYS);
        Future<Map<ZonedDateTime, TempData>> dateMapFuture = getDateMapFuture(now.getYear(), now.getMonth().getValue());
        ZonedDateTime curDate = now.withDayOfMonth(1).minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);

        this.setLayout(new GridLayoutManager(8, 7, JBUI.insets(1), 2, 2));
        this.add(dateTimePanel, GridConstraintsUtil.getPositionGridConstraints(0, 0, 7, 100, 100));

        for (int i = 0; i < 7; i++) {
            WeekPanel l = new WeekPanel(weekdays[i]);
            add(l, GridConstraintsUtil.getPositionGridConstraints(1, i % 7, -1, 0, 50));
        }

        Map<ZonedDateTime, TempData> dateMap;
        try {
            dateMap = dateMapFuture.get();
        } catch (Exception e) {
            dateMap = new HashMap<>();
        }

        for(int i = 0; i < 42; ++i) {
            TempData tempData = dateMap.getOrDefault(curDate, TempData.DEFAULT);
            DateNumPanel l = new DateNumPanel(String.valueOf(curDate.getDayOfMonth()), tempData.lMonth, tempData.lDate);
            this.labels[i] = l;

            l.setBorder(getBorder(now, curDate, tempData.status));
            if (now.equals(curDate)) {
                l.setBackground(new JBColor(Color.orange, new Color(255, 100, 0)));
                l.setLabelForeground(new JBColor(Color.black, Color.black));
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
        return statusMap.getOrDefault(status, borderRuleMap.get(Pair.create(isCurrentMonth, isWeekend)));
    }

    @NotNull
    private Future<Map<ZonedDateTime, TempData>> getDateMapFuture(int year, int month) {
        return executorService.submit(() -> {
            Map<ZonedDateTime, TempData> dateMap;
            try {
                String url = String.format("https://sp1.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?tn=wisetpl&resource_id=39043&query=%d年%d月", year, month);
                String response = HttpRequests.request(url).readString();
                int start = response.indexOf("{");
                int end = response.lastIndexOf("}");
                String json = response.substring(start, end + 1);
                JsonMapper jsonMapper = new JsonMapper();
                jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                TempData tempData = jsonMapper.readValue(json, TempData.class);
                dateMap = tempData.data.get(0).almanac.parallelStream().collect(Collectors.toMap(td -> Instant.parse(td.oDate).atZone(zoneId), td -> td));
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

    class WeekPanel extends TextPanel {
        private JBLabel label = new JBLabel();

        WeekPanel(String text) {
            label.setText(text);
            add(label);
        }
    }

    class DateNumPanel extends TextPanel {
        private JBLabel label = new JBLabel();
        private JBLabel lunarLabel = new JBLabel();

        DateNumPanel(String monthDay, String lunarMonth, String lunarDate) {
            label.setText(monthDay);
            setLayout(new GridLayoutManager(2, 1, JBUI.insets(1), 2, 2));

            label.setHorizontalAlignment(JBLabel.CENTER);
            label.setVerticalTextPosition(JBLabel.TOP);
            add(label, GridConstraintsUtil.getPositionGridConstraints(0, 0));

            lunarLabel.setText(lunarMonth + " 月 " + lunarDate);
            add(lunarLabel, GridConstraintsUtil.getPositionGridConstraints(1, 0));
        }

        public void setLabelForeground(Color fg) {
            label.setForeground(fg);
            lunarLabel.setForeground(fg);
        }
    }

    static class TempData {
        public static final TempData DEFAULT = new TempData();
        
        public java.util.List<TempData> data;
        public java.util.List<TempData> almanac;
        public String animal;
        public String avoid;
        public String cnDay;
        public String day;
        public String desc;
        public String gzDate;
        public String gzMonth;
        public String gzYear;
        public String isBigMonth;
        public String lDate;
        public String lMonth;
        public String lunarDate;
        public String lunarMonth;
        public String lunarYear;
        public String month;
        public String oDate;
        public String status;
        public String suit;
        public String term;
        public String type;
        public String value;
        public String year;
    }

    enum CalBorder {
        REST_DAY(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.RED)),
        WORK_WEEKEND(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GREEN)),
        WEEKEND_LIGHT(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.BLUE)),
        WEEKDAY_LIGHT(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.BLUE)),
        WEEKEND_DARK(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.GRAY)),
        WEEKDAY_DARK(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GRAY)),
        ;

        private Border border;

        CalBorder(Border border){
            this.border = border;
        }
    }
}
