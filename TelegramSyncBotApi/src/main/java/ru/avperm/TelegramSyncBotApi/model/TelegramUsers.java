package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "telegram_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegramUsers {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "nickname")
    private String nickName;
}
