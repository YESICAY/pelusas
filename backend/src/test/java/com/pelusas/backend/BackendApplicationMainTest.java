package com.pelusas.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BackendApplicationMainTest {

    @Test
    void mainArrancaLaAplicacion() {
        assertDoesNotThrow(() -> BackendApplication.main(new String[] {
                "--spring.main.web-application-type=none",
                "--spring.datasource.url=jdbc:h2:mem:prueba-main",
                "--spring.jpa.hibernate.ddl-auto=create-drop"
        }));
    }
}