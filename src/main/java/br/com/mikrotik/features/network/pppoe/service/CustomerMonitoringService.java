package br.com.mikrotik.features.network.pppoe.service;

import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.contracts.repository.ContractRepository;
import br.com.mikrotik.features.customers.model.Customer;
import br.com.mikrotik.features.customers.repository.CustomerRepository;
import br.com.mikrotik.features.network.pppoe.dto.LiveConnectionDTO;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import br.com.mikrotik.features.network.server.adapter.MikrotikApiService;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service de monitoramento ao vivo de clientes via Mikrotik.
 *
 * ARQUITETURA:
 * - Busca dados no banco (transação readOnly) para obter o username PPPoE e credenciais do servidor.
 * - A chamada ao Mikrotik ocorre APÓS o encerramento da transação (readOnly não segura connection pool).
 * - Nunca carrega a lista inteira de sessões: filtra por username diretamente no Adapter.
 * - Use sob demanda (pesquisa por cliente) — nunca em loop ou polling massivo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerMonitoringService {

    private final CustomerRepository customerRepository;
    private final ContractRepository contractRepository;
    private final PppoeUserRepository pppoeUserRepository;
    private final MikrotikApiService mikrotikApiService;

    /**
     * Consulta a sessão PPPoE ativa de um cliente diretamente no Mikrotik.
     *
     * Fluxo: Customer → Contract(ACTIVE) → PppoeUser → MikrotikServer → /ppp/active/print
     */
    @Transactional(readOnly = true)
    public LiveConnectionDTO getLiveConnectionByCustomerId(Long customerId) {
        Long companyId = CompanyContextHolder.getCompanyId();

        Customer customer = customerRepository.findByIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + customerId));

        // Busca contrato ACTIVE do cliente dentro da empresa (multi-tenant seguro)
        Contract contract = contractRepository
                .findByCompanyIdAndStatus(companyId, Contract.ContractStatus.ACTIVE)
                .stream()
                .filter(c -> c.getCustomerId().equals(customerId) && c.getPppoeUserId() != null)
                .findFirst()
                .orElse(null);

        if (contract == null) {
            return LiveConnectionDTO.builder()
                    .customerId(customerId)
                    .customerName(customer.getName())
                    .online(false)
                    .message("Cliente não possui contrato ativo ou credencial PPPoE vinculada")
                    .build();
        }

        PppoeUser pppoeUser = pppoeUserRepository.findById(contract.getPppoeUserId()).orElse(null);

        if (pppoeUser == null) {
            return LiveConnectionDTO.builder()
                    .customerId(customerId)
                    .customerName(customer.getName())
                    .online(false)
                    .message("Credencial PPPoE não encontrada no banco")
                    .build();
        }

        MikrotikServer server = pppoeUser.getMikrotikServer();

        // ⚠️ Chamada ao Mikrotik FORA do escopo transacional (readOnly já encerrou acima).
        // Segue a Regra de Ouro: nenhuma chamada de rede dentro de @Transactional aberta.
        Map<String, String> session = mikrotikApiService.getActivePppoeSessionByUsername(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                pppoeUser.getUsername()
        );

        if (session == null || session.isEmpty()) {
            log.debug("Sem sessão ativa no Mikrotik para cliente={} username={}", customerId, pppoeUser.getUsername());
            return LiveConnectionDTO.builder()
                    .customerId(customerId)
                    .customerName(customer.getName())
                    .pppoeUsername(pppoeUser.getUsername())
                    .online(false)
                    .message("Cliente não está conectado no momento")
                    .build();
        }

        log.info("Sessão ativa encontrada — cliente={} username={} ip={}",
                customerId, pppoeUser.getUsername(), session.get("address"));

        return LiveConnectionDTO.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .pppoeUsername(pppoeUser.getUsername())
                .remoteAddress(session.get("address"))
                .localAddress(session.get("local-address"))
                .callingStationId(session.get("calling-station-id"))
                .uptime(session.get("uptime"))
                .service(session.get("service"))
                .online(true)
                .build();
    }
}

