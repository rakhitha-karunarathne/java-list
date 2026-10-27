package com.listmailer.mailbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class MimeBodyParserTest {

  @Test
  void extractsHtmlTextAndAttachmentFromMixedAlternative() throws Exception {
    byte[] pdf = "%PDF-1.4 fake receipt".getBytes(StandardCharsets.US_ASCII);
    MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
    msg.setSubject("Payment Receipt for 562459");
    msg.setFrom("hostmaster@domains.lk");

    MimeMultipart alternative = new MimeMultipart("alternative");
    MimeBodyPart plain = new MimeBodyPart();
    plain.setText("Dear LK Customer,\nPlease find the attached Receipt.", "UTF-8");
    MimeBodyPart html = new MimeBodyPart();
    html.setContent(
        "<div dir=\"ltr\"><p>Dear LK Customer,</p><p>Order# <b>562459</b>.</p></div>",
        "text/html; charset=UTF-8");
    alternative.addBodyPart(plain);
    alternative.addBodyPart(html);

    MimeBodyPart body = new MimeBodyPart();
    body.setContent(alternative);

    MimeBodyPart file = new MimeBodyPart();
    file.setFileName("Receipt_562459.pdf");
    file.setContent(pdf, "application/pdf");
    file.setDisposition(MimeBodyPart.ATTACHMENT);

    MimeMultipart mixed = new MimeMultipart("mixed");
    mixed.addBodyPart(body);
    mixed.addBodyPart(file);
    msg.setContent(mixed);
    msg.saveChanges();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    msg.writeTo(bos);
    byte[] raw = bos.toByteArray();
    // sanity: raw dump still contains base64/boundary noise (the old bug surface)
    assertTrue(new String(raw, StandardCharsets.UTF_8).contains("Content-Type:"));

    MimeBodyParser.Parsed parsed = MimeBodyParser.parse(raw);

    assertTrue(parsed.html().contains("Dear LK Customer"));
    assertTrue(parsed.html().contains("562459"));
    assertFalse(parsed.html().contains("JVBERi0"));
    assertFalse(parsed.html().contains("Content-Transfer-Encoding"));
    assertTrue(parsed.text().contains("Dear LK Customer"));
    assertFalse(parsed.text().contains("%PDF"));
    assertEquals(1, parsed.attachments().size());
    assertEquals("Receipt_562459.pdf", parsed.attachments().get(0).filename());
    assertEquals("application/pdf", parsed.attachments().get(0).contentType());
    assertEquals(new String(pdf, StandardCharsets.US_ASCII), new String(parsed.attachments().get(0).content(), StandardCharsets.US_ASCII));
  }

  @Test
  void plainOnlyMessage() throws Exception {
    MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
    msg.setText("hello only", "UTF-8");
    msg.saveChanges();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    msg.writeTo(bos);

    MimeBodyParser.Parsed parsed = MimeBodyParser.parse(bos.toByteArray());
    assertEquals("hello only", parsed.text().trim());
    assertTrue(parsed.html().contains("hello only"));
    assertTrue(parsed.attachments().isEmpty());
  }

  @Test
  void multipartRelatedInlineCidImageBecomesAttachmentAndRewritesHtml() throws Exception {
    byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
    MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
    msg.setSubject("Logo mail");
    msg.setFrom("author@example.com");

    MimeBodyPart html = new MimeBodyPart();
    html.setContent(
        "<p>Hi</p><img src=\"cid:logo@domain\" alt=\"logo\"/>", "text/html; charset=UTF-8");

    MimeBodyPart image = new MimeBodyPart();
    image.setContent(png, "image/png");
    image.setHeader("Content-ID", "<logo@domain>");
    image.setDisposition(MimeBodyPart.INLINE);

    MimeMultipart related = new MimeMultipart("related");
    related.addBodyPart(html);
    related.addBodyPart(image);
    msg.setContent(related);
    msg.saveChanges();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    msg.writeTo(bos);
    MimeBodyParser.Parsed parsed = MimeBodyParser.parse(bos.toByteArray());

    assertEquals(1, parsed.attachments().size());
    assertEquals("image/png", parsed.attachments().get(0).contentType());
    assertEquals(png.length, parsed.attachments().get(0).content().length);
    assertFalse(parsed.html().contains("cid:"));
    assertTrue(parsed.html().contains("src=\"" + parsed.attachments().get(0).filename() + "\""));
    assertTrue(parsed.html().contains("<p>Hi</p>"));
  }

  @Test
  void htmlWithExternalImageLinkKeepsHttpsSrcAndNoImageAttachment() throws Exception {
    MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
    msg.setSubject("External img");
    msg.setFrom("author@example.com");

    MimeMultipart alternative = new MimeMultipart("alternative");
    MimeBodyPart plain = new MimeBodyPart();
    plain.setText("See the banner", "UTF-8");
    MimeBodyPart html = new MimeBodyPart();
    html.setContent(
        "<p>See the banner</p><img src=\"https://cdn.example.com/banner.png\" width=\"100\"/>",
        "text/html; charset=UTF-8");
    alternative.addBodyPart(plain);
    alternative.addBodyPart(html);
    msg.setContent(alternative);
    msg.saveChanges();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    msg.writeTo(bos);
    MimeBodyParser.Parsed parsed = MimeBodyParser.parse(bos.toByteArray());

    assertTrue(parsed.attachments().isEmpty());
    assertTrue(parsed.html().contains("src=\"https://cdn.example.com/banner.png\""));
    assertFalse(parsed.html().contains("cid:"));
  }

  @Test
  void relatedCidPlusExternalLinkKeepsBothForms() throws Exception {
    byte[] gif = new byte[] {'G', 'I', 'F', '8', '9', 'a'};
    MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
    msg.setSubject("Mixed images");
    msg.setFrom("author@example.com");

    MimeBodyPart html = new MimeBodyPart();
    html.setContent(
        "<div><img src=\"cid:ii_local\"/><img src=\"https://cdn.example.com/ext.jpg\"/></div>",
        "text/html; charset=UTF-8");
    MimeBodyPart image = new MimeBodyPart();
    image.setFileName("pixel.gif");
    image.setContent(gif, "image/gif");
    image.setHeader("Content-ID", "<ii_local>");
    image.setDisposition(MimeBodyPart.INLINE);

    MimeMultipart related = new MimeMultipart("related");
    related.addBodyPart(html);
    related.addBodyPart(image);
    msg.setContent(related);
    msg.saveChanges();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    msg.writeTo(bos);
    MimeBodyParser.Parsed parsed = MimeBodyParser.parse(bos.toByteArray());

    assertEquals(1, parsed.attachments().size());
    assertEquals("pixel.gif", parsed.attachments().get(0).filename());
    assertTrue(parsed.html().contains("src=\"pixel.gif\""));
    assertTrue(parsed.html().contains("src=\"https://cdn.example.com/ext.jpg\""));
    assertFalse(parsed.html().contains("cid:"));
  }
}
