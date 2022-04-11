package betterstatusbar.status.components;

import betterstatusbar.status.data.BaiduCalendarData;
import betterstatusbar.status.util.GridConstraintsUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.ui.JBUI;
import com.intellij.util.xml.ui.TextPanel;
import org.apache.commons.lang3.StringUtils;
import org.apache.groovy.util.Maps;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.TextAttribute;
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
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

class CalendarGridPanel extends OpaquePanel implements Disposable {

    private static final Map<String, Border> STATUS_MAP = new HashMap<>();
    private static final Map<Pair<Boolean, Boolean>, Border> BORDER_RULE_MAP = new HashMap<>();
    private static final String[] WEEKDAYS = {"日", "一", "二", "三", "四", "五", "六"};
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final String CALENDAR_URL_FORMAT = "https://sp1.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?tn=wisetpl&resource_id=39043&query=%d年%d月";
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private ZonedDateTime now = ZonedDateTime.now(ZONE_ID).truncatedTo(ChronoUnit.DAYS);
    private ZonedDateTime curMonth = ZonedDateTime.now(ZONE_ID).truncatedTo(ChronoUnit.DAYS);
    private StampedLock lock = new StampedLock();
    private JBLabel preYear;
    private JBLabel preMonth;
    private JBLabel curYearMonth;
    private JBLabel nextMonth;
    private JBLabel nextYear;

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
        Future<Map<ZonedDateTime, BaiduCalendarData>> dateMapFuture = getDateMapFuture(now.getYear(), now.getMonth().getValue());
        ZonedDateTime curDate = now.withDayOfMonth(1).minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);

        setLayout(new GridLayoutManager(9, 7, JBUI.insets(1), 2, 2));

        DateTimePanel dateTimePanel = new DateTimePanel();
        Disposer.register(this, dateTimePanel);
        add(dateTimePanel, GridConstraintsUtil.getPositionGridConstraints(0, 0, 7, 100, 160));

        preYear = new JBLabel(AllIcons.Diff.Arrow);
        preMonth = new JBLabel(AllIcons.Actions.ArrowCollapse);
        curYearMonth = new JBLabel(curMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        curYearMonth.setFont(Font.getFont(Maps.of(TextAttribute.SIZE, 15)));
        nextMonth = new JBLabel(AllIcons.Actions.ArrowExpand);
        nextYear = new JBLabel(AllIcons.Diff.ArrowRight);
        add(preYear, GridConstraintsUtil.getPositionGridConstraints(1, 1));
        add(preMonth, GridConstraintsUtil.getPositionGridConstraints(1, 2));
        add(curYearMonth, GridConstraintsUtil.getPositionGridConstraints(1, 3));
        add(nextMonth, GridConstraintsUtil.getPositionGridConstraints(1, 4));
        add(nextYear, GridConstraintsUtil.getPositionGridConstraints(1, 5));

        preYear.addMouseListener(new MouseListener(-1, 0));
        preMonth.addMouseListener(new MouseListener(0, -1));
        curYearMonth.addMouseWheelListener(new MouseListener());
        nextMonth.addMouseListener(new MouseListener(0, 1));
        nextYear.addMouseListener(new MouseListener(1, 0));

        for (int i = 0; i < 7; i++) {
            JBLabel weekdayLabel = new JBLabel(WEEKDAYS[i]);
            add(weekdayLabel, GridConstraintsUtil.getPositionGridConstraints(2, i % 7, -1, 0, 50));
        }

        Map<ZonedDateTime, BaiduCalendarData> dateMap;
        try {
            dateMap = dateMapFuture.get();
        } catch (Exception e) {
            dateMap = new HashMap<>();
        }

        for(int i = 0; i < 42; ++i) {
            BaiduCalendarData tempData = dateMap.getOrDefault(curDate, BaiduCalendarData.DEFAULT);
            DateNumPanel panel = new DateNumPanel(String.valueOf(curDate.getDayOfMonth()), tempData);

            panel.setBorder(getBorder(now, curDate, tempData.status));
            if (now.equals(curDate)) {
                panel.setBackground(new JBColor(0XFF6400, 0XFF6400));
                panel.setLabelForeground(new JBColor(0, 0));
                dateTimePanel.setInfoText(tempData.suit, tempData.avoid);
            }
            add(panel, GridConstraintsUtil.getPositionGridConstraints(i / 7 + 3, i % 7, -1, 100, 100));
            curDate = curDate.plusDays(1);
        }

        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        this.addMouseWheelListener(new MouseListener());
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
        boolean isCurrentYear = now.getYear() == curDate.getYear();
        boolean isWeekend = curDate.get(ChronoField.DAY_OF_WEEK) == 6 || curDate.get(ChronoField.DAY_OF_WEEK) == 7;
        return STATUS_MAP.getOrDefault(status, BORDER_RULE_MAP.get(Pair.create(isCurrentMonth && isCurrentYear, isWeekend)));
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

    public static class DateNumPanel extends TextPanel {
        private static ShowDetailListener showDetailListener = new ShowDetailListener();

        private BaiduCalendarData data;
        private JBLabel label = new JBLabel();
        private JBLabel lunarLabel = new JBLabel();
        private JBLabel termLabel = new JBLabel();

        private DateNumPanel(String monthDay, BaiduCalendarData tempData) {
            data = tempData;
            label.setText(monthDay);
            setLayout(new GridLayoutManager(3, 1, JBUI.insets(1), 2, 2));

            label.setHorizontalAlignment(JBLabel.CENTER);
            label.setVerticalTextPosition(JBLabel.TOP);
            add(label, GridConstraintsUtil.getPositionGridConstraints(0, 0));

            lunarLabel.setText(data.lMonth + " 月 " + data.lDate);
            add(lunarLabel, GridConstraintsUtil.getPositionGridConstraints(1, 0));

            String termString = " ";
            if (StringUtils.isNoneBlank(data.term, data.value)) {
                termString = data.term + " " + data.value;
            } else if (StringUtils.isNotBlank(data.term)) {
                termString = data.term;
            } else if (StringUtils.isNotBlank(data.value)) {
                termString = data.value;
            }
            termLabel.setText(StringUtils.abbreviate(termString, "...", 9));
            add(termLabel, GridConstraintsUtil.getPositionGridConstraints(2, 0));
            addMouseListener(showDetailListener);
        }

        private void setLabelForeground(JBColor fg) {
            label.setForeground(fg);
            lunarLabel.setForeground(fg);
        }
    }

    private static class ShowDetailListener extends MouseAdapter {

        private JBPopup popup;

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
            DateNumPanel dateNumPanel = (DateNumPanel) mouseEvent.getComponent();
            BaiduCalendarData data = dateNumPanel.data;
            String avoid = data.avoid;
            String suit = data.suit;
            String term = data.term;
            String value = data.value;

            GridLayoutManager gridLayoutManager = new GridLayoutManager(3, 1);
            JBPanel<?> panel = new JBPanel<>(gridLayoutManager);
            JBLabel suitLabel = new JBLabel();
            panel.add(suitLabel, GridConstraintsUtil.getPositionGridConstraints(0, 0));
            JBLabel avoidLabel = new JBLabel();
            panel.add(avoidLabel, GridConstraintsUtil.getPositionGridConstraints(1, 0));
            JBLabel termLabel = new JBLabel();
            panel.add(termLabel, GridConstraintsUtil.getPositionGridConstraints(2, 0));

            suitLabel.setText(String.format("<html><b style='font-size:12px'>宜：</b>%s</html>", suit));
            avoidLabel.setText(String.format("<html><b style='font-size:12px'>忌：</b>%s</html>", avoid));

            String termString = " ";
            if (StringUtils.isNoneBlank(term, value)) {
                termString = term + " " + value;
            } else if (StringUtils.isNotBlank(term)) {
                termString = term;
            } else if (StringUtils.isNotBlank(value)) {
                termString = value;
            }
            termLabel.setText(String.format("<html>%s</html>", termString));
            popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null)
                    .setTitle(data.year + "-" + data.month + "-" + data.day)
                    .createPopup();
            Dimension dimension = popup.getContent().getPreferredSize();
            Point at = new Point(0, -dimension.height);
            popup.show(new RelativePoint(mouseEvent.getComponent(), at));

            suitLabel.setPreferredSize(new Dimension(panel.getWidth(), 20));
            avoidLabel.setPreferredSize(new Dimension(panel.getWidth(), 20));
            panel.setSize(new Dimension(panel.getWidth(), 20));
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            if (popup != null) {
                popup.dispose();
            }
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

    private class MouseListener extends MouseAdapter {

        int year;
        int month;

        MouseListener(int year, int month) {
            this.year = year;
            this.month = month;
        }

        MouseListener() {

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            curMonth = curMonth.plusYears(year);
            curMonth = curMonth.plusMonths(month);
            changeText();
        }

        private void changeText() {
            curYearMonth.setText(curMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            new Thread(() -> {
                long stamp = lock.tryWriteLock();
                try {
                    if (stamp == 0L) {
                        return;
                    }
                    Thread.sleep(300);
                    Future<Map<ZonedDateTime, BaiduCalendarData>> dateMapFuture = getDateMapFuture(curMonth.getYear(), curMonth.getMonth().getValue());
                    ZonedDateTime curDate = curMonth.withDayOfMonth(1).minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);

                    Map<ZonedDateTime, BaiduCalendarData> dateMap;
                    try {
                        dateMap = dateMapFuture.get();
                    } catch (Exception e) {
                        dateMap = new HashMap<>();
                    }

                    for(int i = 0; i < 42; ++i) {
                        BaiduCalendarData tempData = dateMap.getOrDefault(curDate, BaiduCalendarData.DEFAULT);
                        DateNumPanel panel = new DateNumPanel(String.valueOf(curDate.getDayOfMonth()), tempData);

                        panel.setBorder(getBorder(now, curDate, tempData.status));
                        if (now.equals(curDate)) {
                            panel.setBackground(new JBColor(0XFF6400, 0XFF6400));
                            panel.setLabelForeground(new JBColor(0, 0));
                        }
                        add(panel, GridConstraintsUtil.getPositionGridConstraints(i / 7 + 3, i % 7, -1, 100, 100));
                        curDate = curDate.plusDays(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (stamp != 0) {
                        lock.unlockWrite(stamp);
                    }
                }
            }).start();

        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent event) {
            if (InputEvent.getModifiersExText(event.getModifiersEx()).equals(Toolkit.getProperty("AWT.shift", "Shift"))) {
                curMonth = curMonth.plusYears(event.getWheelRotation());
            } else {
                curMonth = curMonth.plusMonths(event.getWheelRotation());
            }
            changeText();
        }
    }
}
