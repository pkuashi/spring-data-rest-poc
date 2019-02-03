package com.example.springdatarestpoc;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Item {
    @Id
    private String id;

    private String content;
}
