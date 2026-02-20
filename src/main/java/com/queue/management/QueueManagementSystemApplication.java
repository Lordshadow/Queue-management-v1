package com.queue.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class QueueManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueueManagementSystemApplication.class, args);
	}

}
