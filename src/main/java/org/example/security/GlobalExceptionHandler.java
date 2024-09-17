package org.example.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Component
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public RedirectView handleAccessDeniedException(HttpServletRequest request,
                                                    RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", "У вас нет прав для выполнения этого действия!");

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/api/reservations");
    }

    @ExceptionHandler(ActionException.class)
    public RedirectView handleActionException(HttpServletRequest request,
                                              ActionException e,
                                              RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", e.getMessage());

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/api/user/account/edit");
    }
}
