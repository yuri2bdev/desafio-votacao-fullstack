package br.com.yuri.votacao.exception;

import br.com.yuri.votacao.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Test
    void deveRetornar403QuandoCpfNaoHabilitado() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/pautas/1/votos");

        ResponseEntity<ErrorResponse> response = handler.handleCpfNaoHabilitado(
                new CpfNaoHabilitadoException("CPF nao habilitado para votar"),
                request
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().status());
    }
}

