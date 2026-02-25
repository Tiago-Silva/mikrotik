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
 * Service de monitoramento ao vivo de contratos via Mikrotik.
 *
 * ARQUITETURA:
 * - Busca dados no banco (transação readOnly) para obter o contrato, username PPPoE e credenciais do servidor.
 * - A chamada ao Mikrotik ocorre APÓS o encerramento da transação (evita Connection Pool Exhaustion).
 * - Nunca carrega a lista inteira de sessões: filtra por username diretamente no Adapter.
 * - Use sob demanda (pesquisa por contrato) — nunca em loop ou polling massivo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractMonitoringService {

    private final ContractRepository contractRepository;
    private final CustomerRepository customerRepository;
    private final PppoeUserRepository pppoeUserRepository;
    private final MikrotikApiService mikrotikApiService;

    /**
     * Consulta a sessão PPPoE ativa de um contrato diretamente no Mikrotik.
     *
     * Fluxo: ContractId → Contract → Customer → PppoeUser → MikrotikServer → /ppp/active/print
     */
    @Transactional(readOnly = true)
    public LiveConnectionDTO getLiveConnectionByContractId(Long contractId) {
        Long companyId = CompanyContextHolder.getCompanyId();

        // Busca o contrato com segurança multi-tenant
        Contract contract = contractRepository.findByIdAndCompanyId(contractId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado: " + contractId));

        // Busca o cliente vinculado ao contrato
        Customer customer = customerRepository.findByIdAndCompanyId(contract.getCustomerId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado para o contrato: " + contractId));

        // Verifica se o contrato possui usuário PPPoE vinculado
        if (contract.getPppoeUserId() == null) {
            return LiveConnectionDTO.builder()
                    .contractId(contractId)
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .online(false)
                    .message("Contrato não possui credencial PPPoE vinculada")
                    .build();
        }

        PppoeUser pppoeUser = pppoeUserRepository.findById(contract.getPppoeUserId()).orElse(null);

        if (pppoeUser == null) {
            return LiveConnectionDTO.builder()
                    .contractId(contractId)
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .online(false)
                    .message("Credencial PPPoE não encontrada no banco")
                    .build();
        }

        // Captura os dados necessários DENTRO da transação antes de ela fechar
        MikrotikServer server = pppoeUser.getMikrotikServer();
        String pppoeUsername = pppoeUser.getUsername();
        String pppoePassword = pppoeUser.getPassword();
        String pppoeProfile = pppoeUser.getProfile() != null ? pppoeUser.getProfile().getName() : null;
        String serverIp = server.getIpAddress();
        Integer serverPort = server.getApiPort();
        String serverUser = server.getUsername();
        String serverPass = server.getPassword();

        // ⚠️ Chamada ao Mikrotik FORA do escopo transacional (readOnly já encerrou acima).
        // Segue a Regra de Ouro: nenhuma chamada de rede dentro de @Transactional aberta.
        Map<String, String> session = mikrotikApiService.getActivePppoeSessionByUsername(
                serverIp, serverPort, serverUser, serverPass, pppoeUsername
        );

        if (session == null || session.isEmpty()) {
            log.debug("Sem sessão ativa no Mikrotik para contrato={} username={}", contractId, pppoeUsername);
            return LiveConnectionDTO.builder()
                    .contractId(contractId)
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .pppoeUsername(pppoeUsername)
                    .pppoePassword(pppoePassword)
                    .pppoeProfile(pppoeProfile)
                    .online(false)
                    .message("Cliente não está conectado no momento")
                    .build();
        }

        log.info("Sessão ativa encontrada — contrato={} username={} ip={}",
                contractId, pppoeUsername, session.get("address"));

        return LiveConnectionDTO.builder()
                .contractId(contractId)
                .customerId(customer.getId())
                .customerName(customer.getName())
                .pppoeUsername(pppoeUsername)
                .pppoePassword(pppoePassword)
                .pppoeProfile(pppoeProfile)
                .remoteAddress(session.get("address"))
                .localAddress(session.get("local-address"))
                .callingStationId(session.get("calling-station-id"))
                .uptime(session.get("uptime"))
                .service(session.get("service"))
                .online(true)
                .build();
    }
}
