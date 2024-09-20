package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурационный класс для настройки MVC в Spring.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * Добавить контроллер для отображения страницы логина по URL "/login".
     *
     * @param registry объект для регистрации контроллеров
     */
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
    }

}
