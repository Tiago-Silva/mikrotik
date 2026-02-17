package br.com.mikrotik.shared.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Configuração de processamento assíncrono para integrações externas.
 *
 * JUSTIFICATIVA:
 * Integrações com hardware (Mikrotik) não podem ser executadas dentro de @Transactional.
 * Se o Mikrotik demorar 30s para responder, a conexão do banco fica travada.
 *
 * ESTRATÉGIA:
 * - Persistir intenção no banco (ex: Contract.status = SUSPENDED_FINANCIAL)
 * - Fechar transação
 * - Processar integração de forma assíncrona com retry
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Thread pool dedicado para integrações de rede (Mikrotik).
     *
     * Configuração conservadora para ISP com 1 desenvolvedor:
     * - Core: 2 threads (suporta 2 operações simultâneas)
     * - Max: 5 threads (picos de ativação/suspensão em lote)
     * - Queue: 100 (buffer para jobs pendentes)
     */
    @Bean(name = "networkIntegrationExecutor")
    public Executor networkIntegrationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("network-integration-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            log.error("❌ CRÍTICO: Fila de integrações de rede CHEIA. Job rejeitado: {}", r);
            // TODO: Em produção, enviar alerta para monitoramento (Slack/Email/SMS)
        });
        executor.initialize();

        log.info("✅ NetworkIntegrationExecutor configurado: core=2, max=5, queue=100");
        return executor;
    }

    /**
     * Executor padrão para @Async sem nome específico
     */
    @Override
    public Executor getAsyncExecutor() {
        return networkIntegrationExecutor();
    }

    /**
     * Handler para capturar exceções não tratadas em métodos @Async
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                log.error("==========================================================");
                log.error("❌ ERRO ASSÍNCRONO NÃO TRATADO");
                log.error("Método: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName());
                log.error("Parâmetros: {}", Arrays.toString(params));
                log.error("Exceção: {}", ex.getMessage());
                log.error("Stack trace:", ex);
                log.error("==========================================================");
                // TODO: Em produção, enviar alerta crítico (Slack/PagerDuty/SMS)
            }
        };
    }
}

