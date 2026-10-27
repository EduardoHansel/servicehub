# Relatório Técnico • Engenharia de Software
## Mudanças Propostas e Stack Tecnológica
*Mapeamento estruturado das ferramentas, bibliotecas e padrões arquiteturais propostos para evolução da ServiceHub API (Semanas 08, 09, 10 e 12).*

> **Visão Geral do Relatório:** Este documento consolida as mudanças práticas e evoluções de arquitetura propostas nos materiais das aulas 08, 09, 10 e 12. O objetivo é guiar a atualização incremental do projeto base (*ServiceHub API*) em quatro pilares: proteção e higienização de dados, autenticação stateless com novos protocolos de comunicação, garantia de qualidade via testes automatizados e orquestração de containers em ambiente reproduzível.

---

## Semana 08 (Aula 08) — Segurança em APIs, Validações e Vulnerabilidades
**Foco:** Segurança & Validação  
**Mudança proposta:** Implementar proteção de rotas, criptografia de senhas, validação declarativa em DTOs, tratamento global de erros, sanitização contra XSS/SQLi e mitigação das vulnerabilidades OWASP API Top 10.

* **Spring Security (`spring-boot-starter-security`) & `SecurityFilterChain`** `[FRAMEWORK]`  
  Framework padrão de segurança do ecossistema Spring que intercepta requisições HTTP por meio de uma cadeia de filtros Servlet (`DelegatingFilterProxy` e `FilterChainProxy`). Substitui o antigo `WebSecurityConfigurerAdapter` por beans declarativos que definem políticas de autenticação e autorização por rota (ex.: liberar `/api/public/**` e restringir `/api/admin/**`).

* **`BCryptPasswordEncoder`** `[CRIPTOGRAFIA]`  
  Implementação de `PasswordEncoder` que aplica o algoritmo de hash adaptativo BCrypt às senhas dos usuários antes de persisti-las no banco de dados, impedindo o armazenamento em texto puro ou hashes fracos (MD5/SHA-1).

* **`AuthenticationManager`, `DaoAuthenticationProvider` e `UserDetailsService`** `[COMPONENTES]`  
  Núcleo do fluxo de autenticação do Spring: o `UserDetailsService` consulta o usuário no banco (via repositório JPA), o `DaoAuthenticationProvider` valida a senha com o `PasswordEncoder` e o `AuthenticationManager` coordena a geração do token autenticado no `SecurityContext`.

* **Bean Validation (JSR-380 / Hibernate Validator / `spring-boot-starter-validation`)** `[BIBLIOTECA]`  
  Especificação Java para validação declarativa de dados de entrada em DTOs por meio de anotações como `@Valid`, `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@Min` e `@Max`, além de permitir a criação de regras customizadas implementando a interface `ConstraintValidator` (como um `@CPF`).

* **`@RestControllerAdvice` e `@ExceptionHandler`** `[COMPONENTE SPRING]`  
  Interceptador global de exceções que captura falhas de validação (`MethodArgumentNotValidException`) e as converte em respostas JSON padronizadas e amigáveis (HTTP 400 Bad Request), ocultando stack traces sensíveis em produção.

* **Apache Commons Text (`StringEscapeUtils`) & OWASP Java HTML Sanitizer** `[BIBLIOTECAS]`  
  Ferramentas de sanitização de entradas na camada de serviço que limpam textos livres e convertem caracteres especiais (como `<`, `>`, `"`) em entidades HTML seguras (`escapeHtml4`) ou aplicam políticas de *allowlist*, prevenindo ataques de Cross-Site Scripting (XSS).

* **Spring Data JPA (`@Query` com `@Param`) & `NamedParameterJdbcTemplate`** `[PERSISTÊNCIA]`  
  Mecanismos que utilizam *Prepared Statements* e parâmetros nomeados em consultas ao banco de dados em vez de concatenação direta de strings, neutralizando ataques de Injeção de SQL (SQL Injection).

