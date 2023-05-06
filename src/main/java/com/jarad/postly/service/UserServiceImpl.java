package com.jarad.postly.service;

import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.repository.RoleRepository;
import com.jarad.postly.repository.UserRepository;
import com.jarad.postly.util.dto.UserDto;
import com.jarad.postly.util.enums.SecurityRole;
import com.jarad.postly.util.exception.EmailNotFoundException;
import com.jarad.postly.util.exception.UserAlreadyExistException;
import com.jarad.postly.util.mapper.UserMapperImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserMapperImpl userMapperImpl;
    private JavaMailSender mailSender;
    private PasswordEncoder passwordEncoder;

    @Value("spring.mail.username")
    private String postlyEmailAddress;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserMapperImpl userMapperImpl, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapperImpl = userMapperImpl;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException {
        String email = userDto.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistException("There is an account with that email address: " + email);
        }

        Optional<Role> userRole = roleRepository.findByName(SecurityRole.USER_ROLE.toString());
        userRole = getOptionalRole(userRole);

        User user = userMapperImpl.mapToEntity(userDto);
        user.setVerificationCode(RandomString.make(64));
        user.setRoles(Stream.of(userRole.get()).collect(toSet()));
        userRepository.save(user);

        sendVerificationEmail(user);
    }

    @Override
    public void resetPasswordForExistingUser(UserDto userDto) throws EmailNotFoundException {
        String email = userDto.getEmail();
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String newVerificationCode = RandomString.make(64);
            user.setVerificationCode(newVerificationCode);

            userRepository.save(user);
            sendForgotPasswordEmail(user);

        } else {
            throw new EmailNotFoundException("There is no account with that email address: " + email);
        }
    }

    @Override
    public void sendVerificationEmail(User user) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        createVerifyEmailTemplate(user, helper);

        mailSender.send(message);
    }

    @Override
    public void sendForgotPasswordEmail(User user) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        createForgotPasswordEmailTemplate(user, helper);

        mailSender.send(message);
    }

    /**
     * Helper method that checks if ROLE_USER already exists in Database. If not creates it.
     *
     * @param userRole entity that represents USER_ROLE in table 'roles'
     * @return USER_ROLE from table 'roles'
     */
    private Optional<Role> getOptionalRole(Optional<Role> userRole) {
        if (userRole.isEmpty()) {
            Role buildUserRole = Role.builder().name(SecurityRole.USER_ROLE.toString()).build();
            roleRepository.save(buildUserRole);

            userRole = roleRepository.findByName(SecurityRole.USER_ROLE.toString());
        }
        return userRole;
    }

    /**
     * Helper method to create a template for user registration verification.
     *
     * @param user   entity that's representing the client.
     * @param helper MimeMessageHelper instance that provides easy access to the JavaMail API.
     */
    private void createVerifyEmailTemplate(User user, MimeMessageHelper helper) {
        String verifyURL = "http://localhost:8080" + "/verify?code=" + user.getVerificationCode();
        String userEmail = user.getEmail();
        String senderName = "Postly";
        String toAddress = userEmail;
        String subject = "Please verify your registration";
        String content = """
                Dear %s,<br>
                Please click the link below to verify your registration:<br>
                <h3><a href="%s" target="_self">VERIFY</a></h3>
                Thank you,<br>
                postly.
                """.formatted(userEmail, verifyURL);
        try {
            helper.setFrom(postlyEmailAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to create a template for user reset password verification.
     *
     * @param user   entity that's representing the client.
     * @param helper MimeMessageHelper instance that provides easy access to the JavaMail API.
     */
    private void createForgotPasswordEmailTemplate(User user, MimeMessageHelper helper) {
        String verifyURL = "http://localhost:8080" + "/forgot-password-verify?code=" + user.getVerificationCode();
        String userEmail = user.getEmail();
        String senderName = "Postly";
        String toAddress = userEmail;
        String subject = "Please reset your password";
        String content = """
                Dear %s,<br>
                Please click the link below to reset your password:<br>
                <h3><a href="%s" target="_self">RESET PASSWORD</a></h3>
                Thank you,<br>
                postly.
                """.formatted(userEmail, verifyURL);
        try {
            helper.setFrom(postlyEmailAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to compare verificationCode that was generated for the new user with verification code in /verify?code=.
     *
     * @param verificationCode that provided in /verify?code=.
     * @return true if verification code is in database, user is present and user is not enabled, false if otherwise.
     */
    public boolean verifyNewUser(String verificationCode) {
        Optional<User> userOptional = Optional.empty();
        if (isNotEmpty(verificationCode)) {
            userOptional = userRepository.findByVerificationCode(verificationCode);
        }

        if (userOptional.isPresent() && !userOptional.get().isEnabled()) {
            User user = userOptional.get();
            user.setVerificationCode(null);
            user.setEnabled(true);

            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Helper method to compare verificationCode that was generated for the user that forgot password with verification code in /verify?code=.
     *
     * @param verificationCode that provided in /verify?code=.
     * @return true if verification code is in database, user is present and user is enabled, false if otherwise.
     */
    public boolean verifyForgotPassword(String verificationCode, UserDto userDto) {
        Optional<User> userOptional = Optional.empty();
        if (isNotEmpty(verificationCode)) {
            userOptional = userRepository.findByVerificationCode(verificationCode);
        }

        if (userOptional.isPresent() && userOptional.get().isEnabled()) {
            User user = userOptional.get();
            String userPassword = userDto.getPassword();
            user.setVerificationCode(null);
            user.setPassword(passwordEncoder.encode(userPassword));

            userRepository.save(user);
            return true;
        }

        return false;
    }
}
