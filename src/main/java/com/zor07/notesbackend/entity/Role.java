package com.zor07.notesbackend.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "role", schema = "public")
public class Role {

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "role_id_seq"
  )
  @SequenceGenerator(
      name = "role_id_seq",
      sequenceName = "role_id_seq",
      allocationSize = 1
  )
  private Long id;
  private String name;

  public Role() {
  }

  public Role(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
