package mouserun.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import mouserun.game.Cheese;
import mouserun.game.Grid;
import mouserun.game.Mouse;

public class M16B06b extends Mouse {

    private HashMap<Pair<Integer, Integer>, Nodo> casillasVisitadas;
    private HashMap<Pair<Integer, Integer>, Nodo> exploracion;

    private HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> recorridosAnchura;
    private final ArrayList<Pair<Integer, Integer>> cola;

    private boolean Corroborar;

    private Stack<Integer> camino;
    private Stack<Integer> pila;
    private boolean muerto;

    private int bombas;
    private int bombasRestantes;

   
    public M16B06b() {
        super("Carlos");
        casillasVisitadas = new HashMap<>();
        exploracion = new HashMap<>();
        camino = new Stack<>();
        pila = new Stack<>();
        muerto = false;
        bombasRestantes = 5;
        bombas = 0;

        Corroborar = false;

        cola = new ArrayList<>();
        recorridosAnchura = new HashMap<>();
    }

    public int move(Grid casilla, Cheese queso) {
        Pair<Integer, Integer> pair = new Pair<>(casilla.getX(), casilla.getY());
        Nodo nodo;

        if (Corroborar) {
            
            if (casillasVisitadas.containsKey(pair)) {
                camino.clear();
                pila.clear();
                exploracion.clear();
                recorridosAnchura.clear();
                cola.clear();
            }
            Corroborar = false;
        }

        if (!casillasVisitadas.containsKey(pair)) {
            nodo = new Nodo(pair, casilla.canGoUp(), casilla.canGoDown(), casilla.canGoLeft(), casilla.canGoRight());
            casillasVisitadas.put(pair, nodo);
            incExploredGrids();
        } else {
            nodo = casillasVisitadas.get(pair);
        }

        if (!exploracion.containsKey(pair)) {
            exploracion.put(pair, nodo);
        }

        if (bombasRestantes > 0) {
            ++bombas;
            int caminos = 0;
            if (casilla.canGoUp()) {
                ++caminos;
            }
            if (casilla.canGoDown()) {
                ++caminos;
            }
            if (casilla.canGoLeft()) {
                ++caminos;
            }
            if (casilla.canGoRight()) {
                ++caminos;
            }
            if (caminos >= 3 && bombas >= 60) {
                bombas = 0;
                --bombasRestantes;
                return 5;
            }
        }
         //en camino se va almacenando el camino del raton 
        if (camino.isEmpty()) {
            if ((casilla.getX() == queso.getX()) && (casilla.getY() == queso.getY())) {
                return 0;
            }
            Pair<Integer, Integer> objetivo = new Pair<>(queso.getX(), queso.getY());

            if (casillasVisitadas.containsKey(objetivo)) {
                recorridosAnchura.put(pair, pair);
                meterencola(casilla);

                objetivo = cola.get(0);
                if (objetivo.first == casilla.getX() && objetivo.second == casilla.getY()) {
                    cola.remove(0);
                    objetivo = cola.get(0);
                }
                calcularCamino(nodo, objetivo);
            } else {
                return explorador(nodo, objetivo);
            }

            if (muerto) {
                camino.clear(); /// revisar
                return exploradorHueco(nodo, objetivo);
            }
        }
        return camino.pop();
    }

  
    private void meterencola(Grid casilla) {

        Pair<Integer, Integer> posibleAnchura;

        if (casilla.canGoUp()) {
            posibleAnchura = funcionDeDispersion(casilla.getX(), casilla.getY(), 1);
            if (casillasVisitadas.containsKey(posibleAnchura) && !recorridosAnchura.containsKey(posibleAnchura)) {
                cola.add(posibleAnchura);
            }
        }

        if (casilla.canGoDown()) {
            posibleAnchura = funcionDeDispersion(casilla.getX(), casilla.getY(), 2);
            if (casillasVisitadas.containsKey(posibleAnchura) && !recorridosAnchura.containsKey(posibleAnchura)) {
                cola.add(posibleAnchura);
            }
        }

        if (casilla.canGoLeft()) {
            posibleAnchura = funcionDeDispersion(casilla.getX(), casilla.getY(), 3);
            if (casillasVisitadas.containsKey(posibleAnchura) && !recorridosAnchura.containsKey(posibleAnchura)) {
                cola.add(posibleAnchura);
            }
        }

        if (casilla.canGoRight()) {
            posibleAnchura = funcionDeDispersion(casilla.getX(), casilla.getY(), 4);
            if (casillasVisitadas.containsKey(posibleAnchura) && !recorridosAnchura.containsKey(posibleAnchura)) {
                cola.add(posibleAnchura);
            }
        }

    }

