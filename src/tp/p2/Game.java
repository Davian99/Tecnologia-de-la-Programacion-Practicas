package tp.p2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;
import tp.printer.*;
import tp.zombies.*;
import tp.plantas.*;

public class Game {
	private int cicleCount;
	private Random generador;
	private Level nivel;
	private ListaGameObject listaObjetos;
	private ZombieManager generadorZombie;
	private DebugPrinter pantallaD;
	private ReleasePrinter pantallaR;
	private SuncoinManager monedas;
	private int tamx, tamy;
	private PrintMode printMode;
	private boolean catchSun;
	
	//Constructora para cuando tenemos una semilla.
	public Game(Level nivel, Random r) {
		this(nivel);
		this.setGenerador(r);
	}
	
	//Constructora con semilla por defecto.
	public Game(Level nivel) {
		this.tamx = 4;
		this.tamy = 8;
		this.nivel = nivel;
		cicleCount = 0;
		setGenerador(new Random());
		listaObjetos = new ListaGameObject(5);
		generadorZombie = new ZombieManager(nivel);
		pantallaR = new ReleasePrinter(tamx, tamy, 50);
		pantallaD = new DebugPrinter(tamx, tamy, 50);
		monedas = new SuncoinManager(50);
		printMode = PrintMode.RELEASE;
		this.setCatchSun(false);
	}
	
	//Hace el update del juego, osea hace la ejecuci�n de un turno.
	public void update() {
		
		//Hacemos el update de todos los objetos de la lista, llamando a un update que se 
		//Sobreescribe por el update de cada subclase
		for (int j = 0; j < listaObjetos.getElementos(); j++) {
			this.listaObjetos.obtenerPos(j).update();
		}		
		//Generacion de zombies
		int zombie = this.getGenerador().nextInt(3);
		addZombie(generarTipoZombie(zombie));
		
		//Update de los suns
		this.monedas.update(this);
		
		//Aumentamos el contador de ciclo
		this.cicleCount++;
		this.setCatchSun(false);
	}
	
	public int turno() {
		return this.cicleCount;
	}
	
	public void eliminarObjeto(int x, int y) {
		this.listaObjetos.eliminarObjeto(x, y);
	}
	
	//Metodo auxiliar para Controller que se utiliza para saber si gana alguien la partida y quien.
	public boolean winner() {
		if(!this.listaObjetos.quedanZombies() && (this.generadorZombie.numZombies() <= 0)) { // si gana el player.
			System.out.println("Game Over\nPlayer Wins");
			return true;
		}
		else if(zombieWinner()) { //Si ganan los zombies.
			System.out.println("Game Over\nZombies Wins");
			return true;
		}	
		else
			return false;
	}	
	
	//Metodo auxiliar para el metodo winnner que comprueba si existe al menos un zombie en la columna 0.
	public boolean zombieWinner(){
		return (this.listaObjetos.zombiesGanan());
	}	
	
	//Metodo que se utiliza en update de Peashooter para ver si existe algun  zombie en su fila y golpearlo
	//Si la vida del zombie llega a 0 tambien lo mata.
	public void atacarZombies(int x, int y, int dmg) {
		GameObject gc;
		for (int i = y+1; i < 8; ++i) {
			if(this.listaObjetos.estaEnCasilla(x, i)) {
				gc = this.listaObjetos.obtenerBoard(x, i);
				if(gc instanceof Zombie) {
					if (gc.muere(dmg)) {
						this.listaObjetos.eliminarObjeto(x, i);
					}
						
					return;
				}
			}
		}
	}
	
	//Metodo auxiliar que enlaza mediante game al update de sunflower con SunconisManager.
	public void aniadirSunGame(int monedas) {
		this.monedas.aniadirSuncoin(monedas);
	}
	
	//Metodo auxiliar que resta los suncoins si se a�ade una planta.
	public boolean suficientesSuncoins(int coste) {
		return this.monedas.restarSuncoins(coste);
	}
	
	//Metodo auxiliar que dados una pos x e y comprueban si la casilla esta vacia dando igual si es zombie o planta.
	public boolean casillaVacia(int x, int y) {
		return !(listaObjetos.estaEnCasilla(x, y));
	}
	
	//Metodo que utilizan los zombies para atacar a las plantas.
	public void atacarPlanta(int x, int y, int fuerza) {
		GameObject gc;
		if(this.listaObjetos.estaEnCasilla(x, y)) {
			gc = this.listaObjetos.obtenerBoard(x, y);
			if(gc instanceof Planta) {
				if (gc.muere(fuerza)) {
					this.listaObjetos.eliminarObjeto(x, y);
				}
				return;
			}
			return;
		}
		return;
	}
	
	public void atacarZombiePos(int x, int y, int dmg) {
		GameObject gc;
		if(this.listaObjetos.estaEnCasilla(x, y)) {
			gc = this.listaObjetos.obtenerBoard(x, y);
			if(gc instanceof Zombie) {
				if (gc.muere(dmg)) {
					this.listaObjetos.eliminarObjeto(x, y);
				}
					
				return;
			}
		}
	}
	
