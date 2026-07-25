package com.pedacinhodemaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da aplicação.
 *
 * @EnableScheduling habilita o job de varredura de timers de pedido
 * (ver OrderTimerService), que é o mecanismo que torna o backend — e não
 * o navegador — a fonte de verdade sobre quanto tempo um pedido está em preparo.
 */
@SpringBootApplication
@EnableScheduling
public class PedacinhoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PedacinhoApplication.class, args);
	}
}