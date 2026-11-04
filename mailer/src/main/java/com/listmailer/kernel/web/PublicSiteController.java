package com.listmailer.kernel.web;

import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.parameters.SiteTitles;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Unauthenticated site branding for login / chrome. */
@RestController
@RequestMapping("/api/public")
public class PublicSiteController {

  private final ParametersPort parameters;

  public PublicSiteController(ParametersPort parameters) {
    this.parameters = parameters;
  }

  @GetMapping("/site-title")
  public Map<String, String> siteTitle() {
    return Map.of("siteTitle", SiteTitles.resolve(parameters));
  }
}
