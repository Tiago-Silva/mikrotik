# 🚀 Arquivos de Deploy

Este diretório contém os arquivos necessários para deploy da aplicação.

## 📁 Arquivos

### Para Desenvolvimento Local

- **`docker-compose.yml`** - Compose para desenvolvimento local (MySQL + PHPMyAdmin)
- **`.env`** - Variáveis de ambiente locais (não commitado)
- **`.env.example`** - Template de variáveis de ambiente

### Para Oracle Cloud (Produção)

- **`docker-compose.cloud.yml`** - Compose otimizado para cloud (MySQL + App)
- **`Dockerfile`** - Build multi-stage da aplicação Spring Boot
- **`.env.cloud`** - Variáveis de ambiente para cloud (não commitado)
- **`.env.cloud.example`** - Template de variáveis para cloud
- **`deploy.sh`** - Script automatizado de deploy
- **`DEPLOY_OCI.md`** - Guia completo de deploy na Oracle Cloud

## 🎯 Quick Start - Deploy na Oracle Cloud

### 1. No seu servidor Oracle Cloud:

```bash
# Clonar repositório
git clone SEU_REPOSITORIO.git
cd mikrotik

# Copiar e configurar variáveis
cp .env.cloud.example .env.cloud
nano .env.cloud  # Ajustar valores (Mikrotik, senhas, etc)

# Deploy
chmod +x deploy.sh
./deploy.sh deploy
```

### 2. Acessar aplicação:

```
http://SEU_IP_PUBLICO:8080/swagger-ui.html
```

## 📚 Documentação Completa

Veja [DEPLOY_OCI.md](./DEPLOY_OCI.md) para:
- Configuração de firewall OCI
- Instalação do Docker
- Troubleshooting
- Monitoramento

## ⚙️ Comandos do Script de Deploy

```bash
./deploy.sh build    # Build das imagens Docker
./deploy.sh up       # Subir serviços
./deploy.sh down     # Parar serviços
./deploy.sh logs     # Ver logs em tempo real
./deploy.sh restart  # Reiniciar serviços
./deploy.sh status   # Status dos containers
./deploy.sh deploy   # Deploy completo (build + up)
```

## 🔒 Segurança

**⚠️ Arquivos .env.cloud e .env NÃO devem ser commitados!**

Eles contém credenciais sensíveis e estão no `.gitignore`.

---

**Próximos passos após primeiro deploy:**
1. Trocar senhas padrão no `.env.cloud`
2. Configurar domínio DNS
3. Adicionar HTTPS (Nginx + Let's Encrypt)
4. Configurar backup do banco de dados

