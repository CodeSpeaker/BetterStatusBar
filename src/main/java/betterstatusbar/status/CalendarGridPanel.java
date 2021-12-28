package betterstatusbar.status;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.time.LocalDate;
import java.time.temporal.ChronoField;

class CalendarGridPanel extends OpaquePanel {

    private DateNumPanel[] labels = new DateNumPanel[42];
    private DateTimePanel dateTimePanel = new DateTimePanel(true, null);
    private String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};

    CalendarGridPanel(){

        LocalDate now = LocalDate.now();
        LocalDate curDate = now.withDayOfMonth(1).minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);

        this.setLayout(new GridLayoutManager(8, 7, JBUI.insets(1), 2, 2));
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setRow(0);
        gridConstraints.setColumn(0);
        gridConstraints.setColSpan(7);
        gridConstraints.myMinimumSize.setSize(100, 100);
        this.add(dateTimePanel, gridConstraints);
        gridConstraints.setColSpan(1);

        gridConstraints.setRow(1);
        gridConstraints.myMinimumSize.setSize(0, 50);
        for (int i = 0; i < 7; i++) {
            DateNumPanel l = new DateNumPanel(i);
            l.setText(weekdays[i]);
            gridConstraints.setColumn(i % 7);
            add(l, gridConstraints);
        }

        gridConstraints.myMinimumSize.setSize(100, 100);
        for(int i = 0; i < 42; ++i) {
            DateNumPanel l = new DateNumPanel(i);
            this.labels[i] = l;
            l.setText(String.valueOf(curDate.getDayOfMonth()));
            if (now.getMonth().equals(curDate.getMonth())) {
                l.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, JBColor.BLUE));
            } else {
                l.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GRAY));
            }
            gridConstraints.setRow(i / 7 + 2);
            gridConstraints.setColumn(i % 7);
            add(l, gridConstraints);
            curDate = curDate.plusDays(1);
        }

        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    class DateNumPanel extends JPanel {
        private int id;
        private JLabel label = new JLabel();

        DateNumPanel(int id) {
            this.id = id;
            add(label);
        }

        void setText(String str) {
            label.setText(str);
        }

    }
}
