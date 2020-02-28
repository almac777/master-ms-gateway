package at.ac.fhcampus.master.micro.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
@Order(0)
public class UsernameHeaderFilter implements GlobalFilter {

    public static final String EXTERNAL_USER_ID_HEADER = "article-username";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(this::getUserId)
                .doOnNext(userId -> exchange.getRequest().mutate().header(EXTERNAL_USER_ID_HEADER, userId).build())
                .then(chain.filter(exchange));
    }

    private String getUserId(Authentication authentication) {
        return Optional.of(authentication)
                .map(JwtAuthenticationToken.class::cast)
                .map(JwtAuthenticationToken::getTokenAttributes)
                .map(map -> map.getOrDefault("user_name", "noUserId"))
                .map(String.class::cast)
                .orElseThrow(() -> new RuntimeException("User Id not retrievable"));
    }
}
