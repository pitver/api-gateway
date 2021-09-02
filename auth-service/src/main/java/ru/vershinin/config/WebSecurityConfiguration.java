package ru.vershinin.config;

import ru.vershinin.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final String signingKey;

    /**
     * внедряем зависимость через конструктор, который в свою очередь инициализирует signingKey из файла application.yml
     * @param signingKey - ключ для генерации hash
     */
    @Autowired
    public WebSecurityConfiguration(@Value("${security.jwt.signing-key}") String signingKey) {
        this.signingKey = signingKey;
    }

    /**
     * создает пользователя In-Memory authentication
     * @param auth - объект вспомогательного класса, который упрощает настройку UserDetailService
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser("username").password(encoder.encode("password")).roles(UserRole.USER.name());
    }
    /**
     * определяет, какие URL пути должны быть защищены, а какие нет
     * @param http - объект HttpSecurity, позволяет настроить безопасность на базе Интернета для определенных HTTP-запросов.
     * По умолчанию он будет применяться ко всем запросам, но может быть ограничен с помощью requestMatcher(RequestMatcher)или других аналогичных методов
     * @throws Exception
     */

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                //выключаем csrf
                .csrf().disable()
                //заявляем что, не создаем HttpSession и никогда не будет использовать его для получения SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
                //Код состояния (401), указывающий, что запрос требует аутентификации HTTP.
                .exceptionHandling()
                .authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .and()
                //Обрабатывает отправку формы аутентификации.
                .addFilterAfter(new JwtUsernamePasswordAuthenticationFilter(authenticationManager(), signingKey), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                //данные адреса будут доступны всем
                .antMatchers("/v1/login").permitAll()
                .antMatchers("/v1/jwt/parse").permitAll()
                .anyRequest().authenticated();
    }
}