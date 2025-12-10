package com.mycom.myapp.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor       // 기본 생성자
@AllArgsConstructor      // 모든 필드 생성자
public class UserDto {
    private String email;
    private String password;
    private String nickname;
}
