# JavaPOO_Proyecto — Short Circuit!

Videojuego en Java que enseña diagramas esquemáticos y circuitos mediante acertijos rápidos contrarreloj. Los componentes son personajes 2D expresivos que dan feedback inmediato, validados en tiempo real bajo arquitectura MVC.

## Cómo jugar

1. Antes de cada nivel aparece un **tutorial de 3 páginas**: qué es la pieza nueva, cómo se conecta y cuál es tu misión. Mientras lo lees, el reloj está en pausa.
2. Arrastra las piezas desde el **almacén** (derecha) a la mesa. Solo la fuente empieza colocada.
3. Haz clic en un pin y arrastra hasta otro para poner un cable. Los cables buscan solos el camino más corto, rodean las piezas y no se cruzan si pueden evitarlo.
4. Arma el LED siempre igual: la corriente entra por su pata (+) y la **resistencia va en su pata (-)**.
5. Cumple la misión del nivel antes de que se acabe el tiempo.

| Tecla / ratón | Acción |
| :--- | :--- |
| Clic izquierdo en pin y arrastrar | Crear un cable |
| Clic en un switch | Abrir / cerrar (en el divisor: mitad de arriba = A, de abajo = B) |
| Clic en el chip 555 | Cambiar entre ASTABLE y MONOESTABLE |
| Clic en el potenciómetro | Abrir su barra; arrastra la perilla para cambiar el brillo |
| Rueda del ratón sobre el potenciómetro | Subir o bajar el brillo de 10 en 10 |
| Clic derecho | Borrar cable o regresar la pieza al almacén |
| H | Abrir el tutorial del nivel |
| R | Reiniciar el nivel |
| ESC | Volver al menú |

## Niveles

Cada nivel relaciona una idea de electrónica con algo de la vida diaria.

| # | Nivel | Idea | En la vida diaria |
| :---: | :--- | :--- | :--- |
| 1 | Enciende el LED | Circuito cerrado y resistencia | Una linterna |
| 2 | Interruptor | Abrir y cerrar el camino | El apagador de tu cuarto |
| 3 | Compuerta AND | A **y** B | Caja fuerte con dos llaves |
| 4 | Compuerta OR | A **o** B | Timbre con dos puertas |
| 5 | Las direccionales | Dos LEDs en paralelo | Las direccionales del carro |
| 6 | El semáforo | Tres LEDs y una secuencia | Verde, amarillo y rojo, una luz a la vez |
| 7 | El capacitor | Guardar y soltar energía | La lucecita del cargador que tarda en apagarse |
| 8 | La ambulancia | Chip 555 en modo astable | Luces que parpadean solas |
| 9 | La luz del pasillo | Chip 555 en modo monoestable | La luz del edificio que se apaga sola |
| 10 | El dimmer | Potenciómetro | El regulador de luz de un cuarto |

## Piezas

LED (amarillo, rojo o verde), resistencia de 220 Ohm, fuente de 5 V, interruptor, switch divisor A/B, compuertas AND y OR, **capacitor** (si se conecta al revés, explota), **chip 555** (cambia de cara según su modo) y **potenciómetro**. La **guía de componentes** del menú muestra cada pieza con qué es, un ejemplo de la vida diaria y una regla de oro; en la tarjeta del 555 puedes tocar cada pin (VCC, GND, OUT, C y TRIG) para ver qué hace, a dónde se conecta y qué pasa si lo olvidas, además de cuáles se usan en cada modo.

## Ejecutar

```powershell
# Jugar (o doble clic en Jugar.bat)
.\gradlew.bat lwjgl3:run

# Pruebas unitarias + compilación del módulo de escritorio
.\gradlew.bat test lwjgl3:compileJava
```

Requiere Java 17. Hecho con LibGDX 1.12.1.

## Estructura

- `core/.../model` — piezas del circuito (`componentes`), niveles `Nivel1` a `Nivel10` con sus tutoriales y el progreso del jugador. No depende de OpenGL, por eso se puede probar con JUnit.
- `core/.../view` — pantallas (`VistaMenu`, `VistaGuia`, `VistaSeleccionNivel`, `VistaNivel`) `EnrutadorCables`, que traza los cables con Dijkstra por el camino más corto, `LetreroNeon` (el título del menú) y `BarraAtajos` (las ayudas de teclas).
- `core/.../controller` — `ControladorNivel` traduce el ratón y el teclado a acciones sobre el modelo.
- `core/src/test` — pruebas JUnit 5 de todos los niveles y los tutoriales.
