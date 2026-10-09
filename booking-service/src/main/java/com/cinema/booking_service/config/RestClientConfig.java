package com.cinema.booking_service.config;

import com.cinema.booking_service.infrastructure.client.BearerTokenRelayInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * Builder @LoadBalanced: permite usar el nombre lógico "movie-service" en la URL.
     * Spring Cloud LoadBalancer lo resuelve (hoy con instancias estáticas, mañana con Eureka).
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient movieServiceClient(@LoadBalanced RestClient.Builder builder,
                                         @Value("${movie-service.url}") String baseUrl,
                                         @Value("${movie-service.connect-timeout}") Duration connectTimeout,
                                         @Value("${movie-service.read-timeout}") Duration readTimeout) {
        // HTTP/1.1 explícito: evita el intento de upgrade h2c del cliente JDK contra Tomcat.
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(new BearerTokenRelayInterceptor()) // propaga el JWT del usuario
                .build();
    }
}
