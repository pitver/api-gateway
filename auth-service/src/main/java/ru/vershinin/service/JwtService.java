package ru.vershinin.service;

import ru.vershinin.dto.JwtParseResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JwtService {

    private final String signingKey;

    /**
     * внедряем зависимость через конструктор, который в свою очередь инициализирует signingKey из файла application.yml
     * @param signingKey - ключ для генерации hash
     */
    @Autowired
    public JwtService(@Value("${security.jwt.signing-key}") String signingKey) {
        this.signingKey = signingKey;
    }

    /**
     * Синтаксический анализатор для чтения строк JWT, используемый для преобразования их в Jwtобъект, представляющий расширенный JWT.
     * @see <a href="https://javadox.com/io.jsonwebtoken/jjwt/0.4/io/jsonwebtoken/JwtParser.html">Java JWT</a>
     * Обертка requireNonNull позволяет прямо и быстро определить причину исключения.
     * @param token - токен для проверки валидации
     * @return
     */
    public JwtParseResponseDto parseJwt(String token) {
        Objects.requireNonNull(token);
        //Набор утверждений JWT .
        //В конечном итоге это карта JSON, и к ней могут быть добавлены любые значения,
        // но для удобства стандартные имена JWT предоставляются как типобезопасные методы получения и установки.
        //Возвращает новый JwtBuilder экземпляр, который можно настроить и затем использовать для создания компактных сериализованных строк JWT
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey.getBytes())
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();//Возвращает значение JWT
        //noinspection unchecked
        List<String> authorities = claims.get("authorities", List.class);

        return new JwtParseResponseDto(username, authorities);
    }
}
