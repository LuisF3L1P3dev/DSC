package br.edu.ifrn.api_chamados_ifrn.exception;

import br.edu.ifrn.api_chamados_ifrn.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validarCampos(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> dadosInvalidos(IllegalArgumentException exception, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + exception.getName(), request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException exception,
            HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException exception, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Os dados entram em conflito com um registro existente.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> demaisErros(Exception exception, HttpServletRequest request) {
        if (exception instanceof ErrorResponse erroHttp) {
            String mensagem = erroHttp.getBody().getDetail();
            if (mensagem == null || mensagem.isBlank()) {
                mensagem = "Não foi possível processar a requisição.";
            }
            return resposta(erroHttp.getStatusCode(), mensagem, request);
        }
        LOG.error("Erro inesperado ao processar {}", request.getRequestURI(), exception);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição.", request);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatusCode status, String mensagem, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(Instant.now(), status.value(), mensagem, request.getRequestURI()));
    }
}
