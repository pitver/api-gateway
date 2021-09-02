package ru.vershinin.dto;

/**
 * класс парсер для токена
 */
public class JwtParseRequestDto {

    private String token;

    public JwtParseRequestDto() {
    }

    public JwtParseRequestDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
