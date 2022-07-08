package betterstatusbar.status.listener;


import betterstatusbar.status.components.CalendarGridPanel;
import betterstatusbar.status.components.DateNumPanel;
import betterstatusbar.status.data.CalendarData;
import betterstatusbar.status.util.GridConstraintsUtil;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ShowDetailListener extends MouseAdapter {

    private JBPopup popup;

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        DateNumPanel dateNumPanel = (DateNumPanel) mouseEvent.getComponent();
        CalendarData data = dateNumPanel.getData();
        String avoid = data.getAvoid();
        String suit = data.getSuit();
        String term = data.getTerm();
        String value = data.getValue();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(3, 1, JBUI.insets(10), 0, 0);
        JBPanel<?> panel = new JBPanel<>(gridLayoutManager);
        JBLabel suitLabel = new JBLabel();
        panel.add(suitLabel, GridConstraintsUtil.getPositionGridConstraints(0, 0));
        JBLabel avoidLabel = new JBLabel();
        panel.add(avoidLabel, GridConstraintsUtil.getPositionGridConstraints(1, 0));
        JBLabel termLabel = new JBLabel();
        panel.add(termLabel, GridConstraintsUtil.getPositionGridConstraints(2, 0));

        suitLabel.setText(String.format("<html><b style='font-size:12px'>宜：</b>%s</html>", suit));
        avoidLabel.setText(String.format("<html><b style='font-size:12px'>忌：</b>%s</html>", avoid));

        String termString = " ";
        if (StringUtils.isNoneBlank(term, value)) {
            termString = term + " " + value;
        } else if (StringUtils.isNotBlank(term)) {
            termString = term;
        } else if (StringUtils.isNotBlank(value)) {
            termString = value;
        }
        termLabel.setText(String.format("<html>%s</html>", termString));
        popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null)
                .setTitle(data.getYear() + "-" + data.getMonth() + "-" + data.getDay())
                .createPopup();
        Dimension dimension = popup.getContent().getPreferredSize();
        Point at = new Point(0, -dimension.height);
        popup.show(new RelativePoint(mouseEvent.getComponent(), at));
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        if (popup != null) {
            popup.dispose();
        }
    }

    public void disposePopup() {
        popup.dispose();
    }
}
