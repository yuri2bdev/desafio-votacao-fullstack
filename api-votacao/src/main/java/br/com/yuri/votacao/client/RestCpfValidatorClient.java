package br.com.yuri.votacao.client;

import br.com.yuri.votacao.dto.CpfValidationResponse;
import br.com.yuri.votacao.exception.CpfInvalidoException;
import br.com.yuri.votacao.exception.CpfNaoHabilitadoException;
import br.com.yuri.votacao.exception.IntegracaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class RestCpfValidatorClient implements CpfValidatorClient {

    private static final String ABLE_TO_VOTE = "ABLE_TO_VOTE";
    private static final String UNABLE_TO_VOTE = "UNABLE_TO_VOTE";

    private final RestClient cpfValidationRestClient;

    @Override
    public void validarCpf(String cpf) {
        try {
            CpfValidationResponse response = cpfValidationRestClient.get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .body(CpfValidationResponse.class);

            if (response == null || response.status() == null) {
                throw new IntegracaoException("Resposta da validacao de CPF veio vazia", null);
            }

            if (ABLE_TO_VOTE.equalsIgnoreCase(response.status())) {
                return;
            }

            if (UNABLE_TO_VOTE.equalsIgnoreCase(response.status())) {
                throw new CpfNaoHabilitadoException("CPF nao habilitado para votar");
            }

            throw new IntegracaoException("Status de validacao de CPF desconhecido: " + response.status(), null);
        } catch (RestClientResponseException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode != null && statusCode.value() == 404) {
                throw new CpfInvalidoException("CPF invalido");
            }
            throw new IntegracaoException("Falha ao integrar com servico de validacao de CPF", ex);
        } catch (CpfInvalidoException | CpfNaoHabilitadoException | IntegracaoException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IntegracaoException("Erro inesperado ao validar CPF", ex);
        }
    }
}

