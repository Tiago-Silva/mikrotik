package br.com.mikrotik.service;

import br.com.mikrotik.dto.AutomationLogDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.model.AutomationLog;
import br.com.mikrotik.repository.AutomationLogRepository;
import br.com.mikrotik.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationLogService {

    private final AutomationLogRepository automationLogRepository;

    /**
     * Criar novo log de automação
     */
    @Transactional
    public AutomationLogDTO create(AutomationLogDTO dto) {
        log.info("Registrando log de automação: {} - {}", dto.getActionType(), dto.getReason());

        Long companyId = CompanyContextHolder.getCompanyId();
        dto.setCompanyId(companyId);

        if (dto.getExecutedAt() == null) {
            dto.setExecutedAt(LocalDateTime.now());
        }

        AutomationLog automationLog = dto.toEntity();
        automationLog = automationLogRepository.save(automationLog);

        log.info("Log de automação registrado: ID={}", automationLog.getId());
        return AutomationLogDTO.fromEntity(automationLog);
    }

    /**
     * Registrar log de bloqueio
     */
    @Transactional
    public AutomationLogDTO logBlock(Long contractId, String reason, boolean success, String outputMessage) {
        return createLog(contractId, AutomationLog.ActionType.BLOCK, reason, success, outputMessage);
    }

    /**
     * Registrar log de desbloqueio
     */
    @Transactional
    public AutomationLogDTO logUnblock(Long contractId, String reason, boolean success, String outputMessage) {
        return createLog(contractId, AutomationLog.ActionType.UNBLOCK, reason, success, outputMessage);
    }

    /**
     * Registrar log de redução de velocidade
     */
    @Transactional
    public AutomationLogDTO logReduceSpeed(Long contractId, String reason, boolean success, String outputMessage) {
        return createLog(contractId, AutomationLog.ActionType.REDUCE_SPEED, reason, success, outputMessage);
    }

    /**
     * Registrar log de envio de aviso
     */
    @Transactional
    public AutomationLogDTO logSendWarning(Long contractId, String reason, boolean success, String outputMessage) {
        return createLog(contractId, AutomationLog.ActionType.SEND_WARNING, reason, success, outputMessage);
    }

    /**
     * Registrar log de envio de email
     */
    @Transactional
    public AutomationLogDTO logSendEmail(Long contractId, String reason, boolean success, String outputMessage) {
        return createLog(contractId, AutomationLog.ActionType.SEND_EMAIL, reason, success, outputMessage);
    }

    /**
     * Registrar log de envio de SMS
     */
    @Transactional
    public AutomationLogDTO logSendSms(Long contractId, String reason, boolean success, String outputMessage) {
        return createLog(contractId, AutomationLog.ActionType.SEND_SMS, reason, success, outputMessage);
    }

    /**
     * Método auxiliar para criar log
     */
    private AutomationLogDTO createLog(Long contractId, AutomationLog.ActionType actionType,
                                       String reason, boolean success, String outputMessage) {
        AutomationLogDTO dto = AutomationLogDTO.builder()
                .contractId(contractId)
                .actionType(actionType)
                .reason(reason)
                .success(success)
                .outputMessage(outputMessage)
                .executedAt(LocalDateTime.now())
                .build();

        return create(dto);
    }

    /**
     * Buscar log por ID
     */
    @Transactional(readOnly = true)
    public AutomationLogDTO findById(Long id) {
        log.info("Buscando log de automação por ID: {}", id);

        AutomationLog automationLog = automationLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log de automação não encontrado com ID: " + id));

        return AutomationLogDTO.fromEntity(automationLog);
    }

    /**
     * Listar logs por empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<AutomationLogDTO> findAll(Pageable pageable) {
        log.info("Listando logs de automação");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<AutomationLog> logs = automationLogRepository.findByCompanyId(companyId, pageable);
        return logs.map(AutomationLogDTO::fromEntity);
    }

    /**
     * Listar logs por contrato (paginado)
     */
    @Transactional(readOnly = true)
    public Page<AutomationLogDTO> findByContract(Long contractId, Pageable pageable) {
        log.info("Listando logs de automação do contrato: {}", contractId);

        Page<AutomationLog> logs = automationLogRepository.findByContractId(contractId, pageable);
        return logs.map(AutomationLogDTO::fromEntity);
    }

    /**
     * Listar logs por tipo de ação (paginado)
     */
    @Transactional(readOnly = true)
    public Page<AutomationLogDTO> findByActionType(AutomationLog.ActionType actionType, Pageable pageable) {
        log.info("Listando logs de automação por tipo: {}", actionType);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<AutomationLog> logs = automationLogRepository.findByCompanyIdAndActionType(companyId, actionType, pageable);
        return logs.map(AutomationLogDTO::fromEntity);
    }

    /**
     * Listar logs recentes (paginado)
     */
    @Transactional(readOnly = true)
    public Page<AutomationLogDTO> findRecent(Pageable pageable) {
        log.info("Listando logs recentes");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<AutomationLog> logs = automationLogRepository.findByCompanyId(companyId, pageable);
        return logs.map(AutomationLogDTO::fromEntity);
    }

    /**
     * Buscar com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<AutomationLogDTO> findByFilters(Long contractId, AutomationLog.ActionType actionType,
                                                Boolean success, Pageable pageable) {
        log.info("Buscando logs com filtros");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<AutomationLog> logs = automationLogRepository.findByFilters(companyId, contractId, actionType, success, pageable);
        return logs.map(AutomationLogDTO::fromEntity);
    }

    /**
     * Deletar log
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando log de automação: ID={}", id);

        AutomationLog automationLog = automationLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log de automação não encontrado com ID: " + id));

        automationLogRepository.delete(automationLog);
        log.info("Log de automação deletado com sucesso: ID={}", id);
    }

    /**
     * Contar logs por empresa
     */
    @Transactional(readOnly = true)
    public long countByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return automationLogRepository.countByCompanyId(companyId);
    }

    /**
     * Contar logs por tipo de ação
     */
    @Transactional(readOnly = true)
    public long countByActionType(AutomationLog.ActionType actionType) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return automationLogRepository.countByCompanyIdAndActionType(companyId, actionType);
    }

    /**
     * Contar logs com sucesso
     */
    @Transactional(readOnly = true)
    public long countSuccessful() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return automationLogRepository.countByCompanyIdAndSuccess(companyId, true);
    }

    /**
     * Contar logs com falha
     */
    @Transactional(readOnly = true)
    public long countFailed() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return automationLogRepository.countByCompanyIdAndSuccess(companyId, false);
    }
}
