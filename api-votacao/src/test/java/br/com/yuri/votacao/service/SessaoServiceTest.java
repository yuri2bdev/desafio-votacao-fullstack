package br.com.yuri.votacao.service;

import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.domain.SessaoVotacao;
import br.com.yuri.votacao.dto.SessaoResponse;
import br.com.yuri.votacao.exception.BusinessConflictException;
import br.com.yuri.votacao.exception.SessaoFechadaException;
import br.com.yuri.votacao.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private PautaService pautaService;

    private SessaoService sessaoService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        sessaoService = new SessaoService(sessaoVotacaoRepository, pautaService, fixedClock);
    }

    @Test
    void deveAbrirSessaoComDuracaoPadrao() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = Pauta.builder().id(pautaId).descricao("Pauta").build();

        when(sessaoVotacaoRepository.existsByPautaId(pautaId)).thenReturn(false);
        when(pautaService.buscarOuFalhar(pautaId)).thenReturn(pauta);
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoResponse response = sessaoService.abrirSessao(pautaId, null);

        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0, 0), response.dataAbertura());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 1, 0), response.dataFechamento());
    }

    @Test
    void deveAbrirSessaoComDuracaoInformada() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = Pauta.builder().id(pautaId).descricao("Pauta").build();

        when(sessaoVotacaoRepository.existsByPautaId(pautaId)).thenReturn(false);
        when(pautaService.buscarOuFalhar(pautaId)).thenReturn(pauta);
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoResponse response = sessaoService.abrirSessao(pautaId, 120);

        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 2, 0), response.dataFechamento());
    }

    @Test
    void deveLancarConflitoQuandoSessaoJaExiste() {
        UUID pautaId = UUID.randomUUID();
        when(sessaoVotacaoRepository.existsByPautaId(pautaId)).thenReturn(true);

        assertThrows(BusinessConflictException.class, () -> sessaoService.abrirSessao(pautaId, null));
    }

    @Test
    void deveLancarErroQuandoSessaoFechada() {
        UUID pautaId = UUID.randomUUID();
        SessaoVotacao sessao = SessaoVotacao.builder()
                .id(UUID.randomUUID())
                .dataAbertura(LocalDateTime.of(2026, 1, 1, 8, 0))
                .dataFechamento(LocalDateTime.of(2026, 1, 1, 9, 0))
                .build();

        when(sessaoVotacaoRepository.findByPautaId(eq(pautaId))).thenReturn(Optional.of(sessao));

        assertThrows(SessaoFechadaException.class, () -> sessaoService.validarSessaoAberta(pautaId));
    }
}

