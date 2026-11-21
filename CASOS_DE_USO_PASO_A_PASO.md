# Documentación Paso a Paso de Casos de Uso — TrámiteFácil HN (Burok)

Este documento describe el flujo operativo paso a paso de cada caso de uso implementado en el sistema **TrámiteFácil HN (Burok)**, detallando la interacción en la interfaz gráfica (**Java Swing**), la lógica de negocio en `BurokControlador`, las excepciones controladas y la persistencia en **MySQL (`burok_db`)**.

---

## CU-01: Inicio del Sistema y Verificación de Conexión MySQL

* **Actor:** Emprendedor / Gestor Contable / Administrador.
* **Clases involucradas:** `main.Main`, `vista.VentanaPrincipal`, `conexion.ConexionMySQL`, `controlador.BurokControlador`.

### Flujo Paso a Paso
1. El usuario ejecuta la aplicación (`main.Main`).
2. `Main.main()` configura el Look & Feel **Nimbus** y lanza `VentanaPrincipal` dentro del *Event Dispatch Thread* (EDT) mediante `SwingUtilities.invokeLater()`.
3. `VentanaPrincipal` construye la barra superior y llama a `ConexionMySQL.verificarConexion()` hacia `jdbc:mysql://localhost:3306/burok_db`:
   * Si la conexión es exitosa, la barra superior muestra **`● MySQL Conectado (localhost:3306 / burok_db)`** en verde.
   * Si falla, muestra **`● MySQL Desconectado`** en rojo.
4. Se ejecuta `cargarTodosLosDatos()`, consultando en MySQL los emprendedores, empresas, catálogo de trámites, expedientes, documentos del expediente único y liquidaciones, poblando todas las tablas y selectores.
5. Se muestra por defecto la vista **Panel principal**.

---

## CU-02: Consulta del Panel Principal, Filtros y Búsqueda de Empresa

* **Actor:** Emprendedor / Gestor.
* **Vista:** Pestaña lateral **Panel principal**.
* **Clases involucradas:** `vista.VentanaPrincipal`, `controlador.BurokControlador`, `excepciones.EmpresaNoEncontradaException`.

### Flujo Paso a Paso
1. El usuario visualiza en la parte superior las **4 tarjetas métricas** calculadas en tiempo real desde MySQL:
   * *Empresas registradas*
   * *Trámites pendientes*
   * *Trámites completados*
   * *Horas estimadas Burok* (`-92% tiempo` frente a 1,933 horas)
2. **Filtrado por municipio:** El usuario selecciona un municipio en el selector desplegable (`Distrito Central`, `San Pedro Sula`, `Choloma`, `Villanueva (Cortés)`, `La Ceiba / Roatán` o `Todos los municipios`). La tabla *Lista de Expedientes de Empresas* se actualiza automáticamente mostrando únicamente las empresas de ese municipio junto con su porcentaje de avance y estado global.
3. **Búsqueda por código de empresa:**
   * El usuario escribe el código (ej. `PYM-001`) en el campo de búsqueda y presiona **`Buscar Código`**.
   * `BurokControlador.buscarEmpresa(codigo)` consulta MySQL vía `EmpresaDAO`.
   * **Resultado exitoso:** Muestra un cuadro de diálogo con el nombre comercial, municipio y RTN.
   * **Flujo alternativo (Error controlado):** Si el código no existe, se lanza `EmpresaNoEncontradaException` y se muestra un aviso `JOptionPane` sin interrumpir la aplicación.

---

## CU-03: Registro de Emprendedor y Función Deshacer (Undo)

* **Actor:** Gestor / Emprendedor.
* **Vista:** Pestaña lateral **Mis empresas** (Panel izquierdo: *1. Emprendedores / Representantes*).
* **Clases involucradas:** `controlador.BurokControlador`, `repositorio.EmprendedorDAO`, `modelo.Emprendedor`.

