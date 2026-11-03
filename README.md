# 🧶 Pelusas – Aplicación web

Aplicación web basada en el juego de mesa **Pelusas**. Permite crear partidas, registrar jugadores, gestionar turnos, sacar y robar cartas, calcular puntuaciones y consultar el histórico de partidas.

El proyecto utiliza una arquitectura cliente-servidor, con un backend desarrollado en Java y Spring Boot, un frontend en React y persistencia mediante H2 Database.

## 1. 👥 Integrantes

- **Realizado por:** Yesica Yurley Mesa Isaza


## 2. 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Desarrollo del backend |
| Spring Boot 4.1.1 | Framework del backend |
| Spring WebMVC | Servicios REST |
| Spring Data JPA | Persistencia de datos |
| Hibernate | Mapeo objeto-relacional |
| H2 Database | Base de datos |
| Maven | Gestión de dependencias |
| React 19 | Desarrollo del frontend |
| JavaScript | Lógica del frontend |
| Vite 8 | Servidor de desarrollo |
| JUnit 5 | Pruebas unitarias |
| Mockito | Simulación de dependencias |
| MockMvc | Pruebas de integración REST |
| JaCoCo | Medición de cobertura |
| PIT | Pruebas de mutación |
| Playwright | Pruebas funcionales |
| Oxlint | Análisis estático del frontend |

## 3. 🏗️ Arquitectura

El proyecto implementa una arquitectura cliente-servidor, separando el frontend, el backend y la persistencia de datos.

**Frontend (React):**
- Presenta la interfaz gráfica del juego.
- Permite registrar jugadores y crear partidas.
- Muestra el estado de la partida, las cartas y los turnos.
- Envía las acciones del usuario al backend mediante peticiones HTTP.

**Backend (Java + Spring Boot):**
- Expone los servicios mediante una API REST.
- Contiene la lógica y las reglas de negocio.
- Gestiona las partidas, jugadores, cartas y turnos.
- Valida las acciones realizadas por los jugadores.
- Calcula los resultados y determina los ganadores.

**Base de datos (H2 + JPA):**
- Almacena el histórico de las partidas terminadas.
- Conserva los jugadores, sus cartas acumuladas, puntuaciones y resultados.
- Las partidas que están en ejecución se administran en memoria.

**Flujo de comunicación:**

```text
       USUARIO
          |
          v
    FRONTEND REACT
     Puerto 5173
          |
          | HTTP / JSON
          v
   BACKEND SPRING BOOT
      Puerto 8080
          |
          v
    CAPA DE SERVICIOS
          |
          v
    LÓGICA DEL JUEGO
          |
          v
    REPOSITORIOS JPA
          |
          v
      BASE DE DATOS
          H2
```

## 4. 💻 Requisitos

Para ejecutar el proyecto se necesitan las siguientes herramientas:

- Java JDK 21.
- Node.js compatible con Vite 8.
- npm.
- Git.
- Conexión a internet para descargar las dependencias durante la primera instalación.

Se puede verificar la instalación ejecutando:

```bash
java -version
node -v
npm -v
git --version
```

## 5. 📥 Instalación

**1. Clonar el repositorio:**

```bash
git clone https://github.com/YESICAY/pelusas.git
```

**2. Ingresar a la carpeta del proyecto:**

```bash
cd pelusas
```

**3. Instalar y preparar el backend:**

```bash
cd backend
```

En Linux o macOS:

```bash
./mvnw clean install
```

En Windows:

```bash
mvnw.cmd clean install
```

**4. Instalar las dependencias del frontend:**

Desde la raíz del proyecto:

```bash
cd frontend
npm install
```

También se puede utilizar:

```bash
npm ci
```

## 6. ⚙️ Configuración

### Backend

La configuración principal se encuentra en:

`backend/src/main/resources/application.properties`

```properties
spring.application.name=backend
spring.datasource.url=jdbc:h2:file:./data/pelusas
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

La base de datos H2 utiliza almacenamiento en archivos locales para conservar el histórico de partidas.

### Perfil de pruebas funcionales

El proyecto dispone de una configuración específica para las pruebas E2E:

`backend/src/main/resources/application-e2e.properties`

```properties
spring.datasource.url=jdbc:h2:mem:pelusas-e2e;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
```

Este perfil utiliza una base de datos en memoria y un mazo fijo de diez cartas para facilitar la reproducción de escenarios de prueba.

### Frontend

La configuración del servidor y del proxy se encuentra en:

`frontend/vite.config.js`

El frontend se ejecuta en el puerto 5173 y redirige las solicitudes `/api` al backend en el puerto 8080.

No es necesario configurar manualmente una conexión adicional a la base de datos.

## 7. ▶️ Ejecución

Para ejecutar la aplicación se deben iniciar el backend y el frontend en terminales independientes.

**1. Iniciar el backend:**

```bash
cd backend
```

Linux o macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

El backend estará disponible en:

`http://localhost:8080`

