# URL Shortener — Desafio Técnico

Encurtador de URLs para teste técnico para Topaz: recebe uma URL (e, opcionalmente, um alias customizado), gera um código curto e redireciona quem acessa o link curto para a URL original.

## Stack

- **Java 8**, empacotado como **WAR** para **WildFly 10** (conforme premissa do desafio).
- **JAX-RS** (API REST) + **CDI** (injeção de dependência) + **JPA/Hibernate** (persistência), todos providos pelo próprio WildFly em runtime (`javaee-api` como dependência `provided`).
- **H2** em memória, via JPA `RESOURCE_LOCAL` (sem datasource JNDI).
- **Frontend**: HTML/CSS/JS puro (sem framework/build step), servido pelo próprio WildFly.
- **Testes**: JUnit 5.
- **Docker Compose**: build do WAR + WildFly 10, prontos com um comando.

## Como rodar

### Opção A — Docker Compose (recomendado)

```bash
docker compose up --build
```

Sobe o WildFly 10 já com o WAR implantado. Acesse:

- Frontend: http://localhost:8080/url-shortener/
- API: http://localhost:8080/url-shortener/api/urls
- Redirect: http://localhost:8080/url-shortener/r/{code}

> A imagem oficial `jboss/wildfly:10.1.0.Final` só existe para `linux/amd64`,
> em Mac com Apple Silicon ela roda via emulação (mais lenta pra subir, mas
> funcional).

### Opção B — IntelliJ + WildFly local

1. `File > Open`, selecione o `pom.xml` na raiz. Aguarde a importação Maven.
2. Baixe o JDK 8 diretamente pelo IntelliJ (`Project Structure > SDK > Add SDK > Download JDK`, versão 8, vendor Temurin ou Zulu).
3. Baixe o WildFly 10.1.0.Final em wildfly.org/downloads e extraia localmente.
4. `Run > Edit Configurations > + > WildFly Server > Local`, aponte pra pasta extraída, confirme que o JRE da configuração é o Java 8 baixado no passo 2.
5. Na aba **Deployment**, adicione o artefato `url-shortener:war exploded`.
6. Rode. A aplicação sobe em `http://localhost:8080/url-shortener/`.

### Rodando os testes

```bash
mvn test
```

Os testes não exigem WildFly rodando e usam H2 em memória com uma `EntityManagerFactory` própria, instanciada diretamente via `HibernatePersistenceProvider` (ver seção de problemas encontrados).

## CI/CD (local)

```bash
./ci.sh
```

Roda o pipeline completo: testes (`mvn test`), empacotamento do WAR (`mvn package`) e build da imagem Docker (`docker build`).
Parando na primeira falha, como um pipeline de CI faria. Detecta automaticamente se o `JAVA_HOME` atual não é Java 8 e usa o JDK baixado pelo IntelliJ, se existir. Rode antes de dar `git push`.

## Endpoints da API

| Método | Caminho             | Descrição                                                        |
|--------|----------------------|--------------------------------------------------------------------|
| POST   | `/api/urls`          | Cria URL curta. Body `{"url": "...", "alias": "..."}` (alias opcional). Retorna 201. |
| GET    | `/api/urls/{code}`   | Consulta dados da URL curta sem redirecionar.                    |
| GET    | `/r/{code}`          | Redireciona (302) para a URL original.                           |

Erros retornam JSON `{"message": "..."}` (400 URL/alias inválido, 404 código inexistente, 409 alias já em uso). O único endpoint sem corpo JSON de erro é `GET /r/{code}` quando o código não existe, porque é um `HttpServlet` puro
(fora do prefixo `/api`), não um recurso JAX-RS.

## Arquitetura

```
domain      -> entidade JPA ShortUrl
repository  -> acesso a dados via EntityManagerFactory (ShortUrlRepository)
service     -> regra de negócio + motor de geração sincronizado (UrlShorteningService)
rest        -> JAX-RS (ShortUrlResource), Servlet de redirect (RedirectServlet), DTOs, exception mapper
config      -> ativação do JAX-RS (JaxRsActivator)
exception   -> exceções de domínio
```

Camadas separadas por responsabilidade: `rest` não conhece JPA, `service` não conhece HTTP, `repository` não conhece regra de negócio.

## Decisões de design e trade-offs

### Motor de geração sincronizado

O requisito pede que o motor de geração processe uma requisição por vez, de forma sincronizada. `UrlShorteningService.shorten(...)` é `synchronized`, e o bean é `@ApplicationScoped` (uma única instância compartilhada via CDI).
o lock intrínseco do Java serializa toda a sequência *validar → gerar/validar alias → checar unicidade → persistir*, fechando a race de duas requisições concorrentes gerando o mesmo código.

