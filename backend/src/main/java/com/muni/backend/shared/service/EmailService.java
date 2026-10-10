package com.muni.backend.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:portal-municipal@muni.gob.gt}")
    private String mailSenderFrom;

    // Envia codigo de verificacion por correo electronico
    public void enviarCodigoRecuperacion(String correoDestino, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String remitente = (mailSenderFrom != null && !mailSenderFrom.isBlank())
                    ? mailSenderFrom
                    : "portal-municipal@muni.gob.gt";
            message.setFrom(remitente);
            message.setTo(correoDestino);
            message.setSubject("Código de Verificación - Portal Municipal");
            message.setText("Su código para restablecer su contraseña es: " + codigo + "\nEste código expira en 15 minutos.");

            mailSender.send(message);
            log.info("Correo de recuperación enviado con éxito a {}", correoDestino);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de recuperación a {}: {}", correoDestino, e.getMessage());
            log.info("[DEV/TEST] CÓDIGO DE RECUPERACIÓN GENERADO PARA {}: {}", correoDestino, codigo);
        }
    }
}
