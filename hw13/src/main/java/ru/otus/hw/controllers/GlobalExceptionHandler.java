package ru.otus.hw.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.exceptions.EntityNotFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        ModelAndView mv = new ModelAndView("error", HttpStatus.NOT_FOUND);
        mv.addObject("errorTitle", "Not Found");
        mv.addObject("errorMessage", "404 – The resource you are looking for does not exist.");
        return mv;
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ModelAndView handleAccessDeniedException(AuthorizationDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ModelAndView mv = new ModelAndView("error", HttpStatus.FORBIDDEN);
        mv.addObject("errorTitle", "Access Denied");
        mv.addObject("errorMessage", "403 - You do not have permission to access this resource.");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        ModelAndView mv = new ModelAndView("error", HttpStatus.INTERNAL_SERVER_ERROR);
        mv.addObject("errorTitle", "Something Went Wrong");
        mv.addObject("errorMessage", "An unexpected error occurred on the server.");
        return mv;
    }
}
