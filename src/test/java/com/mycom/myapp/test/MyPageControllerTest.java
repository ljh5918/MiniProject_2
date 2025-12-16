package com.mycom.myapp.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.user.dto.NicknameUpdateDto;
import com.mycom.myapp.user.dto.PasswordUpdateDto;
import com.mycom.myapp.user.dto.UserLoginDto;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ⚠️ 실 DB에 반드시 존재
    private final String EMAIL = "asd@asd.com";
    private final String PASSWORD = "asd";

    /* ---------------------------------
     * 공통: JWT 발급
     --------------------------------- */
    private String getAccessToken() {
        UserLoginDto loginDto = new UserLoginDto(EMAIL, PASSWORD);
        return userService.login(loginDto);
    }

    /* ---------------------------------
     * 닉네임 변경 성공
     --------------------------------- */
    @Test
    @DisplayName("실 DB 기준: 닉네임 변경 성공")
    @WithMockUser(username = "asd@asd.com") // username = email
    void update_nickname_success() throws Exception {
        NicknameUpdateDto dto = new NicknameUpdateDto();
        dto.setNickname("newNickname123");

        mockMvc.perform(
            put("/mypage/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk());
    }

 
    /* ---------------------------------
     * 비밀번호 변경 성공
     --------------------------------- */
    @Test
    @DisplayName("실 DB 기준: 비밀번호 변경 성공")
    @WithMockUser(username = "asd@asd.com")
    void update_password_success() throws Exception {

        PasswordUpdateDto dto = new PasswordUpdateDto();
        dto.setCurrentPassword(PASSWORD);
        dto.setNewPassword("newPassword123!");

        mockMvc.perform(
                put("/mypage/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
        .andExpect(status().isOk());

        // 🔐 변경된 비밀번호로 로그인 가능해야 함
        String newToken =
                userService.login(new UserLoginDto(EMAIL, "newPassword123!"));

        assertThat(newToken).isNotBlank();
    }

    /* ---------------------------------
     * 비밀번호 변경 실패 (현재 비밀번호 틀림)
     --------------------------------- */
    @Test
    @DisplayName("실 DB 기준: 현재 비밀번호 틀리면 변경 실패")
    @WithMockUser(username = "asd@asd.com")
    void update_password_fail_wrong_current_password() throws Exception {

        PasswordUpdateDto dto = new PasswordUpdateDto();
        dto.setCurrentPassword("wrongPassword");
        dto.setNewPassword("anyPassword");

        mockMvc.perform(
                put("/mypage/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
        .andExpect(status().isBadRequest());
    }
}