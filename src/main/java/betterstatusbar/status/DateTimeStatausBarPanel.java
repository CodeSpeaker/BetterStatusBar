package betterstatusbar.status;

import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.ClickListener;
import com.intellij.util.concurrency.EdtExecutorService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DateTimeStatausBarPanel extends TextPanel implements CustomStatusBarWidget {
    private ScheduledFuture<?> myFuture;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DateTimeStatausBarPanel(ClickListener clickListener) {
        myFuture = EdtExecutorService.getScheduledExecutorInstance().scheduleWithFixedDelay(this::setText, 0, 1, TimeUnit.SECONDS);

        if (clickListener != null) {
            setToolTipText("show calendar");
            clickListener.installOn(this, true);
        }
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
        String text = LocalDateTime.now().format(FORMATTER);
        setText(text);
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getMinimumSize();
    }
}
