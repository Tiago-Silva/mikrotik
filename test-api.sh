#!/bin/bash

# Mikrotik PPPoE Management API - Script de Teste
# Este script contém exemplos de requisições cURL para testar a API

API_BASE_URL="http://localhost:8080/api"
TOKEN=""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Mikrotik PPPoE Management API - Testes${NC}"
echo -e "${YELLOW}========================================${NC}"

# 1. LOGIN
echo -e "\n${YELLOW}[1] Testando LOGIN...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "Resposta: $LOGIN_RESPONSE"
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Token obtido: ${TOKEN:0:20}...${NC}"

# 2. CRIAR SERVIDOR MIKROTIK
echo -e "\n${YELLOW}[2] Criando Servidor Mikrotik...${NC}"
SERVER_RESPONSE=$(curl -s -X POST "$API_BASE_URL/mikrotik-servers" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Servidor Principal",
    "ipAddress": "192.168.1.1",
    "port": 22,
    "username": "admin",
    "password": "password123",
    "description": "Servidor Mikrotik principal de teste"
  }')

echo "Resposta: $SERVER_RESPONSE"
SERVER_ID=$(echo $SERVER_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}Servidor criado com ID: $SERVER_ID${NC}"

# 3. LISTAR SERVIDORES
echo -e "\n${YELLOW}[3] Listando Servidores...${NC}"
curl -s -X GET "$API_BASE_URL/mikrotik-servers" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | head -c 200
echo -e "\n${GREEN}OK${NC}"

# 4. CRIAR PERFIL PPPOE
echo -e "\n${YELLOW}[4] Criando Perfil PPPoE...${NC}"
PROFILE_RESPONSE=$(curl -s -X POST "$API_BASE_URL/profiles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Plano 10Mb\",
    \"description\": \"10 Mbps download / 5 Mbps upload\",
    \"maxBitrateDl\": 10000000,
    \"maxBitrateUl\": 5000000,
    \"sessionTimeout\": 3600,
    \"mikrotikServerId\": $SERVER_ID
  }")

echo "Resposta: $PROFILE_RESPONSE"
PROFILE_ID=$(echo $PROFILE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}Perfil criado com ID: $PROFILE_ID${NC}"

# 5. LISTAR PERFIS
echo -e "\n${YELLOW}[5] Listando Perfis...${NC}"
curl -s -X GET "$API_BASE_URL/profiles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | head -c 200
echo -e "\n${GREEN}OK${NC}"

# 6. CRIAR USUÁRIO PPPOE
echo -e "\n${YELLOW}[6] Criando Usuário PPPoE...${NC}"
USER_RESPONSE=$(curl -s -X POST "$API_BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"cliente1\",
    \"password\": \"senha123\",
    \"email\": \"cliente1@example.com\",
    \"comment\": \"Cliente de teste\",
    \"profileId\": $PROFILE_ID,
    \"mikrotikServerId\": $SERVER_ID
  }")

echo "Resposta: $USER_RESPONSE"
USER_ID=$(echo $USER_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}Usuário criado com ID: $USER_ID${NC}"

# 7. LISTAR USUÁRIOS
echo -e "\n${YELLOW}[7] Listando Usuários...${NC}"
curl -s -X GET "$API_BASE_URL/users?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | head -c 200
echo -e "\n${GREEN}OK${NC}"

# 8. LISTAR CONEXÕES
echo -e "\n${YELLOW}[8] Listando Conexões...${NC}"
curl -s -X GET "$API_BASE_URL/connections" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | head -c 200
echo -e "\n${GREEN}OK${NC}"

# 9. DESATIVAR USUÁRIO
echo -e "\n${YELLOW}[9] Desativando Usuário...${NC}"
curl -s -X POST "$API_BASE_URL/users/$USER_ID/disable" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
echo -e "\n${GREEN}Usuário desativado${NC}"

# 10. REATIVAR USUÁRIO
echo -e "\n${YELLOW}[10] Reativando Usuário...${NC}"
curl -s -X POST "$API_BASE_URL/users/$USER_ID/enable" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
echo -e "\n${GREEN}Usuário reativado${NC}"

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Testes Concluídos com Sucesso!${NC}"
echo -e "${GREEN}========================================${NC}"
