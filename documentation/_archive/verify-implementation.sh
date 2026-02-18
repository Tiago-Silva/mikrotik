#!/bin/bash

# Mikrotik PPPoE Management API - Checklist de Verificação
# Este script verifica se todos os arquivos foram criados corretamente

echo "=========================================="
echo "Verificando Implementação da API"
echo "=========================================="
echo ""

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Contador
total=0
ok=0

# Função para verificar arquivo
check_file() {
    local file=$1
    local description=$2
    total=$((total + 1))

    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ok=$((ok + 1))
    else
        echo -e "${RED}✗${NC} $description (Arquivo não encontrado: $file)"
    fi
}

# Função para verificar diretório
check_dir() {
    local dir=$1
    local description=$2
    total=$((total + 1))

    if [ -d "$dir" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ok=$((ok + 1))
    else
        echo -e "${RED}✗${NC} $description (Diretório não encontrado: $dir)"
    fi
}

echo -e "${YELLOW}ARQUIVOS DE CONFIGURAÇÃO${NC}"
check_file "pom.xml" "pom.xml (Maven)"
check_file "docker-compose.yml" "docker-compose.yml (Docker)"
check_file "schema.sql" "schema.sql (Banco de Dados)"
check_file "application.properties" "application.properties (Configuração)"
check_file ".env.example" ".env.example (Variáveis de Ambiente)"
check_file ".gitignore" ".gitignore (Git)"

echo ""
echo -e "${YELLOW}DOCUMENTAÇÃO${NC}"
check_file "API_README.md" "API_README.md"
check_file "QUICK_START.md" "QUICK_START.md"
check_file "DEVELOPMENT.md" "DEVELOPMENT.md"
check_file "ARCHITECTURE.md" "ARCHITECTURE.md"
check_file "SUMMARY.md" "SUMMARY.md"
check_file "INDEX.md" "INDEX.md"
check_file "IMPLEMENTATION_COMPLETE.md" "IMPLEMENTATION_COMPLETE.md"

echo ""
echo -e "${YELLOW}SCRIPTS DE TESTE${NC}"
check_file "test-api.sh" "test-api.sh (Script cURL)"
check_file "requests.http" "requests.http (REST Client)"

echo ""
echo -e "${YELLOW}DIRETÓRIOS JAVA${NC}"
check_dir "src/main/java/br/com/mikrotik" "Diretório principal"
check_dir "src/main/java/br/com/mikrotik/controller" "Controllers"
check_dir "src/main/java/br/com/mikrotik/service" "Services"
check_dir "src/main/java/br/com/mikrotik/repository" "Repositories"
check_dir "src/main/java/br/com/mikrotik/model" "Models"
check_dir "src/main/java/br/com/mikrotik/dto" "DTOs"
check_dir "src/main/java/br/com/mikrotik/exception" "Exceptions"
check_dir "src/main/java/br/com/mikrotik/security" "Security"
check_dir "src/main/java/br/com/mikrotik/config" "Config"
check_dir "src/main/java/br/com/mikrotik/constant" "Constants"

echo ""
echo -e "${YELLOW}CLASSES - MODELS${NC}"
check_file "src/main/java/br/com/mikrotik/model/ApiUser.java" "ApiUser.java"
check_file "src/main/java/br/com/mikrotik/model/MikrotikServer.java" "MikrotikServer.java"
check_file "src/main/java/br/com/mikrotik/model/PppoeProfile.java" "PppoeProfile.java"
check_file "src/main/java/br/com/mikrotik/model/PppoeUser.java" "PppoeUser.java"
check_file "src/main/java/br/com/mikrotik/model/PppoeConnection.java" "PppoeConnection.java"
check_file "src/main/java/br/com/mikrotik/model/AuditLog.java" "AuditLog.java"

echo ""
echo -e "${YELLOW}CLASSES - DTOs${NC}"
check_file "src/main/java/br/com/mikrotik/dto/LoginDTO.java" "LoginDTO.java"
check_file "src/main/java/br/com/mikrotik/dto/LoginResponseDTO.java" "LoginResponseDTO.java"
check_file "src/main/java/br/com/mikrotik/dto/MikrotikServerDTO.java" "MikrotikServerDTO.java"
check_file "src/main/java/br/com/mikrotik/dto/PppoeProfileDTO.java" "PppoeProfileDTO.java"
check_file "src/main/java/br/com/mikrotik/dto/PppoeUserDTO.java" "PppoeUserDTO.java"
check_file "src/main/java/br/com/mikrotik/dto/PppoeConnectionDTO.java" "PppoeConnectionDTO.java"

echo ""
echo -e "${YELLOW}CLASSES - REPOSITORIES${NC}"
check_file "src/main/java/br/com/mikrotik/repository/ApiUserRepository.java" "ApiUserRepository.java"
check_file "src/main/java/br/com/mikrotik/repository/MikrotikServerRepository.java" "MikrotikServerRepository.java"
check_file "src/main/java/br/com/mikrotik/repository/PppoeProfileRepository.java" "PppoeProfileRepository.java"
check_file "src/main/java/br/com/mikrotik/repository/PppoeUserRepository.java" "PppoeUserRepository.java"
check_file "src/main/java/br/com/mikrotik/repository/PppoeConnectionRepository.java" "PppoeConnectionRepository.java"
check_file "src/main/java/br/com/mikrotik/repository/AuditLogRepository.java" "AuditLogRepository.java"

echo ""
echo -e "${YELLOW}CLASSES - SERVICES${NC}"
check_file "src/main/java/br/com/mikrotik/service/MikrotikServerService.java" "MikrotikServerService.java"
check_file "src/main/java/br/com/mikrotik/service/PppoeProfileService.java" "PppoeProfileService.java"
check_file "src/main/java/br/com/mikrotik/service/PppoeUserService.java" "PppoeUserService.java"
check_file "src/main/java/br/com/mikrotik/service/PppoeConnectionService.java" "PppoeConnectionService.java"
check_file "src/main/java/br/com/mikrotik/service/MikrotikSshService.java" "MikrotikSshService.java"
check_file "src/main/java/br/com/mikrotik/service/CustomUserDetailsService.java" "CustomUserDetailsService.java"

echo ""
echo -e "${YELLOW}CLASSES - CONTROLLERS${NC}"
check_file "src/main/java/br/com/mikrotik/controller/AuthController.java" "AuthController.java"
check_file "src/main/java/br/com/mikrotik/controller/MikrotikServerController.java" "MikrotikServerController.java"
check_file "src/main/java/br/com/mikrotik/controller/PppoeProfileController.java" "PppoeProfileController.java"
check_file "src/main/java/br/com/mikrotik/controller/PppoeUserController.java" "PppoeUserController.java"
check_file "src/main/java/br/com/mikrotik/controller/PppoeConnectionController.java" "PppoeConnectionController.java"

echo ""
echo -e "${YELLOW}CLASSES - SECURITY${NC}"
check_file "src/main/java/br/com/mikrotik/security/JwtTokenProvider.java" "JwtTokenProvider.java"
check_file "src/main/java/br/com/mikrotik/security/JwtAuthenticationFilter.java" "JwtAuthenticationFilter.java"

echo ""
echo -e "${YELLOW}CLASSES - CONFIGURAÇÃO${NC}"
check_file "src/main/java/br/com/mikrotik/config/SecurityConfig.java" "SecurityConfig.java"
check_file "src/main/java/br/com/mikrotik/config/OpenApiConfig.java" "OpenApiConfig.java"
check_file "src/main/java/br/com/mikrotik/config/DataInitializationConfig.java" "DataInitializationConfig.java"

echo ""
echo -e "${YELLOW}CLASSES - EXCEÇÕES${NC}"
check_file "src/main/java/br/com/mikrotik/exception/ResourceNotFoundException.java" "ResourceNotFoundException.java"
check_file "src/main/java/br/com/mikrotik/exception/MikrotikConnectionException.java" "MikrotikConnectionException.java"
check_file "src/main/java/br/com/mikrotik/exception/ApiError.java" "ApiError.java"
check_file "src/main/java/br/com/mikrotik/exception/GlobalExceptionHandler.java" "GlobalExceptionHandler.java"

echo ""
echo -e "${YELLOW}CLASSES - CONSTANTES${NC}"
check_file "src/main/java/br/com/mikrotik/constant/ApiConstants.java" "ApiConstants.java"

echo ""
echo -e "${YELLOW}CLASSE PRINCIPAL${NC}"
check_file "src/main/java/br/com/mikrotik/MikrotikApplication.java" "MikrotikApplication.java"

echo ""
echo "=========================================="
echo -e "Resultado: ${GREEN}$ok / $total${NC} verificações OK"
echo "=========================================="

if [ $ok -eq $total ]; then
    echo ""
    echo -e "${GREEN}✓ TODOS OS ARQUIVOS FORAM CRIADOS COM SUCESSO!${NC}"
    echo ""
    echo -e "${YELLOW}Próximos passos:${NC}"
    echo "1. docker-compose up -d"
    echo "2. mvn clean install"
    echo "3. mvn spring-boot:run"
    echo "4. Abrir http://localhost:8080/swagger-ui.html"
    echo ""
    exit 0
else
    echo ""
    echo -e "${RED}✗ ALGUNS ARQUIVOS ESTÃO FALTANDO!${NC}"
    echo ""
    exit 1
fi
