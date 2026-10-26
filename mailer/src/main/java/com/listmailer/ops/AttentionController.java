package com.listmailer.ops;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attention")
public class AttentionController {

  private final AttentionService attention;

  public AttentionController(AttentionService attention) {
    this.attention = attention;
  }

  @GetMapping
  public List<AttentionView> inbox() {
    return attention.inboxForCurrentUser().stream().map(AttentionView::from).toList();
  }

  @PostMapping("/{id}/ack")
  public Map<String, String> ack(@PathVariable String id) {
    attention.acknowledge(id);
    return Map.of("status", "acked");
  }

  public record AttentionView(
      String id,
      String title,
      String detail,
      String kind,
      String listId,
      boolean opsHighlight,
      String createdAt) {
    static AttentionView from(AttentionItemEntity e) {
      boolean highlight = "OPS_ERROR".equals(e.getKind());
      return new AttentionView(
          e.getId(),
          e.getTitle(),
          e.getDetail(),
          e.getKind(),
          e.getListId(),
          highlight,
          e.getCreatedAt().toString());
    }
  }
}
