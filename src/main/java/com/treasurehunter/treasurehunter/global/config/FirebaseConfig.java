package com.treasurehunter.treasurehunter.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${fcm.service.account.key.path}")
    private Resource SERVICE_ACCOUNT_KEY_PATH;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {

        // 1) service account key 읽어오기
        final InputStream serviceAccountKey = SERVICE_ACCOUNT_KEY_PATH.getInputStream();

        // 2) service account key로 Credentials 만들고 적용
        final FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountKey))
                .build();

        // 3) 이미 initialize 되어있는지 확인후 initialize
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(firebaseOptions);
        }

        return FirebaseApp.getInstance();
    }
}