    private void calcularCamino(Nodo nodo, Pair<Integer, Integer> objetivo) {
        ArrayList<Pair<Integer, Nodo>> candidatos = new ArrayList<>();
        HashMap<Pair<Integer, Integer>, Nodo> explorados = new HashMap<>();
        Nodo obj;

        aEstrella(nodo, objetivo, explorados);
        obj = casillasVisitadas.get(objetivo);

        if (!explorados.containsKey(objetivo)) {
            muerto = true;
            return;
        } else {
            muerto = false;
        }

        Nodo nodoActual = explorados.get(obj.getCasilla());
        camino.add(calculaDireccion(nodoActual.getCasilla(), obj.getCasilla()));

        while (nodoActual != nodo) {
            Pair<Integer, Integer> posicionObjetivo = nodoActual.getCasilla();
            nodoActual = explorados.get(nodoActual.getCasilla());
            camino.add(calculaDireccion(nodoActual.getCasilla(), posicionObjetivo));
        }
    }

    
    private void aEstrella(Nodo nodo, Pair<Integer, Integer> objetivo, HashMap<Pair<Integer, Integer>, Nodo> explorados) {
        List<Pair<Integer, Nodo>> abiertos = new ArrayList<>();
        HashMap<Pair<Integer, Integer>, Nodo> cerrados = new HashMap<>();
        Nodo nodoActual;
        Pair<Integer, Integer> pos;

        abiertos.add(new Pair<>(0, nodo));

        while (!abiertos.isEmpty()) {
            int min = 1000;
            int cont = 0;
            int i = 0;
            while (i < abiertos.size()) {
                Pair<Integer, Nodo> pair = abiertos.get(i);
                if (pair.second.getCasilla() == objetivo) {
                    cont = i;
                    break;
                }

                int costeActual = pair.first + manhattan(pair.second.getCasilla(), objetivo);
                if (costeActual < min) {
                    min = costeActual;
                    cont = i;
                }
                ++i;
            }

            Pair<Integer, Nodo> pairComp = abiertos.get(cont);
            abiertos.remove(pairComp);
            cerrados.put(pairComp.second.getCasilla(), pairComp.second);
            int nivel = pairComp.first + 1;

            if (pairComp.second.x == objetivo.first && pairComp.second.y == objetivo.second) {
                break;
            }

            if (pairComp.second.getCasillaAbajo()) {
                pos = funcionDeDispersion(pairComp.second.getCasilla().first, pairComp.second.getCasilla().second, 2);

                if (casillasVisitadas.containsKey(pos)) {
                    nodoActual = casillasVisitadas.get(pos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, nodoActual);
                    if (!cerrados.containsKey(insert.second.getCasilla())) {
                        abiertos.add(insert);
                        explorados.put(nodoActual.getCasilla(), pairComp.second);
                    }
                }
            }

            if (pairComp.second.getCasillaIzquierda()) {
                pos = funcionDeDispersion(pairComp.second.getCasilla().first, pairComp.second.getCasilla().second, 3);

                if (casillasVisitadas.containsKey(pos)) {
                    nodoActual = casillasVisitadas.get(pos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, nodoActual);
                    if (!cerrados.containsKey(insert.second.getCasilla())) {
                        abiertos.add(insert);
                        explorados.put(nodoActual.getCasilla(), pairComp.second);
                    }
                }
            }

            if (pairComp.second.getCasillaDerecha()) {
                pos = funcionDeDispersion(pairComp.second.getCasilla().first, pairComp.second.getCasilla().second, 4);

                if (casillasVisitadas.containsKey(pos)) {
                    nodoActual = casillasVisitadas.get(pos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, nodoActual);
                    if (!cerrados.containsKey(insert.second.getCasilla())) {
                        abiertos.add(insert);
                        explorados.put(nodoActual.getCasilla(), pairComp.second);
                    }
                }
            }

            if (pairComp.second.getCasillaArriba()) {
                pos = funcionDeDispersion(pairComp.second.getCasilla().first, pairComp.second.getCasilla().second, 1);

                if (casillasVisitadas.containsKey(pos)) {
                    nodoActual = casillasVisitadas.get(pos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, nodoActual);
                    if (!cerrados.containsKey(insert.second.getCasilla())) {
                        abiertos.add(insert);
                        explorados.put(nodoActual.getCasilla(), pairComp.second);
                    }
                }
            }
        }
    }

 
    private int manhattan(Pair<Integer, Integer> inicio, Pair<Integer, Integer> objetivo) {
        return (Math.abs(inicio.getFirst() - objetivo.getFirst())) + (Math.abs(inicio.getSecond() - objetivo.getSecond()));
    }

