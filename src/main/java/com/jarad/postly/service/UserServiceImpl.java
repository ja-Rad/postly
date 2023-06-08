package com.jarad.postly.service;

import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.dto.UserDtoOnlyEmail;
import com.jarad.postly.util.dto.UserDtoOnlyPassword;
import com.jarad.postly.util.enums.SecurityRole;
import com.jarad.postly.util.exception.EmailTemplateException;
import com.jarad.postly.util.exception.UserAlreadyExistException;
import com.jarad.postly.util.exception.UserNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("spring.mail.username")
    private String postlyEmailAddress;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void registerNewUserAccount(UserDto userDto) {
        String userEmail = userDto.getEmail();
        if (userRepository.existsByEmail(userEmail)) {
            String message = MessageFormat.format("Account with this user email: {0} already exist", userEmail);
            log.info(message);
            throw new UserAlreadyExistException(message);
        }

        User user = User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .verificationCode(RandomString.make(64))
                .build();
        user.addRole(Role.builder().name(SecurityRole.ROLE_USER.toString()).build());

        userRepository.save(user);
        sendVerificationEmail(user);

        log.info("User {} wants to register an account", userEmail);
    }

    @Transactional
    @Override
    public void resetPasswordForExistingUser(UserDtoOnlyEmail userDto) {
        String userEmail = userDto.getEmail();

        Optional<User> optionalUser = userRepository.findByEmail(userEmail);
        if (optionalUser.isEmpty()) {
            String message = MessageFormat.format("User with email: {0} doesn''t exist", userEmail);
            log.info(message);
            throw new UserNotFoundException(message);
        }

        User user = optionalUser.get();
        String newVerificationCode = RandomString.make(64);
        user.setVerificationCode(newVerificationCode);

        userRepository.save(user);
        sendForgotPasswordEmail(user);

        log.info("User {} wants to reset the password", user.getEmail());
    }

    @Transactional
    @Override
    public void sendVerificationEmail(User user) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        createVerifyEmailTemplate(user, helper);

        mailSender.send(message);

        log.info("Email was sent to user {}", user.getEmail());
    }

    @Transactional
    @Override
    public void sendForgotPasswordEmail(User user) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        createForgotPasswordEmailTemplate(user, helper);

        mailSender.send(message);

        log.info("Email was sent to user {}", user.getEmail());
    }

    /**
     * Helper method to create a template for user email verification
     *
     * @param user    entity representing the client
     * @param helper  MimeMessageHelper instance providing access to the JavaMail API
     * @param subject email subject
     */
    private void createEmailVerificationTemplate(User user, MimeMessageHelper helper, String subject) {
        String verifyURL = "http://localhost:8080/users/verify?code=" + user.getVerificationCode();
        String userEmail = user.getEmail();
        String senderName = "Postly";
        String toAddress = userEmail;
        String content = String.format(
                "Dear %s,<br>" +
                        "Please click the link below to verify your registration:<br>" +
                        "<h3><a href=\"%s\" target=\"_self\">VERIFY</a></h3>" +
                        "Thank you,<br>" +
                        "postly.", userEmail, verifyURL);

        setEmailTemplateProperties(helper, subject, senderName, toAddress, content, userEmail);
    }

    /**
     * Helper method to create a template for resetting the user's password
     *
     * @param user    entity representing the client
     * @param helper  MimeMessageHelper instance providing access to the JavaMail API
     * @param subject email subject
     */
    private void createForgotPasswordTemplate(User user, MimeMessageHelper helper, String subject) {
        String verifyURL = "http://localhost:8080/users/forgot-password-verify?code=" + user.getVerificationCode();
        String userEmail = user.getEmail();
        String senderName = "Postly";
        String toAddress = userEmail;
        String content = String.format(
                "Dear %s,<br>" +
                        "Please click the link below to reset your password:<br>" +
                        "<h3><a href=\"%s\" target=\"_self\">RESET PASSWORD</a></h3>" +
                        "Thank you,<br>" +
                        "postly.", userEmail, verifyURL);

        setEmailTemplateProperties(helper, subject, senderName, toAddress, content, userEmail);
    }

    /**
     * Sets the common properties for email templates
     *
     * @param helper     MimeMessageHelper instance providing access to the JavaMail API
     * @param subject    email subject
     * @param senderName sender name
     * @param toAddress  recipient email address
     * @param content    email content
     * @param userEmail  user's email
     */
    private void setEmailTemplateProperties(MimeMessageHelper helper, String subject, String senderName,
                                            String toAddress, String content, String userEmail) {
        try {
            helper.setFrom(postlyEmailAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);

            log.info("Email template for user: {} has been created", userEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            String message = String.format("Exception occurred while creating the email template. Details: %s", e.getMessage());
            log.info(message);
            throw new EmailTemplateException(message);
        }
    }

    /**
     * Helper method to create a template for user registration verification
     *
     * @param user   entity representing the client
     * @param helper MimeMessageHelper instance providing access to the JavaMail API
     */
    @Override
    public void createVerifyEmailTemplate(User user, MimeMessageHelper helper) {
        String subject = "Please verify your registration";
        createEmailVerificationTemplate(user, helper, subject);
    }

    /**
     * Helper method to create a template for user password reset verification
     *
     * @param user   entity representing the client
     * @param helper MimeMessageHelper instance providing access to the JavaMail API
     */
    @Override
    public void createForgotPasswordEmailTemplate(User user, MimeMessageHelper helper) {
        String subject = "Please reset your password";
        createForgotPasswordTemplate(user, helper, subject);
    }

    /**
     * Helper method to compare verificationCode that was generated for the new user with verification code in /verify?code=
     * If comparison was successful update user password with provided password in UserDto
     *
     * @param verificationCode that provided in /verify?code=
     * @return true if verification code is in database, user is present and user is not enabled, false if otherwise
     */
    @Transactional
    @Override
    public boolean verifyNewUser(String verificationCode) {
        if (isNotEmpty(verificationCode)) {
            Optional<User> optionalUser = userRepository.findByVerificationCode(verificationCode);

            if (optionalUser.isEmpty()) {
                String message = MessageFormat.format("User with verificationCode: {0} doesn''t exist", verificationCode);
                log.info(message);
                throw new UserNotFoundException(message);
            }

            User user = optionalUser.get();

            if (!user.isEnabled()) {
                user.setVerificationCode(null);
                user.setEnabled(true);
                user.setActiveProfile(false);
                userRepository.save(user);

                log.info("User with id: {} has been verified and enabled", user.getId());

                return true;
            }

            log.warn("User with id: {} is already enabled", user.getId());
        }

        log.warn("Verification code is empty or null");
        return false;
    }

    /**
     * Helper method to compare verificationCode that was generated for the user that forgot password with verification code in /verify?code=
     * If comparison was successful update user password with provided password in UserDto
     *
     * @param verificationCode is provided in /verify?code=
     * @return true if verification code is in database, user is present and user is enabled, false if otherwise
     */
    @Transactional
    @Override
    public boolean verifyForgotPassword(String verificationCode, UserDtoOnlyPassword userDto) {
        if (isNotEmpty(verificationCode)) {
            Optional<User> optionalUser = userRepository.findByVerificationCode(verificationCode);

            if (optionalUser.isEmpty()) {
                String message = MessageFormat.format("User with verificationCode: {0} doesn''t exist", verificationCode);
                log.info(message);
                throw new UserNotFoundException(message);
            }

            User user = optionalUser.get();

            if (user.isEnabled()) {
                String newUserPassword = userDto.getPassword();
                user.setVerificationCode(null);
                user.setPassword(passwordEncoder.encode(newUserPassword));
                userRepository.save(user);

                log.info("User with id: {} updated their password", user.getId());

                return true;
            }

            log.warn("User with id: {} is not enabled", user.getId());
        }

        log.warn("Verification code is empty or null");
        return false;
    }


}
