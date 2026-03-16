package dev.sitconsulting.library.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnClass(HttpSecurity.class) // Wird nur aktiv, wenn Spring Security im Projekt ist
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ActuatorSecurityConfiguration {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**") // Gilt nur für Actuator-Pfade
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()    // Erlaube alles unter /actuator/
                )
                .csrf(csrf -> csrf.disable())    // Deaktiviere CSRF für diese Endpunkte
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
