package betterstatusbar.status.listener;


import betterstatusbar.status.components.CalendarGridPanel;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ChangeMonthMouseListener extends MouseAdapter {

    CalendarGridPanel calendarGridPanel;
    int year;
    int month;

    public ChangeMonthMouseListener(CalendarGridPanel calendarGridPanel, int year, int month) {
        this.calendarGridPanel = calendarGridPanel;
        this.year = year;
        this.month = month;
    }

    public ChangeMonthMouseListener(CalendarGridPanel calendarGridPanel) {
        this.calendarGridPanel = calendarGridPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (year != 0) {
            calendarGridPanel.plusYears(year);
        }
        if (month != 0) {
            calendarGridPanel.plusMonths(month);
        }

        calendarGridPanel.changeText();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        if (InputEvent.getModifiersExText(event.getModifiersEx()).equals(Toolkit.getProperty("AWT.shift", "Shift"))) {
            calendarGridPanel.plusYears(event.getWheelRotation());
        } else {
            calendarGridPanel.plusMonths(event.getWheelRotation());
        }
        calendarGridPanel.changeText();
    }
}