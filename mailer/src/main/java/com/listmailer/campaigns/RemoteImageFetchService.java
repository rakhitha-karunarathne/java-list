package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.lists.ListEntity;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RemoteImageFetchService {

  private static final Pattern IMG_SRC =
      Pattern.compile("(?i)<img[^>]+src=[\"'](https?://[^\"']+)[\"']");

  private final CampaignAttachmentRepository attachments;
  private final SsrfGuard ssrf;
  private final ParametersPort parameters;

  public RemoteImageFetchService(
      CampaignAttachmentRepository attachments, SsrfGuard ssrf, ParametersPort parameters) {
    this.attachments = attachments;
    this.ssrf = ssrf;
    this.parameters = parameters;
  }

  public String ingest(
      String campaignId, String html, ImageFetchMode mode, List<ListEntity> selectedLists) {
    if (html == null) {
      return "";
    }
    int maxPayload = intParam(ParameterKeys.CAMPAIGN_PAYLOAD_MAX_BYTES, 2_000_000);
    int maxImage = intParam(ParameterKeys.CAMPAIGN_IMAGE_MAX_BYTES, 512_000);
    for (ListEntity list : selectedLists) {
      if (list.getPayloadOverride() != null && !list.getPayloadOverride().isBlank()) {
        try {
          maxPayload = Math.min(maxPayload, Integer.parseInt(list.getPayloadOverride().trim()));
        } catch (NumberFormatException ignored) {
          // keep system default
        }
      }
    }
    int approx = html.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    if (approx > maxPayload) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "payload exceeds ceiling (" + maxPayload + " bytes)");
    }
    if (mode == ImageFetchMode.OFF) {
      return html;
    }
    LinkedHashSet<String> urls = new LinkedHashSet<>();
    Matcher m = IMG_SRC.matcher(html);
    while (m.find()) {
      urls.add(m.group(1));
    }
    String out = html;
    List<String> failures = new ArrayList<>();
    for (String url : urls) {
      try {
        byte[] bytes = fetch(url, maxImage);
        String filename = filenameFor(url);
        attachments
            .findByCampaignIdAndFilename(campaignId, filename)
            .ifPresentOrElse(
                existing -> {},
                () ->
                    attachments.save(
                        new CampaignAttachmentEntity(
                            Guids.newGuid(),
                            campaignId,
                            filename,
                            guessContentType(url),
                            bytes)));
        out = out.replace(url, filename);
      } catch (Exception ex) {
        failures.add(url);
        if (mode == ImageFetchMode.MANDATORY) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "image fetch failed (mandatory): " + safe(ex.getMessage()));
        }
      }
    }
    if (mode == ImageFetchMode.MANDATORY && !failures.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image fetch failed (mandatory)");
    }
    return out;
  }

  private byte[] fetch(String urlStr, int maxImage) throws IOException {
    URI uri = URI.create(urlStr);
    ssrf.assertSafe(uri);
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setInstanceFollowRedirects(false);
    conn.setConnectTimeout(5_000);
    conn.setReadTimeout(10_000);
    conn.setRequestMethod("GET");
    int code = conn.getResponseCode();
    if (code < 200 || code >= 300) {
      throw new IOException("HTTP " + code);
    }
    byte[] bytes = conn.getInputStream().readAllBytes();
    if (bytes.length > maxImage) {
      throw new IOException("image exceeds ceiling");
    }
    return bytes;
  }

  private static String filenameFor(String url) {
    String path = URI.create(url).getPath();
    String base = path == null || path.isBlank() ? "image.bin" : path.substring(path.lastIndexOf('/') + 1);
    if (base.isBlank()) {
      base = "image.bin";
    }
    base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
    if (base.contains("..")) {
      base = "image.bin";
    }
    return base.length() > 200 ? base.substring(0, 200) : base;
  }

  private static String guessContentType(String url) {
    String lower = url.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".gif")) {
      return "image/gif";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "image/jpeg";
  }

  private int intParam(String key, int fallback) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                return Integer.parseInt(v.trim());
              } catch (NumberFormatException e) {
                return fallback;
              }
            })
        .orElse(fallback);
  }

  private static String safe(String msg) {
    if (msg == null) {
      return "error";
    }
    return msg.length() > 120 ? msg.substring(0, 120) : msg;
  }
}
