package betterstatusbar.status;

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
        return "DateWidget";
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return "DateWidget";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    @NotNull
    public StatusBarWidget createWidget(@NotNull Project project) {
        return new DateTimePanel(false, new PopupCalendarGrid());
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