**2. Iniciar el frontend:**

En otra terminal:

```bash
cd frontend
npm run dev
```

El frontend estará disponible en:

`http://localhost:5173`

**3. Abrir la aplicación:**

Ingresar desde el navegador a:

`http://localhost:5173`

## 8. 🧪 Ejecución de pruebas unitarias

Las pruebas unitarias se encuentran en:

`backend/src/test/java/com/pelusas/backend/`

Se utiliza JUnit 5 y Mockito para validar el comportamiento de las clases y la lógica de negocio.

Para ejecutar las pruebas unitarias:

Linux o macOS:

```bash
./mvnw -Dtest='CartaTest,DesempateTest,JugadorTest,MazoTest,PartidaTest,PartidaServiceTest,MazoAleatorioFactoryTest,MazoFijoFactoryTest' test
```

Windows:

```bash
mvnw.cmd -Dtest="CartaTest,DesempateTest,JugadorTest,MazoTest,PartidaTest,PartidaServiceTest,MazoAleatorioFactoryTest,MazoFijoFactoryTest" test
```

Estas pruebas verifican, entre otros aspectos:

- Creación y distribución del mazo.
- Comportamiento de las cartas.
- Gestión de jugadores.
- Desarrollo de turnos.
- Reglas de pérdida.
- Robo y almacenamiento de cartas.
- Puntuaciones y desempates.

## 9. 🔗 Ejecución de pruebas de integración

Las pruebas de integración se encuentran en:

`backend/src/test/java/com/pelusas/backend/`

Se utiliza Spring Boot Test y MockMvc para comprobar la integración entre los controladores, servicios y repositorios.

**Pruebas de integración de servicios REST:**

Linux o macOS:

```bash
./mvnw -Dtest=PartidaControllerTest test
```

Windows:

```bash
mvnw.cmd -Dtest=PartidaControllerTest test
```

**Pruebas de integración con base de datos:**

Linux o macOS:

```bash
./mvnw -Dtest=PartidaRepositoryTest test
```

Windows:

```bash
mvnw.cmd -Dtest=PartidaRepositoryTest test
```

Estas pruebas verifican:

- Creación y consulta de partidas.
- Funcionamiento de los endpoints.
- Códigos de respuesta HTTP.
- Validaciones y manejo de errores.
- Persistencia y recuperación de información.
- Relaciones entre partidas y jugadores.
- Consulta del histórico de partidas.

Para ejecutar todas las pruebas del backend:

```bash
./mvnw test
```

En Windows:

```bash
mvnw.cmd test
```

## 10. 🎭 Ejecución de pruebas funcionales

Las pruebas funcionales se desarrollan con Playwright y JavaScript.

Se encuentran en:

`frontend/e2e/partida.spec.js`

Estas pruebas simulan la interacción de un usuario real con la aplicación.

**1. Ingresar a la carpeta frontend:**

```bash
cd frontend
```

**2. Instalar las dependencias:**

```bash
npm install
```

**3. Instalar los navegadores de Playwright:**

```bash
npx playwright install
```

**4. Ejecutar las pruebas funcionales:**

```bash
npm run test:e2e
```

La configuración de Playwright inicia automáticamente el backend con el perfil `e2e` y el frontend mediante Vite.

Los escenarios funcionales comprueban:

- Desarrollo de un turno exitoso.
- Pérdida del turno por repetición de cartas.
- Descarte de cartas y cambio de turno.
- Robo de cartas entre jugadores.
- Validación de acciones no permitidas.

**Consultar el reporte de Playwright:**

```bash
npx playwright show-report
```

## 11. 🧬 Ejecución de pruebas de mutación

El proyecto utiliza PIT Mutation Testing para evaluar la efectividad de las pruebas automatizadas mediante modificaciones controladas en el código fuente.

La configuración se encuentra en el archivo:

`backend/pom.xml`

El umbral mínimo de mutación configurado es del 90 %.

**Ejecutar las pruebas de mutación:**

Linux o macOS:

```bash
cd backend
./mvnw org.pitest:pitest-maven:mutationCoverage
```

Windows:

```bash
cd backend
mvnw.cmd org.pitest:pitest-maven:mutationCoverage
```

El reporte HTML se genera en:

```text
backend/target/pit-reports/index.html
```

Para consultar el reporte se puede abrir el archivo `index.html` desde un navegador.

**Cobertura de código con JaCoCo:**

También se puede generar el reporte de cobertura mediante:

```bash
./mvnw clean verify
```

El reporte HTML se encuentra en:

```text
backend/target/site/jacoco/index.html
```

### Reportes de pruebas

Los resultados de las pruebas se encuentran en `docs/reportes/`.

- Cobertura JaCoCo: `docs/reportes/cobertura-jacoco/`
- Mutación PIT: `docs/reportes/mutacion-pit/`
- Pruebas funcionales Playwright: `docs/reportes/playwright/`
