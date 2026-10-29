package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class InlineImagePlannerTest {

  @Test
  void flagOffUsesDlForFilenameImages() {
    byte[] img = new byte[100];
    CampaignAttachmentEntity blob =
        new CampaignAttachmentEntity("a1", "c1", "pic.png", "image/png", img);
    var plan =
        InlineImagePlanner.plan(
            false, "c1", "https://lists.example.com", "<img src=\"pic.png\"/>", List.of(blob), 256, 2048);
    assertTrue(plan.html().contains("/dl/c1/pic.png"));
    assertTrue(plan.inlineParts().isEmpty());
    assertEquals(1, plan.fileParts().size());
  }

  @Test
  void flagOnEmbedsUnderPerImageCap() {
    byte[] img = new byte[100];
    CampaignAttachmentEntity blob =
        new CampaignAttachmentEntity("a1", "c1", "pic.png", "image/png", img);
    var plan =
        InlineImagePlanner.plan(
            true, "c1", "https://lists.example.com", "<img src=\"pic.png\"/>", List.of(blob), 256, 2048);
    assertTrue(plan.html().contains("cid:img-pic.png"));
    assertEquals(1, plan.inlineParts().size());
    assertTrue(plan.inlineParts().get(0).inline());
    assertTrue(plan.fileParts().isEmpty());
  }

  @Test
  void flagOnOverPerImageUsesDl() {
    byte[] img = new byte[300 * 1024];
    CampaignAttachmentEntity blob =
        new CampaignAttachmentEntity("a1", "c1", "big.png", "image/png", img);
    var plan =
        InlineImagePlanner.plan(
            true, "c1", "https://lists.example.com", "<img src=\"big.png\"/>", List.of(blob), 256, 2048);
    assertTrue(plan.html().contains("/dl/c1/big.png"));
    assertTrue(plan.inlineParts().isEmpty());
  }

  @Test
  void totalCapDemotesLaterImages() {
    byte[] a = new byte[200 * 1024];
    byte[] b = new byte[200 * 1024];
    CampaignAttachmentEntity one =
        new CampaignAttachmentEntity("a1", "c1", "a.png", "image/png", a);
    CampaignAttachmentEntity two =
        new CampaignAttachmentEntity("a2", "c1", "b.png", "image/png", b);
    var plan =
        InlineImagePlanner.plan(
            true,
            "c1",
            "https://lists.example.com",
            "<img src=\"a.png\"/><img src=\"b.png\"/>",
            List.of(one, two),
            256,
            256);
    assertTrue(plan.html().contains("cid:img-a.png"));
    assertTrue(plan.html().contains("/dl/c1/b.png"));
    assertEquals(1, plan.inlineParts().size());
  }

  @Test
  void inboundCidPreservedEvenWhenFlagOff() {
    byte[] img = new byte[50];
    CampaignAttachmentEntity blob =
        new CampaignAttachmentEntity("a1", "c1", "logo.png", "image/png", img, "logo@dom");
    var plan =
        InlineImagePlanner.plan(
            false,
            "c1",
            "https://lists.example.com",
            "<img src=\"cid:logo@dom\"/>",
            List.of(blob),
            256,
            2048);
    assertTrue(plan.html().contains("cid:logo@dom"));
    assertFalse(plan.html().contains("/dl/"));
    assertEquals(1, plan.inlineParts().size());
    assertEquals("logo@dom", plan.inlineParts().get(0).contentId());
  }
}
