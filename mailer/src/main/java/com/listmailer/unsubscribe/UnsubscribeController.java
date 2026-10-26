package com.listmailer.unsubscribe;

import com.listmailer.kernel.web.PublicHtmlPage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnsubscribeController {

  private final UnsubscribeService unsub;

  public UnsubscribeController(UnsubscribeService unsub) {
    this.unsub = unsub;
  }

  @GetMapping(value = "/unsubscribe/{token}", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> get(@PathVariable String token) {
    return html(unsub.apply(token));
  }

  /** RFC 8058 List-Unsubscribe-Post one-click (same removals as GET). */
  @PostMapping(value = "/unsubscribe/{token}", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> post(
      @PathVariable String token, @RequestBody(required = false) String body) {
    if (!isOneClickBody(body)) {
      return html(UnsubscribeService.Outcome.INVALID);
    }
    return html(unsub.apply(token));
  }

  static boolean isOneClickBody(String body) {
    if (body == null) {
      return false;
    }
    String normalized = body.replace("\r", "").trim();
    return "List-Unsubscribe=One-Click".equalsIgnoreCase(normalized);
  }

  private static ResponseEntity<String> html(UnsubscribeService.Outcome outcome) {
    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(page(outcome));
  }

  static String page(UnsubscribeService.Outcome outcome) {
    return switch (outcome) {
      case UNSUBSCRIBED ->
          PublicHtmlPage.simple(
              "Unsubscribed",
              "You have been unsubscribed from the list(s) for this message.");
      case ALREADY ->
          PublicHtmlPage.simple(
              "Already unsubscribed",
              "You were already unsubscribed from the list(s) for this message.");
      default ->
          PublicHtmlPage.simple(
              "Unable to unsubscribe",
              "This unsubscribe link is invalid or no longer available.");
    };
  }
}
