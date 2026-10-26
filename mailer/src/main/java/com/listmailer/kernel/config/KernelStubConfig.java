package com.listmailer.kernel.config;

import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.SchedulerHost;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** In-memory / no-op stubs until feature slices and adaptors land. */
@Configuration
public class KernelStubConfig {

  @Bean
  MailboxPort mailboxPort() {
    return (mailboxId, handler) -> {
      /* stub: mailbox-query wires GreenMail later */
    };
  }

  @Bean(destroyMethod = "shutdown")
  ScheduledExecutorService kernelScheduler() {
    // Per-job executors come later; host stub keeps a tiny dedicated pool (not a global app pool).
    return Executors.newScheduledThreadPool(2, r -> {
      Thread t = new Thread(r, "kernel-scheduler");
      t.setDaemon(true);
      return t;
    });
  }

  @Bean
  SchedulerHost schedulerHost(ScheduledExecutorService kernelScheduler) {
    return (jobName, job) ->
        kernelScheduler.scheduleWithFixedDelay(job, 1, 60, TimeUnit.SECONDS);
  }
}
