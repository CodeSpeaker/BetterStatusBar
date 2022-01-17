package betterstatusbar.status;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ClickListener;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;

public class PopupCalendarGrid extends ClickListener {
    CalendarGridPanel calendarGridPanel = new CalendarGridPanel();

    @Override
    public boolean onClick(@NotNull MouseEvent event, int clickCount) {
        Disposer.dispose(calendarGridPanel);
        calendarGridPanel = new CalendarGridPanel();

        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(calendarGridPanel, null)
                .setTitle("Calendar")
                .createPopup();
        Dimension dimension = popup.getContent().getPreferredSize();
        Point at = new Point(0, -dimension.height);
        popup.show(new RelativePoint(event.getComponent(), at));
        Disposer.register((Disposable) event.getComponent(), calendarGridPanel);
        return true;
    }
}
