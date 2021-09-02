package ru.vershinin.config;

import ru.vershinin.model.UserRole;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AuthenticationFilter authenticationFilter;

    public WebSecurityConfiguration(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    /**
     * определяет, какие URL пути должны быть защищены, а какие нет
     * @param http - объект HttpSecurity, позволяет настроить безопасность на базе Интернета для определенных HTTP-запросов.
     * По умолчанию он будет применяться ко всем запросам, но может быть ограничен с помощью requestMatcher(RequestMatcher)или других аналогичных методов
     * @throws Exception
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                //выключаем csrf
                .csrf().disable()
                //заявляем что, не создаем HttpSession и никогда не будет использовать его для получения SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
                .anonymous()
        .and()
                //Код состояния (401), указывающий, что запрос требует аутентификации HTTP.
                .exceptionHandling().authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .and()
                //Обрабатывает отправку формы аутентификации.
                .addFilterAfter(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                    .antMatchers("/auth/**").permitAll()
                    .anyRequest().hasRole(UserRole.USER.name());
    }
}

