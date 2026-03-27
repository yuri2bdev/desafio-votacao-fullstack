package br.com.yuri.votacao.integration;

import br.com.yuri.votacao.client.CpfValidatorClient;
import br.com.yuri.votacao.domain.EscolhaVoto;
import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.domain.Voto;
import br.com.yuri.votacao.messaging.VoteProducer;
import br.com.yuri.votacao.repository.PautaRepository;
import br.com.yuri.votacao.repository.SessaoVotacaoRepository;
import br.com.yuri.votacao.repository.VotoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class VotacaoApiIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @MockitoBean
    private CpfValidatorClient cpfValidatorClient;

    @MockitoBean
    private VoteProducer voteProducer;

    @BeforeEach
    void cleanDatabase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        votoRepository.deleteAll();
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    void deveCriarPautaAbrirSessaoEReceberVotoCom202() throws Exception {
        doNothing().when(cpfValidatorClient).validarCpf(any());

        UUID pautaId = criarPautaViaApi("Pauta integracao");

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"duracaoSegundos\":120}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pautaId").value(pautaId.toString()));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"associadoId\":\"52998224725\",\"escolha\":\"SIM\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.pautaId").value(pautaId.toString()))
                .andExpect(jsonPath("$.associadoId").value("52998224725"));

        verify(voteProducer).publicar(any());
    }

    @Test
    void deveRetornar409QuandoAssociadoJaVotouNaPauta() throws Exception {
        doNothing().when(cpfValidatorClient).validarCpf(any());

        UUID pautaId = criarPautaViaApi("Pauta com duplicidade");

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"duracaoSegundos\":120}"))
                .andExpect(status().isCreated());

        Pauta pauta = pautaRepository.findById(pautaId).orElseThrow();
        votoRepository.save(Voto.builder()
                .pauta(pauta)
                .associadoId("52998224725")
                .escolha(EscolhaVoto.SIM)
                .dataVoto(LocalDateTime.now())
                .build());

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"associadoId\":\"52998224725\",\"escolha\":\"NAO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Associado ja votou nesta pauta"));
    }

    @Test
    void deveRetornarResultadoComContagemCorreta() throws Exception {
        UUID pautaId = criarPautaViaApi("Resultado integrado");
        Pauta pauta = pautaRepository.findById(pautaId).orElseThrow();

        votoRepository.save(Voto.builder()
                .pauta(pauta)
                .associadoId("52998224725")
                .escolha(EscolhaVoto.SIM)
                .dataVoto(LocalDateTime.now())
                .build());

        votoRepository.save(Voto.builder()
                .pauta(pauta)
                .associadoId("16899535009")
                .escolha(EscolhaVoto.NAO)
                .dataVoto(LocalDateTime.now())
                .build());

        votoRepository.save(Voto.builder()
                .pauta(pauta)
                .associadoId("11144477735")
                .escolha(EscolhaVoto.SIM)
                .dataVoto(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(pautaId.toString()))
                .andExpect(jsonPath("$.totalSim").value(2))
                .andExpect(jsonPath("$.totalNao").value(1))
                .andExpect(jsonPath("$.status").value("APROVADA"));
    }

    private UUID criarPautaViaApi(String descricao) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"" + descricao + "\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = JsonPath.read(response, "$.id");
        return UUID.fromString(id);
    }
}

