#!/bin/bash
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== Mikrotik ISP - Deploy Oracle Cloud ===${NC}\n"

case "${1:-help}" in
    build)
        echo -e "${YELLOW}🔨 Building...${NC}"
        docker-compose build --no-cache
        ;;
    up)
        echo -e "${YELLOW}🚀 Starting...${NC}"
        docker-compose up -d
        docker-compose ps
        ;;
    down)
        docker-compose down
        ;;
    logs)
        docker-compose logs -f --tail=100
        ;;
    restart)
        docker-compose restart
        ;;
    deploy)
        docker-compose build --no-cache
        docker-compose down
        docker-compose up -d
        echo -e "\n${GREEN}✅ Deploy OK!${NC}"
        ;;
    *)
        echo "Usage: $0 {build|up|down|logs|restart|deploy}"
        exit 1
        ;;
esac
