package com.backend.medichat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // Thêm cái này để tránh lỗi khi mapping dữ liệu
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // Thường dùng Bearer cho JWT
    private String email;
    private String fullName;

    // Constructor rút gọn nếu bạn không muốn truyền chữ "Bearer" mỗi lần
    public JwtResponse(String token, String email, String fullName) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
    }
}