# Informe Técnico de Diseño: Sistema Burok (Honduras)
**Plataforma de Formalización Empresarial y Gestión de Trámites PyME**

**Stack del Demo:** Java (Swing + JDBC) y MySQL  
**Patrón de Diseño:** MVC (Modelo - Vista - Controlador) + DAO Genérico  
**Referencia Estructural:** Patrón de diseño *AutoTechSolutions*  

---

## 1. Introducción y Enunciado General

El proyecto **Burok** responde a una problemática crítica documentada en el mercado hondureño: la formalización de una pequeña o mediana empresa requiere en promedio **1,933 horas**, convirtiéndose en la principal causa de la informalidad laboral y empresarial (>70%), pese a que la operación tributaria anual posterior toma únicamente **242 horas**.

El presente documento especifica la arquitectura, los **10 módulos funcionales**, el diseño detallado de **pantallas (Swing)**, las reglas de negocio y la estrategia de persistencia en **MySQL** para construir un demo funcional robusto sin escribir código fuente todavía.

---

## 2. Arquitectura del Software y Organización en Paquetes

Se adopta estrictamente la separación de responsabilidades del patrón **MVC**, incorporando una capa de acceso a datos (**DAO / Repositorio**) conectada a **MySQL**:

* **`main`**: Punto de entrada (`Main.java`). Configura el tema visual Nimbus, valida la disponibilidad de la base de datos `burok_db` y lanza `VentanaPrincipal` en el *Event Dispatch Thread* (EDT).
* **`conexion`**: Clase `ConexionMySQL.java` con patrón Singleton ligero para proveer conexiones JDBC limpias y parametrizadas.
* **`modelo`**: Entidades de dominio (`Emprendedor`, `Empresa`, `TramiteCatalogo`, `RequisitoDocumento`, `ExpedienteFormalizacion`, `DetalleTramiteExpediente`, `LiquidacionCostos` y el enum `EstadoTramite`).
* **`repositorio`**: `RepositorioDAO<T>` con implementaciones MySQL (`EmprendedorDAO`, `EmpresaDAO`, `TramiteDAO`, `ExpedienteDAO`, `LiquidacionDAO`) e integración de `Stack` para deshacer la última acción de registro.
* **`controlador`**: `BurokControlador.java` centraliza las validaciones (DNI, códigos CIIU, montos en Lempiras), la generación de rutas críticas, el control de documentos únicos y el lanzamiento de hilos.
* **`vista`**: `VentanaPrincipal.java` (`JTabbedPane` con 7 pestañas operativas) y `CanvasReporte2D.java` (renderizado manual con `Graphics2D`).
* **`excepciones`**: `DocumentoFaltanteException`, `EmpresaNoEncontradaException` y `PresupuestoInsuficienteException`.
* **`hilos`**: `ProcesoVentanillaHilo.java` (`Runnable`) para simular el procesamiento secuencial de ventanillas y la activación del Silencio Administrativo Positivo.

---

## 3. Especificación Detallada de Pantallas (Interfaz Swing)

La interfaz principal (`VentanaPrincipal`) utiliza un `JTabbedPane` superior con 7 pestañas que cubren el flujo completo de punta a punta:

### Pantalla 1: Gestión de Emprendedores (Módulo 1)
* **Panel Superior (Formulario):** Campos `Código` (`EMP-XXX`), `Nombre Completo`, `DNI / Identidad` (formato `0801-XXXX-XXXXX`), `Teléfono`, `Correo`, `Dirección`.
* **Barra de Acciones:** Botones `Registrar`, `Buscar`, `Actualizar`, `Eliminar`, `Deshacer (Undo)`, `Limpiar`.
* **Panel Inferior (`JTable`):** Listado en tiempo real desde la tabla `emprendedores` de MySQL.
* **Validaciones:** Campos vacíos, longitud de DNI, prevención de códigos duplicados.

### Pantalla 2: Empresas y Clasificación CIIU (Módulo 2)
* **Panel Superior (Formulario):** `Código Empresa` (`PYM-XXX`), `Nombre Comercial`, `Propietario` (`JComboBox` cargado desde `Emprendedor`), `Forma Jurídica` (`Comerciante Individual`, `S. de R.L.`, `S.A.`), `Código CIIU` (`JComboBox` con actividades económicas), `Categoría de Riesgo` (1 a 4), `Municipio` (`Distrito Central`, `San Pedro Sula`, `Choloma`, `Villanueva`, `La Ceiba`), `Capital Semilla (L.)`.
* **Filtros:** Búsqueda rápida por código de empresa y filtro desplegable por propietario.
* **Panel Inferior (`JTable`):** Muestra empresas registradas con su nivel de riesgo y municipio asignado.

### Pantalla 3: Catálogo Regulatorio Nacional (Módulo 3)
* **Contenido:** Tabla maestra (`JTable`) sincronizada con `tramites_catalogo` en MySQL que lista los trámites de:
  * **IP / Notaría:** Escritura Pública y Registro Mercantil (~120 hrs tradicionales / ~24 hrs Burok).
  * **SAR:** RTN Numérico, Oficina Virtual y CAI de Facturación (~180 hrs / ~16 hrs).
  * **Alcaldía Municipal:** Compatibilidad de Uso de Suelo, Inspección de Bomberos y Permiso de Operación (~750 hrs / ~32 hrs).
  * **MiAmbiente / ARSA:** Constancia Categoría 1 y Licencia Sanitaria (~883 hrs / ~40 hrs).
