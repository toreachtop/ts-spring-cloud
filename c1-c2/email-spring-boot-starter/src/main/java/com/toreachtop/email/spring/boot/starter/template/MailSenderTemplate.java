package com.toreachtop.email.spring.boot.starter.template;

import cn.hutool.core.util.ReUtil;
import com.toreachtop.email.spring.boot.starter.model.ResultContent;
import com.toreachtop.email.spring.boot.starter.model.dto.MailDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "reach.email.enable", havingValue = "true")
public class MailSenderTemplate {

    //注入Spring Boot提供的mail中的邮件发送类
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;


    /**
     * 邮件发送
     *
     * @param to      收件邮箱
     * @param content 内容
     * @param subject 主题
     * @return ResultContent
     */
    public ResultContent send(String to, String content, String subject) {
        return send(MailDto.builder().to(to).content(content).subject(subject).build());
    }

    /**
     * 发送邮件(抄送)
     *
     * @param to      收件邮箱
     * @param content 内容
     * @param subject 主题
     * @param cc      抄送人
     * @return ResultContent
     */
    public ResultContent send(String to, String content, String subject, String cc) {
        return send(MailDto.builder().to(to).content(content).subject(subject).cc(cc).build());
    }

    /**
     * 发送模版邮件
     *
     * @param to       收件邮箱
     * @param model    模版参数
     * @param template 模版
     * @param subject  主题
     * @return ResultContent
     */
    public ResultContent sendTemplate(String to, Map<String, Object> model, String template, String subject) {
        return send(MailDto.builder().to(to).content(getTemplateStr(model, template)).subject(subject).build());
    }

    /**
     * 发送模版邮件(带抄送)
     *
     * @param to       收件邮箱
     * @param model    模版参数
     * @param template 模版
     * @param subject  主题
     * @param cc       抄送人
     * @return ResultContent
     */
    public ResultContent sendTemplate(String to, Map<String, Object> model, String template, String subject, String cc) {
        return send(MailDto.builder().to(to).content(getTemplateStr(model, template)).subject(subject).cc(cc).build());
    }

    /**
     * 解析freemarker模版
     **/
    private String getTemplateStr(Map<String, Object> model, String template) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfigurer.getConfiguration().getTemplate(template), model);
        } catch (Exception e) {
            log.error("获取模版数据异常：{}", e.getMessage(), e);
        }
        return "";
    }

    /**
     * 发送邮件
     **/
    public ResultContent send(MailDto mailDto) {

        if (StringUtils.isAnyBlank(mailDto.getTo(), mailDto.getContent())) {
            return ResultContent.buildError("接收人或邮件内容不能为空");
        }

        String[] tos = filterEmail(mailDto.getTo().split(","));
        if (tos == null) {
            log.error("邮件发送失败，接收人邮箱格式不正确：{}", mailDto.getTo());
            return ResultContent.buildError("邮件发送失败，接收人邮箱格式不正确");
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(from);
            helper.setTo(tos);
            helper.setText(mailDto.getContent(), true);
            helper.setSubject(mailDto.getSubject());

            //抄送
            if (StringUtils.isNotBlank(mailDto.getCc())) {
                String[] ccs = filterEmail(mailDto.getCc().split(","));
                if (ccs != null) {
                    helper.setCc(ccs);
                }
            }

            //秘抄
            if (StringUtils.isNotBlank(mailDto.getBcc())) {
                String[] bccs = filterEmail(mailDto.getBcc().split(","));
                if (bccs != null) {
                    helper.setBcc(bccs);
                }
            }

            //定时发送
            if (mailDto.getSendDate() != null) {
                helper.setSentDate(mailDto.getSendDate());
            }

            //附件
            File[] files = mailDto.getFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    helper.addAttachment(file.getName(), file);
                }
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("邮件发送异常：{}", e.getMessage(), e);
            return ResultContent.buildError("邮件发送异常：" + e.getMessage());
        }
        return ResultContent.buildSuccess();

    }

    /**
     * 邮箱格式校验过滤
     *
     * @param emails
     * @return
     */
    private String[] filterEmail(String[] emails) {
        List<String> list = Arrays.asList(emails);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        list = list.stream().filter(e -> checkEmail(e)).collect(Collectors.toList());
        return list.toArray(new String[list.size()]);
    }

    private boolean checkEmail(String email) {
        return ReUtil.isMatch("\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", email);
    }
}
