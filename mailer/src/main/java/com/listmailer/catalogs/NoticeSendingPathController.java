package com.listmailer.catalogs;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notice-sending-path")
public class NoticeSendingPathController {

  private final NoticeSendingPathRepository notice;
  private final SendingPathRepository paths;

  public NoticeSendingPathController(
      NoticeSendingPathRepository notice, SendingPathRepository paths) {
    this.notice = notice;
    this.paths = paths;
  }

  @GetMapping
  public Map<String, String> get() {
    NoticeSendingPathEntity row = row();
    String id = row.getSendingPathId();
    return Map.of("sendingPathId", id == null ? "" : id);
  }

  @PutMapping
  public Map<String, String> put(@RequestBody NoticeBody body) {
    NoticeSendingPathEntity row = row();
    if (body == null || body.sendingPathId() == null || body.sendingPathId().isBlank()) {
      row.setSendingPathId(null);
    } else {
      if (!paths.existsById(body.sendingPathId())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown sending path");
      }
      row.setSendingPathId(body.sendingPathId());
    }
    notice.save(row);
    return get();
  }

  private NoticeSendingPathEntity row() {
    return notice
        .findById((byte) 1)
        .orElseGet(() -> notice.save(new NoticeSendingPathEntity(null)));
  }

  public record NoticeBody(String sendingPathId) {}
}
