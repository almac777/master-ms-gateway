package at.ac.fhcampus.master.micro.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    @Value("${auth.http-basic-password}")
    String httpBasicPassword;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("users", r -> r.path("/api/v1/users/**")
                        .filters(f -> f.addRequestHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.httpBasicPassword).stripPrefix(3))
                        .uri("lb://user-service/"))
                .route("articles", r -> r.path("/api/v1/articles/**")
                        .filters(f -> f.stripPrefix(3))
                        .uri("lb://article-service/"))
                .route("ratings", r -> r.path("/api/v1/ratings/**")
                        .filters(f -> f.stripPrefix(3))
                        .uri("lb://rating-service/"))
                .route("accumulated-ratings", r -> r.path("/api/v1/accumulated-ratings/**")
                        .filters(f -> f.stripPrefix(3))
                        .uri("lb://accumulated-rating-service/"))
                .build();
    }
}
