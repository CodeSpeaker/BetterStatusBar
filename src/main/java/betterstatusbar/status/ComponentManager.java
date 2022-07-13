package betterstatusbar.status;

import betterstatusbar.status.components.CalendarGridPanel;
import betterstatusbar.status.components.DateTimePanel;
import betterstatusbar.status.components.DateTimeStatusBarPanel;
import betterstatusbar.status.data.DataRepository;
import betterstatusbar.status.listener.ChangeMonthMouseListener;
import betterstatusbar.status.listener.PopupCalendarGridListener;
import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import org.apache.groovy.util.Maps;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ComponentManager {
    private static final Map<String, Object> COMPONENT_MAP = new HashMap<>();

    static {
        init();
    }

    private static void init() {
        COMPONENT_MAP.put("dateTimeStatusBarPanel", new DateTimeStatusBarPanel(new PopupCalendarGridListener()));

        COMPONENT_MAP.put("calendarGridPanel", (Supplier<CalendarGridPanel>) () -> {
            DateTimePanel dateTimePanel = new DateTimePanel();
            COMPONENT_MAP.put("dateTimePanel", dateTimePanel);

            JBLabel preYear = new JBLabel(AllIcons.Diff.Arrow);
            JBLabel preMonth = new JBLabel(AllIcons.Actions.ArrowCollapse);
            JBLabel curYearMonth = new JBLabel();
            curYearMonth.setFont(Font.getFont(Maps.of(TextAttribute.SIZE, 15)));
            JBLabel nextMonth = new JBLabel(AllIcons.Actions.ArrowExpand);
            JBLabel nextYear = new JBLabel(AllIcons.Diff.ArrowRight);

            COMPONENT_MAP.put("preYear", preYear);
            COMPONENT_MAP.put("preMonth", preMonth);
            COMPONENT_MAP.put("curYearMonth", curYearMonth);
            COMPONENT_MAP.put("nextMonth", nextMonth);
            COMPONENT_MAP.put("nextYear", nextYear);

            CalendarGridPanel calendarGridPanel = new CalendarGridPanel();

            preYear.addMouseListener(new ChangeMonthMouseListener(calendarGridPanel, -1, 0));
            preMonth.addMouseListener(new ChangeMonthMouseListener(calendarGridPanel, 0, -1));
            curYearMonth.addMouseWheelListener(new ChangeMonthMouseListener(calendarGridPanel));
            nextMonth.addMouseListener(new ChangeMonthMouseListener(calendarGridPanel, 0, 1));
            nextYear.addMouseListener(new ChangeMonthMouseListener(calendarGridPanel, 1, 0));

            calendarGridPanel.addMouseWheelListener(new ChangeMonthMouseListener(calendarGridPanel));
            calendarGridPanel.changeText();

            // 将Supplier替换为具体对象，不必每次都new
            COMPONENT_MAP.put("calendarGridPanel", calendarGridPanel);
            return calendarGridPanel;
        });

        COMPONENT_MAP.put("dataRepository", new DataRepository("data"));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getComponent(String componentName) {
        if (COMPONENT_MAP.get(componentName) instanceof Supplier) {
            // 用Supplier可以实现懒加载，并使每次getComponent都重新new一个新对象
            return ((Supplier<T>) COMPONENT_MAP.get(componentName)).get();
        }
        return (T) COMPONENT_MAP.get(componentName);
    }

    public static JBLabel getJBLabel(String componentName) {
        return getComponent(componentName);
    }
}
