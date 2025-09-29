package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Восстановление пароля - Школьные олимпиады");
            message.setText(
                    "Здравствуйте!\n\n" +
                            "Вы запросили восстановление пароля для вашего аккаунта.\n\n" +
                            "Для установки нового пароля перейдите по ссылке:\n" +
                            resetLink + "\n\n" +
                            "Ссылка действительна в течение 24 часов.\n\n" +
                            "Если вы не запрашивали восстановление пароля, проигнорируйте это письмо.\n\n" +
                            "С уважением,\n" +
                            "Команда Школьных олимпиад"
            );

            mailSender.send(message);
            System.out.println("✅ Email успешно отправлен на: " + toEmail);
            System.out.println("🔗 Ссылка для сброса: " + resetLink);

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки email на " + toEmail + ": " + e.getMessage());
            e.printStackTrace();

            // В случае ошибки все равно выводим ссылку в консоль для разработки
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            System.out.println("🔗 Ссылка для сброса (для разработки): " + resetLink);
            System.out.println("📧 Получатель: " + toEmail);

            throw new RuntimeException("Не удалось отправить email: " + e.getMessage());
        }
    }
}