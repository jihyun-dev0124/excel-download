package io.github.jihyundev.excel_download.entity;

import io.github.jihyundev.excel_download.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;

@Entity
@Table(name = "tb_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String realName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String email;

}
