package com.toreachtop.email.spring.boot.starter.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * 邮件发送
 */
@Data
@Builder

public class MailDto implements Serializable {
    @Tolerate
    public MailDto() {
    }

    /**
     * 接收人，多个以逗号分隔
     */
    private String to;
    /**
     * 抄送人，多个以逗号分隔
     */
    private String cc;
    /**
     * 暗抄送人，多个以逗号分隔
     */
    private String bcc;
    /**
     * 主题
     */
    private String subject;
    /**
     * 邮件内容
     */
    private String content;
    /**
     * 附件
     */
    private File[] files;
    /**
     * 发送时间
     */
    private Date sendDate;

}
