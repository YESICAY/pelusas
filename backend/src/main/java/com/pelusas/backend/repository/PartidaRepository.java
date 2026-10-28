package com.pelusas.backend.repository;

import com.pelusas.backend.entity.PartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {

    List<PartidaEntity> findAllByOrderByFechaDesc();
}