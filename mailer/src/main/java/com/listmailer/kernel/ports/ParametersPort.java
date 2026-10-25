package com.listmailer.kernel.ports;

import java.util.Optional;

/** Kernel Parameters port (AD-15). Persistence arrives in catalogs epic. */
public interface ParametersPort {

  Optional<String> get(String key);

  void set(String key, String value);
}
