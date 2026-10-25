package com.listmailer.parameters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parameters")
public class ParameterEntity {

  @Id
  @Column(name = "param_key", length = 120, nullable = false)
  private String key;

  @Column(name = "param_value", nullable = false, length = 2000)
  private String value;

  @Column(nullable = false, length = 1000)
  private String description;

  protected ParameterEntity() {}

  public ParameterEntity(String key, String value, String description) {
    this.key = key;
    this.value = value;
    this.description = description;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
