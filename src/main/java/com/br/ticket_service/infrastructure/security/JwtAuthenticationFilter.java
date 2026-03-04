package com.br.ticket_service.infrastructure.security;

import com.br.ticket_service.infrastructure.config.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SecurityErrorWriter errorWriter;

    private static final String TENANT_KEY = "tenantId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_KEY = "userId";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String requestUri = request.getRequestURI();
        final String authHeader = request.getHeader("Authorization");

        MDC.put(REQUEST_ID_KEY, UUID.randomUUID().toString());

        boolean isPublicUrl = Arrays.stream(SecurityConfig.PUBLIC_URLS)
                .anyMatch(pattern -> requestUri.startsWith(pattern.replace("/**", "")));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            MDC.clear();
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String userEmail = jwtService.extractUsername(jwt);
            String tenantId = jwtService.extractClaim(jwt, "tenant_id");
            Boolean isFirstAccess = jwtService.extractFirstAccess(jwt);

            if (tenantId != null) {
                TenantContext.setCurrentTenant(tenantId);
                MDC.put(TENANT_KEY, tenantId);
            }

            if (Boolean.TRUE.equals(isFirstAccess) && !isPublicUrl) {
                log.warn("[Security] Bloqueio de primeiro acesso no Ticket Service para: {}", userEmail);
                errorWriter.writeError(response, HttpStatus.FORBIDDEN,
                        "Necessário trocar a senha no sistema de identidade antes de acessar os chamados.", requestUri);
                return;
            }

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                MDC.put(USER_KEY, userEmail);
                var authorities = jwtService.extractAuthorities(jwt);
                var authToken = new UsernamePasswordAuthenticationToken(userEmail, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("[Security] Erro ao validar token: {}", e.getMessage());
            if (isPublicUrl) {
                filterChain.doFilter(request, response);
            } else {
                errorWriter.writeError(response, HttpStatus.UNAUTHORIZED, "Token inválido.", requestUri);
            }
        } finally {
            TenantContext.clear();
            MDC.clear();
        }
    }
}