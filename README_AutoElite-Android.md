# AutoElite — App Android

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Stripe](https://img.shields.io/badge/Stripe-635BFF?style=flat-square&logo=stripe&logoColor=white)
![License](https://img.shields.io/badge/Uso-Académico-lightgrey?style=flat-square)

Cliente Android nativo de **AutoElite**, un sistema de gestión de talleres mecánicos desarrollado como Proyecto de Fin de Grado (TFG) en DAM. AutoElite está compuesto por tres módulos —backend REST, esta app Android y un panel de administración en JavaFX—; este repositorio contiene únicamente la app Android, escrita en Kotlin con Jetpack Compose.

## Capturas de pantalla

<img width="398" height="839" alt="Captura de pantalla 2026-05-11 163026" src="https://github.com/user-attachments/assets/acd21557-33ce-4578-b3c0-edcab05520b2" />
<img width="401" height="842" alt="Captura de pantalla 2026-05-11 163454" src="https://github.com/user-attachments/assets/1f71810f-4a71-48bd-8f9c-f5e3beda628b" />
<img width="389" height="834" alt="Captura de pantalla 2026-05-11 163053" src="https://github.com/user-attachments/assets/32917292-1c79-4cd8-958b-eb9200db71dc" />
<img width="399" height="840" alt="Captura de pantalla 2026-05-11 162845" src="https://github.com/user-attachments/assets/63fce990-126a-40ae-bc06-ad96e635164f" />
<img width="400" height="840" alt="Captura de pantalla 2026-05-11 163312" src="https://github.com/user-attachments/assets/3a5adde9-87fa-486f-9f05-76fcd1159d03" />
<img width="396" height="834" alt="Captura de pantalla 2026-05-11 163333" src="https://github.com/user-attachments/assets/ca714fc5-c708-451e-9d27-4e1f1f31d744" />



## Funcionalidades

- Autenticación de usuarios con **Firebase Auth**
- Reserva de citas en franjas de **30 minutos**
- Seguimiento del estado de la reparación mediante una máquina de estados: `PRESENTADA → EN_PROCESO / RECHAZADA → TERMINADA → CONFIRMADA`, con generación automática de factura al confirmarse
- Pagos integrados con **Stripe** (Payment Sheet)
- Notificaciones push con **Firebase Cloud Messaging**, con centro de notificaciones in-app y badge de no leídas
- Generación de facturas en **PDF** mediante la API nativa de Android (`PdfDocument`)
- Valoración por estrellas de reparaciones completadas
- Sistema CRM de fidelización con niveles **Bronze / Silver / Gold**
- Historial unificado de actividad (timeline de citas, reparaciones y facturas)
- Internacionalización completa: español, inglés y árabe, con soporte **RTL**
- Tema claro/oscuro con **Material3**, persistente mediante **DataStore**
- Animaciones de transición entre pantallas, shimmer/skeleton loading y pull-to-refresh
- Splash screen nativa (Android 12+ `SplashScreen` API)

## Stack técnico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose, Material3 |
| Backend auth | Firebase Auth |
| Notificaciones | Firebase Cloud Messaging |
| Pagos | Stripe Android SDK (Payment Sheet) |
| Persistencia local | DataStore |
| Red | OkHttp (consumo de la API REST del backend) |
| Concurrencia | Coroutines / Flow |
| PDF | `android.graphics.pdf.PdfDocument` |

## Arquitectura

La app sigue una organización por capas típica de Compose: pantallas (`UI`) que observan estado expuesto por **ViewModels**, los cuales delegan en una capa de red/repositorio que habla con el backend de AutoElite vía OkHttp. La sesión de usuario se mantiene con DataStore para sobrevivir a reinicios de la app.

## Configuración y puesta en marcha

> Esta app necesita credenciales propias de Firebase y Stripe para funcionar. **No subas nunca estas claves al repositorio** — usa `local.properties` (ya ignorado por git) o variables de entorno.

1. Clona el repositorio:
   ```bash
   git clone https://github.com/HibaSam611/AutoElite-Android.git
   ```
2. Abre el proyecto en **Android Studio** (versión reciente recomendada).
3. Añade tu propio `google-services.json` (descargado desde la consola de Firebase) en `app/`.
4. Configura en `local.properties` la URL base del backend y tu clave pública de Stripe:
   ```properties
   BASE_URL=https://tu-backend-autoelite/
   STRIPE_PUBLISHABLE_KEY=pk_test_xxxxxxxx
   ```
5. Sincroniza Gradle y ejecuta sobre un emulador o dispositivo físico.

> El backend de AutoElite (Spring Boot) se encuentra en un repositorio independiente y debe estar levantado para que la app funcione por completo.

## Autoría

Proyecto de Fin de Grado — DAM, I.E.S. Laguna de Joatzel.

- **Hiba Samraoui** — app Android (este repositorio) y parte del backend
- **Sergio** — panel de administración en JavaFX y la otra mitad del backend
- Tutor: Alain Fernández Fernández

## Licencia

Proyecto desarrollado con fines académicos. Sin licencia de uso comercial.
