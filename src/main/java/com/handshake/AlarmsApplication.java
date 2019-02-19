package com.handshake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static j2html.TagCreator.body;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.li;

@SpringBootApplication
public class AlarmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlarmsApplication.class, args);
	}
}
