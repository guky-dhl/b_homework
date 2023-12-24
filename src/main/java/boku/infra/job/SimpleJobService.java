package boku.infra.job;

import boku.infra.command.Command;
import boku.infra.command.CommandHandler;
import boku.infra.time.Timestamp;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.Optional;
import java.util.PriorityQueue;

@Singleton
public class SimpleJobService implements JobService {
    private final PriorityQueue<Job<?, ?>> jobs = new PriorityQueue<>();

    private final CommandHandler commandHandler;
    private final Clock clock;

    @Inject
    public SimpleJobService(CommandHandler commandHandler, Clock clock) {
        this.commandHandler = commandHandler;
        this.clock = clock;
        this.start();

    }

    private void start() {
        new Thread(() -> {
            while (true) {
                this.processNext();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    void processNext() {
        var optionalJob = this.nextReady(new Timestamp(clock.millis()));
        optionalJob.ifPresent((job) -> {
            try {
                commandHandler.handle(job.command);
            } catch (Exception e) {
                System.err.printf("Job failed [%s] with error [%s] %n", job.command.getClass(), e);
            }
        });
    }

    @Override
    synchronized public <R, C extends Command<R>> void scheduleCommand(C command, Timestamp when) {
        jobs.add(new Job<>(when, command));
    }

    synchronized private Optional<Job<?, ?>> nextReady(Timestamp when) {
        var jobReady = Optional.ofNullable(jobs.peek()).map((job) -> job.when).filter((ts) -> ts.compareTo(when) >= 0).isPresent();
        if (jobReady) {
            return Optional.of(jobs.poll());
        } else {
            return Optional.empty();
        }
    }

    private record Job<R, C extends Command<R>>(Timestamp when, C command) implements Comparable<Job<?, ?>> {
        @Override
        public int compareTo(@NotNull SimpleJobService.Job<?, ?> job) {
            return this.when.compareTo(job.when);
        }
    }
}
