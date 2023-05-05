package com.jarad.postly.config;

import com.jarad.postly.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// jsr250Enabled enables RolesAllowed annotation
@EnableMethodSecurity(jsr250Enabled = true)
public class WebSpringSecurityConfig {

    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    public WebSpringSecurityConfig(UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsServiceImpl);
        // Encode password when user Authenticates
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authProvider())
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorization Filter
                .authorizeHttpRequests((requests) -> requests
                        // Public access: registration and login pages
                        .requestMatchers("registration").permitAll()

                        // Authenticated access to everything else
                        .anyRequest().authenticated()

                )

                // Login Filter
                .formLogin((form) -> form
                        .loginPage("/login").usernameParameter("email").permitAll()
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                )

                // Logout Filter
                .logout((logout) -> logout
                        .logoutUrl("/logout").permitAll()
                        .logoutSuccessUrl("/login")
                );

        return http.build();
    }
}