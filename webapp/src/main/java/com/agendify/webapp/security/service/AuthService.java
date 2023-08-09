package com.agendify.webapp.security.service;

import com.agendify.domain.entities.Usuario;
import com.agendify.domain.repositories.ClienteRepository;
import com.agendify.webapp.security.exceptions.InvalidCredentialsException;
import com.agendify.webapp.security.exceptions.RequestTokenException;
import com.agendify.webapp.security.records.AuthRequest;
import com.agendify.webapp.security.records.AuthResponse;
import com.agendify.webapp.security.records.KeycloakTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
public class AuthService {

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private ClienteRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String verifyTokenUrl;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.clientSecret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String EMAIL_SENHA_INVALIDOS = "Email ou senha incorretos, verique os dados informados e tente novamente";

    public AuthResponse handleAuthRequest(AuthRequest authRequest) throws InvalidCredentialsException, RequestTokenException {
        logger.trace(String.format("Verificando dados de login do usuário %s", authRequest.email()));

        Usuario usuario = usuarioRepository.findByEmail(authRequest.email());

        checkPassword(authRequest, usuario);

        String token = getToken();

        return new AuthResponse(usuario.getEmail(), token);
    }

    private void checkPassword(AuthRequest authRequest, Usuario usuario) throws InvalidCredentialsException {
        logger.trace("Verificando senha fornecida");

        if (usuario == null) {
            throw new InvalidCredentialsException(EMAIL_SENHA_INVALIDOS);
        }

        boolean isPasswordValid = passwordEncoder.matches(authRequest.password(), usuario.getSenha());

        if (!isPasswordValid) {
            throw new InvalidCredentialsException(EMAIL_SENHA_INVALIDOS);
        }
    }

    private String getToken() throws RequestTokenException {
        logger.info("Solicitando novo token ao Keycloak");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "client_credentials");

        HttpEntity<Object> request = new HttpEntity<>(map, headers);
        ResponseEntity<KeycloakTokenResponse> response;

        try {

            response = restTemplate.postForEntity(verifyTokenUrl, request, KeycloakTokenResponse.class);
            return response.getBody().access_token();

        } catch (RestClientException e) {
            logger.error("Erro ao solicitar token ao keycloak", e);

            throw new RequestTokenException("Erro ao obter ao token de acesso");
        }
    }
}