package io.github.devhector.mpi_execute_api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.devhector.mpi_execute_api.service.AdminService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public String adminPanel(Model model) {
    model.addAttribute("accessKey", adminService.getAccessKey());
    model.addAttribute("maxContainers", adminService.getMaxContainers());
    return "admin";
  }

  @PostMapping("/update")
  public String updateSettings(@RequestParam String accessKey,
      @RequestParam int maxContainers) {
    adminService.updateSettings(accessKey, maxContainers);
    return "redirect:/admin?success";
  }
}
