package com.listmailer.catalogs;

import com.listmailer.kernel.ids.Guids;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/footers")
public class FootersController {

  private final FooterRepository footers;
  private final CatalogReferenceService references;

  public FootersController(FooterRepository footers, CatalogReferenceService references) {
    this.footers = footers;
    this.references = references;
  }

  @GetMapping
  public List<FooterSummary> list() {
    return footers.findAll().stream().map(FooterSummary::from).toList();
  }

  @PostMapping
  public FooterDetail create(@RequestBody FooterRequest body) {
    validate(body);
    try {
      return FooterDetail.from(
          footers.save(
              new FooterEntity(
                  Guids.newGuid(),
                  body.name().trim(),
                  body.htmlBody(),
                  body.textBody(),
                  Instant.now())),
          List.of());
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @GetMapping("/{id}")
  public FooterDetail get(@PathVariable String id) {
    return FooterDetail.from(require(id), usedBy(id));
  }

  @PutMapping("/{id}")
  public FooterDetail update(@PathVariable String id, @RequestBody FooterRequest body) {
    FooterEntity footer = require(id);
    validate(body);
    footer.setName(body.name().trim());
    footer.setHtmlBody(body.htmlBody());
    footer.setTextBody(body.textBody());
    try {
      return FooterDetail.from(footers.save(footer), usedBy(id));
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<?> delete(@PathVariable String id) {
    require(id);
    List<UsedBy> used = usedBy(id);
    if (!used.isEmpty() || references.isReferenced(CatalogTargetType.FOOTER, id)) {
      used = usedBy(id);
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("code", "IN_USE");
      body.put("message", "Footer is referenced");
      body.put("usedBy", used);
      return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    footers.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private FooterEntity require(String id) {
    return footers
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
  }

  private List<UsedBy> usedBy(String id) {
    return references.usedBy(CatalogTargetType.FOOTER, id).stream()
        .map(r -> new UsedBy(r.getSourceType(), r.getSourceId(), r.getLabel()))
        .toList();
  }

  private static void validate(FooterRequest body) {
    if (body == null
        || !StringUtils.hasText(body.name())
        || body.htmlBody() == null
        || body.textBody() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid footer");
    }
  }

  public record FooterRequest(String name, String htmlBody, String textBody) {}

  public record FooterSummary(String id, String name) {
    static FooterSummary from(FooterEntity f) {
      return new FooterSummary(f.getId(), f.getName());
    }
  }

  public record UsedBy(String sourceType, String sourceId, String label) {}

  public record FooterDetail(
      String id, String name, String htmlBody, String textBody, List<UsedBy> usedBy) {
    static FooterDetail from(FooterEntity f, List<UsedBy> usedBy) {
      return new FooterDetail(f.getId(), f.getName(), f.getHtmlBody(), f.getTextBody(), usedBy);
    }
  }
}
