package betterstatusbar.status.components;


import betterstatusbar.status.data.CalendarData;
import betterstatusbar.status.listener.ShowDetailListener;
import betterstatusbar.status.util.GridConstraintsUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.intellij.util.xml.ui.TextPanel;
import org.apache.commons.lang3.StringUtils;

public class DateNumPanel extends TextPanel {
    private static ShowDetailListener showDetailListener = new ShowDetailListener();

    private CalendarData data;
    private JBLabel label = new JBLabel();
    private JBLabel lunarLabel = new JBLabel();
    private JBLabel termLabel = new JBLabel();

    public DateNumPanel(String monthDay, CalendarData tempData) {
        data = tempData;
        label.setText(monthDay);
        setLayout(new GridLayoutManager(3, 1, JBUI.insets(1), 2, 2));

        label.setHorizontalAlignment(JBLabel.CENTER);
        label.setVerticalTextPosition(JBLabel.TOP);
        add(label, GridConstraintsUtil.getPositionGridConstraints(0, 0));

        lunarLabel.setText(data.getlMonth() + " 月 " + data.getlDate());
        add(lunarLabel, GridConstraintsUtil.getPositionGridConstraints(1, 0));

        String termString = " ";
        if (StringUtils.isNoneBlank(data.getTerm())) {
            termString = data.getTerm();
        }
        termLabel.setText(StringUtils.abbreviate(termString, "...", 9));
        add(termLabel, GridConstraintsUtil.getPositionGridConstraints(2, 0));
        addMouseListener(showDetailListener);
    }

    public void setLabelForeground(JBColor fg) {
        label.setForeground(fg);
        lunarLabel.setForeground(fg);
    }

    public CalendarData getData() {
        return data;
    }
}