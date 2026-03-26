package br.com.yuri.votacao.messaging;

import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.domain.Voto;
import br.com.yuri.votacao.dto.VoteEventDto;
import br.com.yuri.votacao.exception.ResourceNotFoundException;
import br.com.yuri.votacao.repository.PautaRepository;
import br.com.yuri.votacao.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoteConsumer {

    private final PautaRepository pautaRepository;
    private final VotoRepository votoRepository;

    @KafkaListener(topics = "${app.kafka.vote-topic}")
    @Transactional
    public void consumir(VoteEventDto event) {
        if (votoRepository.existsByPautaIdAndAssociadoId(event.pautaId(), event.associadoId())) {
            log.warn("Voto duplicado ignorado para pauta={} associado={}", event.pautaId(), event.associadoId());
            return;
        }

        Pauta pauta = pautaRepository.findById(event.pautaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pauta nao encontrada para voto: " + event.pautaId()));

        Voto voto = Voto.builder()
                .pauta(pauta)
                .associadoId(event.associadoId())
                .escolha(event.escolha())
                .dataVoto(LocalDateTime.parse(event.dataVoto()))
                .build();

        try {
            votoRepository.save(voto);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Concorrencia detectada ao salvar voto duplicado para pauta={} associado={}",
                    event.pautaId(), event.associadoId());
        }
    }
}

