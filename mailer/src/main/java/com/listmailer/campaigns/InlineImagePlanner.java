package com.listmailer.campaigns;

import com.listmailer.kernel.ports.SmtpPort;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Builds outbound HTML + SMTP parts: preserve inbound CID; optionally embed fetched blobs as CID
 * under per-image and total KB caps; otherwise {@code /dl} URLs.
 */
public final class InlineImagePlanner {

  private static final Pattern IMG_SRC =
      Pattern.compile("(?i)<img[^>]+src=[\"']([^\"']+)[\"']");

  private InlineImagePlanner() {}

  public record Plan(
      String html, List<SmtpPort.Attachment> inlineParts, List<SmtpPort.Attachment> fileParts) {}

  public static Plan plan(
      boolean embedImages,
      String campaignId,
      String publicBase,
      String html,
      List<CampaignAttachmentEntity> blobs,
      int perImageMaxKb,
      int totalMaxKb) {
    String body = html == null ? "" : html;
    if (blobs == null || blobs.isEmpty()) {
      return new Plan(body, List.of(), List.of());
    }
    String base =
        publicBase.endsWith("/") ? publicBase.substring(0, publicBase.length() - 1) : publicBase;

    if (!embedImages) {
      return planFlagOff(campaignId, base, body, blobs);
    }
    return planFlagOn(campaignId, base, body, blobs, perImageMaxKb, totalMaxKb);
  }

  /** Flag off: /dl rewrite for filename imgs; all blobs as file attachments (legacy). */
  private static Plan planFlagOff(
      String campaignId, String base, String html, List<CampaignAttachmentEntity> blobs) {
    String out = html;
    List<SmtpPort.Attachment> files = new ArrayList<>();
    for (CampaignAttachmentEntity blob : blobs) {
      if (StringUtils.hasText(blob.getContentId()) && bodyContainsCid(out, blob.getContentId())) {
        // inbound CID stays CID — related part, not /dl
        continue;
      }
      String name = blob.getFilename();
      if (StringUtils.hasText(name) && htmlReferencesFilename(out, name)) {
        out = replaceFilenameSrc(out, name, base + "/dl/" + campaignId + "/" + name);
      }
      files.add(
          new SmtpPort.Attachment(
              blob.getFilename(), blob.getContentType(), blob.getContent(), null, false));
    }
    List<SmtpPort.Attachment> inline = new ArrayList<>();
    for (CampaignAttachmentEntity blob : blobs) {
      if (!StringUtils.hasText(blob.getContentId())) {
        continue;
      }
      if (!bodyContainsCid(html, blob.getContentId())
          && !bodyContainsCid(out, blob.getContentId())) {
        continue;
      }
      inline.add(
          new SmtpPort.Attachment(
              blob.getFilename(),
              blob.getContentType(),
              blob.getContent(),
              blob.getContentId().trim(),
              true));
      files.removeIf(f -> f.filename().equals(blob.getFilename()));
    }
    return new Plan(out, List.copyOf(inline), List.copyOf(files));
  }

  private static Plan planFlagOn(
      String campaignId,
      String base,
      String html,
      List<CampaignAttachmentEntity> blobs,
      int perImageMaxKb,
      int totalMaxKb) {
    long perMax = Math.max(1, perImageMaxKb) * 1024L;
    long totalMax = Math.max(1, totalMaxKb) * 1024L;
    String out = html;
    List<SmtpPort.Attachment> inline = new ArrayList<>();
    List<SmtpPort.Attachment> files = new ArrayList<>();
    Set<String> used = new HashSet<>();
    long totalInline = 0;

    for (CampaignAttachmentEntity blob : blobs) {
      if (!StringUtils.hasText(blob.getContentId())) {
        continue;
      }
      String cid = blob.getContentId().trim();
      if (!bodyContainsCid(out, cid)) {
        continue;
      }
      long size = len(blob);
      inline.add(
          new SmtpPort.Attachment(
              blob.getFilename(), blob.getContentType(), blob.getContent(), cid, true));
      used.add(blob.getId());
      totalInline += size;
    }

    for (CampaignAttachmentEntity blob : blobs) {
      if (used.contains(blob.getId())) {
        continue;
      }
      String name = blob.getFilename();
      if (!StringUtils.hasText(name) || !htmlReferencesFilename(out, name)) {
        if (!isImage(blob) && len(blob) > 0) {
          files.add(
              new SmtpPort.Attachment(
                  blob.getFilename(), blob.getContentType(), blob.getContent(), null, false));
          used.add(blob.getId());
        }
        continue;
      }
      long size = len(blob);
      if (isImage(blob) && size <= perMax && totalInline + size <= totalMax) {
        String cid = "img-" + sanitize(name);
        inline.add(
            new SmtpPort.Attachment(
                blob.getFilename(), blob.getContentType(), blob.getContent(), cid, true));
        out = replaceFilenameSrc(out, name, "cid:" + cid);
        used.add(blob.getId());
        totalInline += size;
      } else {
        out = replaceFilenameSrc(out, name, base + "/dl/" + campaignId + "/" + name);
        used.add(blob.getId());
      }
    }

    for (CampaignAttachmentEntity blob : blobs) {
      if (used.contains(blob.getId()) || isImage(blob) || len(blob) == 0) {
        continue;
      }
      files.add(
          new SmtpPort.Attachment(
              blob.getFilename(), blob.getContentType(), blob.getContent(), null, false));
    }
    return new Plan(out, List.copyOf(inline), List.copyOf(files));
  }

  private static long len(CampaignAttachmentEntity blob) {
    return blob.getContent() == null ? 0 : blob.getContent().length;
  }

  private static boolean bodyContainsCid(String html, String cid) {
    String id = stripBrackets(cid);
    return html.contains("cid:" + cid) || html.contains("cid:" + id);
  }

  private static String stripBrackets(String cid) {
    String id = cid.trim();
    if (id.startsWith("<") && id.endsWith(">") && id.length() > 2) {
      return id.substring(1, id.length() - 1);
    }
    return id;
  }

  private static boolean htmlReferencesFilename(String html, String name) {
    Matcher m = IMG_SRC.matcher(html);
    while (m.find()) {
      String src = m.group(1);
      if (name.equals(src) || ("./".concat(name)).equals(src) || ("/" + name).equals(src)) {
        return true;
      }
    }
    return false;
  }

  private static String replaceFilenameSrc(String html, String name, String replacement) {
    String out = html;
    out = out.replace("src=\"" + name + "\"", "src=\"" + replacement + "\"");
    out = out.replace("src='" + name + "'", "src='" + replacement + "'");
    out = out.replace("src=\"./" + name + "\"", "src=\"" + replacement + "\"");
    out = out.replace("src=\"/" + name + "\"", "src=\"" + replacement + "\"");
    return out;
  }

  private static boolean isImage(CampaignAttachmentEntity blob) {
    String ct = blob.getContentType() == null ? "" : blob.getContentType().toLowerCase(Locale.ROOT);
    return ct.startsWith("image/");
  }

  private static String sanitize(String name) {
    String base = name.replaceAll("[^a-zA-Z0-9._-]", "_");
    if (base.length() > 80) {
      base = base.substring(0, 80);
    }
    return base.isBlank() ? "image" : base;
  }
}
