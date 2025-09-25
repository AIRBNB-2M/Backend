package project.airbnb.clone.config.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(@Value("${spring.mail.host}") String host,
                                         @Value("${spring.mail.username}") String username,
                                         @Value("${spring.mail.password}") String password,
                                         @Value("${spring.mail.port}") int port,
                                         @Value("${spring.mail.properties.mail.smtp.auth}") boolean auth,
                                         @Value("${spring.mail.properties.mail.smtp.starttls.enable}") boolean starttlsEnable,
                                         @Value("${spring.mail.properties.mail.smtp.starttls.required}") boolean starttlsRequired,
                                         @Value("${spring.mail.properties.mail.smtp.connection-timeout}") int connectionTimeout,
                                         @Value("${spring.mail.properties.mail.smtp.timeout}") int timeout,
                                         @Value("${spring.mail.properties.mail.smtp.write-timeout}") int writeTimeout) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setPort(port);
        mailSender.setDefaultEncoding("UTF-8");

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", auth);
        properties.put("mail.smtp.starttls.enable", starttlsEnable);
        properties.put("mail.smtp.starttls.required", starttlsRequired);
        properties.put("mail.smtp.connectiontimeout", connectionTimeout);
        properties.put("mail.smtp.timeout", timeout);
        properties.put("mail.smtp.writetimeout", writeTimeout);

        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }
}
