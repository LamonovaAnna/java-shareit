package ru.practicum.shareit.item.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "items", schema = "public")
@Getter
@Setter
@ToString
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(name = "owner_id")
    private Long ownerId;

    @Size(max = 20)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    private String description;

    @Column(name = "available")
    private Boolean isAvailable;

    private Long requestId;
}