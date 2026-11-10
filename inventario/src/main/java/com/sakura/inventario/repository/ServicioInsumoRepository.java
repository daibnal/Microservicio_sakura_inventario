package com.sakura.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sakura.inventario.model.ServicioInsumo;

@Repository
public interface ServicioInsumoRepository extends JpaRepository<ServicioInsumo, Long> {

	List<ServicioInsumo> findByServicioId(Long servicioId);

	void deleteByServicioId(Long servicioId);

}