    private int explorador(Nodo casilla, Pair<Integer, Integer> queso) {

        Pair clave;
        for (int i = 1; i < 5; i++) {
            clave = funcionDeDispersion(casilla.getCasilla().getFirst(), casilla.getCasilla().getSecond(), i);
            if (Proximidad(i, casilla.getCasilla(), queso) && movimientoValido(casilla, i) && !exploracion.containsKey(clave)) {
                pila.add(retrocede(i));
                return i;
            }
        }

        for (int i = 1; i < 5; i++) {
            clave = funcionDeDispersion(casilla.getCasilla().getFirst(), casilla.getCasilla().getSecond(), i);
            if (movimientoValido(casilla, i) && !exploracion.containsKey(clave)) {
                pila.add(retrocede(i));
                return i;
            }
        }
        return pila.pop();
    }

    private int exploradorHueco(Nodo casilla, Pair<Integer, Integer> queso) {

        Pair clave;
        for (int i = 1; i < 5; i++) {
            clave = funcionDeDispersion(casilla.getCasilla().getFirst(), casilla.getCasilla().getSecond(), i);
            if (Proximidad(i, casilla.getCasilla(), queso) && movimientoValido(casilla, i) && !exploracion.containsKey(clave) && !casillasVisitadas.containsKey(clave)) {
                pila.add(retrocede(i));
                return i;
            }
        }
        //Elegir un movimiento sin prioridad
        for (int i = 1; i < 5; i++) {
            clave = funcionDeDispersion(casilla.getCasilla().getFirst(), casilla.getCasilla().getSecond(), i);
            if (movimientoValido(casilla, i) && !exploracion.containsKey(clave) && !casillasVisitadas.containsKey(clave)) {
                pila.add(retrocede(i));
                return i;
            }
        }
        return pila.pop();
    }

   //en caso de que aparezca un nuevo queso,este metodo reinicia todas las estructuras
    @Override
    public void newCheese() {
        camino.clear();
        pila.clear();
        exploracion.clear();
        recorridosAnchura.clear();
        cola.clear();
    }

  //en caso de que el raton muera se procede a reiniciar todas las estructuras
    @Override
    public void respawned() {
        Corroborar = true;
    }
//metodo que se encarga de retroceder dado un determinado movimiento
    private int retrocede(int movimiento) {
        switch (movimiento) {
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 4;
            case 4:
                return 3;
        }
        return 0;
    }

