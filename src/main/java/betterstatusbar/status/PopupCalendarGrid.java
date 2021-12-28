package betterstatusbar.status;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ClickListener;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;

public class PopupCalendarGrid extends ClickListener {
    @Override
    public boolean onClick(@NotNull MouseEvent event, int clickCount) {
        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(new CalendarGridPanel(), null)
                .setTitle("Calendar")
                .createPopup();
        Dimension dimension = new Dimension(100, 100);
        Point at = new Point(0, -dimension.height);
        popup.show(new RelativePoint(event.getComponent(), at));
        DateTimePanel parent = (DateTimePanel) event.getComponent();
        Disposer.register(parent, popup);
        parent.setToolTipText("show calendar");
        return true;
    }
}
