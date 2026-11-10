package com.sakura.inventario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.inventario.client.SesionGuard;
import com.sakura.inventario.model.Insumo;
import com.sakura.inventario.model.MovimientoRequest;
import com.sakura.inventario.model.ServicioInsumo;
import com.sakura.inventario.model.ServicioInsumoRequest;
import com.sakura.inventario.service.InsumoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/insumos")
public class InsumoController {

	@Autowired
	private InsumoService insumoService;

	@Autowired
	private SesionGuard sesionGuard;

	@GetMapping
	public List<Insumo> listar(@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestParam(required = false) String q) {
		sesionGuard.exigirAdmin(authorization);
		return insumoService.buscar(q);
	}

	@GetMapping("/criticos")
	public List<Insumo> criticos(@RequestHeader(value = "Authorization", required = false) String authorization) {
		sesionGuard.exigirAdmin(authorization);
		return insumoService.criticos();
	}

	@GetMapping("/servicio/{servicioId}")
	public List<ServicioInsumo> deServicio(@PathVariable Long servicioId) {
		return insumoService.listarDeServicio(servicioId);
	}

	@PutMapping("/servicio/{servicioId}")
	public List<ServicioInsumo> asociar(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long servicioId, @RequestBody List<ServicioInsumoRequest> pedidos) {
		sesionGuard.exigirAdmin(authorization);
		return insumoService.asociar(servicioId, pedidos);
	}

	@GetMapping("/{id}")
	public Insumo obtener(@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id) {
		sesionGuard.exigirAdmin(authorization);
		return insumoService.buscar(id);
	}

	@PostMapping
	public ResponseEntity<Insumo> crear(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody Insumo insumo) {
		sesionGuard.exigirAdmin(authorization);
		return new ResponseEntity<>(insumoService.crear(insumo), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public Insumo actualizar(@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id, @Valid @RequestBody Insumo insumo) {
		sesionGuard.exigirAdmin(authorization);
		return insumoService.actualizar(id, insumo);
	}

	@PostMapping("/{id}/movimientos")
	public Insumo mover(@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id, @Valid @RequestBody MovimientoRequest movimiento) {
		sesionGuard.exigirAdmin(authorization);
		return insumoService.registrarMovimiento(id, movimiento.getTipo(), movimiento.getCantidad());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id) {
		sesionGuard.exigirAdmin(authorization);
		insumoService.desactivar(id);
		return ResponseEntity.noContent().build();
	}

}
