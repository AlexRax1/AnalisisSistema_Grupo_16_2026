package com.muni.backend.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // Envia codigo de verificacion por correo electronico
    public void enviarCodigoRecuperacion(String correoDestino, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tu-correo-municipal@gmail.com");
            message.setTo(correoDestino);
            message.setSubject("Código de Verificación - Portal Municipal");
            message.setText("Su código para restablecer su contraseña es: " + codigo + "\nEste código expira en 15 minutos.");

            mailSender.send(message);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de recuperación a {}: {}", correoDestino, e.getMessage());
            log.info("CODIGO TEMPORAL (DEV/OFFLINE) PARA {}: {}", correoDestino, codigo);
        }
    }
}
