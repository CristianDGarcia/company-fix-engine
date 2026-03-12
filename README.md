# FIX Engine - Demo

Below is the structured and stylized documentation for the FIX engine using QuickFIX. I have organized the information into tables and code blocks to facilitate reading and use by the technical team.

---

## Technical Specifications

### Base Stack

| Technology | Version / Detail |
| --- | --- |
| **FIX Engine** | QuickFIX/J 3.0.0 |
| **Protocol** | FIX 4.2 |
| **Language** | Java 21 (compiled with JDK 22) |
| **Dependency Manager** | Maven |
| **Target Operating System** | Windows 11 |

### Supported FIX 4.2 Messages

| Category | Message | Type (MsgType) |
| --- | --- | --- |
| **Orders** | NewOrderSingle | `D` |
|   | OrderCancelRequest | `F` |
|   | OrderCancelReplaceRequest | `G` |
|   | OrderStatusRequest | `H` |
| **Execution Reports** | ExecutionReport | `8` |
|   | OrderCancelReject | `9` |
| **Market Data** | MarketDataRequest | `V` |
|   | Snapshot | `W` |
|   | Incremental | `X` |
|   | Reject | `Y` |
| **Securities** | SecurityDefinitionRequest | `c` |
|   | SecurityDefinition | `d` |

---

## Production Environment Structure

The final packaging generates a self-contained structure of approximately **167 MB** in the `dist/FIXEngine/` directory.

```text
FIXEngine/
├── FIXEngine.exe              # Native launcher (jpackage, with embedded JRE)
├── FIXEngineService.exe       # WinSW v2.12.0 (Windows service wrapper)
├── FIXEngineService.xml       # Service configuration
├── install-service.bat        # Install as service (Requires Admin)
├── uninstall-service.bat      # Uninstall service (Requires Admin)
├── app/                       # Application JAR
├── runtime/                   # Embedded JRE (does not require Java on the OS)
├── config/                    # quickfix.cfg + logback.xml (editable)
├── logs/                      # Service logs
├── store/                     # FIX session store
└── data/                      # Additional data files


```

---

## Deployment Guide

Follow these steps to install the engine on the target machine:

1. **Copy the binaries:** Transfer the `dist/FIXEngine/` folder to the installation directory on the target server (for example, `C:\Services\FIXEngine\`).
2. **Configure the environment:** Edit the `config/quickfix.cfg` file with the actual values of the environment (production or testing). Make sure to update the following parameters:

* `SenderCompID`
* `TargetCompID`
* `SocketConnectHost`
* `SocketConnectPort`

3. **Install and start:** Run the `install-service.bat` file as an **Administrator**. This will register the service in Windows and start it automatically.

---

## Administration and Maintenance

### Service Commands

All commands must be executed from a console with **Administrator** privileges.

| Action | Command |
| --- | --- |
| **Register service** | `FIXEngineService.exe install` |
| **Start service** | `FIXEngineService.exe start` |
| **Stop service** | `FIXEngineService.exe stop` |
| **View status** | `FIXEngineService.exe status` |
| **Remove service** | `FIXEngineService.exe uninstall` |
| **Verify via OS** | `sc query FIXEngine` |

### Operational Characteristics

* **Unattended startup:** Starts automatically with Windows (`startmode=Automatic`).
* **High availability:** Automatic restart upon process failure (3 scheduled retries at 10s, 20s, and 60s).
* **Log Management:** Automatic size-based rotation (10MB limit, with retention of 8 historical files).
* **Safe shutdown:** Graceful shutdown with a maximum timeout of 30 seconds.
* **Independence:** Does not require a system-level Java installation thanks to the embedded JRE in the `runtime/` folder.

### Rebuilding (Build)

To regenerate the distributable package from the source code, run:

```bat
build-dist.bat


```
---
