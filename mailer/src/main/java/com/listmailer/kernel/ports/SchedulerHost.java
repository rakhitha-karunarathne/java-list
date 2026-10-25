package com.listmailer.kernel.ports;

/** Registers in-process schedules without global multi-thread pool (AD-13). */
public interface SchedulerHost {

  void register(String jobName, Runnable job);
}
