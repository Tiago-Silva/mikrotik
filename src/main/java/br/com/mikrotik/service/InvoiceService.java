package br.com.mikrotik.service;

import br.com.mikrotik.dto.InvoiceDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.Invoice;
import br.com.mikrotik.repository.ContractRepository;
import br.com.mikrotik.repository.CustomerRepository;
import br.com.mikrotik.repository.InvoiceRepository;
import br.com.mikrotik.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final CustomerRepository customerRepository;

    /**
     * Criar nova fatura
     */
    @Transactional
    public InvoiceDTO create(InvoiceDTO dto) {
        log.info("Criando nova fatura para contrato: {}", dto.getContractId());

        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new ValidationException("Company ID não encontrado no contexto");
        }

        // Validar se contrato existe
        if (!contractRepository.findByIdAndCompanyId(dto.getContractId(), companyId).isPresent()) {
            throw new ValidationException("Contrato não encontrado ou não pertence à empresa");
        }

        // Validar se cliente existe
        if (!customerRepository.findByIdAndCompanyId(dto.getCustomerId(), companyId).isPresent()) {
            throw new ValidationException("Cliente não encontrado ou não pertence à empresa");
        }

        // Verificar se já existe fatura para este contrato no mês
        if (invoiceRepository.existsByContractIdAndReferenceMonth(dto.getContractId(), dto.getReferenceMonth())) {
            throw new ValidationException("Já existe uma fatura para este contrato neste mês");
        }

        dto.setCompanyId(companyId);

        // Calcular valor final se não foi informado
        if (dto.getFinalAmount() == null) {
            dto.setFinalAmount(calculateFinalAmount(dto));
        }

        Invoice invoice = dto.toEntity();
        invoice = invoiceRepository.save(invoice);

        log.info("Fatura criada com sucesso: ID={}", invoice.getId());
        return InvoiceDTO.fromEntity(invoice);
    }

    /**
     * Buscar fatura por ID
     */
    @Transactional(readOnly = true)
    public InvoiceDTO findById(Long id) {
        log.info("Buscando fatura por ID: {}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada com ID: " + id));

        return InvoiceDTO.fromEntity(invoice);
    }

    /**
     * Listar faturas por empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> findAll(Pageable pageable) {
        log.info("Listando faturas");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Invoice> invoices = invoiceRepository.findByCompanyId(companyId, pageable);
        return invoices.map(InvoiceDTO::fromEntity);
    }

    /**
     * Listar faturas por cliente (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> findByCustomer(Long customerId, Pageable pageable) {
        log.info("Listando faturas do cliente: {}", customerId);

        Page<Invoice> invoices = invoiceRepository.findByCustomerId(customerId, pageable);
        return invoices.map(InvoiceDTO::fromEntity);
    }

    /**
     * Listar faturas por status (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> findByStatus(Invoice.InvoiceStatus status, Pageable pageable) {
        log.info("Listando faturas por status: {}", status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Invoice> invoices = invoiceRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        return invoices.map(InvoiceDTO::fromEntity);
    }

    /**
     * Buscar faturas vencidas (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> findOverdue(Pageable pageable) {
        log.info("Buscando faturas vencidas");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Invoice> invoices = invoiceRepository.findOverdueInvoices(companyId, LocalDate.now(), pageable);
        return invoices.map(InvoiceDTO::fromEntity);
    }

    /**
     * Buscar com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> findByFilters(Long customerId, Invoice.InvoiceStatus status,
                                          LocalDate month, Pageable pageable) {
        log.info("Buscando faturas com filtros");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Invoice> invoices = invoiceRepository.findByFilters(companyId, customerId, status, month, pageable);
        return invoices.map(InvoiceDTO::fromEntity);
    }

    /**
     * Atualizar fatura
     */
    @Transactional
    public InvoiceDTO update(Long id, InvoiceDTO dto) {
        log.info("Atualizando fatura: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Invoice existing = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada com ID: " + id));

        existing.setDescription(dto.getDescription());
        existing.setDueDate(dto.getDueDate());
        existing.setDiscountAmount(dto.getDiscountAmount());
        existing.setInterestAmount(dto.getInterestAmount());
        existing.setFinalAmount(calculateFinalAmount(dto));
        existing.setPaymentLink(dto.getPaymentLink());
        existing.setExternalId(dto.getExternalId());

        existing = invoiceRepository.save(existing);

        log.info("Fatura atualizada com sucesso: ID={}", id);
        return InvoiceDTO.fromEntity(existing);
    }

    /**
     * Alterar status da fatura
     */
    @Transactional
    public InvoiceDTO updateStatus(Long id, Invoice.InvoiceStatus status) {
        log.info("Alterando status da fatura: ID={}, status={}", id, status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada com ID: " + id));

        invoice.setStatus(status);
        invoice = invoiceRepository.save(invoice);

        log.info("Status da fatura alterado com sucesso: ID={}", id);
        return InvoiceDTO.fromEntity(invoice);
    }

    /**
     * Marcar fatura como paga
     */
    @Transactional
    public InvoiceDTO markAsPaid(Long id) {
        return updateStatus(id, Invoice.InvoiceStatus.PAID);
    }

    /**
     * Cancelar fatura
     */
    @Transactional
    public InvoiceDTO cancel(Long id) {
        return updateStatus(id, Invoice.InvoiceStatus.CANCELED);
    }

    /**
     * Deletar fatura
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando fatura: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada com ID: " + id));

        invoiceRepository.delete(invoice);
        log.info("Fatura deletada com sucesso: ID={}", id);
    }

    /**
     * Contar faturas por empresa
     */
    @Transactional(readOnly = true)
    public long countByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return invoiceRepository.countByCompanyId(companyId);
    }

    /**
     * Contar faturas por status
     */
    @Transactional(readOnly = true)
    public long countByStatus(Invoice.InvoiceStatus status) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return invoiceRepository.countByCompanyIdAndStatus(companyId, status);
    }

    /**
     * Calcular valor final da fatura
     */
    private BigDecimal calculateFinalAmount(InvoiceDTO dto) {
        BigDecimal amount = dto.getOriginalAmount();

        if (dto.getDiscountAmount() != null) {
            amount = amount.subtract(dto.getDiscountAmount());
        }

        if (dto.getInterestAmount() != null) {
            amount = amount.add(dto.getInterestAmount());
        }

        return amount;
    }
}
