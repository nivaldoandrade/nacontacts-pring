package com.nasa.nacontacts.domain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Contact contact = new Contact();
        contact.setName("Nivaldo Andrade");
        contact.setEmail("nivaldoandradef@gmail.com");
        contact.setUrl("https://nivaldoandrade.dev.br");


        Info info = new Info()
                .title("Nacontacts API")
                .version("1.0")
                .contact(contact);

        return new OpenAPI().info(info);
    }
}
