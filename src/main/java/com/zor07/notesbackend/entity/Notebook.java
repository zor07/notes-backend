package com.zor07.notesbackend.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "notebook", schema = "public")
public class Notebook {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "notebook_id_seq"
    )
    @SequenceGenerator(
            name = "notebook_id_seq",
            sequenceName = "notebook_id_seq",
            allocationSize = 1
    )
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String name;

    private String description;

    public Notebook(Long id, User user, String name, String description) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.description = description;
    }

    public Notebook() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