### Flujo Paso a Paso
1. El usuario ingresa los datos en el formulario: `Código Emprendedor` (ej. `EMP-003`), `Nombre Completo`, `DNI (RNP)`, `Teléfono`, `Correo` y `Dirección`.
2. Presiona **`Registrar Emprendedor`**.
3. `BurokControlador.registrarEmprendedor()` valida que `Código`, `Nombre completo` y `DNI` no estén vacíos y verifica en MySQL que el código no esté duplicado (`emprendedorDAO.existe(codigo)`).
4. `EmprendedorDAO.guardar()` ejecuta el `INSERT INTO emprendedores` en MySQL y apila el código recién insertado en `historialCodigosInsertados` (`Stack<String>`).
5. Se recargan las tablas y el selector de propietarios en el formulario de empresas.
6. **Flujo alternativo — Deshacer (`Undo`):**
   * Si el usuario presiona **`Deshacer (Undo)`**, `EmprendedorDAO.deshacerUltimoRegistro()` extrae el último código de la pila `Stack` y ejecuta `DELETE FROM emprendedores WHERE codigo = ?`, sincronizando inmediatamente la vista y MySQL.

---

## CU-04: Registro de Empresa PyME (CIIU, Riesgo y Municipio) y Deshacer

* **Actor:** Gestor / Emprendedor.
* **Vista:** Pestaña lateral **Mis empresas** (Panel derecho: *2. Empresas y Clasificación CIIU*).
* **Clases involucradas:** `controlador.BurokControlador`, `repositorio.EmpresaDAO`, `repositorio.ExpedienteDAO`, `modelo.Empresa`.

### Flujo Paso a Paso
1. El usuario completa el formulario de empresa:
   * `Código Empresa` (ej. `PYM-004`), `Nombre Comercial`, `RTN`.
   * `Propietario` (seleccionado del `JComboBox` de emprendedores registrados).
   * `Forma Jurídica` (`Comerciante Individual`, `Sociedad de Resp. Ltda.`, `Sociedad Anónima (S.A.)`).
   * `Actividad CIIU` (ej. `5610 - Restaurantes y Cafés`).
   * `Cat. Riesgo (1-4)` y `Municipio`.
   * `Capital Semilla (L.)` (debe ser numérico y mayor a `L. 0.00`).
2. Presiona **`Registrar Empresa`**.
3. `BurokControlador.registrarEmpresa()` valida campos obligatorios, verifica que no exista código duplicado e inserta la empresa en la tabla `empresas` de MySQL mediante `EmpresaDAO.guardar()`.
4. Automáticamente, `ExpedienteDAO.inicializarDocumentosBaseEmpresa()` crea en la tabla `expediente_documentos` los **4 documentos maestros** base para la empresa (`DOC-01` DNI, `DOC-02` Escritura Pública, `DOC-03` Contrato de Arrendamiento, `DOC-04` Croquis Catastral), evitando que el usuario deba cargarlos por separado para cada institución.
5. **Flujo alternativo — Deshacer (`Undo`):** Al presionar **`Deshacer (Undo)`** en el panel de empresas, `EmpresaDAO.deshacerUltimoRegistro()` extrae el último código de su `Stack` y elimina el registro de MySQL (eliminando en cascada sus documentos base).

---

## CU-05: Gestión del Expediente Único Digital (Cero Duplicidad)

* **Actor:** Gestor / Emprendedor.
* **Vista:** Pestaña lateral **Trámites y Exp. Único**.
* **Clases involucradas:** `controlador.BurokControlador`, `repositorio.ExpedienteDAO`, `modelo.RequisitoDocumento`, `excepciones.DocumentoFaltanteException`.