* **Bucket4j & Spring Data `Pageable`** `[BIBLIOTECAS]`  
  Utilizados para mitigar vulnerabilidades de Consumo Irrestrito de Recursos (OWASP API4) e Força Bruta (API2): o **Bucket4j** aplica *Rate Limiting* (algoritmo Token Bucket, retornando HTTP 429) por IP/usuário, enquanto o **Pageable** obriga a paginação nas listagens de dados.

* **`@EnableMethodSecurity`, `@PreAuthorize` (SpEL) e `@AuthenticationPrincipal`** `[AUTORIZAÇÃO]`  
  Permitem definir controle de acesso granular diretamente em métodos de Controller ou Service usando expressões SpEL (ex.: `hasRole('ADMIN')`, `isAuthenticated()`) e validar a titularidade do objeto requisitado contra o usuário logado, prevenindo BOLA (API1) e BFLA (API5).

* **`CorsConfigurationSource` & `@JsonIgnore`** `[CONFIGURAÇÃO]`  
  O `CorsConfigurationSource` define explicitamente origens, métodos HTTP e cabeçalhos permitidos entre domínios (evitando o uso inseguro de wildcard `*` com credenciais), enquanto DTOs dedicados e `@JsonIgnore` evitam exposição excessiva de dados e *Mass Assignment* (API3).

---

## Semana 09 (Aula 09) — Autenticação JWT e Padrões de API (REST, gRPC e GraphQL)
**Foco:** JWT & Padrões de API  
**Mudança proposta:** Migrar a API para autenticação 100% stateless baseada em tokens JWT com controle de acesso por Roles/Authorities e implementar um endpoint alternativo ao REST utilizando gRPC ou GraphQL para comparação prática.

* **JSON Web Token (JWT — RFC 7519) & Biblioteca `jjwt` (`io.jsonwebtoken`)** `[PADRÃO / BIBLIOTECA]`  
  Padrão de token compacto, autocontido e assinado digitalmente (HMAC SHA256 / HS256) estruturado em *Header*, *Payload* (claims como `sub`, `exp`, `roles`) e *Signature* em Base64URL. A biblioteca **jjwt** é usada no `JwtService` para gerar, assinar, extrair claims e validar a expiração dos tokens.

* **`OncePerRequestFilter` (`JwtAuthFilter`), `SecurityContextHolder` e `SessionCreationPolicy.STATELESS`** `[SPRING SECURITY]`  
  Configuração que desativa a criação de sessões HTTP em memória no servidor e insere um filtro customizado antes do `UsernamePasswordAuthenticationFilter`. Esse filtro intercepta cada requisição, lê o header `Authorization: Bearer <token>`, valida o JWT e popula o `SecurityContextHolder` com as permissões (`ROLE_ADMIN`, `ROLE_PROVIDER`, `ROLE_CLIENT`).

* **gRPC (Google Remote Procedure Call) & Protocol Buffers (`.proto` / `proto3`)** `[FRAMEWORK / SERIALIZAÇÃO]`  
  Framework RPC de altíssima performance que opera sobre **HTTP/2** com suporte a *streaming* bidirecional e serialização binária via **Protocol Buffers** (até 10x mais compacto que JSON). O arquivo `.proto` define o contrato fortemente tipado das mensagens e serviços para comunicação interna entre microsserviços.

* **`grpc-spring-boot-starter`, `@GrpcService` e `grpcurl`** `[BIBLIOTECA / CLI]`  
  Integra o servidor gRPC ao Spring Boot (escutando por padrão na porta `9090` em paralelo à porta REST `8080`) com geração automática de *stubs* Java via plugin Maven/Gradle. O **grpcurl** (ou Postman) é utilizado para testar as chamadas remotas e medir a latência.

* **GraphQL (`schema.graphqls`) & Spring for GraphQL** `[LINGUAGEM / FRAMEWORK]`  
  Linguagem de consulta para APIs que permite ao cliente especificar exatamente quais campos deseja receber, eliminando problemas de *over-fetching* e *under-fetching* do REST. Separa operações em **Queries** (leitura), **Mutations** (escrita) e **Subscriptions** (eventos em tempo real via WebSocket).

