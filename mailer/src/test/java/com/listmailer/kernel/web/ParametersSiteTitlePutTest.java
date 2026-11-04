package com.listmailer.kernel.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.SendPauseCache;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.parameters.ParameterRepository;
import com.listmailer.parameters.SiteTitles;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ParametersSiteTitlePutTest {

  private ParametersPort parameters;
  private ParametersProbeController controller;

  @BeforeEach
  void setUp() {
    parameters = mock(ParametersPort.class);
    controller =
        new ParametersProbeController(parameters, mock(ParameterRepository.class), mock(SendPauseCache.class));
  }

  @Test
  void rejectsBlankSiteTitle() {
    assertThrows(
        ResponseStatusException.class,
        () -> controller.put(ParameterKeys.SITE_TITLE, new ParametersProbeController.ValueBody("  ")));
  }

  @Test
  void rejectsTooLongSiteTitle() {
    String longTitle = "x".repeat(SiteTitles.MAX_LENGTH + 1);
    assertThrows(
        ResponseStatusException.class,
        () ->
            controller.put(
                ParameterKeys.SITE_TITLE, new ParametersProbeController.ValueBody(longTitle)));
  }

  @Test
  void storesTrimmedSiteTitle() {
    when(parameters.get(ParameterKeys.SITE_TITLE)).thenReturn(Optional.of("Acme"));
    ParametersProbeController.ParameterView view =
        controller.put(
            ParameterKeys.SITE_TITLE, new ParametersProbeController.ValueBody("  Acme  "));
    assertEquals("Acme", view.value());
    verify(parameters).set(ParameterKeys.SITE_TITLE, "Acme");
  }
}
