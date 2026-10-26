package com.listmailer.kernel.web;

/** Minimal HTML shells for public member links (AD-17). */
public final class PublicHtmlPage {

  private PublicHtmlPage() {}

  public static String simple(String title, String body) {
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><title>"
        + title
        + "</title></head><body><h1>"
        + title
        + "</h1><p>"
        + body
        + "</p></body></html>";
  }
}
