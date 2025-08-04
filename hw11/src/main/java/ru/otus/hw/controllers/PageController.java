package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String mainPageRedirect() {
        return "redirect:/list.html";
    }

    @GetMapping("/list.html")
    public String listPage() {
        return "list";
    }

    @GetMapping("/detail.html")
    public String detailPage() {
        return "detail";
    }

    @GetMapping("/edit.html")
    public String editPage() {
        return "edit";
    }
}
