package com.example.NotesApi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Смотрим заголовок Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Если заголовка нет или он не начинается с "Bearer " — пропускаем дальше
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Достаём токен (после "Bearer ")
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            // 4. Извлекаем email из токена
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Токен битый, просроченный, подпись не совпала — просто пропускаем
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Если email есть и пользователь ещё не аутентифицирован
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Загружаем пользователя из БД
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // 7. Проверяем: токен валидный для этого пользователя?
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 8. Создаём объект аутентификации
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                              // credentials (пароль) — не нужен, уже аутентифицированы
                        userDetails.getAuthorities()       // список ролей
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Кладём в SecurityContext — теперь Security знает, кто это
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Передаём запрос дальше по цепочке
        filterChain.doFilter(request, response);
    }
}