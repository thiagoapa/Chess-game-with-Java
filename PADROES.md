# Patrones de Diseño Aplicados — Ajedrez en Terminal

Este documento explica los **5 patrones GoF** usados, por qué encajan en el ajedrez
y dónde están en el código. Sirve como base para el informe del trabajo.

---

## 1. Abstract Factory (Creacional)

**Problema que resuelve:** crear las piezas de cada bando garantizando coherencia
(el color correcto y la estrategia de movimiento correcta), sin que el resto del
código dependa de clases concretas ni del color.

**Participantes en el proyecto:**
- *Fábrica abstracta:* `AbstractPieceFactory` — declara la familia de productos
  (`createPawn`, `createRook`, `createKnight`, `createBishop`, `createQueen`, `createKing`).
- *Fábricas concretas:* `WhitePieceFactory` y `BlackPieceFactory` — cada una produce
  un "ejército" completo del color correspondiente.
- *Productos:* objetos `Piece`.

**Dónde se usa:** `Board.setupInitialPosition()` arma la posición inicial pidiendo
las piezas a las dos fábricas; `Board.applyMove()` la usa también para la coronación.

```java
AbstractPieceFactory white = new WhitePieceFactory();
AbstractPieceFactory black = new BlackPieceFactory();
grid[1][col] = white.createPawn();   // peón blanco, ya con su estrategia
grid[6][col] = black.createPawn();   // peón negro
```

**Beneficio:** si mañana se quisiera un set de piezas "variante" (otra estrategia o
representación), basta una nueva fábrica; el tablero no cambia.

---

## 2. Strategy (Comportamiento)

**Problema que resuelve:** cada pieza se mueve distinto. En lugar de un `switch`
gigante por tipo de pieza, cada regla de movimiento vive en su propia clase
intercambiable, y la pieza delega en ella.

**Participantes en el proyecto:**
- *Estrategia:* interfaz `MovementStrategy` con `generateMoves(board, from, piece)`.
- *Estrategias concretas:* `PawnMovement`, `KnightMovement`, `KingMovement`,
  y `RookMovement`/`BishopMovement`/`QueenMovement` (que comparten `SlidingMovement`).
- *Contexto:* `Piece`, que guarda una `MovementStrategy` y le delega.

**Dónde se usa:** `Piece.generateMoves()` no contiene lógica de movimiento; sólo
delega. La identidad de la pieza (tipo, color) queda separada de su comportamiento.

```java
public List<Move> generateMoves(Board board, Position from) {
    return movementStrategy.generateMoves(board, from, this);
}
```

**Beneficio:** agregar o modificar reglas de movimiento no toca la clase `Piece`.
Las piezas deslizantes comparten código vía `SlidingMovement` (sin duplicación).

---

## 3. State (Comportamiento)

**Problema que resuelve:** una partida pasa por situaciones distintas (en curso,
jaque, jaque mate, ahogado, tablas) que cambian si el juego continúa y qué se
muestra. En vez de un amasijo de banderas booleanas, cada situación es un objeto.

**Participantes en el proyecto:**
- *Estado:* interfaz `GameState` (`isGameOver()`, `sideToMove()`, `statusLine()`).
- *Estados concretos:* `OngoingState`, `CheckState`, `CheckmateState`,
  `StalemateState`, `DrawState`.
- *Contexto:* `Game`, que mantiene el estado actual y transiciona tras cada jugada.

**Dónde se usa:** el bucle principal (`Main`) sólo pregunta `game.state().isGameOver()`.
`Game.transitionStateFor()` elige el nuevo estado evaluando jaque y jugadas legales.

```java
if (!hasMoves && inCheck)      state = new CheckmateState(next);
else if (!hasMoves)            state = new StalemateState();
else if (inCheck)              state = new CheckState(next);
else                           state = new OngoingState(next);
```

**Beneficio:** el bucle de juego no conoce los detalles; cada estado encapsula su
propio comportamiento y mensaje.

---

