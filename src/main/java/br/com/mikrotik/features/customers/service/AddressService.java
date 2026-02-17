package br.com.mikrotik.features.customers.service;

import br.com.mikrotik.features.customers.dto.AddressDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.features.customers.model.Address;
import br.com.mikrotik.features.customers.repository.AddressRepository;
import br.com.mikrotik.features.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    /**
     * Criar novo endereço
     */
    @Transactional
    public AddressDTO create(AddressDTO dto) {
        log.info("Criando endereço para cliente: {}", dto.getCustomerId());

        // Validar se cliente existe
        if (!customerRepository.existsById(dto.getCustomerId())) {
            throw new ResourceNotFoundException("Cliente não encontrado: " + dto.getCustomerId());
        }

        Address address = dto.toEntity();
        address.setCreatedAt(LocalDateTime.now());

        Address saved = addressRepository.save(address);
        log.info("Endereço criado com sucesso: ID={}", saved.getId());

        return AddressDTO.fromEntity(saved);
    }

    /**
     * Buscar endereço por ID
     */
    @Transactional(readOnly = true)
    public AddressDTO findById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));
        return AddressDTO.fromEntity(address);
    }

    /**
     * Listar endereços de um cliente
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> findByCustomer(Long customerId) {
        // Validar se cliente existe
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Cliente não encontrado: " + customerId);
        }

        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        return addresses.stream()
                .map(AddressDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Buscar endereço de instalação do cliente
     */
    @Transactional(readOnly = true)
    public AddressDTO findInstallationAddress(Long customerId) {
        Address address = addressRepository.findInstallationAddress(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço de instalação não encontrado para o cliente: " + customerId));
        return AddressDTO.fromEntity(address);
    }

    /**
     * Buscar endereço de cobrança do cliente
     */
    @Transactional(readOnly = true)
    public AddressDTO findBillingAddress(Long customerId) {
        Address address = addressRepository.findBillingAddress(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço de cobrança não encontrado para o cliente: " + customerId));
        return AddressDTO.fromEntity(address);
    }

    /**
     * Atualizar endereço
     */
    @Transactional
    public AddressDTO update(Long id, AddressDTO dto) {
        log.info("Atualizando endereço: {}", id);

        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));

        // Atualizar campos
        existing.setType(dto.getType());
        existing.setZipCode(dto.getZipCode());
        existing.setStreet(dto.getStreet());
        existing.setNumber(dto.getNumber());
        existing.setComplement(dto.getComplement());
        existing.setDistrict(dto.getDistrict());
        existing.setCity(dto.getCity());
        existing.setState(dto.getState());
        existing.setLatitude(dto.getLatitude());
        existing.setLongitude(dto.getLongitude());

        Address updated = addressRepository.save(existing);
        log.info("Endereço atualizado com sucesso: {}", id);

        return AddressDTO.fromEntity(updated);
    }

    /**
     * Deletar endereço
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando endereço: {}", id);

        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endereço não encontrado: " + id);
        }

        addressRepository.deleteById(id);
        log.info("Endereço deletado com sucesso: {}", id);
    }
}
