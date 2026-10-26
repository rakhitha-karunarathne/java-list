package com.listmailer.campaigns;

/** How many messages this schedule run may send. */
final class SendLimits {

  private SendLimits() {}

  /** Remaining room in the rolling hour, capped by the batch size. */
  static int allowed(int batchSize, int perHour, long sentInWindow) {
    if (batchSize <= 0 || perHour <= 0) {
      return 0;
    }
    long room = perHour - Math.max(0, sentInWindow);
    if (room <= 0) {
      return 0;
    }
    return (int) Math.min(batchSize, room);
  }
}
