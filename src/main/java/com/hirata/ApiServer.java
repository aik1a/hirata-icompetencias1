package com.hirata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ApiServer
 * Servidor REST con Javalin que expone todos los módulos de la flota.
 * Reemplaza las ventanas Swing por una API consumida desde el dashboard web.
 *
 * Puertos:
 *   Backend API: http://localhost:7000/api/*
 *   Frontend dev: http://localhost:5173 (Vite proxy → :7000)
 *   Frontend prod: http://localhost:7000 (sirve frontend/dist/)
 */
public class ApiServer {

    private static final ConexionDB db = new ConexionDB();
    private static final ObjectMapper json = new ObjectMapper();
    private static final int UMBRAL_KM = 5000;
    private static final int PORT = 7000;

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // CORS: permite solicitudes desde el frontend (Vite dev y producción)
            config.bundledPlugins.enableCors(cors ->
                cors.addRule(it -> it.anyHost())
            );

            // Sirve el build de React si existe (modo producción)
            File distDir = new File("frontend/dist");
            if (distDir.exists() && distDir.isDirectory()) {
                config.staticFiles.add(sf -> {
                    sf.hostedPath = "/";
                    sf.directory = "frontend/dist";
                    sf.location = Location.EXTERNAL;
                });
                config.spaRoot.addFile("/", "frontend/dist/index.html", Location.EXTERNAL);
            }
        }).start(PORT);

        // ── Dashboard ──────────────────────────────────────────────────────
        app.get("/api/dashboard", ApiServer::getDashboard);

        // ── Conductores ────────────────────────────────────────────────────
        app.get("/api/conductores",          ApiServer::getConductores);
        app.post("/api/conductores",         ApiServer::createConductor);
        app.put("/api/conductores/{id}",     ApiServer::updateConductor);
        app.delete("/api/conductores/{id}",  ApiServer::deleteConductor);

        // ── Camiones ───────────────────────────────────────────────────────
        app.get("/api/camiones",             ApiServer::getCamiones);
        app.post("/api/camiones",            ApiServer::createCamion);
        app.put("/api/camiones/{id}",        ApiServer::updateCamion);
        app.delete("/api/camiones/{id}",     ApiServer::deleteCamion);

        // ── Kilometraje ────────────────────────────────────────────────────
        app.get("/api/kilometraje",                       ApiServer::getKilometraje);
        app.post("/api/kilometraje",                      ApiServer::createKilometraje);
        app.get("/api/kilometraje/total/{patente}",       ApiServer::getKilometrajeTotalPorPatente);

        // ── Mantenimiento ──────────────────────────────────────────────────
        app.get("/api/mantenimiento",        ApiServer::getMantenimiento);
        app.post("/api/mantenimiento",       ApiServer::createMantenimiento);
        app.put("/api/mantenimiento/{id}",   ApiServer::updateMantenimiento);
        app.delete("/api/mantenimiento/{id}", ApiServer::deleteMantenimiento);

        System.out.println("=================================================");
        System.out.println("  Hirata Fleet API  →  http://localhost:" + PORT);
        System.out.println("  Dashboard Web     →  http://localhost:5173");
        System.out.println("=================================================");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Dashboard
    // ──────────────────────────────────────────────────────────────────────

    private static void getDashboard(Context ctx) throws Exception {
        List<String[]> kmPorCamionRaw = db.obtenerKilometrajePorCamion();

        int totalKm = 0;
        List<Map<String, Object>> alertas = new ArrayList<>();
        List<Map<String, Object>> kmPorCamion = new ArrayList<>();

        for (String[] row : kmPorCamionRaw) {
            int total = Integer.parseInt(row[1]);
            totalKm += total;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("patente", row[0]);
            item.put("total", total);
            kmPorCamion.add(item);
            if (total >= UMBRAL_KM) alertas.add(item);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCamiones",   db.listarCamiones().size());
        stats.put("totalConductores", db.listarConductores().size());
        stats.put("totalKm",         totalKm);
        stats.put("alertas",         alertas);
        stats.put("kmPorCamion",     kmPorCamion);
        ctx.json(stats);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Conductores
    // ──────────────────────────────────────────────────────────────────────

    private static void getConductores(Context ctx) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] row : db.listarConductores()) result.add(conductorToMap(row));
        ctx.json(result);
    }

    private static void createConductor(Context ctx) throws Exception {
        Map<String, String> body = parseBody(ctx);
        db.agregarConductor(body.get("nombre"), body.get("rut"), body.get("telefono"), body.get("email"));
        ctx.status(201).json(Map.of("status", "created"));
    }

    private static void updateConductor(Context ctx) throws Exception {
        int id = pathInt(ctx, "id");
        Map<String, String> body = parseBody(ctx);
        db.actualizarConductor(id, body.get("nombre"), body.get("rut"), body.get("telefono"), body.get("email"));
        ctx.json(Map.of("status", "updated"));
    }

    private static void deleteConductor(Context ctx) {
        int id = pathInt(ctx, "id");
        boolean ok = db.eliminarConductor(id);
        if (ok) {
            ctx.json(Map.of("status", "deleted"));
        } else {
            ctx.status(409).json(Map.of("error", "No se puede eliminar: el conductor está asignado a un camión"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Camiones
    // ──────────────────────────────────────────────────────────────────────

    private static void getCamiones(Context ctx) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] row : db.listarCamiones()) result.add(camionToMap(row));
        ctx.json(result);
    }

    private static void createCamion(Context ctx) throws Exception {
        Map<String, Object> body = parseBodyObj(ctx);
        db.agregarCamion(
            str(body, "patente"),
            str(body, "marca"),
            str(body, "modelo"),
            Integer.parseInt(str(body, "anio")),
            conductorIdOrNull(body)
        );
        ctx.status(201).json(Map.of("status", "created"));
    }

    private static void updateCamion(Context ctx) throws Exception {
        int id = pathInt(ctx, "id");
        Map<String, Object> body = parseBodyObj(ctx);
        db.actualizarCamion(
            id,
            str(body, "patente"),
            str(body, "marca"),
            str(body, "modelo"),
            Integer.parseInt(str(body, "anio")),
            conductorIdOrNull(body)
        );
        ctx.json(Map.of("status", "updated"));
    }

    private static void deleteCamion(Context ctx) {
        int id = pathInt(ctx, "id");
        boolean ok = db.eliminarCamion(id);
        if (ok) ctx.json(Map.of("status", "deleted"));
        else ctx.status(400).json(Map.of("error", "No se pudo eliminar el camión"));
    }

    // ──────────────────────────────────────────────────────────────────────
    // Kilometraje
    // ──────────────────────────────────────────────────────────────────────

    private static void getKilometraje(Context ctx) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] row : db.listarKilometrajes()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",          Integer.parseInt(row[0]));
            item.put("patente",     row[1]);
            item.put("kilometraje", Integer.parseInt(row[2]));
            item.put("fecha",       row[3]);
            result.add(item);
        }
        ctx.json(result);
    }

    private static void createKilometraje(Context ctx) throws Exception {
        Map<String, Object> body = parseBodyObj(ctx);
        String patente = str(body, "patente");
        int km = Integer.parseInt(str(body, "kilometraje"));
        db.registrarKilometraje(patente, km);
        int total = db.obtenerKilometrajeTotalPorPatente(patente);
        boolean alerta = total >= UMBRAL_KM;
        ctx.status(201).json(Map.of("status", "registered", "total", total, "alerta", alerta));
    }

    private static void getKilometrajeTotalPorPatente(Context ctx) {
        String patente = ctx.pathParam("patente");
        int total = db.obtenerKilometrajeTotalPorPatente(patente);
        ctx.json(Map.of("patente", patente, "total", total, "alerta", total >= UMBRAL_KM));
    }

    // ──────────────────────────────────────────────────────────────────────
    // Mantenimiento
    // ──────────────────────────────────────────────────────────────────────

    private static void getMantenimiento(Context ctx) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] row : db.listarMantenimientos()) result.add(mantenimientoToMap(row));
        ctx.json(result);
    }

    private static void createMantenimiento(Context ctx) throws Exception {
        Map<String, String> body = parseBody(ctx);
        db.agregarMantenimiento(body.get("patente"), body.get("tipo"), body.get("descripcion"), body.get("fecha"));
        ctx.status(201).json(Map.of("status", "created"));
    }

    private static void updateMantenimiento(Context ctx) throws Exception {
        int id = pathInt(ctx, "id");
        Map<String, String> body = parseBody(ctx);
        db.actualizarMantenimiento(id, body.get("patente"), body.get("tipo"), body.get("descripcion"), body.get("fecha"));
        ctx.json(Map.of("status", "updated"));
    }

    private static void deleteMantenimiento(Context ctx) {
        int id = pathInt(ctx, "id");
        boolean ok = db.eliminarMantenimiento(id);
        if (ok) ctx.json(Map.of("status", "deleted"));
        else ctx.status(400).json(Map.of("error", "No se pudo eliminar el registro"));
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helpers: conversión String[] → Map
    // ──────────────────────────────────────────────────────────────────────

    private static Map<String, Object> conductorToMap(String[] row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",       Integer.parseInt(row[0]));
        m.put("nombre",   row[1]);
        m.put("rut",      row[2]);
        m.put("telefono", row[3]);
        m.put("email",    row[4]);
        return m;
    }

    private static Map<String, Object> camionToMap(String[] row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              Integer.parseInt(row[0]));
        m.put("patente",         row[1]);
        m.put("marca",           row[2]);
        m.put("modelo",          row[3]);
        m.put("anio",            Integer.parseInt(row[4]));
        m.put("conductorNombre", row[5]);
        m.put("conductorId",     row[6] != null && !row[6].isEmpty() ? Integer.parseInt(row[6]) : null);
        return m;
    }

    private static Map<String, Object> mantenimientoToMap(String[] row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          Integer.parseInt(row[0]));
        m.put("patente",     row[1]);
        m.put("tipo",        row[2]);
        m.put("descripcion", row[3]);
        m.put("fecha",       row[4]);
        return m;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helpers: parsing
    // ──────────────────────────────────────────────────────────────────────

    private static Map<String, String> parseBody(Context ctx) throws Exception {
        return json.readValue(ctx.body(), new TypeReference<Map<String, String>>() {});
    }

    private static Map<String, Object> parseBodyObj(Context ctx) throws Exception {
        return json.readValue(ctx.body(), new TypeReference<Map<String, Object>>() {});
    }

    private static int pathInt(Context ctx, String param) {
        return Integer.parseInt(ctx.pathParam(param));
    }

    private static String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private static Integer conductorIdOrNull(Map<String, Object> body) {
        Object val = body.get("conductorId");
        if (val == null || val.toString().isBlank()) return null;
        return Integer.parseInt(val.toString());
    }
}
