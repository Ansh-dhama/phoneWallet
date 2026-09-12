package com.example.phoneWallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhoneWalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhoneWalletApplication.class, args);
	}
}