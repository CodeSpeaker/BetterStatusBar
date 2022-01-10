package betterstatusbar.status.util;

import com.intellij.util.concurrency.EdtExecutorService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleUtil {

    private static ScheduledExecutorService scheduledThread = EdtExecutorService.getScheduledExecutorInstance();

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledThread.schedule(command, delay, unit);
    }
}
