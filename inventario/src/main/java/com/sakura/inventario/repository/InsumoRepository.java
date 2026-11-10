package com.sakura.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sakura.inventario.model.Insumo;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {

	List<Insumo> findByActivoTrueOrderByNombreAsc();

}
