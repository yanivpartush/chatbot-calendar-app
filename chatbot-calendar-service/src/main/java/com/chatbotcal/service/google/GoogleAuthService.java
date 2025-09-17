package com.chatbotcal.service.google;

import com.chatbotcal.repository.TokenRepository;
import com.chatbotcal.repository.UserRepository;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserToken;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Optional;

import static com.chatbotcal.util.JsonTemplateUtil.loadExternalResource;

@Service
public class GoogleAuthService {

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static final String TOKENS_DIRECTORY_PATH = System.getenv("CONFIG_DIR") + "/tokens";
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    private GoogleAuthorizationCodeFlow flow;

    public GoogleAuthService(TokenRepository tokenRepository, UserRepository userRepository) throws Exception {

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;

        InputStream in = loadExternalResource("gcp-chatbot-credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    public String getAuthUrl(String userId) {
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(userId)
                .set("prompt", "consent")
                .build();
    }

    public void exchangeCode(String userId, String code) throws Exception {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Credential credential = flow.createAndStoreCredential(tokenResponse, user.getId());


        String refreshToken = credential.getRefreshToken();
        if (refreshToken == null) {
            refreshToken = tokenResponse.getRefreshToken();
        }

        UserToken userToken = UserToken.builder().user(user)
                .accessToken(credential.getAccessToken())
                .refreshToken(refreshToken)
                .expiryTime(credential.getExpirationTimeMilliseconds())
                .build();

        tokenRepository.save(userToken);

    }


    public Optional<Credential> getAndUpdateUserCredential(String userId) {
        return tokenRepository.findById(userId)
                .map(token -> {
                    try {
                        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                                .setJsonFactory(JSON_FACTORY)
                                .setClientAuthentication(flow.getClientAuthentication())
                                .setTokenServerEncodedUrl(flow.getTokenServerEncodedUrl())
                                .build()
                                .setAccessToken(token.getAccessToken())
                                .setRefreshToken(token.getRefreshToken())
                                .setExpirationTimeMilliseconds(token.getExpiryTime());


                        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
                            credential.refreshToken();

                            token.setAccessToken(credential.getAccessToken());
                            token.setExpiryTime(credential.getExpirationTimeMilliseconds());
                            tokenRepository.save(token);
                        }

                        return credential;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to build Credential for user " + userId, e);
                    }
                });
    }

}
