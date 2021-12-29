package betterstatusbar.status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class CalendarGridPanel extends OpaquePanel {

    private final DateTimeFormatter oDateFormatter = DateTimeFormatter.ISO_INSTANT;
    private final ZoneId zoneId = ZoneId.of("Asia/Shanghai");
    private DateNumPanel[] labels = new DateNumPanel[42];
    private DateTimePanel dateTimePanel = new DateTimePanel(true, null);
    private String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    CalendarGridPanel(){
        ZonedDateTime now = ZonedDateTime.now(zoneId).truncatedTo(ChronoUnit.DAYS);
        Future<Map<ZonedDateTime, TempData>> dateMapFuture = getDateMapFuture(now.getYear(), now.getMonth().getValue());
        ZonedDateTime curDate = now.withDayOfMonth(1).minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);

        this.setLayout(new GridLayoutManager(8, 7, JBUI.insets(1), 2, 2));
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setRow(0);
        gridConstraints.setColumn(0);
        gridConstraints.setColSpan(7);
        gridConstraints.myMinimumSize.setSize(100, 100);
        this.add(dateTimePanel, gridConstraints);
        gridConstraints.setColSpan(1);

        gridConstraints.setRow(1);
        gridConstraints.myMinimumSize.setSize(0, 50);
        for (int i = 0; i < 7; i++) {
            DateNumPanel l = new DateNumPanel(i);
            l.setText(weekdays[i]);
            gridConstraints.setColumn(i % 7);
            add(l, gridConstraints);
        }

        Map<ZonedDateTime, TempData> dateMap;
        try {
            dateMap = dateMapFuture.get();
        } catch (Exception e) {
            dateMap = new HashMap<>();
        }

        gridConstraints.myMinimumSize.setSize(100, 100);
        for(int i = 0; i < 42; ++i) {
            DateNumPanel l = new DateNumPanel(i);
            this.labels[i] = l;
            l.setText(String.valueOf(curDate.getDayOfMonth()));
            setBoder(now, curDate, dateMap, l);
            gridConstraints.setRow(i / 7 + 2);
            gridConstraints.setColumn(i % 7);
            add(l, gridConstraints);
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
    private void setBoder(ZonedDateTime now, ZonedDateTime curDate, Map<ZonedDateTime, TempData> dateMap, DateNumPanel l) {
        MatteBorder border = null;
        if (dateMap.containsKey(curDate)) {
            TempData tempData = dateMap.get(curDate);
            if ("1".equals(tempData.status)) {
                border = BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.RED);
            } else if ("2".equals(tempData.status)) {
                border = BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GREEN);
            }
        }

        if (border == null) {
            if (now.getMonth().equals(curDate.getMonth())) {
                if (curDate.get(ChronoField.DAY_OF_WEEK) == 6 || curDate.get(ChronoField.DAY_OF_WEEK) == 7) {
                    border = BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.BLUE);
                } else {
                    border = BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.BLUE);
                }
            } else {
                if (curDate.get(ChronoField.DAY_OF_WEEK) == 6 || curDate.get(ChronoField.DAY_OF_WEEK) == 7) {
                    border = BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.GRAY);
                } else {
                    border = BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GRAY);
                }
            }
        }

        l.setBorder(border);
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

    class DateNumPanel extends JPanel {
        private int id;
        private JLabel label = new JLabel();

        DateNumPanel(int id) {
            this.id = id;
            add(label);
        }

        void setText(String str) {
            label.setText(str);
        }

    }

    static class TempData {
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
}
