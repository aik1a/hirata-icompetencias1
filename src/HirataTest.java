import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * HirataTest
 * Pruebas unitarias para el Sistema de Gestión de Flota (RF-05).
 * Verifica el registro de kilometraje y la lógica de alertas de mantenimiento.
 * Requiere conexión activa a MySQL con la base de datos hirata_flota.
 *
 * Dependencia: JUnit 5 (junit-jupiter-api y junit-jupiter-engine).
 * Descargar desde https://mvnrepository.com/artifact/org.junit.jupiter
 * y colocar los JAR en la carpeta lib/.
 */
public class HirataTest {

    private ConexionDB db;
    private static final String PATENTE_TEST = "TEST99";

    /**
     * Inicializa la instancia de ConexionDB antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        db = new ConexionDB();
    }

    /**
     * Elimina los registros de prueba de la tabla registro_kilometraje
     * después de cada prueba para evitar datos residuales.
     */
    @AfterEach
    void limpiarDatosPrueba() {
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM registro_kilometraje WHERE patente = ?")) {
            ps.setString(1, PATENTE_TEST);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica que obtenerKilometrajeTotalPorPatente retorna 0
     * cuando no existen registros para una patente dada.
     */
    @Test
    void testKilometrajeAcumuladoSinRegistros() {
        int total = db.obtenerKilometrajeTotalPorPatente("NOEXISTE00");
        assertEquals(0, total,
            "El kilometraje acumulado debe ser 0 para una patente sin registros");
    }

    /**
     * Verifica que obtenerKilometrajeTotalPorPatente retorna la suma correcta
     * cuando se insertan múltiples registros para una misma patente.
     */
    @Test
    void testKilometrajeAcumuladoConRegistros() {
        db.registrarKilometraje(PATENTE_TEST, 1500);
        db.registrarKilometraje(PATENTE_TEST, 2500);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertEquals(4000, total,
            "El kilometraje acumulado debe ser 1500 + 2500 = 4000");
    }

    /**
     * Verifica que una patente con 5000 km o más acumulados
     * activa la condición de alerta de mantenimiento preventivo (RF-03).
     */
    @Test
    void testAlertaMantenimientoRequerido() {
        db.registrarKilometraje(PATENTE_TEST, 3000);
        db.registrarKilometraje(PATENTE_TEST, 2500);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertTrue(total >= 5000,
            "El total acumulado (" + total + " km) debería ser >= 5000 para activar alerta");
    }

    /**
     * Verifica que una patente con menos de 5000 km acumulados
     * NO activa la condición de alerta de mantenimiento preventivo.
     */
    @Test
    void testAlertaMantenimientoNoRequerido() {
        db.registrarKilometraje(PATENTE_TEST, 1000);
        db.registrarKilometraje(PATENTE_TEST, 500);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertTrue(total < 5000,
            "El total acumulado (" + total + " km) debería ser < 5000, sin alerta");
    }

    /**
     * Verifica que registrarKilometraje guarda efectivamente el dato en la BD,
     * comprobando con obtenerKilometrajeTotalPorPatente que el valor fue persistido.
     */
    @Test
    void testRegistrarKilometrajeGuardaEnBD() {
        db.registrarKilometraje(PATENTE_TEST, 750);

        int total = db.obtenerKilometrajeTotalPorPatente(PATENTE_TEST);
        assertEquals(750, total,
            "El kilometraje registrado (750) debe coincidir con el total consultado");
    }
}
