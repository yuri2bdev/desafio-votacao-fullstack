package br.com.yuri.votacao.client;

import br.com.yuri.votacao.dto.CpfValidationResponse;
import br.com.yuri.votacao.exception.CpfInvalidoException;
import br.com.yuri.votacao.exception.CpfNaoHabilitadoException;
import br.com.yuri.votacao.exception.IntegracaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestCpfValidatorClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    void devePermitirQuandoCpfEstiverHabilitado() {
        RestCpfValidatorClient client = new RestCpfValidatorClient(restClient);
        mockChainWithResponse(new CpfValidationResponse("ABLE_TO_VOTE"));

        assertDoesNotThrow(() -> client.validarCpf("12345678901"));
    }

    @Test
    void deveLancarExcecaoNegocioQuandoCpfNaoHabilitado() {
        RestCpfValidatorClient client = new RestCpfValidatorClient(restClient);
        mockChainWithResponse(new CpfValidationResponse("UNABLE_TO_VOTE"));

        assertThrows(CpfNaoHabilitadoException.class, () -> client.validarCpf("12345678901"));
    }

    @Test
    void deveLancarCpfInvalidoQuandoServicoRetornar404() {
        RestCpfValidatorClient client = new RestCpfValidatorClient(restClient);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), eq("12345678901"))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CpfValidationResponse.class)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null)
        );

        assertThrows(CpfInvalidoException.class, () -> client.validarCpf("12345678901"));
    }

    @Test
    void deveLancarIntegracaoParaStatusDesconhecido() {
        RestCpfValidatorClient client = new RestCpfValidatorClient(restClient);
        mockChainWithResponse(new CpfValidationResponse("UNKNOWN"));

        assertThrows(IntegracaoException.class, () -> client.validarCpf("12345678901"));
    }

    private void mockChainWithResponse(CpfValidationResponse response) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), eq("12345678901"))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CpfValidationResponse.class)).thenReturn(response);
    }
}


