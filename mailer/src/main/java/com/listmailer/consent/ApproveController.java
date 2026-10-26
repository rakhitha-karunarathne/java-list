package com.listmailer.consent;

import com.listmailer.kernel.web.PublicHtmlPage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApproveController {

  private final ConsentService consent;

  public ApproveController(ConsentService consent) {
    this.consent = consent;
  }

  @GetMapping(value = "/approve/{token}", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> get(@PathVariable String token) {
    ConsentService.ApproveOutcome outcome = consent.approve(token);
    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(page(outcome));
  }

  static String page(ConsentService.ApproveOutcome outcome) {
    return switch (outcome) {
      case CLEARED ->
          PublicHtmlPage.simple(
              "Confirmed",
              "Your unsubscribe has been cleared. You may receive mail from these lists again.");
      case ALREADY_CLEAR ->
          PublicHtmlPage.simple(
              "Already confirmed",
              "Your unsubscribe was already cleared for these lists.");
      case EXPIRED ->
          PublicHtmlPage.simple(
              "Link expired",
              "This confirmation link has expired. Ask the list operator to send a new one.");
      default ->
          PublicHtmlPage.simple(
              "Unable to confirm",
              "This confirmation link is invalid or no longer available.");
    };
  }
}
