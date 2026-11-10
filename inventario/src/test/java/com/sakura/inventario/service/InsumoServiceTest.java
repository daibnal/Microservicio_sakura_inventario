package com.sakura.inventario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sakura.inventario.client.CatalogoCliente;
import com.sakura.inventario.exception.RecursoNoEncontradoException;
import com.sakura.inventario.exception.ReglaNegocioException;
import com.sakura.inventario.model.Insumo;
import com.sakura.inventario.model.ServicioInsumo;
import com.sakura.inventario.model.ServicioInsumoRequest;
import com.sakura.inventario.repository.InsumoRepository;
import com.sakura.inventario.repository.ServicioInsumoRepository;

@ExtendWith(MockitoExtension.class)
class InsumoServiceTest {

    @Mock
    private InsumoRepository insumoRepository;

    @Mock
    private ServicioInsumoRepository servicioInsumoRepository;

    @Mock
    private CatalogoCliente catalogoCliente;

    @InjectMocks
    private InsumoService insumoService;

    private Insumo insumoNormal;
    private Insumo insumoCritico;

    @BeforeEach
    void setUp() {
        insumoNormal = new Insumo(1L, "Tinta Negra", "Tintas", "ml", 100, 10, true);
        insumoCritico = new Insumo(2L, "Aguja 3RL", "Agujas", "Unidad", 5, 10, true);
    }

    // ==========================================
    // CONSULTAS: listar, buscar(q), criticos, buscar(id)
    // ==========================================
    @Nested
    @DisplayName("Pruebas de Búsqueda y Lectura")
    class LecturaTests {

        @Test
        @DisplayName("listar() debe retornar la lista de insumos activos")
        void listar_RetornaInsumosActivos() {
            when(insumoRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(insumoNormal, insumoCritico));

            List<Insumo> resultado = insumoService.listar();

            assertEquals(2, resultado.size());
            verify(insumoRepository, times(1)).findByActivoTrueOrderByNombreAsc();
        }


        @Test
        @DisplayName("buscar(q) debe filtrar los insumos por coincidencia en el nombre (case-insensitive)")
        void buscar_ConFiltro_RetornaCoincidencias() {
            when(insumoRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(insumoNormal, insumoCritico));

            List<Insumo> resultado = insumoService.buscar("TINTA");

            assertEquals(1, resultado.size());
            assertEquals("Tinta Negra", resultado.get(0).getNombre());
            verify(insumoRepository).findByActivoTrueOrderByNombreAsc();
        }

        @Test
        @DisplayName("buscar(q) sin texto retorna el listado completo")
        void buscar_SinFiltro_RetornaListadoCompleto() {
            when(insumoRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(insumoNormal, insumoCritico));

            assertEquals(2, insumoService.buscar((String) null).size());
            assertEquals(2, insumoService.buscar("   ").size());
            verify(insumoRepository, times(2)).findByActivoTrueOrderByNombreAsc();
        }

        @Test
        @DisplayName("criticos() debe retornar solo los insumos con stock <= stockMinimo")
        void criticos_RetornaSoloInsumosCriticos() {
            when(insumoRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(insumoNormal, insumoCritico));

            List<Insumo> resultado = insumoService.criticos();

            assertEquals(1, resultado.size());
            assertEquals(2L, resultado.get(0).getId());
            assertEquals("CRITICO", resultado.get(0).getEstado());
        }

        @Test
        @DisplayName("buscar(id) retorna insumo cuando existe")
        void buscarPorId_Existente_RetornaInsumo() {
            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));

            Insumo resultado = insumoService.buscar(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
        }

