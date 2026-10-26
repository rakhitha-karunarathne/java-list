package com.listmailer.campaigns;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

/** Blocks fetch to loopback, link-local, and RFC1918 / other non-public targets. */
@Component
public class SsrfGuard {

  public void assertSafe(URI uri) {
    String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
    if (!"http".equals(scheme) && !"https".equals(scheme)) {
      throw new IllegalArgumentException("blocked scheme");
    }
    String host = uri.getHost();
    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("missing host");
    }
    InetAddress[] addrs;
    try {
      addrs = InetAddress.getAllByName(host);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("unresolvable host");
    }
    for (InetAddress addr : addrs) {
      if (addr.isAnyLocalAddress()
          || addr.isLoopbackAddress()
          || addr.isLinkLocalAddress()
          || addr.isSiteLocalAddress()
          || addr.isMulticastAddress()) {
        throw new IllegalArgumentException("blocked address");
      }
    }
  }
}
