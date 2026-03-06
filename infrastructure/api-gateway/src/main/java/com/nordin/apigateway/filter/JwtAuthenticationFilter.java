package com.nordin.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Filtro global de validación JWT.
 *
 * Patrón: Edge Security — el JWT se valida una sola vez en el Gateway.
 * Los microservicios internos confían en la red interna y no validan JWT.
 *
 * Rutas públicas (sin JWT): /actuator/**, /api/auth/**
 * Rutas protegidas: todo lo demás
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:nordin-secret-key-para-desarrollo-minimo-256-bits}")
    private String jwtSecret;

    // Rutas que no requieren JWT
    private static final List<String> PUBLIC_PATHS = List.of(
        "/actuator",
        "/api/auth"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        log.info("entre al filter gateway: {}",path );
        // Si es ruta pública, continuar sin validar
//        if (isPublicPath(path)) {
//            return chain.filter(exchange);
//        }

        if (true ) {
            log.info("entro al if" );
            return chain.filter(exchange);
        }

        // Verificar header Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = validateToken(token);

            // Propagar el subject (userId) a los microservicios via header
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> r.header("X-User-Id", claims.getSubject()))
                .build();

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad, ejecutar primero
    }
}
