package com.eric.store.common.controller;

import com.eric.store.auth.service.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final EmailService emailService;
    private final String adminEmail;

    public ContactController(EmailService emailService,
                             @Value("${app.admin.seed-email}") String adminEmail) {
        this.emailService = emailService;
        this.adminEmail = adminEmail;
    }

    public record ContactRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 100) String email,
            @NotBlank @Size(max = 200) String subject,
            @NotBlank @Size(max = 2000) String message
    ) {}

    @PostMapping
    public ResponseEntity<Void> sendContactMessage(@Valid @RequestBody ContactRequest req) {
        String html = """
                <h3>New Contact Form Message</h3>
                <p><strong>From:</strong> %s (%s)</p>
                <p><strong>Subject:</strong> %s</p>
                <hr/>
                <p>%s</p>
                """.formatted(
                escapeHtml(req.name()),
                escapeHtml(req.email()),
                escapeHtml(req.subject()),
                escapeHtml(req.message()).replace("\n", "<br/>")
        );

        emailService.sendEmail(adminEmail, "Contact Form: " + req.subject(), html);
        return ResponseEntity.noContent().build();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