	//Metodo auxiliar que hace que el controller printe el tablero.
	public void printTablero() {
		if (this.printMode == PrintMode.RELEASE) {
			this.pantallaR.encodeGame(this, this.cicleCount, monedas.getSunCoins(), generadorZombie.numZombies());
			System.out.println(this.pantallaR.boardToString());
		}
		else if (this.printMode == PrintMode.DEBUG) {
			this.pantallaD.encodeGame(this, this.cicleCount, monedas.getSunCoins(), generadorZombie.numZombies());
			System.out.println(this.pantallaD.boardToString());
		}
	}
	
	//Metodo utilizado en controller que resetea el juego y cuando no tiene semilla.
	public void reset(){
		cicleCount = 0;
		setGenerador(new Random());
		listaObjetos = new ListaGameObject(5);
		generadorZombie = new ZombieManager(nivel);
		//pantalla= new GameprinterObs(4, 8, 50); //Modificar al gusto
		monedas = new SuncoinManager(50);
	}

	//Metodo utilizado en controller que resetea el juego y cuando si tiene semilla.
	public void reset(long seed){
		this.reset();
		this.setGenerador(new Random(seed));
	}

	public String printCasilla(int i, int j) {
		return this.listaObjetos.printObjeto(i, j);
	}
	
	public int objetosLista() {
		return this.listaObjetos.getElementos();
	}
	
	public GameObject getObject(int i) {
		return this.listaObjetos.obtenerPos(i);
	}
	
	public PrintMode getPrintMode() {
		return this.printMode;
	}
	
	public boolean addPlanta(String planta, int x, int y) {
		FactoryPlanta parseador = new FactoryPlanta();
		Planta p;
		if (x >= this.tamx || x < 0 || y >= this.tamy-1 || y < 0) {
			System.out.println("La casilla introducida esta fuera del dominio\n");
			return false;
		}
		if(this.casillaVacia(x, y)) {
			p = parseador.parse(planta, x, y, this);
			if(p != null) {
				if (this.suficientesSuncoins(p.getCoste())) {
					this.listaObjetos.aniadirPlanta(p);
					return true;
				}
				System.out.println("No tienes suficientes SunCoins\n");
				return false;
			}
				
			else {
				System.out.println("No se reconocio la planta\n");
				return false;
			}
		}
		System.out.println("La casilla ya esta ocupada\n");
		return false;
	}
	
	public void changeGamePrinter(PrintMode change) {
		this.printMode = change;
	}

	public void addZombie(String zombie){
		
		FactoryZombie parseador = new FactoryZombie();
		Zombie z;
		boolean salida = false;
		
		if (generadorZombie.isZombieAdded(this.getGenerador())) {
			int pos = this.getGenerador().nextInt(4);
			while(!salida){
				
				if(casillaVacia(pos, 7)) {
					z = parseador.parse(zombie, pos, 7, this);
					this.listaObjetos.aniadirZombie(z);
					salida = true;
				}
				else pos = this.getGenerador().nextInt(4);
			}
		}
	}
	
	public String generarTipoZombie(int zombie){
		
		String tipoZombie = "";
		
		switch(zombie){
		
		case 0:
			tipoZombie = "z";
			break;
		case 1:
			tipoZombie = "b";
			break;
		default:
			tipoZombie = "r";
			break;
		}
		
		return tipoZombie;
	}
	
	public void addSun(int x, int y) {
		if(this.monedas.casillaVacia(x, y)) {
			Sun s = new Sun(x, y, this);
			this.monedas.aniadirSun(s);
		}
		else
			return;
	}
	
	public GameObject getter(int x, int y) {
		return this.listaObjetos.obtenerBoard(x, y);
	}
	
	public boolean sunEnCasilla(int x, int y) {
		if (this.monedas.casillaVacia(x, y))
			return false;
		return true;
	}

	public Sun getSun(int x, int y) {
		return this.monedas.getSunBoard(x, y);
	}

	public Random getGenerador() {
		return generador;
	}

	public void setGenerador(Random generador) {
		this.generador = generador;
	}

	public boolean catchSunEstate() {
		return catchSun;
	}

	public void setCatchSun(boolean catchSun) {
		this.catchSun = catchSun;
	}
	
	public void cogerSun(int x, int y) {
		if (this.monedas.catchSun(x, y))
			this.setCatchSun(true);
		else
			System.out.println("No hay ning�n Sol en esa casilla\n");
	}

	public String listInfoPlanta() {
		FactoryPlanta parseador = new FactoryPlanta();
		StringBuilder str = new StringBuilder();
		str.append(parseador.listAll());
		return str.toString();
	}
	
	public String listInfoZombie() {
		FactoryZombie parseador = new FactoryZombie();
		StringBuilder str = new StringBuilder();
		str.append(parseador.listAll());
		return str.toString();
	}

	public void store(BufferedWriter bw) {
		try {
			bw.write("cycle: " + this.cicleCount + "\n");
			bw.write("sunCoins: " + this.monedas.getSunCoins() + "\n");
			bw.write("level: " + this.nivel.toString() + "\n");
			bw.write("remZombies: " + this.generadorZombie.numZombies() + "\n");
			this.listaObjetos.store(bw);
			this.monedas.store(bw);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return;
	}
}









