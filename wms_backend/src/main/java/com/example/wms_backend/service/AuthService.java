package com.example.wms_backend.service;

import com.example.wms_backend.dto.LoginDTO;
import com.example.wms_backend.dto.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO loginDTO);
}