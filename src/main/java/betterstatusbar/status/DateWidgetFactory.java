package betterstatusbar.status;

import betterstatusbar.status.components.DateTimeStatusBarPanel;
import betterstatusbar.status.components.PopupCalendarGridListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

public class DateWidgetFactory implements StatusBarWidgetFactory {
    @Override
    @NotNull
    public String getId() {
        return "DateTimeWidget";
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return "DateTime";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    @NotNull
    public StatusBarWidget createWidget(@NotNull Project project) {
        return new DateTimeStatusBarPanel(new PopupCalendarGridListener());
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
