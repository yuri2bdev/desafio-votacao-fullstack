package br.com.yuri.votacao.messaging;

import br.com.yuri.votacao.dto.VoteEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteProducer {

    private final KafkaTemplate<String, VoteEventDto> kafkaTemplate;

    @Value("${app.kafka.vote-topic}")
    private String voteTopic;

    public void publicar(VoteEventDto event) {
        kafkaTemplate.send(voteTopic, event.pautaId().toString(), event);
    }
}

