package org.example.config;

import org.example.Jwt.JwtAuthenticationFilter;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * Конфигурационный класс для настройки безопасности приложения с использованием Spring Security.
 * Включает настройку веб-безопасности, фильтров, методов аутентификации и управления сессиями.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Настроить цепочку фильтров безопасности и управление сессиями.
     *
     * @param http объект HttpSecurity для конфигурации политики безопасности
     * @return сконфигурированная цепочка фильтров безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/v1/auth/**", "/api/users/checkUsername",
                                "/registration", "/login", "/auth.js").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/api/reservations", true) // Указываем URL для успешной аутентификации
                )
                .logout((logout) -> logout
                        .permitAll()
                        .logoutUrl("/logout")
                        .addLogoutHandler(new CookieClearingLogoutHandler("accessToken"))
                        .logoutSuccessUrl("/login?logout")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")); // Перехватчик неавторизованных запросов

        return http.build();
    }

    /**
     * Настроить менеджер аутентификации с использованием кастомного UserService и PasswordEncoder.
     *
     * @param builder объект для построения менеджера аутентификации
     */
    @Autowired
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userService)
                .passwordEncoder(passwordEncoder);
    }

    /**
     * Создать бин для кодирования паролей с использованием BCrypt с 8 раундами шифрования.
     *
     * @return объект PasswordEncoder
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    /**
     * Создать бин для менеджера аутентификации на основе текущей конфигурации.
     *
     * @param authenticationConfiguration конфигурация аутентификации
     * @return объект AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Создать бин для фильтра скрытых HTTP-методов (для поддержки метода DELETE в формах).
     *
     * @return объект FilterRegistrationBean с настроенным HiddenHttpMethodFilter
     */
    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
        FilterRegistrationBean<HiddenHttpMethodFilter> filterRegistrationBean = new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
        filterRegistrationBean.setFilter(new HiddenHttpMethodFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

}