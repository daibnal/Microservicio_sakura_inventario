package com.sakura.inventario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sakura.inventario.client.CatalogoCliente;
import com.sakura.inventario.exception.RecursoNoEncontradoException;
import com.sakura.inventario.exception.ReglaNegocioException;
import com.sakura.inventario.model.Insumo;
import com.sakura.inventario.model.ServicioInsumo;
import com.sakura.inventario.model.ServicioInsumoRequest;
import com.sakura.inventario.repository.InsumoRepository;
import com.sakura.inventario.repository.ServicioInsumoRepository;

@Service
public class InsumoService {

	@Autowired
	private InsumoRepository insumoRepository;

	@Autowired
	private ServicioInsumoRepository servicioInsumoRepository;

	@Autowired
	private CatalogoCliente catalogoCliente;

	public List<Insumo> listar() {
		return insumoRepository.findByActivoTrueOrderByNombreAsc();
	}

	public List<Insumo> buscar(String q) {
		if (q == null || q.isBlank()) {
			return listar();
		}
		String texto = q.toLowerCase();
		return listar().stream().filter(insumo -> insumo.getNombre().toLowerCase().contains(texto)).toList();
	}

	public List<Insumo> criticos() {
		return listar().stream().filter(insumo -> "CRITICO".equals(insumo.getEstado())).toList();
	}

	public Insumo buscar(Long id) {
		return insumoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el insumo"));
	}

	public Insumo crear(Insumo insumo) {
		if (insumo.getStock() < 0) {
			throw new ReglaNegocioException("El stock no puede ser negativo");
		}
		insumo.setId(null);
		insumo.setActivo(true);
		return insumoRepository.save(insumo);
	}

	public Insumo actualizar(Long id, Insumo datos) {
		Insumo actual = buscar(id);
		actual.setNombre(datos.getNombre());
		actual.setCategoria(datos.getCategoria());
		actual.setUnidadMedida(datos.getUnidadMedida());
		actual.setStockMinimo(datos.getStockMinimo());
		return insumoRepository.save(actual);
	}

	public void desactivar(Long id) {
		Insumo insumo = buscar(id);
		insumo.setActivo(false);
		insumoRepository.save(insumo);
	}

	public Insumo registrarMovimiento(Long insumoId, String tipo, int cantidad) {
		if (cantidad <= 0) {
			throw new ReglaNegocioException("La cantidad debe ser mayor a cero");
		}
		Insumo insumo = buscar(insumoId);
		if ("ENTRADA".equals(tipo)) {
			insumo.setStock(insumo.getStock() + cantidad);
		} else if ("SALIDA".equals(tipo)) {
			if (insumo.getStock() - cantidad < 0) {
				throw new ReglaNegocioException("El stock no puede quedar negativo");
			}
			insumo.setStock(insumo.getStock() - cantidad);
		} else {
			throw new ReglaNegocioException("El tipo de movimiento debe ser ENTRADA o SALIDA");
		}
		return insumoRepository.save(insumo);
	}

	public List<ServicioInsumo> asociar(Long servicioId, List<ServicioInsumoRequest> pedidos) {
		catalogoCliente.exigirServicio(servicioId);
		servicioInsumoRepository.deleteByServicioId(servicioId);
		List<ServicioInsumo> guardados = new ArrayList<>();
		if (pedidos == null) {
			return guardados;
		}
		for (ServicioInsumoRequest pedido : pedidos) {
			ServicioInsumo vinculo = new ServicioInsumo();
			vinculo.setServicioId(servicioId);
			vinculo.setInsumo(buscar(pedido.getInsumoId()));
			vinculo.setCantidad(pedido.getCantidad());
			guardados.add(servicioInsumoRepository.save(vinculo));
		}
		return guardados;
	}

	public List<ServicioInsumo> listarDeServicio(Long servicioId) {
		catalogoCliente.exigirServicio(servicioId);
		return servicioInsumoRepository.findByServicioId(servicioId);
	}

}
