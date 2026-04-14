# SuppliesRescueSystem - Food Rescue MVP

Plataforma logística en tiempo real para reducir el desperdicio de alimentos conectando donantes con voluntarios y refugios.

## 🚀 El Problema y la Solución
**El Problema:** Grandes cantidades de comida en perfecto estado se desperdician diariamente en restaurantes y comercios al final de su jornada, mientras organizaciones sociales sufren carencias.
**La Solución:** Una aplicación móvil que permite publicar excedentes de comida rápidamente, asignarlos a voluntarios para su transporte y confirmar su recepción en refugios.

---

## 📸 Demo de la Aplicación
*Guarda tus capturas en la carpeta `readme-images/` para visualizarlas aquí.*

### 1. Acceso y Registro
| Login | Registro de Roles |
| :---: | :---: |
| ![Login Screen](readme-images/login.png) | ![Register Screen](readme-images/register.png) |
| *Diseño Bento Box de alto contraste para acceso rápido.* | *Selección obligatoria de rol (Donante, Voluntario, Receptor).* |

### 2. Flujo del Donante (Restaurantes)
| Panel de Estado | Publicar Lote |
| :---: | :---: |
| ![Donor Home](readme-images/donor_home.png) | ![Publish Screen](readme-images/publish.png) |
| *Seguimiento en tiempo real de lotes: Pendiente, Asignado o Completado.* | *Formulario de texto optimizado para publicaciones en menos de 30 segundos.* |

### 3. Flujo del Voluntario (Repartidores)
| Feed de Rescates | Ruta Activa |
| :---: | :---: |
| ![Volunteer Feed](readme-images/volunteer_feed.png) | ![Active Route](readme-images/active_route.png) |
| *Lista filtrada por tiempo de expiración para evitar comida caducada.* | *Integración con Google Maps para navegación rápida al donante y refugio.* |

### 4. Flujo del Receptor (Refugios/ONG)
| Entregas Entrantes | Confirmación |
| :---: | :---: |
| ![Recipient Home](readme-images/recipient_home.png) | ![Confirmation](readme-images/confirmation.png) |
| *Vista de qué voluntarios están en camino hacia su ubicación.* | *Botón de confirmación final para cerrar el ciclo de rescate.* |

---

## 🛠️ Stack Tecnológico
- **Lenguaje:** Kotlin 2.1
- **UI:** Jetpack Compose (Modern Utility Design)
- **Arquitectura:** Clean Architecture + MVVM
- **DI:** Hilt (Dagger)
- **Backend:** Firebase Auth & Firestore (Spark Plan friendly)
- **Navegación:** Compose Navigation
- **Asincronía:** Kotlin Coroutines & StateFlow

---

## ⚙️ Configuración del Proyecto
Para ejecutar este proyecto localmente:

1.  **Clonar el repositorio.**
2.  **Firebase Setup:**
    - Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
    - Añade una app de Android con el paquete `com.udlap.suppliesrescuesystem`.
    - Descarga el archivo `google-services.json` y colócalo en la carpeta `app/`.
3.  **Habilitar Servicios:**
    - **Auth:** Activar Email/Password.
    - **Firestore:** Crear base de datos en modo prueba o con las reglas proporcionadas en la documentación técnica.
4.  **Build:** Sincroniza Gradle y ejecuta la app en un emulador o dispositivo físico.

---

## 🛡️ Seguridad (Reglas de Firestore)
El sistema utiliza transacciones para evitar colisiones entre voluntarios y reglas de validación por UID para proteger los datos de perfil.

---
*Desarrollado para el impacto social y la eficiencia logística.*
