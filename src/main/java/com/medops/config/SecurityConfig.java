package com.medops.config;

import com.medops.adapter.in.security.JwtAuthFilter;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.port.out.LoadUserPort;
import com.medops.application.port.out.TokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenPort tokenPort;
    private final LoadUserPort loadUserPort;
    private final LoadAdminPort loadAdminPort;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private static final String[] SWAGGER_ALLOWLIST = {
        "/swagger-ui/**",
        "/v3/**",
    };

    private static final String[] AUTH_POST_ALLOWLIST = {
        "/api/user",
        "/api/user/login",
        "/api/admin",
        "/api/admin/login",
        "/api/admin/verify-invitation-code",
        "/api/admin/activate-account",
        "/api/hospital"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf((csrf)->csrf.disable());
        http.cors(Customizer.withDefaults());

        http.sessionManagement(
            sessionManagement -> sessionManagement.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        );

        http.formLogin((form)-> form.disable());
        http.addFilterBefore(new JwtAuthFilter(tokenPort, loadUserPort, loadAdminPort), UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(
            authorize -> authorize
                .requestMatchers(SWAGGER_ALLOWLIST).permitAll()
                .requestMatchers(HttpMethod.POST, AUTH_POST_ALLOWLIST).permitAll()
//                .requestMatchers("/api/admin/employee").hasRole("ADMIN")
//                .requestMatchers("/api/employee").hasRole("USER")
//                .requestMatchers("/api/department").hasRole("USER")
//                .requestMatchers("/api/apps").hasRole("USER")
                .anyRequest().authenticated()
        );

        http.exceptionHandling(exceptionHandling ->
            exceptionHandling
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
