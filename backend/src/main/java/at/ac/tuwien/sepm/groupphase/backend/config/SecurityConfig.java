package at.ac.tuwien.sepm.groupphase.backend.config;

import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.security.CustomerJwtAuthenticationFilter;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtAuthorizationFilter;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepm.groupphase.backend.security.OperatorJwtAuthenticationFilter;
import at.ac.tuwien.sepm.groupphase.backend.service.CustomerService;
import at.ac.tuwien.sepm.groupphase.backend.service.OperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Configuration
    @Order(1)
    public static class OperatorSecurityConfig extends WebSecurityConfigurerAdapter {
        private final OperatorService operatorService;
        private final PasswordEncoder passwordEncoder;
        private final SecurityProperties securityProperties;
        private final JwtTokenizer jwtTokenizer;

        @Autowired
        public OperatorSecurityConfig(OperatorService operatorService,
                                      PasswordEncoder passwordEncoder,
                                      SecurityProperties securityProperties, JwtTokenizer jwtTokenizer) {
            this.operatorService = operatorService;
            this.securityProperties = securityProperties;
            this.passwordEncoder = passwordEncoder;
            this.jwtTokenizer = jwtTokenizer;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.cors().and()
                .csrf().disable()
                .antMatcher("/api/v1/authentication/operators")
                .addFilter(new OperatorJwtAuthenticationFilter(authenticationManager(), securityProperties, jwtTokenizer))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), securityProperties));
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(operatorService).passwordEncoder(passwordEncoder);
        }
    }

    @Configuration
    @Order(2)
    @EnableGlobalMethodSecurity(securedEnabled = true)
    public static class CustomerSecurityConfig extends WebSecurityConfigurerAdapter {

        private final CustomerService customerService;
        private final PasswordEncoder passwordEncoder;
        private final SecurityProperties securityProperties;
        private final JwtTokenizer jwtTokenizer;

        @Autowired
        public CustomerSecurityConfig(CustomerService customerService,
                                      PasswordEncoder passwordEncoder,
                                      SecurityProperties securityProperties, JwtTokenizer jwtTokenizer) {
            this.customerService = customerService;
            this.securityProperties = securityProperties;
            this.passwordEncoder = passwordEncoder;
            this.jwtTokenizer = jwtTokenizer;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.cors().and()
                .csrf().disable()
                .addFilter(new CustomerJwtAuthenticationFilter(authenticationManager(), securityProperties, jwtTokenizer))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), securityProperties));
            http.headers().frameOptions().disable();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(customerService).passwordEncoder(passwordEncoder);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            return getCorsConfigurationSource();
        }
    }

    private static CorsConfigurationSource getCorsConfigurationSource() {
        final List<String> permitAll = Collections.singletonList("*");
        final List<String> permitMethods = List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name(), HttpMethod.HEAD.name(),
            HttpMethod.TRACE.name());
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(permitAll);
        configuration.setAllowedMethods(permitMethods);
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(permitAll);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
