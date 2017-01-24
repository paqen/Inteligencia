package mouserun.mouse;

import mouserun.game.*;
import java.util.*;

public class M16C10 extends Mouse {

    private class Pair<A, B> {
        public A primero;
        public B segundo;

        public Pair() {
        }

        public Pair(A _primero, B _segundo) {
            this.primero = _primero;
            this.segundo = _segundo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pair)) {
                return false;
            }
            
            Pair key = (Pair) o;
            return primero == key.primero && segundo == key.segundo;
        }

        @Override
        public int hashCode() {
            //Comprobamos que primero y segundo son enteros
            if (primero instanceof Integer && segundo instanceof Integer) {
                Integer result = (Integer) primero;
                Integer sec = (Integer) segundo;
                //probar programa quitando el 1000
                return result * 1000+ sec;
            }
           return 0;
        }

        @Override
        public String toString() {
            return "X: " + primero + " Y: " + segundo;
        }
    }

    /**
     * Guardamos una posición(coordenadas x e y) y, en caso de que hayamos visitado dicha casilla comprobamos las direcciones a las que nos podemos mover.
     */
    private class Nodo{

        public int x;
        public int y;
        public boolean up;
        public boolean down;
        public boolean left;
        public boolean right;

        public boolean explored;

        public Nodo(int _x, int _y, boolean _up, boolean _down, boolean _left, boolean _right) {
            x = _x;
            y = _y;

            up = _up;
            down = _down;
            left = _left;
            right = _right;
            explored = true;
        }

        public Nodo(Pair<Integer, Integer> pos, boolean _up, boolean _down, boolean _left, boolean _right) {
            this(pos.primero, pos.segundo, _up, _down, _left, _right);
        }

        public Nodo(int _x, int _y) {
            x = _x;
            y = _y;
            explored = false;
        }

        public Nodo(Pair<Integer, Integer> pos) {
            this(pos.primero, pos.segundo);
        }

        public Pair<Integer, Integer> getPos() {
            return new Pair(x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Nodo)) {
                return false;
            }
            Nodo node = (Nodo) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return x * 10000 + y;
        }

        @Override
        public String toString() {
            return "X: " + x + " Y: " + y;
        }
    }

   
    private HashMap<Pair<Integer, Integer>, Nodo> tablero;
    //Utilizamos un HashMap que contiene todas las casillas del tablero
    //Usa una posición (x,y) como clave.
    

    private Stack<Integer> recorrido;
    private Stack<Grid> casillasVisitadas;
    //Contiene los movimientos a realizar. Bien para llegar a un Cheese,
    //o para llegar a una casilla no explorada.

    private int moveCount;  
    private int bombsLeft;  

    private boolean aestrella;

    
    public M16C10() {
        super("a*");

        moveCount = 0;
        bombsLeft = 5;
        aestrella = false;
        recorrido = new Stack<>();
        tablero = new HashMap<>();
        casillasVisitadas=new Stack<Grid>();
    }

    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        
        if(!noEstaPila(currentGrid,casillasVisitadas)){
            casillasVisitadas.add(currentGrid);
            incExploredGrids();
            
        }
       //Creamos un Par con una referencia a la casilla actual.
        Pair<Integer, Integer> currentPos = new Pair<>(currentGrid.getX(), currentGrid.getY());
        Nodo currentNode;

        //Buscamos en tablero la posición actual. .
        if (tablero.containsKey(currentPos)) {
            currentNode = tablero.get(currentPos);
        } else {
            //creamos un nuevo nodo con los posibles movimientos que podemos hacer.
            currentNode = new Nodo(
                    currentPos,
                    currentGrid.canGoUp(), currentGrid.canGoDown(),
                    currentGrid.canGoLeft(), currentGrid.canGoRight()
            );

            tablero.put(currentPos, currentNode);
        }

        //En caso de que nos encontremos en la casilla del cheese,
        //abandonamos la casilla y volvemos a ella
        if (cheese.getX() == currentNode.x && cheese.getY() == currentNode.y && recorrido.isEmpty()) {
            if (currentGrid.canGoUp()) {
                recorrido.add(Mouse.DOWN);
                recorrido.add(Mouse.UP);
            } else {
                if (currentGrid.canGoDown()) {
                    recorrido.add(Mouse.UP);
                    recorrido.add(Mouse.DOWN);
                } else {
                    if (currentGrid.canGoLeft()) {
                        recorrido.add(Mouse.RIGHT);
                        recorrido.add(Mouse.LEFT);
                    } else {
                        if (currentGrid.canGoRight()) {
                            recorrido.add(Mouse.LEFT);
                            recorrido.add(Mouse.RIGHT);
                        }
                    }
                }
            }
        }

        //Comprobamos si quedan bombas
        if (bombsLeft > 0) {
            int exitCount = 0;
            //Almacena la cantidad de direcciones por las que
            //se puede avanzar, desde el nodo actual.
            if (currentNode.up) {
                exitCount++;
            }
            if (currentNode.down) {
                exitCount++;
            }
            if (currentNode.left) {
                exitCount++;
            }
            if (currentNode.right) {
                exitCount++;
            }
            //Según el número de movimientos y el número de salidas, se decide
            //si colocar o no una bomba.
            if (moveCount > 30 && exitCount == 4) {
                moveCount = 0;
                bombsLeft--;
                return Mouse.BOMB;
            } else {
                if (moveCount > 100 && exitCount >= 3) {
                    moveCount = 0;
                    bombsLeft--;
                    return Mouse.BOMB;
                } else {
                    moveCount++;
                }
            }
        }

        //En caso de que recorrido esté vacío generamos un camino
        if (recorrido.isEmpty()) {
            Pair<Integer, Integer> target = new Pair<>(cheese.getX(), cheese.getY());//target=nodo objetivo

            if (tablero.containsKey(target)) {
                aestrella= true;
               
            } else {
               aestrella = false;
                //Exploramos con profundidadLimitada
            }

            getRecorrido(currentNode, target);
            //Obtenemos un recorrido al Cheese
            //o a una casilla no explorada.
        }
        System.out.println((double)getExploredGrids()/(400));
        return recorrido.pop();
    }
    public boolean noEstaPila(Grid currentGrid,Stack <Grid>pila){
        boolean bandera=false;
        for(int i=0;i<pila.size();i++){
            if(pila.get(i).getX()==currentGrid.getX() && pila.get(i).getY()==currentGrid.getY()){
                bandera=true;
            }
        }
        return bandera;
    }

    @Override
    public void newCheese() {
        recorrido.clear();
    }

    @Override
    public void respawned() {
        recorrido.clear();
    }

    void getRecorrido(Nodo rootNode, Pair<Integer, Integer> target) {
      
        HashMap<Pair<Integer, Integer>, Nodo> anteriores = new HashMap<>();
        Nodo targetNode = null;
        //Llamadas a la búsqueda
        if (aestrella) {
            busquedaAStar(rootNode, target, anteriores);
            targetNode = tablero.get(target); //El nodo objetivo es el mismo queso.
        } else {
           //Se empieza con un profundidad de 5 casillas y, se irá incrementando
            int limite = 5;
            targetNode = null;
            while (targetNode == null) {
                targetNode = busquedaProfundidadLimitada(rootNode, target, anteriores, limite);
                limite += 5;
            }
        }

        //Se selecciona método a* pero no se puede utilizar porque el queso sale en zona desconocida por lo que 
        //aplicamos profundidad limitada
        if (aestrella&& !anteriores.containsKey(target)) {
         

            int limite = 5;

            targetNode = null;
            while (targetNode == null) {
                targetNode = busquedaProfundidadLimitada(rootNode, target, anteriores, limite);
                limite += 5;
            }
        }

        //Finalmente calculamos el recorrido al nodo objetivo          
        Nodo curNode = anteriores.get(targetNode.getPos());
        recorrido.add(getDirection(curNode.getPos(), targetNode.getPos()));

        while (curNode != rootNode) {
            Pair<Integer, Integer> targetPos = curNode.getPos();
            curNode = anteriores.get(curNode.getPos());
            recorrido.add(getDirection(curNode.getPos(), targetPos));
        }

    }

    void busquedaAStar(Nodo rootNode, Pair<Integer, Integer> target, HashMap<Pair<Integer, Integer>, Nodo> anteriores) {
        List<Pair<Integer, Nodo>> abiertos = new LinkedList<>();
        HashMap<Pair<Integer, Integer>, Nodo> cerrados = new HashMap<>();

        abiertos.add(new Pair<>(0, rootNode));

        while (!abiertos.isEmpty()) {
            int min = Integer.MAX_VALUE;
            int minIndex = Integer.MIN_VALUE;

            for (int i = 0; i < abiertos.size(); i++) {
                Pair<Integer, Nodo> w = abiertos.get(i);
                if (w.segundo.getPos() == target) {
                    minIndex = i;
                    break;
                }

                int curValue =  w.primero +distanciaManhattam(w.segundo.getPos(), target);
                if (curValue < min) {//Valor actual
                    min = curValue;
                    minIndex = i;
                }
            }

            Pair<Integer, Nodo> v = abiertos.get(minIndex);
            abiertos.remove(v);
            cerrados.put(v.segundo.getPos(), v.segundo);
            int nivel = v.primero + 1;

            if (v.segundo.x == target.primero && v.segundo.y == target.segundo) {
                break;
            }

            //DOWN
            if (v.segundo.down) {
                Pair<Integer, Integer> curPos = v.segundo.getPos();
                curPos.segundo--;

                if (tablero.containsKey(curPos)) {
                    Nodo w = tablero.get(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (!cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                    }
                }
            }

            //LEFT
            if (v.segundo.left) {
                Pair<Integer, Integer> curPos = v.segundo.getPos();
                curPos.primero--;

                if (tablero.containsKey(curPos)) {
                    Nodo w = tablero.get(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (!cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                    }
                }
            }

            //RIGHT
            if (v.segundo.right) {
                Pair<Integer, Integer> curPos = v.segundo.getPos();
                curPos.primero++;

                if (tablero.containsKey(curPos)) {
                    Nodo w = tablero.get(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (!cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                    }
                }
            }

            //UP
            if (v.segundo.up) {
                Pair<Integer, Integer> curPos = v.segundo.getPos();
                curPos.segundo++;

                if (tablero.containsKey(curPos)) {
                    Nodo w = tablero.get(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (!cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                    }
                }
            }
        }
    }

    /**
     *
     * Realiza una búsqueda en profundidad limitada. Almacenara los antecesores
     * de cada nodo para poder calcular el recorrido y manejará una lista de nodos
     * candidatos, no explorados exclusivamente, que se emplearon en el cálculo
     * del nodo a devolver.
   
     */
    Nodo busquedaProfundidadLimitada(Nodo rootNode, Pair<Integer, Integer> target, HashMap<Pair<Integer, Integer>, Nodo> anteriores, int limite) {
        Stack<Pair<Integer, Nodo>> abiertos = new Stack<>();
        HashMap<Pair<Integer, Integer>, Nodo> cerrados = new HashMap<>();
        List<Pair<Integer, Nodo>> candidatos = new LinkedList<>();

        abiertos.add(new Pair<>(0, rootNode));

        while (!abiertos.isEmpty()) {
            
            Pair<Integer, Nodo> v = abiertos.pop();
            cerrados.put(v.segundo.getPos(), v.segundo);//HashMap

            int nivel = v.primero + 1;
            //En caso de que el queso se encuentre en la misma casilla que estamos,salimos del while
            if (v.segundo.x == target.primero && v.segundo.y == target.segundo) {
                candidatos.add(v);
                break;
            }

            if (v.segundo.explored) {
                //DOWN
                if (v.segundo.down) {
                    Pair<Integer, Integer> curPos = v.segundo.getPos();
                    curPos.segundo--;

                    if (tablero.containsKey(curPos)) {
                        Nodo w = tablero.get(curPos);
                        Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                        if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.segundo);
                        }
                    } else {
                        Nodo w = new Nodo(curPos);
                        Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                        if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.segundo);
                            candidatos.add(insert);
                        }
                    }
                }

                //LEFT
                if (v.segundo.left) {
                    Pair<Integer, Integer> curPos = v.segundo.getPos();
                    curPos.primero--;

                    if (tablero.containsKey(curPos)) {
                        Nodo w = tablero.get(curPos);
                        Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                        if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.segundo);
                        }
                    } else {
                        Nodo w = new Nodo(curPos);
                        Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                        if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.segundo);
                            candidatos.add(insert);
                        }
                    }
                }
            }

            //RIGHT
            if (v.segundo.right) {
                Pair<Integer, Integer> curPos = v.segundo.getPos();
                curPos.primero++;

                if (tablero.containsKey(curPos)) {
                    Nodo w = tablero.get(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                    }
                } else {
                    Nodo w = new Nodo(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                        candidatos.add(insert);
                    }
                }
            }

            //UP
            if (v.segundo.up) {
                Pair<Integer, Integer> curPos = v.segundo.getPos();
                curPos.segundo++;

                if (tablero.containsKey(curPos)) {
                    Nodo w = tablero.get(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                    }
                } else {
                    Nodo w = new Nodo(curPos);
                    Pair<Integer, Nodo> insert = new Pair<>(nivel, w);
                    if (nivel <= limite && !cerrados.containsKey(insert.segundo.getPos())) {
                        abiertos.add(insert);
                        anteriores.put(w.getPos(), v.segundo);
                        candidatos.add(insert);
                    }
                }
            }
        }

        int targetIndex = getMinimoIndice(candidatos, target, rootNode);
        if (targetIndex == -1) {
            return null;
        }
        return candidatos.get(targetIndex).segundo;
    }

    int distanciaManhattam(Pair<Integer, Integer> init, Pair<Integer, Integer> target) {
        return (Math.abs(target.primero - init.primero)) + (Math.abs(target.segundo - init.segundo));
    }

    /**
     * Dada una lista de nodos, emplea una función heurística para encontrar el
     * nodo con menor valor y devuelve su índice.
    
     */
    private int getMinimoIndice(List<Pair<Integer, Nodo>> nodes, Pair<Integer, Integer> target, Nodo init) {
        if (nodes.isEmpty()) {
            return -1;
        }

        int minValue = Integer.MAX_VALUE;
        int minPos = Integer.MIN_VALUE;

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).segundo == init) {
                continue;
            }
            if (nodes.get(i).segundo.getPos() == target) {
                return i;
            }

            int curValue = getValue(nodes.get(i), target);

            if (curValue < minValue) {
                minPos = i;
                minValue = curValue;
            }
        }

        return minPos;
    }

    /**
     * El nodo de entrada es evaluado, respecto a target, mediante una funciónn
     * heurística y se devuelve el resultado
    
     */
    private int getValue(M16C10.Pair<Integer, M16C10.Nodo> init, M16C10.Pair<Integer, Integer> target) {

        int distTarget = distanciaManhattam(init.segundo.getPos(), target);
        int costeCasilla = init.primero;

        return costeCasilla * 2 + distTarget;
    }

    /**
     * Dadas dos posiciones, devuelve la direcciÃ³n a seguir por el ratÃ³n para
     * llegar de una a otra.
    
     */
    private int getDirection(M16C10.Pair<Integer, Integer> init, M16C10.Pair<Integer, Integer> target) {
        if (target.segundo - 1 == init.segundo) {
            return Mouse.UP;
        } else if (target.segundo + 1 == init.segundo) {
            return Mouse.DOWN;
        } else if (target.primero - 1 == init.primero) {
            return Mouse.RIGHT;
        } else {
            return Mouse.LEFT;
        }
    }
}