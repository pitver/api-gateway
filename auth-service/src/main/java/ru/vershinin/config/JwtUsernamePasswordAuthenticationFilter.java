package ru.vershinin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.vershinin.dto.LoginDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

public class JwtUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String HEADER = "Authorization";

    public static final String HEADER_VALUE_PREFIX = "Bearer";

    private final String signingKey;

    /**
     * Конструктор -
     * Создает сопоставление с предоставленным шаблоном, которое будет соответствовать методу HTTP.
     * Проверяет аутентификацию переданного Authentication объекта, возвращая полностью заполненный Authentication объект
     * (включая предоставленные права доступа) в случае успеха.
     * @param authenticationManager
     * @param signingKey - ключ для генерации уникального hash
     */
    public JwtUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, String signingKey) {
        super(new AntPathRequestMatcher("/v1/login", "POST"));
        setAuthenticationManager(authenticationManager);
        this.signingKey = signingKey;
    }

    /**
     * Выполняет фактическую аутентификацию.
     * Реализация должна выполнять одно из следующих действий:
     * Вернуть заполненный токен аутентификации для аутентифицированного пользователя, указывающий на успешную аутентификацию.
     * Вернуть null, указывая, что процесс аутентификации все еще продолжается.
     * Перед возвратом реализация должна выполнить любую дополнительную работу, необходимую для завершения процесса.
     * Выбрасывать исключение AuthenticationException, если процесс аутентификации завершается неудачно.
     *
     * @param request - объект из которого можно извлечь параметры и выполнить аутентификацию
     * @param response -ответ, который может потребоваться, если реализация должна выполнить перенаправление в рамках многоэтапного процесса аутентификации
     * @return - токен аутентифицированного пользователя или null, если аутентификация не завершена.
     * @throws AuthenticationException
     * @throws IOException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
            // получение объекта loginDto из запроса, в данной реализации получаем имя пользователя, пароль и роль
        LoginDto loginDto = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);
        //
        return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(),
                loginDto.getPassword(),
                Collections.emptyList()
        ));
    }

    /**
     *
     * Поведение по умолчанию для успешной аутентификации.
     * Устанавливает успешный объект аутентификации на SecurityContextHolder
     * Вызывает сконфигурированный SessionAuthenticationStrategy для обработки любого поведения,
     * связанного с сеансом (например, создание нового сеанса для защиты от атак фиксации сеанса).
     * Информирует настроенные RememberMeServices об успешном входе в систему
     * Запускает InteractiveAuthenticationSuccessEvent через настроенный ApplicationEventPublisher
     * Делегирует дополнительное поведение классу AuthenticationSuccessHandler.
     *
     * @param request - объект из которого можно извлечь параметры и выполнить аутентификацию
     *  @param response -ответ, который может потребоваться, если реализация должна выполнить перенаправление в рамках многоэтапного процесса аутентификации
     * @param chain - объект, предоставляемый контейнером сервлета разработчику, дающий представление о цепочке вызовов отфильтрованного запроса ресурса
     * @param auth - Представляет токен для запроса аутентификации или для аутентифицированного участника
     *             после обработки запроса AuthenticationManager.authenticate(Authentication)методом.
     * После аутентификации запроса аутентификация обычно сохраняется в локальном для потока SecurityContext,
     *             управляемом SecurityContextHolder используемым механизмом аутентификации.

     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication auth) {
        Instant now = Instant.now();
        //Строитель для построения JWT.
        String token = Jwts.builder()
                .setSubject(auth.getName())//проверяет, существует ли экземпляр Claims как тело JWT, после устанавливает subject поле Claims с указанным значением
                .claim("authorities", auth.getAuthorities().stream()//гарантирует, что экземпляр Claims существует как тело JWT, после устанавливает указанное свойство в экземпляре Claims
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .setIssuedAt(Date.from(now))//отметка времени, когда был создан JWT
                .setExpiration(Date.from(now.plusSeconds(24 * 60 * 60))) // token expires in 24 hours
                .signWith(SignatureAlgorithm.HS256, signingKey.getBytes())//Подписываем построенный JWT с использованием указанного алгоритма с указанным ключом, создавая JWS.
                .compact();//создаем JWT и сериализуем его в компактную URL-безопасную строку в соответствии с правилами компактной сериализации JWT
        response.addHeader(HEADER, HEADER_VALUE_PREFIX + " " + token);// добавляем в ответ заголовок с названием "Authorization" токен
    }
}
