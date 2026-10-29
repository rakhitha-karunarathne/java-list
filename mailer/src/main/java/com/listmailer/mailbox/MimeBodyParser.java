package com.listmailer.mailbox;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.springframework.util.StringUtils;

/**
 * Walks a raw RFC822 message and pulls text/html bodies plus binary attachments. Jakarta Mail
 * decodes quoted-printable / base64; callers must not treat {@code RawMessage.bytes} as the body.
 *
 * <p>Inline {@code multipart/related} images (Content-ID) become attachments with {@code contentId}
 * set; {@code cid:} references in HTML are <strong>kept</strong> so send can preserve inbound CID.
 * External {@code https://} image links are left unchanged (Campaign image-fetch / embed handles them).
 */
public final class MimeBodyParser {

  private MimeBodyParser() {}

  public record Attachment(String filename, String contentType, byte[] content, String contentId) {
    public Attachment(String filename, String contentType, byte[] content) {
      this(filename, contentType, content, null);
    }
  }

  public record Parsed(String html, String text, List<Attachment> attachments) {}

  public static Parsed parse(byte[] rawBytes) {
    if (rawBytes == null || rawBytes.length == 0) {
      return new Parsed("", "", List.of());
    }
    try {
      Session session = Session.getInstance(new Properties());
      MimeMessage msg = new MimeMessage(session, new ByteArrayInputStream(rawBytes));
      Collector c = new Collector();
      walk(msg, c);
      String html = c.html == null ? "" : c.html;
      String text = c.text == null ? "" : c.text;
      if (!StringUtils.hasText(html) && StringUtils.hasText(text)) {
        html = "<p>" + escape(text) + "</p>";
      }
      return new Parsed(html, text, List.copyOf(c.attachments));
    } catch (Exception ex) {
      String fallback = new String(rawBytes, StandardCharsets.UTF_8);
      return new Parsed("<p>" + escape(fallback) + "</p>", fallback, List.of());
    }
  }

  private static void walk(Part part, Collector c) throws Exception {
    if (part.isMimeType("multipart/*")) {
      MimeMultipart multi = (MimeMultipart) part.getContent();
      for (int i = 0; i < multi.getCount(); i++) {
        walk(multi.getBodyPart(i), c);
      }
      return;
    }
    String contentId = contentId(part);
    String filename = decodeFilename(part.getFileName());
    String type = primaryType(part);
    String disposition = part.getDisposition();
    if (!StringUtils.hasText(filename)) {
      if (StringUtils.hasText(contentId)) {
        filename = filenameFromCid(contentId, type);
      } else if (type.startsWith("image/")) {
        filename = "image-" + (c.attachments.size() + 1) + extFor(type);
      }
    }
    boolean explicitAttachment =
        disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT);
    boolean namedNonInline =
        StringUtils.hasText(filename)
            && (disposition == null || !disposition.equalsIgnoreCase(Part.INLINE));
    boolean inlineBinary =
        !part.isMimeType("text/*")
            && (type.startsWith("image/")
                || (disposition != null && disposition.equalsIgnoreCase(Part.INLINE))
                || StringUtils.hasText(contentId));
    if (StringUtils.hasText(filename) && (explicitAttachment || namedNonInline || inlineBinary)) {
      byte[] bytes = readBytes(part.getInputStream());
      c.attachments.add(new Attachment(filename, type, bytes, contentId));
      rememberCid(c, contentId, filename);
      return;
    }
    if (part.isMimeType("text/html") && c.html == null) {
      c.html = readText(part);
      return;
    }
    if (part.isMimeType("text/plain") && c.text == null) {
      c.text = readText(part);
    }
  }

  private static void rememberCid(Collector c, String contentId, String filename) {
    if (!StringUtils.hasText(contentId) || !StringUtils.hasText(filename)) {
      return;
    }
    c.cidToFilename.put(contentId, filename);
    int at = contentId.indexOf('@');
    if (at > 0) {
      c.cidToFilename.putIfAbsent(contentId.substring(0, at), filename);
    }
  }

  private static String contentId(Part part) throws MessagingException {
    String[] headers = part.getHeader("Content-ID");
    if (headers == null || headers.length == 0 || !StringUtils.hasText(headers[0])) {
      return null;
    }
    String id = headers[0].trim();
    if (id.startsWith("<") && id.endsWith(">") && id.length() > 2) {
      id = id.substring(1, id.length() - 1);
    }
    return StringUtils.hasText(id) ? id : null;
  }

  private static String filenameFromCid(String contentId, String type) {
    String base = contentId.replaceAll("[^a-zA-Z0-9._-]", "_");
    if (base.isBlank()) {
      base = "inline";
    }
    if (base.length() > 180) {
      base = base.substring(0, 180);
    }
    if (!base.contains(".")) {
      base = base + extFor(type);
    }
    return base;
  }

  private static String extFor(String type) {
    if (type == null) {
      return ".bin";
    }
    return switch (type.toLowerCase(Locale.ROOT)) {
      case "image/png" -> ".png";
      case "image/gif" -> ".gif";
      case "image/webp" -> ".webp";
      case "image/jpeg", "image/jpg" -> ".jpg";
      default -> ".bin";
    };
  }

  private static String readText(Part part) throws MessagingException, IOException {
    Object content = part.getContent();
    if (content instanceof String s) {
      return s;
    }
    if (content instanceof InputStream in) {
      return new String(readBytes(in), charsetOf(part));
    }
    return content == null ? "" : content.toString();
  }

  private static Charset charsetOf(Part part) throws MessagingException {
    try {
      ContentType ct = new ContentType(part.getContentType());
      String cs = ct.getParameter("charset");
      if (StringUtils.hasText(cs)) {
        return Charset.forName(MimeUtility.javaCharset(cs));
      }
    } catch (Exception ignored) {
      // fall through
    }
    return StandardCharsets.UTF_8;
  }

  private static String primaryType(Part part) throws MessagingException {
    String ct = part.getContentType();
    if (!StringUtils.hasText(ct)) {
      return "application/octet-stream";
    }
    int semi = ct.indexOf(';');
    return (semi > 0 ? ct.substring(0, semi) : ct).trim().toLowerCase(Locale.ROOT);
  }

  private static String decodeFilename(String name) {
    if (!StringUtils.hasText(name)) {
      return null;
    }
    try {
      return MimeUtility.decodeText(name);
    } catch (Exception ex) {
      return name;
    }
  }

  private static byte[] readBytes(InputStream in) throws IOException {
    return in.readAllBytes();
  }

  private static String escape(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static final class Collector {
    String html;
    String text;
    final List<Attachment> attachments = new ArrayList<>();
    final Map<String, String> cidToFilename = new LinkedHashMap<>();
  }
}
