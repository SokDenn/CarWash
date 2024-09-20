package org.example.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Класс обрабатывает глобальные исключения, возникающие в приложении,
 * и перенаправляет пользователей на соответствующие страницы
 * с сообщениями об ошибках.
 */
@Component
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения AccessDeniedException.
     * Перенаправляет пользователя на предыдущую страницу с сообщением о нехватке прав.
     *
     * @param request объект HttpServletRequest для получения заголовков
     * @param redirectAttributes атрибуты для передачи сообщений между запросами
     * @return RedirectView перенаправляет на предыдущую страницу или на страницу бронирований
     */
    @ExceptionHandler(AccessDeniedException.class)
    public RedirectView handleAccessDeniedException(HttpServletRequest request,
                                                    RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", "У вас нет прав для выполнения этого действия!");

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/api/reservations");
    }

    /**
     * Обрабатывает исключения DeleteAccountException.
     * Перенаправляет пользователя на предыдущую страницу с сообщением об ошибке удаления аккаунта.
     *
     * @param request объект HttpServletRequest для получения заголовков
     * @param e исключение, содержащее информацию об ошибке
     * @param redirectAttributes атрибуты для передачи сообщений между запросами
     * @return RedirectView перенаправляет на предыдущую страницу или на страницу редактирования аккаунта
     */
    @ExceptionHandler(DeleteAccountException.class)
    public RedirectView handleDeleteAccountException(HttpServletRequest request,
                                              DeleteAccountException e,
                                              RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", e.getMessage());

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/api/user/account/edit");
    }
}