* **Acciones:** Permite agregar o ajustar tasas municipales (`costo_oficial_lps > 0`) y plazos SLA (días hábiles) según el Plan de Arbitrios vigente.

### Pantalla 4: Expediente Único Digital (Módulo 4)
* **Propósito:** Evitar que el emprendedor presente el mismo documento ante 4 instituciones distintas.
* **Componentes:** Selector de `Empresa`, checklist interactivo de documentos base (`DNI`, `Escritura`, `RTN`, `Contrato Arrendamiento`, `Croquis de Ubicación`), indicador visual tipo semáforo (`COMPLETO` en verde / `INCOMPLETO` en rojo).
* **Regla de Negocio:** Un documento validado aquí queda disponible para todos los trámites del expediente.

### Pantalla 5: Orquestador de Ruta Crítica y Simulador Multihilo (Módulos 5 y 10)
* **Panel Superior:** Selección de `Empresa`, botón **"Generar Ruta Crítica Automática"** (calcula trámites aplicables según CIIU y Categoría de Riesgo, mostrando la comparativa `1,933 hrs vs. ~112 hrs`).
* **Panel Central (`JTable`):** Lista de trámites del expediente con columnas: `Entidad`, `Trámite`, `Días Transcurridos`, `Plazo SLA`, `Estado`, `Silencio Positivo`.
* **Panel de Concurrencia:**
  * Botón **"Simular Ventanillas y SLA (>= 5 seg)"**.
  * `JProgressBar` y etiqueta de estado en vivo (actualizados con `SwingUtilities.invokeLater`).
  * **Comportamiento del Hilo:** Recorre las etapas en segundo plano (> 5 segundos). Si un trámite de **Categoría 1** supera su plazo SLA (15 días simulados), el sistema lo marca automáticamente como `SILENCIO_POSITIVO` y habilita la operación provisoria. Bloquea ejecuciones simultáneas sobre el mismo expediente.

### Pantalla 6: Presupuesto y Liquidación de Tasas (Módulos 6 y 9)
* **Panel de Selección:** `JComboBox` que filtra únicamente expedientes en estado `APROBADO` o `SILENCIO_POSITIVO`.
* **Desglose Automático:**
  * Subtotal Tasas Municipales (L.)
  * Subtotal Gobierno Central — IP / SAR / ARSA / MiAmbiente (L.)
  * Timbres y Honorarios Estimados (L.)
  * **Total a Pagar (L.)** y porcentaje que representa respecto al Capital Semilla de la empresa.
* **Caja de Liquidación:** Campo `Monto Recibido (L.)`, botón `Emitir Liquidación`. Si el monto es menor al total, lanza `PresupuestoInsuficienteException` y muestra el faltante sin cerrar la aplicación.

### Pantalla 7: Analítica y Reportes Java 2D (Módulo 7)
* **Componente:** `CanvasReporte2D` (`JPanel` con `paintComponent(Graphics g)` y `Graphics2D`).
* **Gráfico 1 (Barras Comparativas):** Horas promedio tradicionales en Honduras (1,933 hrs) vs. Horas optimizadas con Burok por entidad (IP/Notaría, SAR, Alcaldía, MiAmbiente/ARSA).
* **Gráfico 2 (Barras de Estado):** Cantidad de expedientes almacenados en MySQL agrupados por `PENDIENTE`, `EN_REVISION`, `SILENCIO_POSITIVO` y `APROBADO`.

---

## 4. Cumplimiento de los 10 Módulos Funcionales

1. **Módulo 1 (Emprendedores):** CRUD MySQL + validaciones de identidad y duplicados.
2. **Módulo 2 (Empresas y CIIU):** Asociación relacional a emprendedor, clasificación de riesgo (1–4) y municipio.
3. **Módulo 3 (Catálogo Regulatorio):** Base parametrizable de trámites hondureños con costos en Lempiras y tiempos SLA.
4. **Módulo 4 (Expediente Único):** Reutilización documental cero-duplicidad y validación previa.
5. **Módulo 5 (Ruta Crítica):** Cálculo automático de secuencia, horas ahorradas y control de estados.
6. **Módulo 6 (Liquidación Financiera):** Desglose de tasas oficiales y cálculo de impacto sobre capital semilla.
7. **Módulo 7 (Reportes Java 2D):** Gráficos manuales con `Graphics2D` alimentados por consultas reales de MySQL.
8. **Módulo 8 (Genéricos y MySQL DAO):** Interfaz genérica `RepositorioDAO<T>` con JDBC y `Stack` para Undo en sesión.
9. **Módulo 9 (Excepciones Personalizadas):** Manejo controlado con `try-catch` y `JOptionPane` para `DocumentoFaltanteException`, `EmpresaNoEncontradaException` y `PresupuestoInsuficienteException`.
10. **Módulo 10 (Multihilos):** `ProcesoVentanillaHilo` (`Runnable`) para simular ventanillas y Silencio Administrativo Positivo sin bloquear el EDT.

---

## 5. Conclusión de Diseño

Siguiendo exactamente la disciplina arquitectónica de *AutoTechSolutions* (MVC limpio, genéricos, excepciones de dominio, concurrencia segura en Swing y Java 2D nativo) e incorporando **MySQL** como motor relacional, el proyecto **Burok** queda completamente especificado para iniciar su construcción en la Fase 2 como un demo sólido, auditable y alineado a la realidad regulatoria de Honduras.
