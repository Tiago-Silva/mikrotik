package br.com.mikrotik.repository;

import br.com.mikrotik.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Buscar todos os endereços de um cliente
    List<Address> findByCustomerId(Long customerId);

    // Buscar endereço por tipo
    List<Address> findByCustomerIdAndType(Long customerId, Address.AddressType type);

    // Buscar endereço principal (instalação ou ambos)
    @Query("SELECT a FROM Address a WHERE a.customerId = :customerId " +
           "AND a.type IN ('INSTALLATION', 'BOTH') " +
           "ORDER BY CASE WHEN a.type = 'BOTH' THEN 0 ELSE 1 END")
    Optional<Address> findInstallationAddress(@Param("customerId") Long customerId);

    // Buscar endereço de cobrança
    @Query("SELECT a FROM Address a WHERE a.customerId = :customerId " +
           "AND a.type IN ('BILLING', 'BOTH') " +
           "ORDER BY CASE WHEN a.type = 'BOTH' THEN 0 ELSE 1 END")
    Optional<Address> findBillingAddress(@Param("customerId") Long customerId);

    // Buscar endereços por cidade
    List<Address> findByCity(String city);

    // Buscar endereços por estado
    List<Address> findByState(String state);

    // Buscar endereços com coordenadas (para mapa)
    @Query("SELECT a FROM Address a WHERE a.latitude IS NOT NULL AND a.longitude IS NOT NULL")
    List<Address> findAllWithCoordinates();

    // Buscar endereços em uma região (lat/long)
    @Query("SELECT a FROM Address a WHERE " +
           "a.latitude BETWEEN :minLat AND :maxLat AND " +
           "a.longitude BETWEEN :minLon AND :maxLon")
    List<Address> findByRegion(@Param("minLat") Double minLat,
                               @Param("maxLat") Double maxLat,
                               @Param("minLon") Double minLon,
                               @Param("maxLon") Double maxLon);

    // Deletar todos os endereços de um cliente
    void deleteByCustomerId(Long customerId);
}
