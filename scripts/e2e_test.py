#!/usr/bin/env python3
"""
Pruebas end-to-end de ParkFlow360.

Requiere el backend corriendo en http://localhost:8080 con la base de datos
ya migrada (Flyway). Ejecuta el flujo completo: autenticación, permisos por rol,
planos con generación de espacios, reservas y pago en línea con Wompi.

Uso:
    python scripts/e2e_test.py
    python scripts/e2e_test.py --base http://localhost:8080

Salida: 0 si todo pasa, 1 si hay fallos.
"""

import argparse
import hashlib
import json
import random
import sys
import urllib.error
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"

# Datos únicos por ejecución para poder repetir las pruebas sin limpiar la base
RUN = random.randint(1000, 9999)
PLACA = f"E2E{RUN}"

# Llaves de Wompi en modo sandbox (pruebas)
WOMPI_INTEGRITY_SECRET = "test_integrity_js76z4Vt9GcjUPEuuRUV0qUU3XZoP8t6"
WOMPI_EVENTS_SECRET = "test_events_VpWLpHMkSR2Nw4u0dxSaucRlnsiFtzql"
WOMPI_PUBLIC_KEY = "pub_test_A0guUdUKlpxDDPedplwYFjnHCepv3eCi"

ADMIN_EMAIL = "admin@parkflow360.com"
ADMIN_PASSWORD = "admin123"
# Usuario cliente único por ejecución: el script no deja rastro al terminar
CLIENT_EMAIL = f"cliente.e2e.{RUN}@parkflow360.com"
CLIENT_PASSWORD = "cliente123"

results = []


def call(method, path, token=None, body=None, raw_body=None):
    url = f"{BASE}{path}"
    data = None
    headers = {"Content-Type": "application/json"}
    if raw_body is not None:
        data = raw_body.encode("utf-8") if isinstance(raw_body, str) else raw_body
    elif body is not None:
        data = json.dumps(body).encode("utf-8")
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            text = resp.read().decode("utf-8")
            return resp.status, (json.loads(text) if text.strip() else None)
    except urllib.error.HTTPError as e:
        text = e.read().decode("utf-8")
        try:
            return e.code, (json.loads(text) if text.strip() else None)
        except json.JSONDecodeError:
            return e.code, text
    except Exception as e:  # noqa: BLE001
        return 0, str(e)


def check(name, condition, detail=""):
    results.append((name, bool(condition), detail))
    icon = "PASS" if condition else "FAIL"
    line = f"[{icon}] {name}"
    if detail and not condition:
        line += f"  ->  {detail}"
    print(line)