## 4. Observer (Comportamiento)

**Problema que resuelve:** desacoplar la lógica del ajedrez de la forma en que se
informan los eventos. El motor no debe saber si los anuncios van a la terminal, a
un archivo de registro o a una interfaz gráfica.

**Participantes en el proyecto:**
- *Sujeto:* `Game`, que mantiene la lista de observadores y los notifica.
- *Observador:* interfaz `GameObserver` (`onMovePlayed`, `onCheck`, `onGameEnded`).
- *Observador concreto:* `ConsoleObserver`, que imprime en la terminal.

**Dónde se usa:** `Game.commitMove()` notifica la jugada; `transitionStateFor()`
notifica jaque o fin de partida.

```java
private void notifyMovePlayed(Color mover, Move move, Piece captured) {
    for (GameObserver o : observers) o.onMovePlayed(mover, move, captured);
}
```

**Beneficio:** se pueden añadir nuevos observadores (registro PGN, sonidos, GUI)
sin tocar el motor. `Main` sólo hace `game.addObserver(new ConsoleObserver())`.

---

## 5. Prototype (Creacional)

**Problema que resuelve:** para saber si una jugada es legal hay que comprobar que
no deje al **rey propio** en jaque. Eso exige "probar" la jugada sin alterar el
tablero real. La solución es clonar el tablero, aplicar la jugada en la copia y
verificar.

**Participantes en el proyecto:**
- *Prototipo:* `Board`, con `copy()` (clonación profunda: cada `Piece` se clona).
- *Cliente:* `Game.isKingSafeAfter()`.

**Dónde se usa:** al filtrar jugadas pseudo-legales para quedarse sólo con las legales.

```java
private boolean isKingSafeAfter(Move move, Color color) {
    Board simulated = board.copy();   // PROTOTYPE: clon profundo
    simulated.applyMove(move);
    return !simulated.isKingInCheck(color);
}
```

**Beneficio:** el tablero real nunca se ensucia con jugadas de prueba; la
simulación es segura y aislada. La clonación también respeta el estado de captura
al paso, así que la validación es correcta incluso en jugadas especiales.

---

## Cómo colaboran los cinco patrones (visión global)

1. **Abstract Factory** crea el ejército inicial de piezas.
2. Cada pieza usa su **Strategy** para proponer jugadas pseudo-legales.
3. El **Game** clona el tablero (**Prototype**) para descartar las que dejan al rey en jaque.
4. Tras la jugada válida, el **Game** transiciona de **State** (en curso → jaque → mate…).
5. En cada paso, el **Game** (sujeto) avisa a sus **Observers**, que muestran todo en pantalla.

Cada patrón carga una responsabilidad distinta y real: ninguno está "de adorno".

---

## Extra: Strategy aplicado también a los jugadores (IA)

Además del movimiento de las piezas, el patrón **Strategy** se reutiliza para
decidir **cómo elige su jugada cada jugador**. La interfaz `PlayerStrategy`
(`ai/PlayerStrategy.java`) tiene dos implementaciones:

- `HumanPlayer`: la jugada proviene de la entrada del usuario (teclado o mouse).
- `AIPlayer`: la jugada la calcula la máquina con **minimax + poda alfa-beta**.

El motor trata a ambos por igual: sólo pide "elige tu jugada". Esto permite
cambiar entre *humano vs humano* y *humano vs máquina* sin tocar la lógica del
ajedrez — exactamente el propósito del patrón Strategy.

```java
public interface PlayerStrategy {
    Move chooseMove(Game game);
    String name();
}
```

La IA, para analizar variantes, **clona el tablero** (patrón Prototype, `Board.copy()`)
y **reutiliza la misma validación de legalidad** del motor creando un `Game`
temporal sobre el clon. Así no duplica ni una sola regla del ajedrez.

**Beneficio:** la inteligencia artificial se integró como una pieza más del
diseño existente, reforzando dos patrones (Strategy y Prototype) en vez de añadir
código ad-hoc desconectado.
