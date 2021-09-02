package ru.vershinin.config;

import ru.vershinin.dto.JwtParseRequestDto;
import ru.vershinin.dto.JwtParseResponseDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "Authorization";

    public static final String HEADER_VALUE_PREFIX = "Bearer";

    /**
     * адрес валидации токена:auth-service
     */
    private static final String JWT_PARSE_URL = "http://auth-service/v1/jwt/parse";

    private final RestTemplate restTemplate;

    public AuthenticationFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * метод базового класса фильтра OncePerRequestFilter, который направлен на обеспечение единого исполнения
     * каждого запроса отправки на любом сервлет container.
     * также есть возможность настроить его на основе шаблона URL.
     * @param request - Объект HttpServletRequest представляет запрос клиента
     * @param response -Объект HttpServletResponse предназначен для формирования и отправки данных клиенту
     * @param filterChain - Объект предоставляемый контейнером сервлета разработчику,
     *                   дающий представление о цепочке вызовов отфильтрованного запроса ресурса
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //получаем токен из заголовка запроса
        String token = request.getHeader(HEADER);
        //если токен не пустой, то обрезаем префикс
        if (token != null) {
            token = token.replace(HEADER_VALUE_PREFIX + " ", "");

            try {
                JwtParseResponseDto responseDto = parseJwt(token);
                //Spring Security хранит основную информацию о каждом аутентифицированном пользователе
                // в ThreadLocal - представленном как объект аутентификации
                //создаем объект аутентификации в ручную и установим полученный объект Authentication в текущий SecurityContext
                //
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        responseDto.getUsername(),
                        null,
                        responseDto.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignore) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * метод передает токен по указанному ранее URL,где проверяется валидация
     * реализует связь между сервисами с помощью restTemplate
     *
     * @param token - токен полученный из заголовка запроса
     * @return - для удобного чтения возращает объект JwtParseResponseDto
     * @see JwtParseResponseDto
     */
    private JwtParseResponseDto parseJwt(String token) {
        JwtParseResponseDto responseDto = restTemplate.postForObject(JWT_PARSE_URL, new JwtParseRequestDto(token),
                JwtParseResponseDto.class);
        //Обертка requireNonNull позволяет прямо и быстро определить причину исключения.
        Objects.requireNonNull(responseDto);
        return responseDto;
    }
}
