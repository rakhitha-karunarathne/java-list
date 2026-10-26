package com.listmailer.campaigns;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Rewrites img/src (and href) that name a campaign blob file to public /dl URLs. */
@Component
public class CampaignHtmlRewriter {

  public String rewrite(String html, String campaignId, String publicBase, List<String> filenames) {
    if (!StringUtils.hasText(html) || filenames == null || filenames.isEmpty()) {
      return html == null ? "" : html;
    }
    String base = publicBase.endsWith("/") ? publicBase.substring(0, publicBase.length() - 1) : publicBase;
    String out = html;
    for (String name : filenames) {
      if (!StringUtils.hasText(name)) {
        continue;
      }
      String url = base + "/dl/" + campaignId + "/" + name;
      out = out.replace("src=\"" + name + "\"", "src=\"" + url + "\"");
      out = out.replace("src='" + name + "'", "src='" + url + "'");
      out = out.replace("src=\"./" + name + "\"", "src=\"" + url + "\"");
      out = out.replace("src=\"/" + name + "\"", "src=\"" + url + "\"");
    }
    return out;
  }
}
