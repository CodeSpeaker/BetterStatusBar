package betterstatusbar.status.components;

import betterstatusbar.status.ComponentManager;
import betterstatusbar.status.data.CalendarData;
import betterstatusbar.status.data.DataRepository;
import betterstatusbar.status.enums.WeekDays;
import betterstatusbar.status.listener.ShowDetailListener;
import betterstatusbar.status.util.BorderUtil;
import betterstatusbar.status.util.DateTimeUtil;
import betterstatusbar.status.util.GridConstraintsUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.concurrent.locks.StampedLock;

public class CalendarGridPanel extends OpaquePanel implements Disposable {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();
    private final StampedLock lock = new StampedLock();
    private final DataRepository dataRepository = ComponentManager.getComponent("dataRepository");
    private LocalDate curDate = DateTimeUtil.today();

    static {
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public CalendarGridPanel(){
        setLayout(new GridLayoutManager(9, 7, JBUI.insets(1), 2, 2));

        DateTimePanel dateTimePanel = ComponentManager.getComponent("dateTimePanel");
        Disposer.register(this, dateTimePanel);
        add(dateTimePanel, GridConstraintsUtil.getPositionGridConstraints(0, 0, 7, 100, 160));

        add(ComponentManager.getComponent("preYear"), GridConstraintsUtil.getPositionGridConstraints(1, 1));
        add(ComponentManager.getComponent("preMonth"), GridConstraintsUtil.getPositionGridConstraints(1, 2));
        add(ComponentManager.getComponent("curYearMonth"), GridConstraintsUtil.getPositionGridConstraints(1, 3));
        add(ComponentManager.getComponent("nextMonth"), GridConstraintsUtil.getPositionGridConstraints(1, 4));
        add(ComponentManager.getComponent("nextYear"), GridConstraintsUtil.getPositionGridConstraints(1, 5));

        for (int i = 0; i < 7; i++) {
            JBLabel weekdayLabel = new JBLabel(WeekDays.values()[i].name());
            add(weekdayLabel, GridConstraintsUtil.getPositionGridConstraints(2, i % 7, -1, 0, 50));
        }

        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    @Override
    public void dispose() {

    }

    public void plusYears(int year) {
        curDate = curDate.plusYears(year);
        if (curDate.compareTo(DateTimeUtil.MAX_DATE) > 0) {
            curDate = DateTimeUtil.MAX_DATE;
        } else if (curDate.compareTo(DateTimeUtil.MIN_DATE) < 0) {
            curDate = DateTimeUtil.MIN_DATE;
        }
    }

    public void plusMonths(int month) {
        curDate = curDate.plusMonths(month);
        if (curDate.compareTo(DateTimeUtil.MAX_DATE) > 0) {
            curDate = DateTimeUtil.MAX_DATE;
        } else if (curDate.compareTo(DateTimeUtil.MIN_DATE) < 0) {
            curDate = DateTimeUtil.MIN_DATE;
        }
    }

    public void changeText() {
        ComponentManager.getJBLabel("curYearMonth").setText(curDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        long stamp = lock.tryWriteLock();
        try {
            if (stamp == 0L) {
                return;
            }
            LocalDate iDate = curDate.withDayOfMonth(1).minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);

            for (Component component : getComponents()) {
                if (component instanceof DateNumPanel) {
                    DateNumPanel panel = (DateNumPanel) component;
                    for (java.awt.event.MouseListener mouseListener : panel.getMouseListeners()) {
                        if (mouseListener instanceof ShowDetailListener) {
                            ShowDetailListener listener = (ShowDetailListener) mouseListener;
                            listener.disposePopup();
                        }
                    }
                    remove(panel);
                }
            }

            for(int i = 0; i < 42; ++i) {
                CalendarData tempData = dataRepository.getData(iDate);
                DateNumPanel panel = new DateNumPanel(String.valueOf(iDate.getDayOfMonth()), tempData);

                panel.setBorder(BorderUtil.getBorder(curDate, iDate, tempData.getStatus()));
                if (DateTimeUtil.today().equals(iDate)) {
                    panel.setBackground(new JBColor(0XFF6400, 0XFF6400));
                    panel.setLabelForeground(new JBColor(0, 0));
                }
                add(panel, GridConstraintsUtil.getPositionGridConstraints(i / 7 + 3, i % 7, -1, 100, 100));
                iDate = iDate.plusDays(1);
            }
        } finally {
            if (stamp != 0) {
                lock.unlockWrite(stamp);
            }
        }
    }

}
