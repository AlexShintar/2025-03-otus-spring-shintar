package ru.otus.hw.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {
    @RequestMapping("/access-denied")
    public ModelAndView showAccessDeniedPage() {
        ModelAndView mv = new ModelAndView("error", HttpStatus.FORBIDDEN);
        mv.addObject("errorTitle", "Access Denied");
        mv.addObject("errorMessage", "403 - You do not have permission to access this resource.");
        return mv;
    }
}