* **`@QueryMapping`, `@MutationMapping`, `@SchemaMapping` e GraphiQL Playground** `[ANOTAÇÕES / TOOLING]`  
  Anotações declarativas do Spring for GraphQL para implementar *resolvers* e sub-resolvers (evitando o problema de *N+1 queries* com DataLoader), expondo automaticamente o endpoint `/graphql` e a interface interativa de testes **GraphiQL** em `/graphiql`.

---

## Semana 10 (Aula 10) — Testes Automatizados em APIs com Spring Boot
**Foco:** Qualidade & Testes  
**Mudança proposta:** Estruturar a Pirâmide de Testes na API (unitários, camada web e integração), aplicando o padrão AAA (Arrange-Act-Assert), isolamento com mocks e validação de cobertura mínima de 80%.

* **JUnit 5 (JUnit Platform + Jupiter + Vintage / `spring-boot-starter-test`)** `[FRAMEWORK DE TESTES]`  
  Framework padrão para execução de testes automatizados em Java. Fornece anotações de ciclo de vida e execução como `@Test`, `@BeforeEach`, `@AfterEach`, `@DisplayName`, `@RepeatedTest`, `@ParameterizedTest` e `@Disabled`.

* **AssertJ (`assertThat`, `assertThatThrownBy`)** `[BIBLIOTECA DE ASSERÇÕES]`  
  Biblioteca de asserções com sintaxe fluente e mensagens de falha altamente descritivas que substitui as asserções nativas do JUnit, facilitando a verificação de valores, coleções e lançamento de exceções na etapa *Assert*.

* **Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@Spy`, `@InjectMocks`)** `[BIBLIOTECA DE MOCKING]`  
  Permite testar a camada de `Service` de forma 100% isolada substituindo dependências reais (como Repositories e serviços de e-mail) por objetos simulados. Utiliza `when(...).thenReturn(...)` / `thenThrow(...)` para *stubs* e `verify(..., times(1))` para auditar interações.

* **MockMvc, `@WebMvcTest`, `@MockBean` e Jackson `ObjectMapper`** `[SPRING TEST (WEB)]`  
  Ferramental que carrega apenas a camada web do Spring sem subir um servidor HTTP real, permitindo simular requisições (`GET`, `POST`) para os Controllers, serializar payloads com `ObjectMapper` e validar status HTTP (200, 201, 400) e expressões `jsonPath`.

* **`@SpringBootTest`, `TestRestTemplate` e `@AutoConfigureTestDatabase`** `[TESTE DE INTEGRAÇÃO]`  
  Inicializa o contexto completo da aplicação Spring em uma porta aleatória (`RANDOM_PORT`) para testar a integração real ponta a ponta entre Controller, Service, Repository e banco de dados.

* **H2 Database In-Memory (`com.h2database:h2`)** `[BANCO DE DADOS]`  
  Banco relacional leve que roda em memória durante a fase de testes (configurado em `src/test/resources/application.properties` com `ddl-auto=create-drop`), proporcionando execução rápida e sem necessidade de infraestrutura externa.

* **Testcontainers (`@Testcontainers`, `@Container`, `PostgreSQLContainer`, `@DynamicPropertySource`)** `[BIBLIOTECA / DOCKER]`  
  Biblioteca que instancia automaticamente um container Docker com o banco de dados real de produção (ex.: `postgres:15`) durante os testes de integração, garantindo paridade total de dialeto SQL e recursos específicos (como JSONB).

* **JaCoCo (Java Code Coverage)** `[PLUGIN MAVEN / MÉTRICA]`  
  Ferramenta integrada ao build Maven que mede o percentual de linhas, *branches* e métodos exercitados pelos testes automatizados, auxiliando a atingir a meta profissional mínima de 80% de cobertura (e 100% nas regras de negócio do Service).

---

## Semana 12 (Aula 12) — Docker II: Docker Compose e Orquestração de Ambientes
**Foco:** DevOps & Containers  
**Mudança proposta:** Substituir a execução manual de containers isolados (`docker run`) por uma arquitetura orquestrada e reproduzível (ServiceHub API + PostgreSQL 15) com persistência em volume, rede interna isolada, healthchecks e separação de ambientes.

* **Docker Compose (`docker-compose.yml`) & Docker Compose CLI** `[ORQUESTRADOR]`  
  Ferramenta oficial do ecossistema Docker que define e executa aplicações multi-container de forma declarativa em YAML. Permite gerenciar todo o ciclo de vida do ambiente com comandos unificados como `docker compose up -d --build`, `down -v`, `ps`, `logs -f`, `exec`, `restart` e `pull`.

* **Dockerfile (Diretiva `build`) & Imagem Oficial `postgres:15`** `[IMAGENS DOCKER]`  
  O serviço `api` utiliza a diretiva `build` para compilar a imagem da aplicação Java a partir do `Dockerfile` local, enquanto o serviço `postgres` consome diretamente a imagem oficial `postgres:15` do Docker Hub provisionando o banco automaticamente via variáveis `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD`.

* **Docker Volumes (Volume Nomeado `pg_data` vs. Bind Mounts)** `[ARMAZENAMENTO]`  
  Mecanismo de persistência que mapeia o diretório interno do PostgreSQL (`/var/lib/postgresql/data`) para uma área gerenciada pelo host fora do ciclo de vida efêmero do container, garantindo que os dados do banco não sejam perdidos ao reiniciar ou recriar containers.

* **Docker Networks (Driver `bridge` / `app-network`) & DNS Interno** `[REDES]`  
  Cria malhas de rede virtuais isoladas onde os containers se comunicam usando o próprio nome do serviço como hostname (ex.: `jdbc:postgresql://postgres:5432/servicehub`), dispensando IPs fixos e isolando o banco de serviços externos não autorizados.

