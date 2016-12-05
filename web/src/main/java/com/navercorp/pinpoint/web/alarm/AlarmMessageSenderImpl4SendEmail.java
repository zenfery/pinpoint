/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.alarm;

import java.text.MessageFormat;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.UserGroupService;

/**
 * implement of AlarmMessageSender, only support sendEmail
 * 
 * @author zenfery
 */
public class AlarmMessageSenderImpl4SendEmail extends EmptyMessageSender {

    protected Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("#{batchProps['batch.mail.from']}")
    private String mailFrom;

    @Value("#{batchProps['batch.mail.subject']}")
    private String mailSubject;

    /**
     * simple implement of sendemail
     * 
     * @author zenfery
     */
    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        logger.info("==> sendEmail(): Begin to send email.");
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());
        String emailMessage = checker.getEmailMessage();
        Rule rule = checker.getRule();
        // String unit = checker.getUnit();
        logger.debug("==> sendEmail():" + "application = " + rule.getApplicationId()
                + ", message = " + emailMessage);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            // assemble mail message
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(mailFrom);
            String subject = MessageFormat.format(mailSubject, new Object[] {
                    rule.getCheckerName(), rule.getApplicationId(), rule.getServiceType() });
            helper.setSubject(subject);
            for (String to : receivers) {
                logger.debug("==> sendEmail(): " + "to:" + to);
                helper.addTo(to);
            }
            helper.setText(checker.getEmailMessage(), true);

            // send
            mailSender.send(message);

            logger.info("<== sendEmail(): ok~, " + "application = " + rule.getApplicationId()
                    + ", message = " + emailMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("<== sendEmail(): " + "application = " + rule.getApplicationId()
                    + ", message = " + emailMessage + ", error = " + e.getMessage());
        }

    }
}
