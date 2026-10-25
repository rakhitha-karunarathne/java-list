package com.listmailer.catalogs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogReferenceServiceTest {

  @Mock CatalogReferenceRepository references;
  @InjectMocks CatalogReferenceService service;

  @Test
  void isReferencedWhenRowsExist() {
    when(references.existsByTargetTypeAndTargetId(CatalogTargetType.SENDING_PATH, "p1"))
        .thenReturn(true);
    assertTrue(service.isReferenced(CatalogTargetType.SENDING_PATH, "p1"));
  }

  @Test
  void addPersistsReference() {
    service.add(CatalogTargetType.SENDING_PATH, "p1", "LIST", "l1", "Demo");
    verify(references).save(any(CatalogReferenceEntity.class));
  }

  @Test
  void usedByMapsRows() {
    when(references.findByTargetTypeAndTargetId(CatalogTargetType.SENDING_PATH, "p1"))
        .thenReturn(
            List.of(
                new CatalogReferenceEntity(
                    "r1", CatalogTargetType.SENDING_PATH, "p1", "LIST", "l1", "Demo")));
    assertFalse(service.usedBy(CatalogTargetType.SENDING_PATH, "p1").isEmpty());
  }
}
