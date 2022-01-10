package betterstatusbar.status;

import betterstatusbar.status.util.ScheduleUtil;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.ClickListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DateTimeStatausBarPanel extends TextPanel implements CustomStatusBarWidget {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private boolean disposed = false;

    public DateTimeStatausBarPanel(ClickListener clickListener) {
        ScheduleUtil.schedule(this::setText, 1, TimeUnit.SECONDS);

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
        disposed = true;
    }

    private void setText() {
        if (!disposed) {
            String text = LocalDateTime.now().format(FORMATTER);
            setText(text);

            long currentTimeMillis = System.currentTimeMillis();
            ScheduleUtil.schedule(this::setText, 1000 - currentTimeMillis % 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getMinimumSize();
    }
}
