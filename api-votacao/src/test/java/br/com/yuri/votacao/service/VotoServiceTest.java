package br.com.yuri.votacao.service;

import br.com.yuri.votacao.client.CpfValidatorClient;
import br.com.yuri.votacao.domain.EscolhaVoto;
import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.domain.ResultadoStatus;
import br.com.yuri.votacao.dto.ResultadoVotacaoResponse;
import br.com.yuri.votacao.dto.VoteEventDto;
import br.com.yuri.votacao.dto.VotoRecebidoResponse;
import br.com.yuri.votacao.dto.VotoRequest;
import br.com.yuri.votacao.exception.BusinessConflictException;
import br.com.yuri.votacao.exception.IntegracaoException;
import br.com.yuri.votacao.messaging.VoteProducer;
import br.com.yuri.votacao.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private PautaService pautaService;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private CpfValidatorClient cpfValidatorClient;

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private VoteProducer voteProducer;

    private VotoService votoService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        votoService = new VotoService(
                pautaService,
                sessaoService,
                cpfValidatorClient,
                votoRepository,
                voteProducer,
                fixedClock
        );
    }

    @Test
    void deveReceberVotoEPublicarEventoNoKafka() {
        UUID pautaId = UUID.randomUUID();
        VotoRequest request = new VotoRequest("52998224725", EscolhaVoto.SIM);

        when(pautaService.buscarOuFalhar(pautaId)).thenReturn(Pauta.builder().id(pautaId).descricao("Pauta").build());
        when(votoRepository.existsByPautaIdAndAssociadoId(pautaId, request.associadoId())).thenReturn(false);

        VotoRecebidoResponse response = votoService.receberVoto(pautaId, request);

        assertEquals(pautaId, response.pautaId());
        assertEquals("52998224725", response.associadoId());
        assertEquals("Voto recebido e enviado para processamento", response.mensagem());

        ArgumentCaptor<VoteEventDto> captor = ArgumentCaptor.forClass(VoteEventDto.class);
        verify(voteProducer).publicar(captor.capture());
        assertEquals(pautaId, captor.getValue().pautaId());
        assertEquals("52998224725", captor.getValue().associadoId());
        assertEquals(EscolhaVoto.SIM, captor.getValue().escolha());
        assertEquals("2026-01-01T10:00", captor.getValue().dataVoto());
    }

    @Test
    void deveLancarConflitoQuandoAssociadoJaVotou() {
        UUID pautaId = UUID.randomUUID();
        VotoRequest request = new VotoRequest("52998224725", EscolhaVoto.NAO);

        when(pautaService.buscarOuFalhar(pautaId)).thenReturn(Pauta.builder().id(pautaId).descricao("Pauta").build());
        when(votoRepository.existsByPautaIdAndAssociadoId(pautaId, request.associadoId())).thenReturn(true);

        assertThrows(BusinessConflictException.class, () -> votoService.receberVoto(pautaId, request));
    }

    @Test
    void deveLancarErroIntegracaoQuandoFalharPublicacaoNoKafka() {
        UUID pautaId = UUID.randomUUID();
        VotoRequest request = new VotoRequest("52998224725", EscolhaVoto.SIM);

        when(pautaService.buscarOuFalhar(pautaId)).thenReturn(Pauta.builder().id(pautaId).descricao("Pauta").build());
        when(votoRepository.existsByPautaIdAndAssociadoId(pautaId, request.associadoId())).thenReturn(false);
        doThrow(new RuntimeException("kafka indisponivel")).when(voteProducer).publicar(any(VoteEventDto.class));

        assertThrows(IntegracaoException.class, () -> votoService.receberVoto(pautaId, request));
    }

    @Test
    void deveRetornarResultadoSemVotos() {
        UUID pautaId = UUID.randomUUID();
        when(pautaService.buscarOuFalhar(pautaId)).thenReturn(Pauta.builder().id(pautaId).descricao("Pauta").build());
        when(votoRepository.countByPautaIdAndEscolha(pautaId, EscolhaVoto.SIM)).thenReturn(0L);
        when(votoRepository.countByPautaIdAndEscolha(pautaId, EscolhaVoto.NAO)).thenReturn(0L);

        ResultadoVotacaoResponse response = votoService.obterResultado(pautaId);

        assertEquals(0L, response.totalSim());
        assertEquals(0L, response.totalNao());
        assertEquals(ResultadoStatus.SEM_VOTOS, response.status());
    }

    @Test
    void deveRetornarResultadoEmpate() {
        UUID pautaId = UUID.randomUUID();
        when(pautaService.buscarOuFalhar(eq(pautaId))).thenReturn(Pauta.builder().id(pautaId).descricao("Pauta").build());
        when(votoRepository.countByPautaIdAndEscolha(pautaId, EscolhaVoto.SIM)).thenReturn(3L);
        when(votoRepository.countByPautaIdAndEscolha(pautaId, EscolhaVoto.NAO)).thenReturn(3L);

        ResultadoVotacaoResponse response = votoService.obterResultado(pautaId);

        assertEquals(ResultadoStatus.EMPATE, response.status());
    }
}

