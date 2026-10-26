package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CampaignHtmlRewriterTest {

  private final CampaignHtmlRewriter rewriter = new CampaignHtmlRewriter();

  @Test
  void rewritesImgSrcToPublicDl() {
    String out =
        rewriter.rewrite(
            "<p><img src=\"logo.png\"/></p>",
            "camp-1",
            "https://mail.example/",
            List.of("logo.png"));
    assertTrue(out.contains("src=\"https://mail.example/dl/camp-1/logo.png\""));
  }

  @Test
  void leavesUnrelatedHtml() {
    assertEquals(
        "<p>hi</p>", rewriter.rewrite("<p>hi</p>", "c", "https://mail.example", List.of("x.png")));
  }
}
