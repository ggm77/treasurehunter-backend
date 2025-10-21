package com.treasurehunter.treasurehunter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // updatedAt컬럼을 위해서 추가
public class TreasurehunterApplication {
    // 2025.10.03 develop start
	public static void main(String[] args) {
		SpringApplication.run(TreasurehunterApplication.class, args);
	}

}
