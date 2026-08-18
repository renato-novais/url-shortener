#!/usr/bin/env bash
# Pipeline de CI/CD local: roda testes, empacota o WAR e builda a imagem Docker.

# Falha rápido (set -e) em qualquer etapa, como um pipeline de CI faria.
set -e

# O build precisa de Java 8 (WildFly 10 e as libs de teste não suportam Java 9+).
# Se o JAVA_HOME atual não for Java 8, tenta usar um JDK 8 instalado via IntelliJ.
if ! java -version 2>&1 | grep -q "1\.8\."; then
    ZULU8="$HOME/Library/Java/JavaVirtualMachines/azul-1.8.0_502/Contents/Home"
    if [ -d "$ZULU8" ]; then
        export JAVA_HOME="$ZULU8"
        echo "==> JAVA_HOME não era Java 8; usando $JAVA_HOME"
    else
        echo "ERRO: é preciso Java 8 no PATH ou em JAVA_HOME para rodar este pipeline." >&2
        exit 1
    fi
fi

echo "==> [1/3] Rodando testes"
mvn test

echo "==> [2/3] Empacotando WAR"
mvn package -DskipTests

echo "==> [3/3] Buildando imagem Docker"
docker build -t url-shortener .

echo "==> Pipeline concluído com sucesso"
