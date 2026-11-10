package com.sakura.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "insumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Insumo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 120, message = "El nombre no puede superar 120 caracteres")
	@Column(nullable = false, length = 120)
	private String nombre;

	@NotBlank(message = "La categoría es obligatoria")
	@Size(max = 80, message = "La categoría no puede superar 80 caracteres")
	@Column(nullable = false, length = 80)
	private String categoria;

	@NotBlank(message = "La unidad de medida es obligatoria")
	@Size(max = 20, message = "La unidad no puede superar 20 caracteres")
	@Column(nullable = false, length = 20)
	private String unidadMedida;

	@NotNull(message = "El stock es obligatorio")
	@PositiveOrZero(message = "El stock no puede ser negativo")
	@Column(nullable = false)
	private Integer stock;

	@PositiveOrZero(message = "El stock mínimo no puede ser negativo")
	@Column(nullable = false)
	private int stockMinimo;

	@Column(nullable = false)
	private boolean activo = true;

	public String getEstado() {
		if (stock != null && stock <= stockMinimo) {
			return "CRITICO";
		}
		return "OK";
	}

}
