package dev.jsinco.gringotts.utility;

import dev.jsinco.gringotts.Gringotts;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class Executors {

    private static final Gringotts instance = Gringotts.getInstance();

    public static ScheduledTask runRepeatingAsync(long delay, long period, TimeUnit timeUnit, Consumer<ScheduledTask> consumer) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(instance, consumer, delay, period, timeUnit);
    }

    public static ScheduledTask runDelayedAsync(long delay, TimeUnit timeUnit, Consumer<ScheduledTask> consumer) {
        return Bukkit.getAsyncScheduler().runDelayed(instance, consumer, delay, timeUnit);
    }

    public static ScheduledTask runRepeatingAsync(long period, TimeUnit timeUnit, Consumer<ScheduledTask> consumer) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(instance, consumer, 0, period, timeUnit);
    }

    public static ScheduledTask runAsync(Consumer<ScheduledTask> consumer) {
        return Bukkit.getAsyncScheduler().runNow(instance, consumer);
    }

    public static BukkitTask runAsync(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(instance, runnable);
    }

    @Nullable
    public static BukkitTask runAsyncWithSQLException(ExceptionUtil.ThrowingSQLException runnable) {
        if (Gringotts.isShutdown()) {
            runSync(() -> ExceptionUtil.runWithSQLExceptionHandling(runnable));
            return null;
        }
        return runAsync(() -> ExceptionUtil.runWithSQLExceptionHandling(runnable));
    }


    // CompletableFuture

    public static <U> CompletableFuture<U> supplyAsyncWithSQLException(ExceptionUtil.ThrowingSQLExceptionWithReturn<U> supplier) {
        if (!Gringotts.isShutdown()) {
            return CompletableFuture.supplyAsync(() -> ExceptionUtil.runWithSQLExceptionHandling(supplier)).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
        } else {
            return CompletableFuture.completedFuture(ExceptionUtil.runWithSQLExceptionHandling(supplier));
        }
    }

    public static <U> CompletableFuture<U> supplyAsyncWithSQLException(ExceptionUtil.ThrowingSQLExceptionWithReturn<U> supplier, Executor executor) {
        if (!Gringotts.isShutdown()) {
            return CompletableFuture.supplyAsync(() -> ExceptionUtil.runWithSQLExceptionHandling(supplier), executor).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
        } else {
            return CompletableFuture.completedFuture(ExceptionUtil.runWithSQLExceptionHandling(supplier));
        }
    }

    // Synchronous

    public static BukkitTask delayedSync(long delay, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLater(instance, runnable, delay);
    }

    public static BukkitTask sync(Runnable runnable) {
        return Bukkit.getScheduler().runTask(instance, runnable);
    }

    public static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            sync(runnable);
        }
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    //thread.setName("SQLite-SingleThread");
                    thread.setDaemon(true);
                    thread.setContextClassLoader(Gringotts.class.getClassLoader()); // even tried setting the classloader
                    return thread;
                }
        );
    }

}
