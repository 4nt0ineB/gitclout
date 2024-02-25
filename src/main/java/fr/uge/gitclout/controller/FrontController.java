package fr.uge.gitclout.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/")
public class FrontController {
  @GetMapping("/swagger-ui")
  public ModelAndView redirectToSwaggerUI() {
    return new ModelAndView(new RedirectView("/swagger-ui/index.html", true));
  }
}
