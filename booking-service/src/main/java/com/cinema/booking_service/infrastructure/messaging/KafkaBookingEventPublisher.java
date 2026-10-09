package com.cinema.booking_service.infrastructure.messaging;

import com.cinema.booking_service.domain.BookingConfirmedEvent;
import com.cinema.booking_service.domain.BookingEventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica BookingConfirmedEvent en el topic cinema.bookings (clave = bookingId, valor = JSON).
 * Es best-effort: la reserva ya está CONFIRMED y committeada; si Kafka no responde se loguea y se sigue.
 */
@Component
@Slf4j
public class KafkaBookingEventPublisher implements BookingEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaBookingEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                      @Value("${cinema.kafka.bookings-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        try {
            kafkaTemplate.send(topic, event.bookingId().toString(), event.toJson())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("No se pudo publicar BookingConfirmedEvent {} en '{}': {}",
                                    event.bookingId(), topic, ex.getMessage());
                        } else {
                            log.info("BookingConfirmedEvent {} publicado en '{}' (partición {}, offset {})",
                                    event.bookingId(), topic,
                                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (RuntimeException ex) {
            // p. ej. Kafka caído y max.block.ms agotado: no debe afectar a la reserva confirmada
            log.error("Kafka no disponible, evento {} omitido: {}", event.bookingId(), ex.getMessage());
        }
    }
}
