package br.com.yuri.votacao.messaging;

import br.com.yuri.votacao.domain.EscolhaVoto;
import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.dto.VoteEventDto;
import br.com.yuri.votacao.exception.ResourceNotFoundException;
import br.com.yuri.votacao.repository.PautaRepository;
import br.com.yuri.votacao.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteConsumerTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private VoteConsumer voteConsumer;

    private VoteEventDto event;

    @BeforeEach
    void setUp() {
        event = new VoteEventDto(
                UUID.randomUUID(),
                "12345678901",
                EscolhaVoto.SIM,
                LocalDateTime.of(2026, 1, 1, 10, 0).toString()
        );
    }

    @Test
    void devePersistirVotoQuandoNaoForDuplicado() {
        Pauta pauta = Pauta.builder().id(event.pautaId()).descricao("Pauta").build();

        when(votoRepository.existsByPautaIdAndAssociadoId(event.pautaId(), event.associadoId())).thenReturn(false);
        when(pautaRepository.findById(event.pautaId())).thenReturn(Optional.of(pauta));

        voteConsumer.consumir(event);

        ArgumentCaptor<br.com.yuri.votacao.domain.Voto> captor = ArgumentCaptor.forClass(br.com.yuri.votacao.domain.Voto.class);
        verify(votoRepository).save(captor.capture());
        assertEquals(event.associadoId(), captor.getValue().getAssociadoId());
        assertEquals(event.escolha(), captor.getValue().getEscolha());
    }

    @Test
    void deveIgnorarVotoDuplicado() {
        when(votoRepository.existsByPautaIdAndAssociadoId(eq(event.pautaId()), eq(event.associadoId()))).thenReturn(true);

        voteConsumer.consumir(event);

        verify(pautaRepository, never()).findById(any());
        verify(votoRepository, never()).save(any());
    }

    @Test
    void deveLancarNotFoundQuandoPautaNaoExistir() {
        when(votoRepository.existsByPautaIdAndAssociadoId(event.pautaId(), event.associadoId())).thenReturn(false);
        when(pautaRepository.findById(event.pautaId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> voteConsumer.consumir(event));
    }

    @Test
    void deveIgnorarErroDeConcorrenciaAoSalvarVoto() {
        Pauta pauta = Pauta.builder().id(event.pautaId()).descricao("Pauta").build();

        when(votoRepository.existsByPautaIdAndAssociadoId(event.pautaId(), event.associadoId())).thenReturn(false);
        when(pautaRepository.findById(event.pautaId())).thenReturn(Optional.of(pauta));
        doThrow(new DataIntegrityViolationException("duplicado")).when(votoRepository).save(any());

        assertDoesNotThrow(() -> voteConsumer.consumir(event));
    }
}
