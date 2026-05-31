package es.damdi.miguelangel.chatdam_servidor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitamos un canal público al que se suscribirán los empleados para escuchar los mensajes
        config.enableSimpleBroker("/topic");

        // Prefijo para los mensajes que el cliente envía HACIA el servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Esta es la URL a la que se conectará tu compañero desde JavaFX para abrir el túnel
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // Permite conexiones desde cualquier sitio (evita errores CORS)
                .withSockJS(); // Soporte de compatibilidad por si falla el WebSocket puro
    }
}