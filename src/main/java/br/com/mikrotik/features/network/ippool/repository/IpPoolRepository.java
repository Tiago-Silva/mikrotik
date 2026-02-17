package br.com.mikrotik.features.network.ippool.repository;

import br.com.mikrotik.features.network.ippool.model.IpPool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpPoolRepository extends JpaRepository<IpPool, Long> {

    // Buscar por servidor Mikrotik
    Page<IpPool> findByMikrotikServerId(Long mikrotikServerId, Pageable pageable);

    // Buscar ativos por servidor
    Page<IpPool> findByMikrotikServerIdAndActive(Long mikrotikServerId, Boolean active, Pageable pageable);

    // Listar todos por servidor (sem paginação)
    List<IpPool> findByMikrotikServerId(Long mikrotikServerId);

    // Listar ativos por servidor (sem paginação)
    List<IpPool> findByMikrotikServerIdAndActive(Long mikrotikServerId, Boolean active);

    // Buscar por nome e servidor
    Optional<IpPool> findByNameAndMikrotikServerId(String name, Long mikrotikServerId);

    // Verificar existência por nome e servidor
    boolean existsByNameAndMikrotikServerId(String name, Long mikrotikServerId);

    // Buscar por ID e servidor (segurança)
    Optional<IpPool> findByIdAndMikrotikServerId(Long id, Long mikrotikServerId);

    // Contar pools por servidor
    long countByMikrotikServerId(Long mikrotikServerId);

    // Contar pools ativos por servidor
    long countByMikrotikServerIdAndActive(Long mikrotikServerId, Boolean active);
}
