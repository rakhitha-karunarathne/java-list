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
}
