package com.jarad.postly.config;

import com.jarad.postly.security.SecurityRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String LOGIN = "/login";
    private static final String[] ENDPOINTS_WHITELIST = {
            "/users/**",
    };

    private static final String[] ENDPOINTS_ROLE_USER = {
            "/",
            "/posts",
            "/posts/{postId}",
            "/posts/{postId}/comments",
            "/posts/{postId}/comments/{commentId}",

            "/profiles",
            "/profiles/{profileId}",
            "/profiles/{profileId}/*"
    };

    private static final String[] ENDPOINTS_ROLE_PROFILE_ACTIVE = {
            "/posts",
            "/posts/**",

            "/profiles",
            "/profiles/**",

            "/followers/*"
    };

    private static final String[] ENDPOINTS_ROLE_ADMIN = {
            "/admin/**"
    };

    private static final String[] STATIC_RESOURCES_WHITELIST = {
            "/images/**",
            "/styles/**"
    };
    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Helper method to format a role name by removing the "ROLE_" prefix.
     * Spring Security throws an exception when we provide role with the "ROLE_" prefix.
     * This helper method fixes that issue by removing the prefix from the given role name.
     *
     * @param roleName the role name to be formatted
     * @return the formatted role name without the "ROLE_" prefix
     */
    private static String formatRole(final String roleName) {
        return roleName.replace("ROLE_", "");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorization Filter
                .authorizeHttpRequests(requests -> requests
                        // Public access
                        .requestMatchers(ENDPOINTS_WHITELIST).permitAll()
                        .requestMatchers(STATIC_RESOURCES_WHITELIST).permitAll()

                        // Authorization
                        .requestMatchers(HttpMethod.GET, "/profiles/create-form").hasRole(formatRole(SecurityRole.ROLE_USER.getRole()))
                        .requestMatchers(HttpMethod.POST, "/profiles").hasRole(formatRole(SecurityRole.ROLE_USER.getRole()))
                        .requestMatchers(HttpMethod.GET, ENDPOINTS_ROLE_USER).hasRole(formatRole(SecurityRole.ROLE_USER.getRole()))

                        .requestMatchers(ENDPOINTS_ROLE_PROFILE_ACTIVE).hasRole(formatRole(SecurityRole.ROLE_PROFILE_ACTIVE.getRole()))

                        .requestMatchers(HttpMethod.DELETE, ENDPOINTS_ROLE_ADMIN).hasRole(formatRole(SecurityRole.ROLE_ADMIN.getRole()))

                        // Authenticated access
                        .anyRequest().authenticated()

                )

                // Login Filter
                .formLogin(form -> form
                        .loginPage(LOGIN).usernameParameter("email").permitAll()
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                )

                // Logout Filter
                .logout(logout -> logout
                        .logoutUrl("/logout").permitAll()
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl(LOGIN)
                )

                // Session Filter
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .invalidSessionUrl("/invalidSession")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl(LOGIN)

                )

                // Exception Filter
                .exceptionHandling()
                .accessDeniedPage("/403");


        return http.build();
    }
}