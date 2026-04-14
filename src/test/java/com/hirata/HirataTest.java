package com.hirata;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * HirataTest
 * Pruebas de integración para el Sistema de Gestión de Flota.
 * Verifica CRUD completo de conductores, camiones, mantenimiento y
 * la lógica de kilometraje/alertas (RF-05).
 *
 * Requiere MySQL activo con la base de datos hirata_flota.
 * Ejecutar con: mvn test
 */
class HirataTest {

    private ConexionDB db;

    // Identificadores únicos para datos de prueba (no colisionan con datos reales)
    private static final String PATENTE_TEST    = "TEST99";
    private static final String PATENTE_TEST2   = "TST100";
    private static final String RUT_TEST        = "99999999-T";

    @BeforeEach
    void setUp() {
        db = new ConexionDB();
    }

    @AfterEach
    void limpiarDatosPrueba() {
        try (Connection con = db.conectar()) {
            // Orden: mantenimiento y kilometraje primero, luego camiones, luego conductores
            ejecutar(con, "DELETE FROM mantenimiento WHERE patente IN (?, ?)", PATENTE_TEST, PATENTE_TEST2);
            ejecutar(con, "DELETE FROM registro_kilometraje WHERE patente IN (?, ?)", PATENTE_TEST, PATENTE_TEST2);
            ejecutar(con, "DELETE FROM camiones WHERE patente IN (?, ?)", PATENTE_TEST, PATENTE_TEST2);
            ejecutar(con, "DELETE FROM conductores WHERE rut = ?", RUT_TEST);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Kilometraje
    // ─────────────────────────────────────────────────────────────

    @Test
    void testKilometrajeAcumuladoSinRegistros() {
        int total = db.obtenerKilometrajeTotalPorPatente("NOEXISTE00");
        assertEquals(0, total,
            "El kilometraje acumulado debe ser 0 para una patente sin registros");
    }

    @Test
    void testKilometrajeAcumuladoConRegistros() {
        db.registrarKilometraje(PATENTE_TEST, 1500);
        db.registrarKilometraje(PATENTE_TEST, 2500);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertEquals(4000, total,
            "El kilometraje acumulado debe ser 1500 + 2500 = 4000");
    }

    @Test
    void testAlertaMantenimientoRequerido() {
        db.registrarKilometraje(PATENTE_TEST, 3000);
        db.registrarKilometraje(PATENTE_TEST, 2500);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertTrue(total >= 5000,
            "Total (" + total + " km) debería ser >= 5000 para activar alerta");
    }

    @Test
    void testAlertaMantenimientoNoRequerido() {
        db.registrarKilometraje(PATENTE_TEST, 1000);
        db.registrarKilometraje(PATENTE_TEST, 500);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertTrue(total < 5000,
            "Total (" + total + " km) debería ser < 5000, sin alerta");
    }

    @Test
    void testRegistrarKilometrajeGuardaEnBD() {
        db.registrarKilometraje(PATENTE_TEST, 750);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertEquals(750, total,
            "El kilometraje registrado (750) debe coincidir con el total consultado");
    }

    // ─────────────────────────────────────────────────────────────
    // Conductores
    // ─────────────────────────────────────────────────────────────

    @Test
    void testAgregarConductorGuardaEnBD() {
        db.agregarConductor("Test Conductor", RUT_TEST, "+56900000000", "test@test.cl");

        List<String[]> lista = db.listarConductores();
        boolean encontrado = lista.stream().anyMatch(row -> RUT_TEST.equals(row[2]));
        assertTrue(encontrado, "El conductor con RUT " + RUT_TEST + " debe aparecer en la lista");
    }

    @Test
    void testActualizarConductorModificaDatos() {
        db.agregarConductor("Nombre Original", RUT_TEST, "+56900000000", "test@test.cl");

        int id = buscarConductorIdPorRut(RUT_TEST);
        assertNotEquals(-1, id, "El conductor debe existir antes de actualizar");

        db.actualizarConductor(id, "Nombre Modificado", RUT_TEST, "+56911111111", "nuevo@test.cl");

        List<String[]> lista = db.listarConductores();
        String nombreActual = lista.stream()
            .filter(row -> RUT_TEST.equals(row[2]))
            .map(row -> row[1])
            .findFirst().orElse("");
        assertEquals("Nombre Modificado", nombreActual,
            "El nombre del conductor debe haberse actualizado");
    }

    @Test
    void testEliminarConductorLoRemueveDeDB() {
        db.agregarConductor("Conductor Borrable", RUT_TEST, "", "");

        int id = buscarConductorIdPorRut(RUT_TEST);
        assertNotEquals(-1, id);

        boolean ok = db.eliminarConductor(id);
        assertTrue(ok, "eliminarConductor debe retornar true al eliminar exitosamente");

        List<String[]> lista = db.listarConductores();
        boolean sigue = lista.stream().anyMatch(row -> RUT_TEST.equals(row[2]));
        assertFalse(sigue, "El conductor eliminado no debe aparecer en la lista");
    }

    // ─────────────────────────────────────────────────────────────
    // Camiones
    // ─────────────────────────────────────────────────────────────

    @Test
    void testAgregarCamionGuardaEnBD() {
        db.agregarCamion(PATENTE_TEST, "Volvo", "FH16", 2022, null);

        List<String[]> lista = db.listarCamiones();
        boolean encontrado = lista.stream().anyMatch(row -> PATENTE_TEST.equals(row[1]));
        assertTrue(encontrado, "El camión con patente " + PATENTE_TEST + " debe aparecer en la lista");
    }

    @Test
    void testAgregarCamionConConductor() {
        db.agregarConductor("Conductor Camion", RUT_TEST, "", "");
        int conductorId = buscarConductorIdPorRut(RUT_TEST);
        assertNotEquals(-1, conductorId);

        db.agregarCamion(PATENTE_TEST, "Scania", "R500", 2021, conductorId);

        List<String[]> lista = db.listarCamiones();
        String[] camion = lista.stream()
            .filter(row -> PATENTE_TEST.equals(row[1]))
            .findFirst().orElse(null);
        assertNotNull(camion, "El camión debe existir");
        assertEquals(String.valueOf(conductorId), camion[6],
            "El conductor_id debe coincidir con el conductor asignado");
    }

    @Test
    void testEliminarCamionLoRemueveDeDB() {
        db.agregarCamion(PATENTE_TEST, "MAN", "TGX", 2019, null);

        int id = buscarCamionIdPorPatente(PATENTE_TEST);
        assertNotEquals(-1, id);

        boolean ok = db.eliminarCamion(id);
        assertTrue(ok, "eliminarCamion debe retornar true al eliminar exitosamente");

        List<String[]> lista = db.listarCamiones();
        boolean sigue = lista.stream().anyMatch(row -> PATENTE_TEST.equals(row[1]));
        assertFalse(sigue, "El camión eliminado no debe aparecer en la lista");
    }

    // ─────────────────────────────────────────────────────────────
    // Mantenimiento
    // ─────────────────────────────────────────────────────────────

    @Test
    void testAgregarMantenimientoGuardaEnBD() {
        db.agregarMantenimiento(PATENTE_TEST, "Preventivo", "Cambio de aceite", "2024-06-01");

        List<String[]> lista = db.listarMantenimientos();
        boolean encontrado = lista.stream()
            .anyMatch(row -> PATENTE_TEST.equals(row[1]) && "Preventivo".equals(row[2]));
        assertTrue(encontrado, "El registro de mantenimiento debe aparecer en la lista");
    }

    @Test
    void testActualizarMantenimientoModificaDatos() {
        db.agregarMantenimiento(PATENTE_TEST, "Preventivo", "Descripción original", "2024-06-01");

        int id = buscarMantenimientoIdPorPatente(PATENTE_TEST);
        assertNotEquals(-1, id);

        db.actualizarMantenimiento(id, PATENTE_TEST, "Correctivo", "Descripción modificada", "2024-06-15");

        List<String[]> lista = db.listarMantenimientos();
        String[] reg = lista.stream()
            .filter(row -> String.valueOf(id).equals(row[0]))
            .findFirst().orElse(null);
        assertNotNull(reg);
        assertEquals("Correctivo", reg[2], "El tipo debe haberse actualizado a Correctivo");
        assertEquals("Descripción modificada", reg[3], "La descripción debe haberse actualizado");
    }

    @Test
    void testEliminarMantenimientoLoRemueveDeDB() {
        db.agregarMantenimiento(PATENTE_TEST, "Revisión", "Revisión de prueba", "2024-06-01");

        int id = buscarMantenimientoIdPorPatente(PATENTE_TEST);
        assertNotEquals(-1, id);

        boolean ok = db.eliminarMantenimiento(id);
        assertTrue(ok, "eliminarMantenimiento debe retornar true al eliminar exitosamente");

        List<String[]> lista = db.listarMantenimientos();
        boolean sigue = lista.stream().anyMatch(row -> String.valueOf(id).equals(row[0]));
        assertFalse(sigue, "El registro eliminado no debe aparecer en la lista");
    }

    // ─────────────────────────────────────────────────────────────
    // Restricción: no eliminar conductor asignado a camión
    // ─────────────────────────────────────────────────────────────

    @Test
    void testEliminarConductorAsignadoFalla() {
        db.agregarConductor("Conductor Asignado", RUT_TEST, "", "");
        int conductorId = buscarConductorIdPorRut(RUT_TEST);
        assertNotEquals(-1, conductorId);

        db.agregarCamion(PATENTE_TEST, "Volvo", "FH16", 2020, conductorId);

        // Intentar eliminar el conductor asignado debe fallar (FK violation → retorna false)
        boolean eliminado = db.eliminarConductor(conductorId);
        assertFalse(eliminado,
            "No debe ser posible eliminar un conductor que está asignado a un camión");

        // Limpiar manualmente: desasignar primero y luego borrar
        int camionId = buscarCamionIdPorPatente(PATENTE_TEST);
        if (camionId != -1) db.eliminarCamion(camionId);
        db.eliminarConductor(conductorId);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers de búsqueda por identificador
    // ─────────────────────────────────────────────────────────────

    private int buscarConductorIdPorRut(String rut) {
        String sql = "SELECT id FROM conductores WHERE rut = ?";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int buscarCamionIdPorPatente(String patente) {
        String sql = "SELECT id FROM camiones WHERE patente = ?";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int buscarMantenimientoIdPorPatente(String patente) {
        String sql = "SELECT id FROM mantenimiento WHERE patente = ? ORDER BY id DESC LIMIT 1";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void ejecutar(Connection con, String sql, String... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            ps.executeUpdate();
        }
    }
}
