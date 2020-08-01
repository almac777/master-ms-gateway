package at.ac.fhcampus.master.micro.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Value("${auth.verifierKey}")
    private String verifierKey;

    @Bean
    @Order(2)
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/**"))
                .authorizeExchange().pathMatchers("/api/v1/users/**").permitAll().and()
                .authorizeExchange().anyExchange().authenticated().and()
                .csrf().disable()
                .oauth2ResourceServer().jwt();
        return http.build();
    }

    @Bean
    @LoadBalanced
    WebClient.Builder oauthWebclientBuilder() {
        return WebClient.builder();
    }

    /**
     * We have to create the jwt decoder ourselves to provide the RSA Key for signature validation.
     * Spring Boot 2/Cloud Gateway supports obtaining the key via an endpoint, HOWEVER the web client
     * used to obtain the keys is not configurable and fixed to default implementation, meaning it does not
     * support service discovery.
     *
     * This means that we'd have to fixate the URL to obtain the keys.
     *
     * If this bean is not defined, the property "spring.oauth2resourceserver.jwt.jwk-set-uri" can be used to have
     * the decoder automatically defined.
     * <br>
     * Example: http://localhost:8081/oauth/jwks.json when using our authorization server locally
     */
    @Bean
    ReactiveJwtDecoder oauthJwtDecoder() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String encodedKey = verifierKey;
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(spec);
        return new NimbusReactiveJwtDecoder(rsaPublicKey);
    }
}
