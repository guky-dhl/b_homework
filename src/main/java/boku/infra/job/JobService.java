package boku.infra.job;

import boku.infra.command.Command;
import boku.infra.time.Timestamp;

public interface JobService {
    <R, C extends Command<R>> void scheduleCommand(C command, Timestamp when);
}
