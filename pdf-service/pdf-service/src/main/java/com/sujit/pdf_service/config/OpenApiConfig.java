package com.sujit.pdf_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PDF Generation Service API")
                        .version("1.0")
                        .description("API documentation for the PDF Generation Service in the event-driven microservices system")
                        .contact(new Contact()
                                .name("Sujit")
                                .email("support@pdfgenerationservice.local")));
    }
}
