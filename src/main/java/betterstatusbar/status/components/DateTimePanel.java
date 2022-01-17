package betterstatusbar.status.components;

import betterstatusbar.status.util.GridConstraintsUtil;
import betterstatusbar.status.util.ScheduleUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import org.apache.groovy.util.Maps;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DateTimePanel extends TextPanel implements Disposable {
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JBLabel dateTimeLabel;
    private JBLabel milliSecondLabel;
    private JBLabel suitLabel;
    private JBLabel avoidLabel;
    private boolean disposed = false;

    public DateTimePanel() {
        setLayout(new GridLayoutManager(4, 1, JBUI.insets(1), 2, 2));

        dateTimeLabel = getLabel(190, 30, 20, JBLabel.CENTER, null);
        add(dateTimeLabel, GridConstraintsUtil.getPositionGridConstraints(0, 0));

        milliSecondLabel = getLabel(190, 30, 20, JBLabel.CENTER, null);
        add(milliSecondLabel, GridConstraintsUtil.getPositionGridConstraints(1, 0));

        suitLabel = getLabel(700, 50, 15, -1, JBColor.GREEN);
        add(suitLabel, GridConstraintsUtil.getPositionGridConstraints(2, 0));

        avoidLabel = getLabel(700, 50, 15, -1, JBColor.RED);
        add(avoidLabel, GridConstraintsUtil.getPositionGridConstraints(3, 0));

        ScheduleUtil.schedule(this::setDateTimeText, 0, TimeUnit.MILLISECONDS);
        ScheduleUtil.schedule(this::setMilliSecondText, 0, TimeUnit.MILLISECONDS);
    }

    private JBLabel getLabel(int width, int height, int size, int align, JBColor foreground) {
        JBLabel label = new JBLabel();
        label.setMinimumSize(new Dimension(width, height));
        label.setFont(Font.getFont(Maps.of(TextAttribute.SIZE, size)));
        if (align != -1) {
            label.setHorizontalAlignment(align);
        }
        if (foreground != null) {
            label.setForeground(foreground);
        }
        return label;
    }

    private void setDateTimeText() {
        if (!disposed) {
            String text = LocalDateTime.now().format(FORMATTER);
            dateTimeLabel.setText(text);
            long currentTimeMillis = System.currentTimeMillis();
            ScheduleUtil.schedule(this::setDateTimeText, 1000 - currentTimeMillis % 1000, TimeUnit.MILLISECONDS);
        }
    }

    private void setMilliSecondText() {
        if (!disposed) {
            milliSecondLabel.setText(String.valueOf(System.currentTimeMillis()));
            ScheduleUtil.schedule(this::setMilliSecondText, 103, TimeUnit.MILLISECONDS);
        }
    }

    public void setInfoText(String suit, String avoid) {
        suitLabel.setText(String.format("<html><b style='font-size:15px'>宜：</b>%s</html>", suit));
        avoidLabel.setText(String.format("<html><b style='font-size:15px'>忌：</b>%s</html>", avoid));
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getMinimumSize();
    }

    @Override
    public void dispose() {
        disposed = true;
    }
}
