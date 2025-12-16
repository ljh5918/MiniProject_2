package com.mycom.myapp.user.dto;

import lombok.Getter;

@Getter
public class PasswordUpdateDto {
    private String currentPassword;
    private String newPassword;
}