def sha256_hex(text):
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def main():
    global BASE
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default=BASE)
    args = parser.parse_args()
    BASE = args.base.rstrip("/")

    print(f"\n=== ParkFlow360 · Pruebas E2E contra {BASE} (ejecucion {RUN}) ===\n")

    # ---------------------------------------------------------------
    print("--- Autenticacion ---")
    st, admin = call("POST", "/api/auth/login",
                     body={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD})
    check("Login del administrador", st == 200 and admin.get("rol") == "ADMIN",
          f"status={st} body={admin}")
    if st != 200:
        print("\nNo se pudo iniciar sesion con el admin. Revisa la migracion V13.")
        return 1
    admin_token = admin["token"]

    st, _ = call("POST", "/api/auth/login",
                 body={"email": ADMIN_EMAIL, "password": "incorrecta"})
    check("Login con contrasena incorrecta devuelve 400", st == 400, f"status={st}")

    st, _ = call("POST", "/api/auth/google", body={"credential": "token-falso"})
    check("Login con Google rechaza un token falso", st == 400, f"status={st}")

    # ---------------------------------------------------------------
    print("\n--- Permisos: el administrador NO registra vehiculos ---")
    st, _ = call("GET", "/api/vehiculos", token=admin_token)
    check("Admin SI puede leer el catalogo de vehiculos", st == 200, f"status={st}")

    st, _ = call("POST", "/api/vehiculos", token=admin_token,
                 body={"placa": "ADM001", "tipo": "CARRO"})
    check("Admin NO puede crear vehiculos (403)", st == 403, f"status={st}")

    st, _ = call("PUT", "/api/vehiculos/1", token=admin_token,
                 body={"placa": "ADM001", "tipo": "CARRO"})
    check("Admin NO puede editar vehiculos (403)", st == 403, f"status={st}")

    st, _ = call("DELETE", "/api/vehiculos/1", token=admin_token)
    check("Admin NO puede eliminar vehiculos (403)", st == 403, f"status={st}")

    # ---------------------------------------------------------------
    print("\n--- El cliente administra sus vehiculos ---")
    st, _ = call("POST", "/api/usuarios", token=admin_token, body={
        "email": CLIENT_EMAIL, "password": CLIENT_PASSWORD,
        "rol": "USUARIO", "nombre": "Cliente E2E"
    })
    check("Admin crea el usuario cliente", st in (201, 400), f"status={st}")

    st, client = call("POST", "/api/auth/login",
                      body={"email": CLIENT_EMAIL, "password": CLIENT_PASSWORD})
    check("Login del cliente", st == 200 and client.get("rol") == "USUARIO",
          f"status={st} body={client}")
    if st != 200:
        return 1
    client_token = client["token"]

    st, _ = call("GET", "/api/vehiculos", token=client_token)
    check("Cliente NO puede leer todo el catalogo (403)", st == 403, f"status={st}")

    st, vehiculo = call("POST", "/api/vehiculos/mios", token=client_token,
                        body={"placa": PLACA, "tipo": "CARRO",
                              "color": "Rojo", "modelo": "Test E2E"})
    check("Cliente registra su vehiculo", st == 201, f"status={st} body={vehiculo}")
    if st != 201:
        return 1
    vehiculo_id = vehiculo["id"]
    check("La placa se guarda en mayusculas", vehiculo["placa"] == PLACA,
          f"placa={vehiculo['placa']}")

    st, _ = call("POST", "/api/vehiculos/mios", token=client_token,
                 body={"placa": PLACA, "tipo": "CARRO"})
    check("Placa duplicada se rechaza (400)", st == 400, f"status={st}")

    st, body = call("PUT", f"/api/vehiculos/mios/{vehiculo_id}", token=client_token,
                    body={"placa": PLACA, "tipo": "CAMIONETA", "modelo": "Editado"})
    check("Cliente edita su vehiculo", st == 200 and body.get("tipo") == "CAMIONETA",
          f"status={st} body={body}")

    st, body = call("GET", "/api/vehiculos/mios", token=client_token)
    check("El cliente ve solo su vehiculo",
          st == 200 and isinstance(body, list) and len(body) == 1,
          f"status={st} total={len(body) if isinstance(body, list) else body}")

    # ---------------------------------------------------------------
    print("\n--- Planos y generacion de espacios en bloque ---")
    st, tarifa = call("POST", "/api/tarifas", token=admin_token,
                      body={"nombre": f"Tarifa E2E Hora {RUN}", "tipo": "HORA",
                            "valor": 5000, "duracion": "1 hora"})
    check("Admin crea una tarifa", st == 201, f"status={st} body={tarifa}")
    if st != 201:
        return 1
    tarifa_id = tarifa["id"]

    st, plano = call("POST", "/api/planos", token=admin_token,
                     body={"nombre": f"Plano E2E {RUN}", "piso": 1,
                           "filas": 2, "columnas": 3})
    check("Admin crea un plano", st == 201, f"status={st} body={plano}")
    if st != 201:
        return 1
    plano_id = plano["id"]

    prefijo = f"P{RUN}"
    st, espacios = call("POST", f"/api/planos/{plano_id}/generar-espacios",
                        token=admin_token,
                        body={"prefijo": prefijo, "tipo": "CARRO", "estadoInicial": "LIBRE"})
    check("Genera 6 espacios (2x3)", st == 201 and isinstance(espacios, list) and len(espacios) == 6,
          f"status={st} total={len(espacios) if isinstance(espacios, list) else espacios}")

    st, espacios2 = call("POST", f"/api/planos/{plano_id}/generar-espacios",
                         token=admin_token,
                         body={"prefijo": prefijo, "tipo": "CARRO", "estadoInicial": "LIBRE"})
    check("No genera espacios duplicados al repetir",
          st == 201 and isinstance(espacios2, list) and len(espacios2) == 0,
          f"status={st} total={len(espacios2) if isinstance(espacios2, list) else espacios2}")

    st, _ = call("GET", f"/api/planos/{plano_id}/espacios")
    check("Sin token el mapa responde 403", st == 403, f"status={st}")

    st, mapa = call("GET", f"/api/planos/{plano_id}/espacios", token=admin_token)
    check("El mapa devuelve los 6 espacios",
          st == 200 and isinstance(mapa, list) and len(mapa) == 6,
          f"status={st} total={len(mapa) if isinstance(mapa, list) else mapa}")
    if not (isinstance(mapa, list) and mapa):
        return 1
    check("Los espacios tienen fila y columna",
          all(e.get("fila") and e.get("columna") for e in mapa), f"mapa={mapa}")

    st, _ = call("GET", f"/api/planos/{plano_id}/espacios", token=client_token)
    check("El cliente tambien puede consultar el plano", st == 200, f"status={st}")

    st, _ = call("POST", f"/api/planos/{plano_id}/generar-espacios", token=client_token,
                 body={"tipo": "CARRO"})
    check("El cliente NO puede generar espacios (403)", st == 403, f"status={st}")

    # ---------------------------------------------------------------
    print("\n--- Reserva del cliente ---")
    libres = [e for e in mapa if e["estado"] == "LIBRE"]
    espacio = libres[0]
    otro = libres[1]

    st, reserva = call("POST", "/api/ingresos/reservar", token=client_token, body={
        "placa": PLACA, "categoria": "CARRO", "vehiculoId": vehiculo_id,
        "espacioId": espacio["id"], "espacioNumero": espacio["numero"],
        "tarifaId": tarifa_id, "tarifaValor": 5000, "modalidad": "POR HORAS"
    })
    check("Cliente reserva un espacio", st == 201, f"status={st} body={reserva}")
    if st != 201:
        return 1
    check("La reserva queda en estado RESERVADO", reserva["estado"] == "RESERVADO",
          f"estado={reserva['estado']}")

    st, _ = call("POST", "/api/ingresos/reservar", token=admin_token, body={
        "placa": "ABC999", "categoria": "CARRO", "espacioId": otro["id"]
    })
    check("El administrador NO puede reservar (403)", st == 403, f"status={st}")

    st, _ = call("POST", "/api/ingresos/reservar", token=client_token, body={
        "placa": PLACA, "categoria": "CARRO", "espacioId": otro["id"]
    })
    check("No se puede reservar dos veces el mismo vehiculo (400)", st == 400, f"status={st}")

    st, esp = call("GET", f"/api/espacios/{espacio['id']}", token=admin_token)
    check("El espacio reservado cambia a RESERVADO", esp.get("estado") == "RESERVADO",
          f"estado={esp.get('estado')}")

    # ---------------------------------------------------------------
    print("\n--- Pago en linea con Wompi (sandbox) ---")
    st, pago = call("POST", "/api/pagos/online", token=client_token,
                    body={"valor": 5000, "ingresoId": reserva["id"],
                          "vehiculoId": vehiculo_id, "espacioId": espacio["id"],
                          "tarifaId": tarifa_id})
    check("Cliente inicia el pago en linea", st == 201, f"status={st} body={pago}")
    if st != 201:
        return 1

    checkout = pago["checkoutUrl"]
    check("El pago inicia en PENDIENTE", pago["estado"] == "PENDIENTE",
          f"estado={pago['estado']}")
    check("La URL usa la llave publica de sandbox", WOMPI_PUBLIC_KEY in checkout)
    check("La URL apunta al checkout de Wompi",
          checkout.startswith("https://checkout.wompi.co/p/"), checkout[:70])

    # Verificamos la firma de integridad contra la formula oficial de Wompi
    qs = urllib.parse.urlparse(checkout).query
    params = dict(urllib.parse.parse_qsl(qs, keep_blank_values=True))
    referencia = params.get("reference", "")
    centavos = params.get("amount-in-cents", "")
    moneda = params.get("currency", "")
    firma = params.get("signature:integrity", "")
    esperada = sha256_hex(f"{referencia}{centavos}{moneda}{WOMPI_INTEGRITY_SECRET}")
    check("El valor se envia en centavos (5000 COP -> 500000)", centavos == "500000",
          f"amount-in-cents={centavos}")
    check("La firma de integridad es correcta", firma == esperada,
          f"recibida={firma} esperada={esperada}")

    st, _ = call("POST", "/api/pagos/online", token=admin_token, body={"valor": 1000})
    check("El administrador NO puede iniciar pagos de cliente (403)", st == 403, f"status={st}")

    # --- Webhook ---
    ts = "1700000000000"
    tx = {"id": "TRANS_E2E", "reference": referencia,
          "status": "APPROVED", "amount_in_cents": 500000}
    props = ["transaction.id", "transaction.status", "transaction.amount_in_cents"]
    concatenado = tx["id"] + tx["status"] + str(tx["amount_in_cents"]) + ts
    checksum_ok = sha256_hex(concatenado + WOMPI_EVENTS_SECRET)

    evento_malo = json.dumps({
        "event": "transaction.updated", "data": {"transaction": tx}, "timestamp": ts,
        "signature": {"checksum": "firma-invalida", "properties": props}
    })
    st, _ = call("POST", "/api/pagos/wompi/webhook", raw_body=evento_malo)
    check("El webhook rechaza una firma invalida (400)", st == 400, f"status={st}")

    evento_ok = json.dumps({
        "event": "transaction.updated", "data": {"transaction": tx}, "timestamp": ts,
        "signature": {"checksum": checksum_ok, "properties": props}
    })
    st, _ = call("POST", "/api/pagos/wompi/webhook", raw_body=evento_ok)
    check("El webhook acepta la firma valida", st == 200, f"status={st}")

    st, pagos = call("GET", f"/api/pagos/{pago['pagoId']}", token=admin_token)
    check("El pago queda en estado PAGADO", st == 200 and pagos.get("estado") == "PAGADO",
          f"estado={pagos.get('estado')}")
    check("Se guarda el id de transaccion de Wompi",
          pagos.get("wompiTransactionId") == "TRANS_E2E",
          f"wompiTransactionId={pagos.get('wompiTransactionId')}")

    st, mis_pagos = call("GET", "/api/pagos/mios", token=client_token)
    check("El cliente ve su historial de pagos",
          st == 200 and isinstance(mis_pagos, list) and len(mis_pagos) >= 1, f"status={st}")

    # ---------------------------------------------------------------
    print("\n--- Borrado de plano con movimientos ---")
    st, body = call("DELETE", f"/api/planos/{plano_id}", token=admin_token)
    check("No se elimina un plano con ingresos/pagos (400)", st == 400,
          f"status={st} body={body}")

    # ---------------------------------------------------------------
    print("\n--- Borrado con movimientos asociados ---")
    st, body = call("DELETE", f"/api/vehiculos/mios/{vehiculo_id}", token=client_token)
    check("No se elimina un vehiculo con pagos (400)", st == 400, f"status={st} body={body}")

    # Limpieza: el admin retira el pago y el ingreso; luego el cliente borra su vehiculo
    call("DELETE", f"/api/pagos/{pago['pagoId']}", token=admin_token)
    call("DELETE", f"/api/ingresos/{reserva['id']}", token=admin_token)

    st, _ = call("DELETE", f"/api/vehiculos/mios/{vehiculo_id}", token=client_token)
    check("Cliente elimina su vehiculo cuando no tiene movimientos", st == 204, f"status={st}")
    st, _ = call("DELETE", f"/api/vehiculos/mios/{vehiculo_id}", token=client_token)
    check("No se puede volver a eliminar (404)", st == 404, f"status={st}")

    st, _ = call("DELETE", f"/api/planos/{plano_id}", token=admin_token)
    check("El plano se elimina cuando ya no tiene movimientos", st == 204, f"status={st}")

    print("\n--- Limpieza final ---")
    st, _ = call("DELETE", f"/api/tarifas/{tarifa_id}", token=admin_token)
    check("Admin elimina la tarifa de prueba", st == 204, f"status={st}")

    st, usuarios = call("GET", "/api/usuarios", token=admin_token)
    if st == 200 and isinstance(usuarios, list):
        for u in usuarios:
            if u.get("email") == CLIENT_EMAIL:
                st2, _ = call("DELETE", f"/api/usuarios/{u['id']}", token=admin_token)
                check("Admin elimina el usuario cliente de prueba", st2 == 204, f"status={st2}")

    # ---------------------------------------------------------------
    ok = sum(1 for _, passed, _ in results if passed)
    total = len(results)
    print(f"\n=== Resultado: {ok}/{total} pruebas superadas ===")
    if ok != total:
        print("\nFallaron:")
        for name, passed, detail in results:
            if not passed:
                print(f"  - {name}: {detail}")
        return 1
    print("Todo correcto.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
