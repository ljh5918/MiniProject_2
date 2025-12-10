package com.mycom.myapp.user.service;

import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.dto.UserLoginDto; // import 추가


public interface UserService {
    UserResultDto register(UserDto userDto);
    
    //  로그인 메소드 정의
    String login(UserLoginDto userLoginDto); 
    
    // 마이페이지 메소드 저의
    UserDto getUserProfile(String email);

    
}