### Flujo Paso a Paso
1. En la parte superior derecha de la vista **Trámites y Exp. Único**, el usuario selecciona la empresa en el combo **`Empresa activa`**.
2. El panel **Expediente Único Digital** consulta la tabla `expediente_documentos` de MySQL y lista los documentos requeridos con su emisor (`RNP`, `Notario / IP`, `Privado`, `Propietario`) y estado (`VALIDADO ✓` o `PENDIENTE`).
3. El indicador superior muestra el conteo (ej. `2 / 4 validados` en color ámbar si faltan documentos, o `4 / 4 validados` en verde si está completo).
4. Para validar o invalidar un documento, el usuario selecciona una fila en la tabla y presiona **`Validar / Invalidar Seleccionado`**.
5. `BurokControlador.cambiarEstadoDocumento()` actualiza el campo `validado` en MySQL y refresca el indicador.
6. **Regla de bloqueo:** Si posteriormente se intenta ejecutar la simulación de ventanillas sin tener todos los documentos validados, `verificarDocumentosCompletos()` lanza `DocumentoFaltanteException` detallando exactamente qué documentos faltan.

---

## CU-06: Generación de Ruta Crítica de Trámites

* **Actor:** Gestor / Emprendedor.
* **Vista:** Pestaña lateral **Trámites y Exp. Único**.
* **Clases involucradas:** `controlador.BurokControlador`, `repositorio.TramiteCatalogoDAO`, `repositorio.ExpedienteDAO`, `modelo.ExpedienteFormalizacion`.

### Flujo Paso a Paso
1. Con una empresa seleccionada en **`Empresa activa`**, el usuario presiona **`Generar / Abrir Expediente`**.
2. `BurokControlador.generarRutaCritica(empresa)` verifica si la empresa ya posee un expediente en MySQL:
   * Si ya existe, lo recupera y muestra sus datos.
   * Si no existe, genera un nuevo código correlativo (`EXP-XXX`), toma los trámites del **Catálogo Regulatorio Nacional** (`tramites_catalogo` en MySQL: IP/Notariado, SAR, Alcaldía/Catastro, Bomberos, MiAmbiente y ARSA), calcula el total de horas tradicionales (`1,933 hrs`) frente a las horas optimizadas Burok (`160 hrs`), y guarda el expediente y su detalle en `expedientes` y `expediente_tramites`.
3. En la pantalla se actualizan:
   * Las barras **Progreso por Etapa** (*Apertura*, *Funcionamiento* y *Cierre*).
   * La tabla inferior del **Catálogo Regulatorio Nacional** con horas tradicionales, horas Burok, costo oficial en Lempiras (`L.`) y plazo legal SLA en días.

---

## CU-07: Simulación Interinstitucional y Silencio Administrativo Positivo (Multihilo)

* **Actor:** Gestor / Emprendedor.
* **Vista:** Pestaña lateral **Seguimiento y SLA**.
* **Clases involucradas:** `hilos.ProcesoVentanillaHilo`, `controlador.BurokControlador`, `excepciones.DocumentoFaltanteException`.

### Flujo Paso a Paso
1. El usuario selecciona un expediente en el selector **`Expediente`**. La tabla muestra los trámites asignados, días transcurridos, plazo SLA, estado y si aplica silencio positivo.
2. Presiona el botón **`Simular Ventanillas y SLA (>= 5 seg)`**.
3. `BurokControlador.simularVentanillaConHilo()` realiza dos validaciones previas:
   * Verifica que el expediente no esté ya siendo procesado (`exp.isEnProceso()`), evitando ejecuciones duplicadas.
   * Invoca `verificarDocumentosCompletos()`: si falta algún documento en el Expediente Único, detiene el proceso y lanza `DocumentoFaltanteException`.
4. Se deshabilita el botón de inicio y se lanza un hilo independiente (`new Thread(new ProcesoVentanillaHilo(...))`).
5. Durante **~5.8 segundos** (10 pasos de 580 ms), el hilo recorre las etapas interinstitucionales (`RNP -> IP -> SAR -> Catastro -> Bomberos -> SLA -> MiAmbiente -> ARSA -> Silencio Positivo -> MySQL`), actualizando la `JProgressBar` y la etiqueta de estado en la interfaz mediante `SwingUtilities.invokeLater()` sin congelar la ventana.
6. Al finalizar los 10 pasos:
   * Si la empresa es **Categoría de Riesgo 1 (Bajo Impacto)** y el trámite pertenece a **Alcaldía** o **MiAmbiente**, se simula el vencimiento del plazo SLA (`sla + 1` días) y el sistema aplica automáticamente el estado **`SILENCIO_POSITIVO`** (`silencioPositivo = true`).
   * Los demás trámites concluyen como **`APROBADO`**.
   * El estado global del expediente cambia a **`APROBADO`** y se persisten todos los cambios en `expedientes` y `expediente_tramites` en MySQL.
