package com.muni.backend.quejas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCodigoRecuperacion(String correoDestino, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tu-correo-municipal@gmail.com");
        message.setTo(correoDestino);
        message.setSubject("Código de Verificación - Portal Municipal");
        message.setText("Su código para restablecer su contraseña es: " + codigo + "\nEste código expira en 15 minutos.");

        mailSender.send(message);
    }
}
