# FIX Engine - Demo

A continuación, la documentación estructurada y estilizada para el motor FIX usando QuickFIX. He organizado la información en tablas y bloques de código para facilitar su lectura y uso por parte del equipo técnico.

---

## Especificaciones Técnicas

### Stack Base

| Tecnología | Versión / Detalle |
| --- | --- |
| **Motor FIX** | QuickFIX/J 3.0.0 |
| **Protocolo** | FIX 4.2 |
| **Lenguaje** | Java 21 (compilado con JDK 22) |
| **Gestor de Dependencias** | Maven |
| **Sistema Operativo Destino** | Windows 11 |

### Mensajes FIX 4.2 Soportados

| Categoría | Mensaje | Tipo (MsgType) |
| --- | --- | --- |
| **Órdenes** | NewOrderSingle | `D` |
|  | OrderCancelRequest | `F` |
|  | OrderCancelReplaceRequest | `G` |
|  | OrderStatusRequest | `H` |
| **Execution Reports** | ExecutionReport | `8` |
|  | OrderCancelReject | `9` |
| **Market Data** | MarketDataRequest | `V` |
|  | Snapshot | `W` |
|  | Incremental | `X` |
|  | Reject | `Y` |
| **Securities** | SecurityDefinitionRequest | `c` |
|  | SecurityDefinition | `d` |

---

## Estructura del Entorno Productivo

El empaquetado final genera una estructura autocontenida de aproximadamente **167 MB** en el directorio `dist/FIXEngine/`.

```text
FIXEngine/
├── FIXEngine.exe              # Launcher nativo (jpackage, con JRE embebido)
├── FIXEngineService.exe       # WinSW v2.12.0 (wrapper servicio Windows)
├── FIXEngineService.xml       # Configuración del servicio
├── install-service.bat        # Instalar como servicio (Requiere Admin)
├── uninstall-service.bat      # Desinstalar servicio (Requiere Admin)
├── app/                       # JAR de la aplicación
├── runtime/                   # JRE embebido (no requiere Java en el SO)
├── config/                    # quickfix.cfg + logback.xml (editables)
├── logs/                      # Logs del servicio
├── store/                     # FIX session store
└── data/                      # Archivos de datos adicionales

```

---

## Guía de Despliegue

Sigue estos pasos para instalar el motor en el equipo destino:

1. **Copiar los binarios:** Transfiere la carpeta `dist/FIXEngine/` al directorio de instalación en el servidor destino (por ejemplo, `C:\Services\FIXEngine\`).
2. **Configurar el entorno:** Edita el archivo `config/quickfix.cfg` con los valores reales del entorno (productivo o pruebas). Asegúrate de actualizar los siguientes parámetros:
* `SenderCompID`
* `TargetCompID`
* `SocketConnectHost`
* `SocketConnectPort`


3. **Instalar y arrancar:** Ejecuta el archivo `install-service.bat` como **Administrador**. Esto registrará el servicio en Windows y lo iniciará automáticamente.

---

## Administración y Mantenimiento

### Comandos del Servicio

Todos los comandos deben ejecutarse desde una consola con privilegios de **Administrador**.

| Acción | Comando |
| --- | --- |
| **Registrar servicio** | `FIXEngineService.exe install` |
| **Iniciar servicio** | `FIXEngineService.exe start` |
| **Detener servicio** | `FIXEngineService.exe stop` |
| **Ver estado** | `FIXEngineService.exe status` |
| **Eliminar servicio** | `FIXEngineService.exe uninstall` |
| **Verificar vía OS** | `sc query FIXEngine` |

### Características de Operación

* **Arranque desatendido:** Inicia automáticamente con Windows (`startmode=Automatic`).
* **Alta disponibilidad:** Reinicio automático ante fallos del proceso (3 reintentos programados a los 10s, 20s y 60s).
* **Gestión de Logs:** Rotación automática por tamaño (Límite de 10MB, con retención de 8 archivos históricos).
* **Cierre seguro:** Shutdown tipo *graceful* con un timeout máximo de 30 segundos.
* **Independencia:** No requiere instalación de Java a nivel de sistema gracias al JRE embebido en la carpeta `runtime/`.

### Re-construcción (Build)

Para volver a generar el empaquetado distribuible desde el código fuente, ejecuta:

```bat
build-dist.bat

```

---