7. Se reactiva el botón, se refrescan las métricas del sistema y se notifica al usuario con un `JOptionPane`.

---

## CU-08: Liquidación Financiera de Tasas Oficiales en Lempiras (L.)

* **Actor:** Gestor / Contador / Emprendedor.
* **Vista:** Pestaña lateral **Reportes y Java 2D** (Panel superior).
* **Clases involucradas:** `controlador.BurokControlador`, `repositorio.LiquidacionDAO`, `modelo.LiquidacionCostos`, `excepciones.PresupuestoInsuficienteException`.

### Flujo Paso a Paso
1. El usuario selecciona un expediente en el combo **`Expediente`**.
2. El sistema calcula y muestra automáticamente el resumen financiero y de tiempo:
   * **Municipal (L.):** Suma de tasas de Alcaldía Municipal y Bomberos.
   * **Central (L.):** Suma de tasas de Notariado/IP, SAR, MiAmbiente y ARSA.
   * **Total (L.):** Monto total oficial requerido.
   * **Ahorro (hrs):** Diferencia entre horas tradicionales (`1,933`) y horas Burok (`160`).
3. El usuario ingresa el monto en Lempiras en el campo **`Pago (L.)`** y presiona **`Emitir Liquidación`**.
4. `BurokControlador.emitirLiquidacion()` valida:
   * Que el expediente esté en estado **`APROBADO`** o **`SILENCIO_POSITIVO`**.
   * Que el monto recibido cubra el total oficial. Si es menor, lanza **`PresupuestoInsuficienteException`** indicando el monto faltante exacto en Lempiras.
5. Si el pago es válido:
   * Calcula el cambio (`montoRecibido - total`).
   * Genera el número correlativo `LIQ-XXX` e inserta el registro en la tabla `liquidaciones` de MySQL.
   * Marca el expediente como `liquidado = true` en MySQL.
   * Actualiza la tabla de liquidaciones emitidas y muestra el comprobante en `JOptionPane`.

---

## CU-09: Visualización de Analítica y Reportes Gráficos Java 2D

* **Actor:** Emprendedor / Gestor / Evaluador.
* **Vista:** Pestaña lateral **Reportes y Java 2D** (Panel inferior).
* **Clases involucradas:** `vista.CanvasReporte2D`, `controlador.BurokControlador`.

### Flujo Paso a Paso
1. Al ingresar a la pestaña **Reportes y Java 2D** (o tras emitir una liquidación), `VentanaPrincipal` invoca `actualizarGrafico2D()`.
2. `BurokControlador` consulta en MySQL:
   * `obtenerHorasTradicionalesPorEntidad()` y `obtenerHorasBurokPorEntidad()` desde `tramites_catalogo`.
   * `obtenerExpedientesPorEstado()` desde `expedientes`.
3. Se pasan los tres mapas de datos a `CanvasReporte2D.setDatos(...)` y se ejecuta `repaint()`.
4. `CanvasReporte2D` sobreescribe `paintComponent(Graphics g)` usando **`Graphics2D`** nativo (sin librerías externas) para renderizar dos gráficos de barras lado a lado:
   * **Gráfico izquierdo:** Comparativa visual de *Horas Tradicionales (1,933h)* vs. *Ruta Burok Optimizada* agrupada por entidad reguladora.
   * **Gráfico derecho:** Conteo en tiempo real de *Expedientes en MySQL por Estado* (`Pendiente`, `En Revisión`, `Silencio Positivo`, `Aprobado`).
