package com.listmailer.kernel.ids;

import java.util.UUID;

/** UUIDv4 public ids (AD-24). */
public final class Guids {

  private Guids() {}

  public static String newGuid() {
    return UUID.randomUUID().toString();
  }
}
