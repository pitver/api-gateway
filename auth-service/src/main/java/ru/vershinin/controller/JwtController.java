package ru.vershinin.controller;

import ru.vershinin.dto.ErrorDto;
import ru.vershinin.dto.JwtParseRequestDto;
import ru.vershinin.dto.JwtParseResponseDto;
import ru.vershinin.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/jwt")
public class JwtController {

    private final Logger log = LoggerFactory.getLogger(JwtController.class);

    final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Validate and parse JWT
     */
    @RequestMapping(value = "/parse", method = RequestMethod.POST)
    public ResponseEntity<?> getSomeSensitiveData(@RequestBody JwtParseRequestDto requestDto) {
        try {
            JwtParseResponseDto jwtParseResponseDto = jwtService.parseJwt(requestDto.getToken());
            return new ResponseEntity<>(jwtParseResponseDto, HttpStatus.OK);

        } catch (Exception ex) {
            log.error("JWT parsing error: {}, token: {}", ex.getLocalizedMessage(), requestDto);
            ex.printStackTrace();

            return new ResponseEntity<>(new ErrorDto(ex.getLocalizedMessage()), HttpStatus.UNAUTHORIZED);
        }
    }
}
