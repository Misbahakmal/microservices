package com.demo.customer_service.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AddressSpringDataRepository extends JpaRepository<AddressJpaEntity, UUID> {
    List<AddressJpaEntity> findByCustomerId(UUID customerId);
    Optional<AddressJpaEntity> findByIdAndCustomerId(UUID id, UUID customerId); 
}
