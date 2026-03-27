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
    void deveRetornar404QuandoRecursoNaoEncontrado() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/pautas/123");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Pauta nao encontrada"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().status());
        assertEquals("Pauta nao encontrada", response.getBody().message());
    }

    @Test
    void deveRetornar502QuandoErroDeIntegracao() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/pautas/123/votos");

        ResponseEntity<ErrorResponse> response = handler.handleIntegracao(
                new IntegracaoException("Falha na integracao", new RuntimeException()),
                request
        );

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getBody().status());
    }

    @Test
    void deveRetornar500QuandoErroGenerico() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/pautas");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erro interno inesperado", response.getBody().message());
    }

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
