package br.com.mikrotik.service;

import br.com.mikrotik.dto.CustomerDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.Address;
import br.com.mikrotik.model.Customer;
import br.com.mikrotik.repository.AddressRepository;
import br.com.mikrotik.repository.CustomerRepository;
import br.com.mikrotik.util.CompanyContextHolder;
import br.com.mikrotik.util.DocumentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    /**
     * Criar novo cliente
     */
    @Transactional
    public CustomerDTO create(CustomerDTO dto) {
        log.info("Criando novo cliente: {}", dto.getName());

        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new ValidationException("Company ID não encontrado no contexto");
        }

        // Validar documento
        validateDocument(dto);

        // Verificar se documento já existe
        if (customerRepository.existsByDocumentAndCompanyId(dto.getDocument(), companyId)) {
            throw new ValidationException("Já existe um cliente com este documento");
        }

        dto.setCompanyId(companyId);
        Customer customer = dto.toEntity();

        // Salvar endereços separadamente
        List<Address> addresses = customer.getAddresses();
        customer.setAddresses(null);

        customer = customerRepository.save(customer);

        // Salvar endereços
        if (addresses != null && !addresses.isEmpty()) {
            final Long customerId = customer.getId();
            addresses.forEach(address -> {
                address.setCustomerId(customerId);
                addressRepository.save(address);
            });
            customer.setAddresses(addresses);
        }

        log.info("Cliente criado com sucesso: ID={}", customer.getId());
        return CustomerDTO.fromEntity(customer);
    }

    /**
     * Buscar cliente por ID
     */
    @Transactional(readOnly = true)
    public CustomerDTO findById(Long id) {
        log.info("Buscando cliente por ID: {}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Customer customer = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        return CustomerDTO.fromEntity(customer);
    }

    /**
     * Buscar cliente por documento
     */
    @Transactional(readOnly = true)
    public CustomerDTO findByDocument(String document) {
        log.info("Buscando cliente por documento: {}", document);

        Long companyId = CompanyContextHolder.getCompanyId();
        String cleanDocument = DocumentValidator.unformat(document);

        Customer customer = customerRepository.findByDocumentAndCompanyId(cleanDocument, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com documento: " + document));

        return CustomerDTO.fromEntity(customer);
    }

    /**
     * Listar todos os clientes da empresa
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findAll(Pageable pageable) {
        log.info("Listando todos os clientes");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Customer> customers = customerRepository.findByCompanyId(companyId, pageable);

        return customers.map(CustomerDTO::fromEntity);
    }

    /**
     * Buscar clientes por status
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByStatus(Customer.CustomerStatus status, Pageable pageable) {
        log.info("Buscando clientes por status: {}", status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Customer> customers = customerRepository.findByCompanyIdAndStatus(companyId, status, pageable);

        return customers.map(CustomerDTO::fromEntity);
    }

    /**
     * Buscar clientes por tipo
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByType(Customer.CustomerType type, Pageable pageable) {
        log.info("Buscando clientes por tipo: {}", type);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Customer> customers = customerRepository.findByCompanyIdAndType(companyId, type, pageable);

        return customers.map(CustomerDTO::fromEntity);
    }

    /**
     * Buscar clientes por nome
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByName(String name, Pageable pageable) {
        log.info("Buscando clientes por nome: {}", name);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Customer> customers = customerRepository.findByCompanyIdAndNameContainingIgnoreCase(
                companyId, name, pageable);

        return customers.map(CustomerDTO::fromEntity);
    }

    /**
     * Buscar clientes por cidade
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByCity(String city, Pageable pageable) {
        log.info("Buscando clientes por cidade: {}", city);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Customer> customers = customerRepository.findByCompanyIdAndCity(companyId, city, pageable);

        return customers.map(CustomerDTO::fromEntity);
    }

    /**
     * Buscar clientes com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByFilters(String name, Customer.CustomerStatus status,
                                           Customer.CustomerType type, Pageable pageable) {
        log.info("Buscando clientes com filtros - nome: {}, status: {}, tipo: {}", name, status, type);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Customer> customers = customerRepository.findByFilters(companyId, name, status, type, pageable);

        return customers.map(CustomerDTO::fromEntity);
    }

    /**
     * Atualizar cliente
     */
    @Transactional
    public CustomerDTO update(Long id, CustomerDTO dto) {
        log.info("Atualizando cliente: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Customer existing = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        // Validar documento se foi alterado
        if (!existing.getDocument().equals(dto.getDocument())) {
            validateDocument(dto);

            // Verificar se novo documento já existe
            if (customerRepository.existsByDocumentAndCompanyId(dto.getDocument(), companyId)) {
                throw new ValidationException("Já existe um cliente com este documento");
            }
        }

        // Atualizar campos
        existing.setName(dto.getName());
        existing.setType(dto.getType());
        existing.setDocument(dto.getDocument());
        existing.setRgIe(dto.getRgIe());
        existing.setEmail(dto.getEmail());
        existing.setPhonePrimary(dto.getPhonePrimary());
        existing.setPhoneWhatsapp(dto.getPhoneWhatsapp());
        existing.setStatus(dto.getStatus());
        existing.setNotes(dto.getNotes());

        existing = customerRepository.save(existing);

        log.info("Cliente atualizado com sucesso: ID={}", id);
        return CustomerDTO.fromEntity(existing);
    }

    /**
     * Alterar status do cliente
     */
    @Transactional
    public CustomerDTO updateStatus(Long id, Customer.CustomerStatus status) {
        log.info("Alterando status do cliente: ID={}, novo status={}", id, status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Customer customer = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        customer.setStatus(status);
        customer = customerRepository.save(customer);

        log.info("Status do cliente alterado com sucesso: ID={}", id);
        return CustomerDTO.fromEntity(customer);
    }

    /**
     * Deletar cliente
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando cliente: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Customer customer = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        customerRepository.delete(customer);
        log.info("Cliente deletado com sucesso: ID={}", id);
    }

    /**
     * Contar clientes por status
     */
    @Transactional(readOnly = true)
    public long countByStatus(Customer.CustomerStatus status) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return customerRepository.countByCompanyIdAndStatus(companyId, status);
    }

    /**
     * Validar documento CPF/CNPJ
     */
    private void validateDocument(CustomerDTO dto) {
        String cleanDocument = DocumentValidator.unformat(dto.getDocument());
        dto.setDocument(cleanDocument);

        boolean isValid = DocumentValidator.isValidDocument(cleanDocument, dto.getType().name());
        if (!isValid) {
            String docType = dto.getType() == Customer.CustomerType.FISICA ? "CPF" : "CNPJ";
            throw new ValidationException(docType + " inválido: " + dto.getDocument());
        }
    }
}
