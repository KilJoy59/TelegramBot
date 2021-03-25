package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "telegram_memory")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegramMemory {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "desciption")
    private String desciption;

    @Column(name = "value")
    private String value;
}
