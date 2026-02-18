package br.com.mikrotik.features.auth.repository;

import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.features.auth.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    /**
     * Busca todas as permissões de um usuário
     */
    List<UserPermission> findByUserId(Long userId);

    /**
     * Busca permissão específica de um módulo para um usuário
     */
    Optional<UserPermission> findByUserIdAndModule(Long userId, SystemModule module);

    /**
     * Verifica se usuário tem permissão em um módulo
     */
    boolean existsByUserIdAndModule(Long userId, SystemModule module);

    /**
     * Remove todas as permissões de um usuário
     */
    void deleteByUserId(Long userId);

    /**
     * Remove permissão específica de um módulo
     */
    void deleteByUserIdAndModule(Long userId, SystemModule module);

    /**
     * Conta quantos módulos um usuário tem acesso
     */
    @Query("SELECT COUNT(p) FROM UserPermission p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Busca usuários com acesso a um módulo específico
     */
    @Query("SELECT p FROM UserPermission p WHERE p.module = :module")
    List<UserPermission> findByModule(@Param("module") SystemModule module);
}

