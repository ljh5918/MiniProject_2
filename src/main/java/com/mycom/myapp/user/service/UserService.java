package com.mycom.myapp.user.service;

import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;


public interface UserService {
    UserResultDto register(UserDto userDto);
    

}