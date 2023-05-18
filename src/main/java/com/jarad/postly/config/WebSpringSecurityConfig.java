package com.jarad.postly.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// jsr250Enabled enables RolesAllowed annotation
@EnableMethodSecurity(jsr250Enabled = true)
public class WebSpringSecurityConfig {

    public static final String[] ENDPOINTS_WHITELIST = {
            "/users/**"
    };

    public static final String[] STATIC_RESOURCES_WHITELIST = {
            "/images/**",
            "/styles/**"
    };

    private final UserDetailsService userDetailsService;

    @Autowired
    public WebSpringSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
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
                        // Public access
                        .requestMatchers(ENDPOINTS_WHITELIST).permitAll()
                        .requestMatchers(STATIC_RESOURCES_WHITELIST).permitAll()

                        // Authenticated access
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
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login")
                )

                // Session Filter
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .invalidSessionUrl("/invalidSession")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/login")

                )

                // Exception Filter
                .exceptionHandling()
                .accessDeniedPage("/403");


        return http.build();
    }
}