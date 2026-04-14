# Guía de procesos — Hirata Flota API

Referencia rápida para apagar y arrancar el proyecto en Linux.
No modifica ningún archivo de código ni configuración.

---

## Apagar todo (kill)

### Backend Java (puerto 7000)

```bash
pkill -f "hirata-flota-api.jar"

# Alternativa por puerto
sudo fuser -k 7000/tcp

# Verificar que paró
lsof -i :7000
```

### Frontend Vite — solo modo dev (puerto 5173)

```bash
pkill -f "vite"

# Alternativa por puerto
sudo fuser -k 5173/tcp
```

### MySQL

```bash
sudo systemctl stop mysql

# Verificar que paró
sudo systemctl status mysql
```

> phpMyAdmin no tiene proceso propio — al parar MySQL deja de funcionar automáticamente.

---

## Arrancar todo

### 1. MySQL

```bash
sudo systemctl start mysql
sudo systemctl status mysql   # debe mostrar "active (running)"
```

### 2. (Opcional) phpMyAdmin

Abrir en el navegador: `http://localhost/phpmyadmin`

### 3. Backend Java

```bash
cd /media/datos/repos/hirata-icompetencias1
./run-web.sh
```

Disponible en: `http://localhost:7000`

### 4. Frontend Vite (solo modo dev) — terminal aparte

```bash
cd /media/datos/repos/hirata-icompetencias1/frontend
npm run dev
```

Disponible en: `http://localhost:5173`

---

## Resumen de puertos

| Servicio        | Puerto | Comando kill                        |
|-----------------|--------|-------------------------------------|
| Backend Java    | 7000   | `pkill -f hirata-flota-api.jar`     |
| Frontend Vite   | 5173   | `pkill -f vite`                     |
| MySQL           | 3306   | `sudo systemctl stop mysql`         |

---

## Scripts del proyecto

| Script            | Qué hace                                              |
|-------------------|-------------------------------------------------------|
| `./compile-web.sh`| Compila el backend Java y genera el fat-jar           |
| `./run-web.sh`    | Lanza la API REST (requiere fat-jar compilado)        |
| `./run-swing.sh`  | Compila y lanza la interfaz Swing legacy              |
