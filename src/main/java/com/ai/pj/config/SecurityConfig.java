package com.ai.pj.config;

import com.ai.pj.security.authentication.RoleBasedAuthenticationProvider;
import com.ai.pj.security.handler.CustomAuthenticationFailureHandler;
import com.ai.pj.security.handler.CustomAuthenticationSuccessHandler;
import com.ai.pj.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                                .ignoringRequestMatchers(PathRequest.toH2Console())
                                .disable()
                        )
                .authorizeHttpRequests(request -> request
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/public/**", "/login").permitAll()
                                .anyRequest().fullyAuthenticated() // 권한에 따른 로그인 다 잡기
//                        .anyRequest().permitAll() // 모든 요청 허용
                )
                .formLogin(form -> form
                        .loginPage("/public/login") // 사용자 정의 로그인 페이지
                        .loginProcessingUrl("/public/loginProc")
                        .successHandler(new CustomAuthenticationSuccessHandler())
                        .failureHandler(new CustomAuthenticationFailureHandler()))
//                        .defaultSuccessUrl("/board/", true)) // 로그인 성공 시 이동할 페이지
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/public/home"))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .build();
    }


    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        System.out.println(1010);
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(new RoleBasedAuthenticationProvider(userDetailsService, passwordEncoder))
                .build();
    }
}