**Trade-off**: em alto volume, isso vira um único ponto de serialização.
Alternativas para produção: fila com um único worker, ou confiar na constraint `UNIQUE` do banco com retry em vez de lock em memória.

### Persistência `RESOURCE_LOCAL` + H2 em memória

Optei por não configurar datasource JNDI no WildFly nesta primeira iteração: `persistence.xml` usa `transaction-type="RESOURCE_LOCAL"`, e o`ShortUrlRepository` abre/fecha um `EntityManager` por operação via
`EntityManagerFactory` injetada (`@PersistenceUnit`).

**Trade-off**: dados não sobrevivem a um restart do WildFly (H2 em memória). Migrar para PostgreSQL com transações gerenciadas pelo container (JTA) exigiria configurar um datasource JNDI e trocar `RESOURCE_LOCAL` por `JTA`
no `persistence.xml`, o resto do código (domain/service/rest) não muda.

## Problemas encontrados durante o desenvolvimento

Registrados aqui porque o processo de diagnosticar e corrigir é parte do que este desafio avalia, não só o resultado final.

1. **`@PersistenceContext` vs `@PersistenceUnit`**: a primeira versão do repositório injetava um `EntityManager` gerenciado pelo container via `@PersistenceContext` e chamava `entityManager.getTransaction().begin()`
 manualmente. Isso funcionava em testes isolados (onde criei o `EntityManager`), mas falhava em runtime no WildFly com `TransactionRequiredException`. Um EntityManager gerenciado pelo container não permite demarcação manual de transação. A correção foi
trocar para `@PersistenceUnit` (injeta a fábrica) e abrir/fechar um `EntityManager` "application-managed" por operação.

2. **Exceções inesperadas engolidas sem log**: o `DomainExceptionMapper` capturava qualquer `RuntimeException` não mapeada e devolvia só `"Unexpected error"`, sem registrar nada no log, o que tornou o problema acima praticamente invisível até adicionar um
   `Logger.log(Level.SEVERE, ..., exception)` antes de responder 500.

3. **URL curta retornada com prefixo errado**: `uriInfo.getBaseUri()` do JAX-RS inclui o prefixo `/api` (definido em `JaxRsActivator`), mas o endpoint de redirect (`GET /r/{code}`) é um `HttpServlet` puro, fora desse prefixo. A URL retornada saía como `.../api/r/{code}` (404 ao
acessar). Corrigido construindo a base a partir do contexto da própria requisição HTTP (`HttpServletRequest`), não do `UriInfo` do JAX-RS.

4. **H2 2.x incompatível com o Hibernate 5.0.x do WildFly 10**: o `H2Dialect` dessa geração do Hibernate gera SQL de `IDENTITY` no formato esperado pelo H2 1.4.x; a série 2.x mudou esse formato e quebrava o
insert com id auto-gerado (`NULL not allowed for column "ID"`). Resolvido fixando a dependência em `h2:1.4.200`.

5. **CDI e testes com campos `private`**: para testar `UrlShorteningService` e `ShortUrlRepository` sem depender de WildFly, inicialmente tornei os campos injetados pacote-privados para permitir injeção manual nos
testes. Troquei para injeção via construtor (`@Inject` no construtor), que é mais idiomático e mantém os campos `private`, mas isso quebrou o deploy: beans `@ApplicationScoped` (escopo normal) exigem que o CDI gere
um *proxy* da classe, e o proxy só pode ser criado se existir um construtor sem argumentos, mesmo que nunca seja chamado de fato. A solução final mantém os dois construtores: um `protected` vazio (só
para o proxy) e o `@Inject` de verdade, usado pela injeção real.

## O que faria diferente com mais tempo

- Migrar para PostgreSQL com datasource JNDI e `transaction-type="JTA"`, usando `@Transactional` em vez de demarcação manual de transação.
- Rate limiting no `POST /api/urls`, já que é o endpoint mais sensível a abuso (geração em massa de códigos) — provavelmente um `Filter` de Servlet com contagem por IP em memória, sabendo que isso só protege por instância (não escalaria horizontalmente sem um contador compartilhado).
- CI remoto (GitHub Actions ou similar) rodando o mesmo pipeline do`ci.sh` a cada push/PR — hoje o pipeline só roda localmente, sob demanda.
- Testes de carga simples no motor sincronizado, para validar o comportamento sob concorrência real (hoje a cobertura de concorrência é só implícita pelo design, não testada com múltiplas threads).
