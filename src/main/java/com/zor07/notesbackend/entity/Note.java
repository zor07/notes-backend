package com.zor07.notesbackend.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import com.zor07.notesbackend.validation.JsonString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "note", schema = "public")
@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
public class Note {

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "note_id_seq"
  )
  @SequenceGenerator(
      name = "note_id_seq",
      sequenceName = "note_id_seq",
      allocationSize = 1
  )
  private Long id;

  @OneToOne
  @JoinColumn(name = "notebook_id", referencedColumnName = "id")
  private Notebook notebook;

  private String title;

  @Type(type = "json")
  @Column(columnDefinition = "jsonb")
  @JsonString
  private String data;

  public Note(Long id, Notebook notebook, String title, String data) {
    this.id = id;
    this.notebook = notebook;
    this.title = title;
    this.data = data;
  }

  public Note() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Notebook getNotebook() {
    return notebook;
  }

  public void setNotebook(Notebook notebook) {
    this.notebook = notebook;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
