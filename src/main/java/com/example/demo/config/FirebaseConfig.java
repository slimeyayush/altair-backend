package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    // On Render, paste the JSON content into this Environment Variable
    @Value("${FIREBASE_JSON_CREDENTIALS:}")
    private String firebaseJson;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;

                if (firebaseJson != null && !firebaseJson.isEmpty()) {
                    // 1. Try loading from Environment Variable (Render)
                    serviceAccount = new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));
                } else {
                    // 2. Fallback to local file (Your Laptop)
                    serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
                }

                if (serviceAccount == null) {
                    throw new RuntimeException("Firebase credentials missing! Set FIREBASE_JSON_CREDENTIALS or add the JSON file.");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}