* **Diretiva `depends_on`, `healthcheck` (`condition: service_healthy`) e `pg_isready`** `[CONTROLE DE INICIALIZAÇÃO]`  
  Coordena a ordem de boot dos serviços: o utilitário nativo `pg_isready` testa periodicamente se o PostgreSQL já aceita conexões e o `depends_on` com `service_healthy` retém a subida da API Java até que o banco esteja efetivamente pronto, evitando falhas de conexão na inicialização.

* **Arquivos `.env`, `.env.example` e Gerenciadores de Segredos (Vault / AWS Secrets / Docker Secrets)** `[CONFIGURAÇÃO & SEGURANÇA]`  
  Desacoplam credenciais sensíveis do código-fonte e do `docker-compose.yml`: o arquivo `.env` injeta variáveis dinamicamente (sendo ignorado no Git via `.gitignore`), o `.env.example` documenta as chaves exigidas e os gerenciadores de segredos protegem credenciais em produção.

* **Múltiplos Arquivos Compose (Override por Ambiente: Dev, Staging e Prod) & Políticas de `restart`** `[ARQUITETURA DE DEPLOY]`  
  Permite combinar um arquivo base com arquivos específicos (`docker-compose.dev.yml` com hot-reload e porta de debug `5005`; `docker-compose.prod.yml` restrito e com limites de recursos) usando a flag `-f`, além de configurar políticas de resiliência automática como `restart: unless-stopped` ou `always`.

---

## Resumo Geral de Entregáveis

| Semana | Foco Principal da Mudança | Entregáveis Práticos no Projeto ServiceHub |
| :--- | :--- | :--- |
| **Semana 08** | Segurança Base & Validação | Configurar `SecurityFilterChain` + `BCrypt`, validar DTOs com Bean Validation + `@RestControllerAdvice`, sanitizar entradas e proteger rotas com `@PreAuthorize`. |
| **Semana 09** | Autenticação Stateless & Padrões | Implementar login `POST /api/auth/login` gerando token JWT com `JwtAuthFilter`, controle por roles (Admin/Provider/Client) e criar endpoint alternativo em **GraphQL** ou **gRPC**. |
| **Semana 10** | Testes Automatizados | Criar testes unitários de Service com **JUnit 5 + Mockito**, testes de Controller com **MockMvc** e testes de integração completos com **@SpringBootTest + H2/Testcontainers**. |
| **Semana 12** | Orquestração com Docker Compose | Estruturar `docker-compose.yml` unificando API + PostgreSQL 15, configurar volume `pg_data`, rede `app-network`, `healthcheck` com `pg_isready` e variáveis em `.env`. |