# 🚀 Deploy na Oracle Cloud Infrastructure (OCI)

Guia rápido para colocar sua aplicação Mikrotik ISP Management rodando na Oracle Cloud.

## 📋 Pré-requisitos

- Instância Oracle Cloud criada (Ubuntu/Oracle Linux)
- Acesso SSH à instância
- Docker e Docker Compose instalados na instância

## 🔧 1. Configurar a Instância Oracle Cloud

### 1.1. Criar Regras de Firewall no OCI Console

No painel da Oracle Cloud, configure o **Security List** da sua subnet:

**Ingress Rules (Entrada):**
```
Source: 0.0.0.0/0
Destination Port: 22 (SSH)
Protocol: TCP

Source: 0.0.0.0/0
Destination Port: 8080 (API)
Protocol: TCP
```

### 1.2. Conectar via SSH

```bash
ssh -i ~/.ssh/sua-chave.pem ubuntu@SEU_IP_PUBLICO
# ou
ssh -i ~/.ssh/sua-chave.pem opc@SEU_IP_PUBLICO
```

### 1.3. Instalar Docker (se ainda não tiver)

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y docker.io docker-compose
sudo systemctl enable docker
sudo systemctl start docker

# Adicionar seu usuário ao grupo docker
sudo usermod -aG docker $USER

# Oracle Linux
sudo yum install -y docker-engine docker-compose
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER

# Relogar para aplicar as permissões
exit
# Conectar novamente via SSH
```

### 1.4. Configurar Firewall do Sistema Operacional

```bash
# Ubuntu (UFW)
sudo ufw allow 22/tcp
sudo ufw allow 8080/tcp
sudo ufw enable

# Oracle Linux (Firewalld)
sudo firewall-cmd --permanent --add-port=22/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

## 📦 2. Fazer Deploy da Aplicação

### 2.1. Transferir Arquivos para o Servidor

**Opção A: Via Git (Recomendado)**
```bash
# No servidor
cd ~
git clone https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git mikrotik
cd mikrotik
```

**Opção B: Via SCP**
```bash
# No seu computador local
cd /home/tiago/workspace-intelij-idea/mikrotik
tar -czf mikrotik.tar.gz --exclude='target' --exclude='node_modules' --exclude='.git' .
scp -i ~/.ssh/sua-chave.pem mikrotik.tar.gz ubuntu@SEU_IP_PUBLICO:~/

# No servidor
ssh -i ~/.ssh/sua-chave.pem ubuntu@SEU_IP_PUBLICO
mkdir -p ~/mikrotik
cd ~/mikrotik
tar -xzf ../mikrotik.tar.gz
```

### 2.2. Configurar Variáveis de Ambiente

```bash
cd ~/mikrotik

# Editar configurações
nano .env.cloud
```

**Configurações importantes:**
```env
# Database (pode deixar como está inicialmente)
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=mikrotik_db
MYSQL_USER=mikrotik
MYSQL_PASSWORD=mikrotik123

# JWT Secret (IMPORTANTE: trocar em produção!)
JWT_SECRET=gere-uma-chave-segura-aqui-123456789

# Mikrotik (configure com seus dados reais)
MIKROTIK_HOST=IP_DO_SEU_MIKROTIK
MIKROTIK_PORT=8728
MIKROTIK_USERNAME=admin
MIKROTIK_PASSWORD=sua-senha-mikrotik
```

### 2.3. Fazer o Deploy

```bash
# Dar permissão de execução ao script
chmod +x deploy.sh

# Deploy completo (build + start)
./deploy.sh deploy
```

**Ou executar passo a passo:**
```bash
# 1. Build da aplicação
./deploy.sh build

# 2. Subir os serviços
./deploy.sh up

# 3. Ver logs em tempo real
./deploy.sh logs
```

## 🧪 3. Testar a Aplicação

### 3.1. Verificar se os containers estão rodando

```bash
docker ps
```

Você deve ver 2 containers:
- `mikrotik-mysql`
- `mikrotik-app`

### 3.2. Acessar a API

```bash
# Health check
curl http://SEU_IP_PUBLICO:8080/actuator/health

# Swagger UI (no navegador)
http://SEU_IP_PUBLICO:8080/swagger-ui.html
```

### 3.3. Criar primeiro usuário admin

```bash
curl -X POST http://SEU_IP_PUBLICO:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com"
  }'
```

## 🔍 4. Monitoramento e Manutenção

### Ver logs
```bash
./deploy.sh logs

# Logs apenas do app
docker logs -f mikrotik-app

# Logs apenas do MySQL
docker logs -f mikrotik-mysql
```

### Ver status dos serviços
```bash
./deploy.sh status
```

### Reiniciar serviços
```bash
./deploy.sh restart
```

### Parar tudo
```bash
./deploy.sh down
```

### Atualizar aplicação
```bash
# Se fez alterações no código
git pull  # ou faça upload dos novos arquivos

# Rebuild e restart
./deploy.sh deploy
```

## 🐛 5. Troubleshooting

### Container não sobe

```bash
# Ver logs de erro
docker logs mikrotik-app
docker logs mikrotik-mysql

# Verificar se as portas estão em uso
sudo netstat -tlnp | grep 8080
sudo netstat -tlnp | grep 3306
```

### Erro de conexão com MySQL

```bash
# Entrar no container do MySQL
docker exec -it mikrotik-mysql mysql -uroot -proot

# Verificar se o banco foi criado
SHOW DATABASES;
USE mikrotik_db;
SHOW TABLES;
```

### Erro de memória (OCI Free Tier = 1GB)

```bash
# Verificar uso de memória
docker stats

# Se necessário, ajustar limites no docker-compose.cloud.yml
# Reduzir memory limits de 512M/1G para 256M/512M
```

### API não responde

```bash
# Verificar se o app iniciou completamente
docker logs mikrotik-app | grep "Started MikrotikApplication"

# Spring Boot pode levar 1-2 minutos para iniciar
# Aguarde e tente novamente
```

## 📊 6. Próximos Passos (Opcional)

- [ ] Configurar domínio DNS apontando para o IP da instância
- [ ] Adicionar HTTPS com Let's Encrypt (Nginx + Certbot)
- [ ] Configurar backup automático do banco de dados
- [ ] Implementar monitoramento (Prometheus + Grafana)
- [ ] Trocar senhas padrão por senhas fortes

## 🔗 Links Úteis

- [Oracle Cloud Free Tier](https://www.oracle.com/cloud/free/)
- [Documentação Docker](https://docs.docker.com/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**🎉 Pronto! Sua aplicação está rodando na Oracle Cloud!**

Acesse: `http://SEU_IP_PUBLICO:8080/swagger-ui.html`

