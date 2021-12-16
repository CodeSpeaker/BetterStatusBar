package betterstatusbar.status;

import javax.swing.*;
import java.awt.*;

class CalendarGridPanel extends JPanel {

    private DateNumPanel[] labels = new DateNumPanel[42];

    CalendarGridPanel(DatePanel titlePanel){
        this.setLayout(new GridLayout(6, 7, 2, 2));
        for(int i = 0; i < 42; ++i) {
            DateNumPanel l = new DateNumPanel(i);
            this.labels[i] = l;
            l.setText(String.valueOf(i));
            l.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            this.add(l);
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
