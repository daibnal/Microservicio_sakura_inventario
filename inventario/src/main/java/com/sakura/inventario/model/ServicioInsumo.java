package com.sakura.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "servicio_insumo")
@Data
@NoArgsConstructor
public class ServicioInsumo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "servicio_id", nullable = false)
	private Long servicioId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "insumo_id", nullable = false)
	private Insumo insumo;

	@Column(nullable = false)
	private int cantidad;

}
