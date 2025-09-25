package ru.otus.hw.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Value("${app.security.remember-me.key}")
    private String rememberMeKey;

    @Value("${app.security.remember-me.validity-seconds:86400}")
    private int tokenValiditySeconds;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/login", "/access-denied").permitAll()
                        .requestMatchers(HttpMethod.POST, "/book/*/comment").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/book/*/comment/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/book/*/comment/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/book/new", "/book/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/book").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/book/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/book/*").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .rememberMe((rememberMe) -> rememberMe
                        .key(rememberMeKey).tokenValiditySeconds(tokenValiditySeconds))
                        .exceptionHandling(exceptions -> exceptions
                                .accessDeniedHandler((request, response, accessDeniedException) ->
                                        response.sendRedirect("/access-denied"))
                        );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