        @Test
        @DisplayName("buscar(id) lanza RecursoNoEncontradoException cuando no existe")
        void buscarPorId_NoExistente_LanzaExcepcion() {
            when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> insumoService.buscar(99L));
        }
    }

    // ==========================================
    // CREACIÓN Y EDICIÓN DE INSUMOS
    // ==========================================
    @Nested
    @DisplayName("Pruebas de Creación, Actualización y Desactivación")
    class EscrituraTests {

        @Test
        @DisplayName("crear() guarda el insumo con ID nulo y activo=true")
        void crear_InsumoValido_Exito() {
            Insumo nuevo = new Insumo(5L, "Guantes", "Protección", "Caja", 50, 5, false);
            when(insumoRepository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

            Insumo creado = insumoService.crear(nuevo);

            assertNull(creado.getId());
            assertTrue(creado.isActivo());
            verify(insumoRepository, times(1)).save(nuevo);
        }

        @Test
        @DisplayName("crear() lanza ReglaNegocioException si el stock es negativo")
        void crear_StockNegativo_LanzaExcepcion() {
            Insumo invalido = new Insumo();
            invalido.setStock(-1);

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> insumoService.crear(invalido));
            assertEquals("El stock no puede ser negativo", ex.getMessage());
            verify(insumoRepository, never()).save(any());
        }

        @Test
        @DisplayName("actualizar() modifica campos y guarda correctamente")
        void actualizar_Exito() {
            Insumo nuevosDatos = new Insumo();
            nuevosDatos.setNombre("Tinta Negra XL");
            nuevosDatos.setCategoria("Tintas Premium");
            nuevosDatos.setUnidadMedida("L");
            nuevosDatos.setStockMinimo(20);

            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));
            when(insumoRepository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

            Insumo actualizado = insumoService.actualizar(1L, nuevosDatos);

            assertEquals("Tinta Negra XL", actualizado.getNombre());
            assertEquals("Tintas Premium", actualizado.getCategoria());
            assertEquals("L", actualizado.getUnidadMedida());
            assertEquals(20, actualizado.getStockMinimo());
        }

        @Test
        @DisplayName("desactivar() cambia el campo activo a false")
        void desactivar_Exito() {
            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));
            when(insumoRepository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

            insumoService.desactivar(1L);

            assertFalse(insumoNormal.isActivo());
            verify(insumoRepository).save(insumoNormal);
        }
    }

    // ==========================================
    // MOVIMIENTOS DE INVENTARIO
    // ==========================================
    @Nested
    @DisplayName("Pruebas de Registrar Movimiento")
    class MovimientoTests {

        @Test
        @DisplayName("registrarMovimiento() lanza excepción si la cantidad es <= 0")
        void movimiento_CantidadInvalida_LanzaExcepcion() {
            ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> insumoService.registrarMovimiento(1L, "ENTRADA", 0)
            );
            assertEquals("La cantidad debe ser mayor a cero", ex.getMessage());
        }

        @Test
        @DisplayName("registrarMovimiento() procesa ENTRADA correctamente")
        void movimiento_Entrada_IncrementaStock() {
            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));
            when(insumoRepository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

            Insumo res = insumoService.registrarMovimiento(1L, "ENTRADA", 20);

            assertEquals(120, res.getStock());
        }

        @Test
        @DisplayName("registrarMovimiento() procesa SALIDA correctamente cuando hay suficiente stock")
        void movimiento_Salida_DecrementaStock() {
            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));
            when(insumoRepository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

            Insumo res = insumoService.registrarMovimiento(1L, "SALIDA", 30);

            assertEquals(70, res.getStock());
        }

        @Test
        @DisplayName("registrarMovimiento() lanza excepción si SALIDA deja stock negativo")
        void movimiento_SalidaInsuficiente_LanzaExcepcion() {
            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));

            ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> insumoService.registrarMovimiento(1L, "SALIDA", 150)
            );
            assertEquals("El stock no puede quedar negativo", ex.getMessage());
            verify(insumoRepository, never()).save(any());
        }

        @Test
        @DisplayName("registrarMovimiento() lanza excepción si el tipo no es ENTRADA ni SALIDA")
        void movimiento_TipoInvalido_LanzaExcepcion() {
            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));

            ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> insumoService.registrarMovimiento(1L, "AJUSTE", 5)
            );
            assertEquals("El tipo de movimiento debe ser ENTRADA o SALIDA", ex.getMessage());
        }
    }

    // ==========================================
    // ASOCIACIÓN CON SERVICIOS (CatalogoCliente)
    // ==========================================
    @Nested
    @DisplayName("Pruebas de Asociación con Servicios")
    class AsociacionServicioTests {

        @Test
        @DisplayName("asociar() limpia vínculos previos y guarda los nuevos pedidos")
        void asociar_PedidosValidos_Exito() {
            Long servicioId = 10L;

            ServicioInsumoRequest request = new ServicioInsumoRequest();
            request.setInsumoId(1L);
            request.setCantidad(3);

            doNothing().when(catalogoCliente).exigirServicio(servicioId);
            when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoNormal));
            when(servicioInsumoRepository.save(any(ServicioInsumo.class))).thenAnswer(i -> i.getArgument(0));

            List<ServicioInsumo> resultado = insumoService.asociar(servicioId, List.of(request));

            assertEquals(1, resultado.size());
            assertEquals(servicioId, resultado.get(0).getServicioId());
            assertEquals(3, resultado.get(0).getCantidad());

            verify(catalogoCliente).exigirServicio(servicioId);
            verify(servicioInsumoRepository).deleteByServicioId(servicioId);
            verify(servicioInsumoRepository, times(1)).save(any(ServicioInsumo.class));
        }

        @Test
        @DisplayName("asociar() con lista nula elimina los vínculos previos y retorna lista vacía")
        void asociar_PedidosNulos_RetornaListaVacia() {
            Long servicioId = 10L;
            doNothing().when(catalogoCliente).exigirServicio(servicioId);

            List<ServicioInsumo> resultado = insumoService.asociar(servicioId, null);

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(servicioInsumoRepository).deleteByServicioId(servicioId);
            verify(servicioInsumoRepository, never()).save(any());
        }

        @Test
        @DisplayName("listarDeServicio() valida existencia del servicio y retorna insumos asociados")
        void listarDeServicio_Exito() {
            Long servicioId = 10L;
            ServicioInsumo vinculo = new ServicioInsumo();
            vinculo.setServicioId(servicioId);
            vinculo.setInsumo(insumoNormal);
            vinculo.setCantidad(2);

            doNothing().when(catalogoCliente).exigirServicio(servicioId);
            when(servicioInsumoRepository.findByServicioId(servicioId)).thenReturn(List.of(vinculo));

            List<ServicioInsumo> resultado = insumoService.listarDeServicio(servicioId);

            assertEquals(1, resultado.size());
            verify(catalogoCliente).exigirServicio(servicioId);
            verify(servicioInsumoRepository).findByServicioId(servicioId);
        }
    }
}