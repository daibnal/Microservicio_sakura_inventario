package com.sakura.inventario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sakura.inventario.client.SesionGuard;
import com.sakura.inventario.exception.AccesoDenegadoException;
import com.sakura.inventario.exception.RecursoNoEncontradoException;
import com.sakura.inventario.exception.ReglaNegocioException;
import com.sakura.inventario.model.Insumo;
import com.sakura.inventario.model.ServicioInsumo;
import com.sakura.inventario.service.InsumoService;

@WebMvcTest(InsumoController.class)
@ActiveProfiles("test")
class InsumoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InsumoService insumoService;

	@MockitoBean
	private SesionGuard sesionGuard;

	private static final String AUTH = "Bearer token-admin";
	private static final String CUERPO = "{\"nombre\":\"Tinta\",\"categoria\":\"Color\",\"unidadMedida\":\"ml\",\"stock\":10,\"stockMinimo\":2,\"activo\":true}";

	@Test
	void listar_Exito() throws Exception {
		when(insumoService.buscar("tin")).thenReturn(List.of(insumo(1L)));

		mockMvc.perform(get("/api/v1/insumos").header("Authorization", AUTH).param("q", "tin"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Tinta"));
	}

	@Test
	void listar_SinPermiso_Responde403() throws Exception {
		doThrow(new AccesoDenegadoException("No tiene permiso")).when(sesionGuard).exigirAdmin(any());

		mockMvc.perform(get("/api/v1/insumos").header("Authorization", AUTH))
				.andExpect(status().isForbidden());
	}

	@Test
	void criticos_Exito() throws Exception {
		when(insumoService.criticos()).thenReturn(List.of(insumo(2L)));

		mockMvc.perform(get("/api/v1/insumos/criticos").header("Authorization", AUTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(2));
	}

	@Test
	void deServicio_Exito() throws Exception {
		ServicioInsumo vinculo = new ServicioInsumo();
		vinculo.setCantidad(3);
		when(insumoService.listarDeServicio(10L)).thenReturn(List.of(vinculo));

		mockMvc.perform(get("/api/v1/insumos/servicio/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].cantidad").value(3));
	}

	@Test
	void asociar_Exito() throws Exception {
		ServicioInsumo vinculo = new ServicioInsumo();
		vinculo.setCantidad(1);
		when(insumoService.asociar(eq(10L), any())).thenReturn(List.of(vinculo));

		mockMvc.perform(put("/api/v1/insumos/servicio/10")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("[{\"insumoId\":1,\"cantidad\":1}]"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].cantidad").value(1));
	}

	@Test
	void obtener_Exito() throws Exception {
		when(insumoService.buscar(1L)).thenReturn(insumo(1L));

		mockMvc.perform(get("/api/v1/insumos/1").header("Authorization", AUTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unidadMedida").value("ml"));
	}

	@Test
	void obtener_NoExiste_Responde404() throws Exception {
		when(insumoService.buscar(99L)).thenThrow(new RecursoNoEncontradoException("No existe el insumo"));

		mockMvc.perform(get("/api/v1/insumos/99").header("Authorization", AUTH))
				.andExpect(status().isNotFound());
	}

	@Test
	void crear_Exito() throws Exception {
		when(insumoService.crear(any(Insumo.class))).thenReturn(insumo(4L));

		mockMvc.perform(post("/api/v1/insumos")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CUERPO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(4));
	}

	@Test
	void crear_CuerpoVacio_Responde400() throws Exception {
		mockMvc.perform(post("/api/v1/insumos")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void actualizar_Exito() throws Exception {
		when(insumoService.actualizar(eq(1L), any(Insumo.class))).thenReturn(insumo(1L));

		mockMvc.perform(put("/api/v1/insumos/1")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CUERPO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Tinta"));
	}

	@Test
	void movimiento_Exito() throws Exception {
		when(insumoService.registrarMovimiento(1L, "ENTRADA", 2)).thenReturn(insumo(1L));

		mockMvc.perform(post("/api/v1/insumos/1/movimientos")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ENTRADA\",\"cantidad\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void movimiento_StockNegativo_Responde409() throws Exception {
		when(insumoService.registrarMovimiento(1L, "SALIDA", 100))
				.thenThrow(new ReglaNegocioException("El stock no puede quedar negativo"));

		mockMvc.perform(post("/api/v1/insumos/1/movimientos")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"SALIDA\",\"cantidad\":100}"))
				.andExpect(status().isConflict());
	}

	@Test
	void eliminar_Exito() throws Exception {
		doNothing().when(insumoService).desactivar(1L);

		mockMvc.perform(delete("/api/v1/insumos/1").header("Authorization", AUTH))
				.andExpect(status().isNoContent());
	}

	private Insumo insumo(Long id) {
		Insumo insumo = new Insumo();
		insumo.setId(id);
		insumo.setNombre("Tinta");
		insumo.setCategoria("Color");
		insumo.setUnidadMedida("ml");
		insumo.setStock(10);
		insumo.setStockMinimo(2);
		return insumo;
	}
}
