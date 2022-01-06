package betterstatusbar.status;

import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.concurrency.EdtExecutorService;
import org.apache.groovy.util.Maps;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DateTimePanel extends TextPanel implements CustomStatusBarWidget {
    private ScheduledFuture<?> myFuture;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private JBLabel label = new JBLabel();

    public DateTimePanel() {
        myFuture = EdtExecutorService.getScheduledExecutorInstance().scheduleWithFixedDelay(this::setText, 0, 17, TimeUnit.MILLISECONDS);

        label.setMinimumSize(new Dimension(190, 70));
        label.setFont(Font.getFont(Maps.of(TextAttribute.SIZE, 20)));
        label.setHorizontalAlignment(JBLabel.CENTER);
        add(label);
    }

    @Override
    @NotNull
    public String ID() {
        return "DatePanel";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {

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
        label.setText(String.format("<html><div align='center'>%s</div><div align='center'>%s</div></html>", text, System.currentTimeMillis()));
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getMinimumSize();
    }
}
