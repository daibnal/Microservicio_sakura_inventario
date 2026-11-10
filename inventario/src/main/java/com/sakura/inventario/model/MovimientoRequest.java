package com.sakura.inventario.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimientoRequest {

	@NotBlank(message = "El tipo es obligatorio")
	private String tipo;

	@NotNull(message = "La cantidad es obligatoria")
	@Positive(message = "La cantidad debe ser mayor a cero")
	private Integer cantidad;

}
