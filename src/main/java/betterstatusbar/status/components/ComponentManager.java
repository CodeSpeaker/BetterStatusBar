package betterstatusbar.status.components;

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
        COMPONENT_MAP.put("calendarGridPanel", (Supplier<CalendarGridPanel>) CalendarGridPanel::new);

    }

    @SuppressWarnings("unchecked")
    public static <T> T getComponent(String componentName) {
        if (COMPONENT_MAP.get(componentName) instanceof Supplier) {
            return ((Supplier<T>) COMPONENT_MAP.get(componentName)).get();
        }
        return (T) COMPONENT_MAP.get(componentName);
    }
}
