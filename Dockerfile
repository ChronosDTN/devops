# ==========================================================================================
# DOCKERFILE MULTI-STAGE PARA A API JAVA SPRING BOOT (CHRONOS DTN)
# CONSTRUIDO SEGUINDO DIRETRIZES DE SEGURANÇA OWASP E PRINCÍPIO DO MENOR PRIVILÉGIO
# ==========================================================================================

# ------------------------------------------------------------------------------------------
# ESTÁGIO 1: COMPILAÇÃO DA APLICAÇÃO (BUILD STAGE)
# Utiliza uma imagem completa do JDK Maven para baixar dependências e gerar o arquivo JAR.
# ------------------------------------------------------------------------------------------
# Define a imagem base oficial do Maven com Eclipse Temurin JDK 21 rodando sobre Alpine Linux.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Define o diretório de trabalho interno onde o código fonte do projeto será copiado e compilado.
WORKDIR /usr/src/app

# Copia o arquivo de configuração de dependências Maven (pom.xml) para o diretório de trabalho atual.
COPY pom.xml .

# Executa o download de todas as dependências declaradas no pom.xml em modo offline para cache do Docker.
RUN mvn dependency:go-offline -B

# Copia a pasta contendo todo o código-fonte da aplicação Java Spring Boot (diretório src).
COPY src ./src

# Compila o projeto ignorando testes unitários para acelerar o empacotamento no pipeline CI/CD.
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------------------
# ESTÁGIO 2: EXECUÇÃO EM PRODUÇÃO (RUNTIME STAGE)
# Utiliza uma imagem JRE extremamente leve e segura (Alpine) para rodar o artefato gerado.
# ------------------------------------------------------------------------------------------
# Define a imagem base oficial do JRE 21 leve (Alpine) para reduzir a superfície de ataque e o tamanho do container.
FROM eclipse-temurin:21-jre-alpine

# Define variáveis de ambiente necessárias para a execução otimizada da Java Virtual Machine (JVM).
# Otimiza o Garbage Collector e limita o uso da memória heap para evitar estouros de RAM no container Docker.
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=75.0"

# Cria um grupo de sistema específico sem privilégios administrativos chamado 'dtn_group'.
RUN addgroup -S dtn_group

# Cria um usuário de sistema sem privilégios de root associado ao grupo criado anteriormente.
RUN adduser -S dtn_user -G dtn_group

# Define o diretório de trabalho padrão seguro onde a aplicação executável residirá.
WORKDIR /app

# Copia o arquivo JAR compilado no Estágio 1 (builder) para o diretório atual do estágio de produção.
COPY --from=builder /usr/src/app/target/*.jar app.jar

# Altera a propriedade do arquivo jar copiado para o usuário e grupo não-privilegiados.
RUN chown dtn_user:dtn_group app.jar

# Informa ao Docker que o container escuta por conexões de rede na porta HTTP padrão 8080.
EXPOSE 8080

# Altera o contexto de execução atual do container do usuário root padrão para o usuário de baixo privilégio.
USER dtn_user

# Define o ponto de entrada principal do container chamando a JVM com as opções de otimização configuradas.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
