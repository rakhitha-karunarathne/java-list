package com.listmailer.kernel.token;

/** Opaque member-link tokens (AD-18). Implemented by AesGcmOpaqueTokenHelper. */
public interface OpaqueTokenHelper {

  String seal(byte[] payload);

  byte[] open(String token);
}
