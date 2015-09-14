import java.awt.Point;

import org.javaarena.engine.AbstractRobot;
import org.javaarena.events.EnemyDetectedEvent;
import org.javaarena.events.FireDetectedEvent;
import org.javaarena.events.FireHitEvent;


public class RaptorOld extends AbstractRobot {

	public RaptorOld() {
		super("Raptor");
	}

	public void update() {
		this.needFire = false;
		switch (this.mode) {
			case openGame :			{ this.openGame(); break; }
			case goToDegree :		{ this.goToDegree(); break; }  
			case searchForTarget :	{ this.searchForTarget(); break; }  
			case attack :			{ this.attack(); break; }  
		}
		this.setRotation(this.toRotate);
		this.toRotate = 0;
		this.setVelocity(this.velocity);
		if (this.needFire) this.fire(this.firePower);
		this.cicleCounter++;
	}

	private void setMode(int pNewMode) {
		this.previousMode = this.mode;
		this.mode = pNewMode;
		switch (this.mode) {
			case openGame :			{ this.setMessage(this.openGameMessage); break; }
			case goToDegree :		{ this.setMessage(this.goToDegreeMessage); break; }  
			case searchForTarget :	{ this.setMessage(this.searchForAttackerMessage); break; }  
			case attack :			{ this.setMessage(this.attackMessage); break; }  
		}
	}

	private void setSpeed(int pNewSpeed) {
		this.previousVelocity = this.velocity;
		this.velocity = pNewSpeed;
	}
	
	private void setDegree(int angle) {
		this.setMode(this.goToDegree);
		this.toDegree = angle;
		this.direction = Math.abs(this.toDegree - this.getOurAngle()) < 180 ? this.Left : this.Right; 
	}

	/**
	 * Gira para a esquerda a quantidade de graus especificada.
	 * Se a quantidade de graus especificadas exceder 5 graus retorna o restante.
	 * @param rotate graus para rodar
	 * @return restante de graus
	 */
	private void turnLeft(int rotate) {
		if (rotate > 5) {
			this.toRotate = 5;
			return;
		}
		this.toRotate = rotate;
	}

	/**
	 * Gira para a direita a quantidade de graus especificada.
	 * Se a quantidade de graus especificadas exceder 5 graus retorna o restante.
	 * @param rotate graus para rodar
	 * @return restante de graus
	 */
	private void turnRight(int rotate) {
		if (rotate > 5) {
			this.toRotate = -5;
			return;
		}
		this.toRotate = rotate;
	}
	
	private void goToDegree() {
		if (this.velocity > 0) {
			this.setSpeed(0);
		}
		int delta;
		delta = this.toDegree - this.getOurAngle();
			
		if (this.direction == this.Left) {
			this.turnLeft(delta);
		} else {
			this.turnRight(delta);
		}
		if (delta == 0) {
			this.setSpeed(this.previousVelocity);
			this.setMode(this.previousMode);
		}
	}

	private void attack() {
		this.velocity = 5;
		this.ciclesOnTrack ++;
		if (this.ciclesOnTrack >= 5) {
			this.setMode(this.searchForTarget);
		}
		this.needFire = (this.getEnemyDistance() <= this.enemyMaximumDistanceForFire);
	}

	private int getEnemyDistance() {
		// sem sqrt para economizar ciclos de processamento.
		return (this.enemyPosition.x - this.getPosition().x) ^ 2 + (this.enemyPosition.y - this.getPosition().y) ^ 2;  
	}

	private void openGame() {
		Task t = new Task();
		this.setSpeed(3);
		this.setMode(searchForTarget);
		switch (this.getCurrentSession()) {
			case northWest: { this.setDegree(0); break; }		
			case northEast: { this.setDegree(270); break; }
			case southWest: { this.setDegree(90); break; }
			case southEast: { this.setDegree(180); break; }
		}
	}

	private void searchForTarget() {
/*		if (this.cicleCounter > this.latencyTime) {
			this.cicleCounter = 0;
			if (this.degree % 45 == 0) { 
				this.turnDirection = this.turnDirection * -1;
			}
			if (this.turnDirection < 0) {
				this.turnLeft(5);
			} else {
				this.turnRight(5);
			}
		}
*/		
	}

	public void onFireHit(FireHitEvent evt) {
	}

	public void onEnemyDetected(EnemyDetectedEvent evt) {
		this.setMode(this.attack);
		if (this.enemyName.equals(new String())) {
			this.enemyName = evt.getRobotName();
		}
		if (this.enemyName.equals(evt.getRobotName())) {
			this.enemyDegree = evt.getAngle();
			this.enemyPosition = new Point(evt.getX(), evt.getY());
			this.enemyVelocity = evt.getVelocity();
			this.ciclesOnTrack = 0;
		}

	}

	public void onFireDetected(FireDetectedEvent evt) {
	}

	
	private Point getArenaCenter() {
		int width = this.getScreenBounds().width / 2; 
		int height = this.getScreenBounds().height / 2;
		return new Point(width, height); 
	}
	
	private int getCurrentSession() {
		Point center = this.getArenaCenter();
		Point me = this.getPosition();
		if (me.x > center.x) {
			return me.y > center.y ? this.northEast : this.southEast; 
		} else  {
			return me.y > center.y ? this.northWest : this.southWest;
		}
	}

	private int getOurAngle() {
		return (int)Math.round(this.getAngle());
	}

	class Task {
		public Task() {
		}
	}

	// properties for modes
	final int openGame = 0;
	final int searchForTarget = 1; 
	final int searchForAttacker = 2; 
	final int attack = 3;
	final int goToDegree = 4; 
	
	final String openGameMessage = "Booting robot...";
	final String searchForTargetMessage = "Searching for target...";
	final String searchForAttackerMessage = "Searching for attacker...";
	final String attackMessage = "Let's dance...";
	final String goToDegreeMessage = "Turning to the right angle...";

	int mode = openGame;
	int previousMode;
	
	// properties for game area sessions
	final int northWest = 0;
	final int northEast = 1;
	final int southEast = 2;
	final int southWest = 3;

	final int Left = 0;
	final int Right = 1;
	int direction; 

	// properties for enemy
	int enemyVelocity;
	double enemyDegree;
	Point enemyPosition;
	String enemyName = new String(); 
	int ciclesOnTrack = 0;
	final int enemyMaximumDistanceForFire = 256 ^ 2;
	
	// properties for update	
	int toRotate;
	int toDegree;

	int velocity = 0;
	int previousVelocity;
	String previousMessage;

	boolean needFire = false;
	int firePower = 0;
	final int latencyTime = 200;
	int cicleCounter = 0;
	
}
