package betterstatusbar.status;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.ClickListener;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.concurrency.EdtExecutorService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DatePanel extends TextPanel implements CustomStatusBarWidget {
    private ScheduledFuture<?> myFuture;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DatePanel() {
        myFuture = EdtExecutorService.getScheduledExecutorInstance().scheduleWithFixedDelay(this::setText, 0, 1, TimeUnit.SECONDS);
        setToolTipText("show calendar");

        new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                showPopup(event);
                return true;
            }
        }.installOn(this, true);
    }

    private void showPopup(MouseEvent event) {
        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(new CalendarGridPanel(this), null)
                .setTitle("Calendar")
                .createPopup();
        Dimension dimension = new Dimension(100, 100);
        Point at = new Point(0, -dimension.height);
        popup.show(new RelativePoint(event.getComponent(), at));
        Disposer.register(this, popup);
    }

    @Override
    @NotNull
    public String ID() {
        return "DatePanel";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        if (statusBar instanceof IdeStatusBarImpl) {
            ((IdeStatusBarImpl)statusBar).setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 6));
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void dispose() {
        if (myFuture != null) {
            myFuture.cancel(true);
            myFuture = null;
        }
    }

    private void setText() {
        setText(LocalDateTime.now().format(FORMATTER));
    }

}
