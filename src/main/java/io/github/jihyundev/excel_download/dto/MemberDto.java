package io.github.jihyundev.excel_download.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.github.jihyundev.excel_download.enums.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MemberDto {
    private Long id;
    private String username;
    private String realName;
    private MemberStatus status;
    private String phone;
    private String email;

    @QueryProjection
    public MemberDto(Long id, String username, String realName, MemberStatus status, String phone, String email) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.status = status;
        this.phone = phone;
        this.email = email;
    }
}
