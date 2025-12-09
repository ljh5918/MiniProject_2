package com.mycom.myapp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResultDto {
    private Long userId;
    private String email;
    private String nickname;
}
