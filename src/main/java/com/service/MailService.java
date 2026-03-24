package com.service;

import com.exception.MailException;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



public interface MailService {
    @Async
    void sendEmail(String recipients, String subject, String content, MultipartFile[] files) throws MailException, MessagingException;
}
