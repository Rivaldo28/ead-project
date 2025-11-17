package com.ead.authuser;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@OpenAPIDefinition(
//        info = @Info(
//                title = "Sistemas API Usuários",
//                description = "Gestão de usuários do sistema"
//        )
//)
public class AuthuserApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthuserApplication.class, args);
	}

}
