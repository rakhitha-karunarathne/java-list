package com.listmailer.mts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "mts_mapping_lists")
@IdClass(MtsMappingListEntity.Pk.class)
public class MtsMappingListEntity {

  @Id
  @Column(name = "mapping_id", length = 36, nullable = false)
  private String mappingId;

  @Id
  @Column(name = "list_id", length = 36, nullable = false)
  private String listId;

  @Column(nullable = false)
  private int position;

  protected MtsMappingListEntity() {}

  public MtsMappingListEntity(String mappingId, String listId, int position) {
    this.mappingId = mappingId;
    this.listId = listId;
    this.position = position;
  }

  public String getMappingId() {
    return mappingId;
  }

  public String getListId() {
    return listId;
  }

  public int getPosition() {
    return position;
  }

  public static final class Pk implements Serializable {
    private String mappingId;
    private String listId;

    public Pk() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Pk pk)) return false;
      return Objects.equals(mappingId, pk.mappingId) && Objects.equals(listId, pk.listId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(mappingId, listId);
    }
  }
}
