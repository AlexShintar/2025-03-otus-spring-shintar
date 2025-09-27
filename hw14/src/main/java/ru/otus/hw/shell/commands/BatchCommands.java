package ru.otus.hw.shell.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@RequiredArgsConstructor
@ShellComponent(value = "batch command")
public class BatchCommands {

    private final Job migrateJob;

    private final JobLauncher jobLauncher;

    @ShellMethod(value = "start migration job", key = {"start", "mig"})
    public String startMigrationJob() throws Exception {
        JobExecution jobExecution = jobLauncher.run(migrateJob, new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()
        );
        return "Job finished, %s"
                .formatted(jobExecution.getExitStatus().getExitCode());
    }
}
