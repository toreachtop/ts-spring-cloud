package com.example.springboot.demo.controller;

import com.toreachtop.email.spring.boot.starter.model.ResultContent;
import com.toreachtop.email.spring.boot.starter.template.MailSenderTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jd
 *
 * 发送邮件
 */
@RestController
@RequestMapping("/send")
public class MailController {

    @Autowired
    private MailSenderTemplate mailSenderTemplate;

    @GetMapping
    public ResultContent sendMail(@RequestParam("to") String to,
                                  @RequestParam("conetnt") String content,
                                  @RequestParam("subject") String subject) {
        return mailSenderTemplate.send(to, content, subject);
    }
}
