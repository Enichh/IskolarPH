package com.example.iskolarphh.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for debouncing search operations.
 * Cancels pending tasks and schedules new ones after a delay,
 * preventing excessive database queries during rapid user input.
 */
public class SearchDebounceHelper {

    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> pendingTask;
    private final long delayMs;

    /**
     * Creates a new SearchDebounceHelper with the specified delay.
     *
     * @param delayMs The delay in milliseconds to wait before executing a task
     */
    public SearchDebounceHelper(long delayMs) {
        this.delayMs = delayMs;
        this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SearchDebounceThread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Debounces the given task by canceling any pending task and scheduling a new one.
     * The task will only execute after the delay period if no new tasks are submitted.
     *
     * @param task The Runnable task to execute
     */
    public void debounce(Runnable task) {
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }
        pendingTask = executorService.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Shuts down the executor service and cleans up resources.
     * Should be called when the helper is no longer needed (e.g., in ViewModel.onCleared()).
     */
    public void shutdown() {
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }
        executorService.shutdown();
    }
}
