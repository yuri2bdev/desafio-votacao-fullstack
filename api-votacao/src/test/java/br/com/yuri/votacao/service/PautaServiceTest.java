package br.com.yuri.votacao.service;

import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.dto.CreatePautaRequest;
import br.com.yuri.votacao.dto.PautaResponse;
import br.com.yuri.votacao.exception.ResourceNotFoundException;
import br.com.yuri.votacao.repository.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    private PautaService pautaService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        pautaService = new PautaService(pautaRepository, fixedClock);
    }

    @Test
    void deveCriarPautaComDataDoRelogioDaAplicacao() {
        CreatePautaRequest request = new CreatePautaRequest("Nova pauta");
        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PautaResponse response = pautaService.criar(request);

        assertNotNull(response.id());
        assertEquals("Nova pauta", response.descricao());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), response.dataCriacao());
    }

    @Test
    void deveBuscarPautaPorId() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = Pauta.builder()
                .id(pautaId)
                .descricao("Pauta de teste")
                .dataCriacao(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));

        PautaResponse response = pautaService.buscarPorId(pautaId);

        assertEquals(pautaId, response.id());
        assertEquals("Pauta de teste", response.descricao());
    }

    @Test
    void deveListarTodasAsPautasMapeandoParaDto() {
        Pauta pauta1 = Pauta.builder().id(UUID.randomUUID()).descricao("P1").dataCriacao(LocalDateTime.of(2026, 1, 1, 9, 0)).build();
        Pauta pauta2 = Pauta.builder().id(UUID.randomUUID()).descricao("P2").dataCriacao(LocalDateTime.of(2026, 1, 1, 9, 30)).build();
        when(pautaRepository.findAll()).thenReturn(List.of(pauta1, pauta2));

        List<PautaResponse> response = pautaService.listarTodas();

        assertEquals(2, response.size());
        assertEquals("P1", response.get(0).descricao());
        assertEquals("P2", response.get(1).descricao());
    }

    @Test
    void deveLancarNotFoundQuandoPautaNaoExistir() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pautaService.buscarOuFalhar(pautaId));
    }
}

