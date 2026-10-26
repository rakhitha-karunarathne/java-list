package com.listmailer.consent;

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
    String title;
    String body;
    switch (outcome) {
      case CLEARED -> {
        title = "Confirmed";
        body = "Your unsubscribe has been cleared. You may receive mail from these lists again.";
      }
      case ALREADY_CLEAR -> {
        title = "Already confirmed";
        body = "Your unsubscribe was already cleared for these lists.";
      }
      case EXPIRED -> {
        title = "Link expired";
        body = "This confirmation link has expired. Ask the list operator to send a new one.";
      }
      default -> {
        title = "Unable to confirm";
        body = "This confirmation link is invalid or no longer available.";
      }
    }
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><title>"
        + title
        + "</title></head><body><h1>"
        + title
        + "</h1><p>"
        + body
        + "</p></body></html>";
  }
}
