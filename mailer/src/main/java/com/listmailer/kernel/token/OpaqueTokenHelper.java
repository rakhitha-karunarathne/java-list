package com.listmailer.kernel.token;

/** Opaque member-token helper stub (AD-18). Real AES-GCM in a later story. */
public interface OpaqueTokenHelper {

  String seal(byte[] payload);

  byte[] open(String token);
}
