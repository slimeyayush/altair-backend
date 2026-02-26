package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

// CRITICAL FIX: Must be jakarta, not javax
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_JSON_CREDENTIALS:}")
    private String firebaseJson;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;

                if (firebaseJson != null && !firebaseJson.isEmpty()) {
                    System.out.println("⏳ Initializing Firebase from Environment Variable...");
                    serviceAccount = new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));
                } else {
                    System.out.println("⏳ Initializing Firebase from local file...");
                    serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
                }

                if (serviceAccount == null) {
                    throw new RuntimeException("❌ Firebase credentials missing! Set FIREBASE_JSON_CREDENTIALS or add the JSON file.");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase Admin SDK initialized successfully.");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}