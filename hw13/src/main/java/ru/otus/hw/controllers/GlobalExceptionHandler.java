package ru.otus.hw.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.exceptions.EntityNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleNotFound(EntityNotFoundException ex) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("errorTitle",   "Not Found");
        mv.addObject("errorMessage", "404 – Page Not Found");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        ModelAndView mv = new ModelAndView("error", HttpStatus.INTERNAL_SERVER_ERROR);
        mv.addObject("errorTitle",   "Something went wrong");
        mv.addObject("errorMessage", ex.getMessage());
        return mv;
    }
}
