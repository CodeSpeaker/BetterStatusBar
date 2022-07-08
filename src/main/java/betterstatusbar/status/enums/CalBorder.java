package betterstatusbar.status.enums;


import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.border.Border;

public enum CalBorder {
    REST_DAY(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.RED)),
    WORK_WEEKEND(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GREEN)),
    WEEKEND_LIGHT(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.BLUE)),
    WEEKDAY_LIGHT(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.BLUE)),
    WEEKEND_DARK(BorderFactory.createMatteBorder(5, 5, 5, 5, JBColor.GRAY)),
    WEEKDAY_DARK(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GRAY)),
    ;

    public final Border border;

    CalBorder(Border border){
        this.border = border;
    }
}