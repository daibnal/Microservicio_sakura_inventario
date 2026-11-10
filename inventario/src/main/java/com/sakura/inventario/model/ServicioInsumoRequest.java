package com.sakura.inventario.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ServicioInsumoRequest {

	@NotNull(message = "El insumo es obligatorio")
	private Long insumoId;

	@NotNull(message = "La cantidad es obligatoria")
	@Positive(message = "La cantidad debe ser mayor a cero")
	private Integer cantidad;

}