   //metodo que calcula si se puede realizar un determinado movimiento
    private boolean movimientoValido(Nodo currentGrid, int direccion) {
        switch (direccion) {
            case 1:
                return currentGrid.getCasillaArriba();
            case 2:
                return currentGrid.getCasillaAbajo();
            case 3:
                return currentGrid.getCasillaIzquierda();
            case 4:
                return currentGrid.getCasillaDerecha();
        }
        return false;
    }

  
    private int calculaDireccion(Pair<Integer, Integer> inicio, Pair<Integer, Integer> objetivo) {
        if (objetivo.second - 1 == inicio.second) {
            return 1;
        } else if (objetivo.second + 1 == inicio.second) {
            return 2;
        } else if (objetivo.first - 1 == inicio.first) {
            return 4;
        } else {
            return 3;
        }
    }

   
    private boolean Proximidad(int direccion, Pair<Integer, Integer> currentGrid, Pair<Integer, Integer> queso) {
        switch (direccion) {
            case 1:
                return queso.getSecond() > (currentGrid.getSecond() + 1);
            case 2:
                return queso.getSecond() < (currentGrid.getSecond() - 1);
            case 3:
                return queso.getFirst() < (currentGrid.getFirst() - 1);
            case 4:
                return queso.getFirst() > (currentGrid.getFirst() + 1);
        }
        return true;
    }

    private Pair<Integer, Integer> funcionDeDispersion(int x, int y, int direccion) {
        switch (direccion) {
            case 1:
                y = y + 1;
                break;
            case 2:
                y = y - 1;
                break;
            case 3:
                x = x - 1;
                break;
            case 4:
                x = x + 1;
                break;
        }
        return new Pair<>(x, y);
    }

    public class Pair<A, B> {

        public A first;
        public B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }

       
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Pair)) {
                return false;
            }
            Pair pair = (Pair) obj;
            return (first == pair.first && second == pair.second);
        }

        //La funcion de dispersion es calculada a partir de las coordenadas y se usa desplazamiento de bits.
        @Override
        public int hashCode() {
            if (first instanceof Integer && second instanceof Integer) {
                Integer prim = (Integer) first;
                Integer seg = (Integer) second;
                return (seg << 3) + (prim << 2) + (seg << 5) + (prim << 7);
            }
            return 0;
        }
    }

    public class Nodo {

        private int x;
        private int y;

        private boolean casillaArriba;
        private boolean casillaAbajo;
        private boolean casillaDerecha;
        private boolean casillaIzquierda;

        public Nodo(int x, int y, boolean arriba, boolean abajo, boolean derecha, boolean izquierda) {
            this.x = x;
            this.y = y;
            casillaArriba = arriba;
            casillaAbajo = abajo;
            casillaIzquierda = izquierda;
            casillaDerecha = derecha;
        }

        public Nodo(Pair<Integer, Integer> casilla, boolean arriba, boolean abajo, boolean derecha, boolean izquierda) {
            this(casilla.first, casilla.second, arriba, abajo, izquierda, derecha);
        }

        public Pair<Integer, Integer> getCasilla() {
            return new Pair<>(x, y);
        }

        /**
         * @return the casillaArriba
         */
        public boolean getCasillaArriba() {
            return casillaArriba;
        }

        /**
         * @return the casillaAbajo
         */
        public boolean getCasillaAbajo() {
            return casillaAbajo;
        }

        /**
         * @return the casillaDerecha
         */
        public boolean getCasillaDerecha() {
            return casillaDerecha;
        }

        /**
         * @return the casillaIzquierda
         */
        public boolean getCasillaIzquierda() {
            return casillaIzquierda;
        }

        //Utilizacion del metodo equals y hashCode para generar de forma automatica y mas eficiente la funcion de dispersion de un pair.
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Nodo)) {
                return false;
            }
            Nodo node = (Nodo) obj;
            return x == node.x && y == node.y;
        }

        //La funcion de dispersion es calculada a partir de las coordenadas y se usa desplazamiento de bits.
        @Override
        public int hashCode() {
            return (y << 3) + (x << 2) + (y << 5) + (x << 7);
        }

    }
}
