package betterstatusbar.status.components;

import betterstatusbar.status.util.DateTimeUtil;
import betterstatusbar.status.util.ScheduleUtil;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.ClickListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class DateTimeStatusBarPanel extends TextPanel implements CustomStatusBarWidget {

    private boolean disposed = false;

    public DateTimeStatusBarPanel(ClickListener clickListener) {
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
            String text = DateTimeUtil.getNowDateTimeString();
            setText(text);

            long currentTimeMillis = System.currentTimeMillis();
            ScheduleUtil.schedule(this::setText, 1000 - currentTimeMillis % 1000, TimeUnit.MILLISECONDS);
        }
    }

}
