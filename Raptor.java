import java.awt.Point;

import org.javaarena.engine.AbstractRobot;
import org.javaarena.events.EnemyDetectedEvent;
import org.javaarena.events.FireDetectedEvent;
import org.javaarena.events.FireHitEvent;

public class Raptor extends AbstractRobot {

	public Raptor() {
		super("Raptor");
		
	}
// ---------------------------------------------   Main methods

	public void update() {
		this.setRotation(this.calculateRotation());
		this.setVelocity(this.calculateVelocity());
		int firePower = this.calculateFirePower();
		this.setMessage(this.calculateMessage());
		if (firePower > 0) this.fire(this.calculateFirePower());
		
		if (this.modeAttackCliclesOutOfTrack ++ > this.modeAttackMaximumCiclesOutOfTrack && this.mode == this.modeAttacking) {
			this.enemyName = new String();
			this.setMode(this.modeSearchingForTarget); 
		}

		if (this.mode == this.modeSearchingForAttacker) {
			this.modeSearchingForAttackerCicles ++;
			if (this.modeSearchingForAttackerCicles > this.modeSearchingForAttackerCiclesLimit) {
				this.modeSearchingForAttackerCicles = 0;
				this.enemyName = new String();
				this.setMode(this.modeSearchingForTarget);
				return;
			}
		}
		this.cliclesCounter ++;
	}

	public void onFireHit(FireHitEvent evt) {
		if (evt.getRobotFrom().equals(this.getName())) {
			return;
		}

		if (!evt.getRobotFrom().equals(this.fireHitEnemyName)) {
			this.fireHitCount = 0;
		}

		this.fireHitCount ++;
		this.fireHitEnemyName = evt.getRobotFrom();
		this.fireHitPower = evt.getPower();

		if (this.fireHitCount >= this.fireHitCountLimitToSearch || this.mode == this.modeSearchingForTarget) {
			this.enemyName = new String();
			this.fireHitCount = 0;
			this.modeAttackCliclesOutOfTrack = 0;
			this.setMode(this.modeSearchingForAttacker);
		}
	}

	public void onEnemyDetected(EnemyDetectedEvent evt) {
		if (this.mode == this.modeSearchingForAttacker) {
			if (!evt.getRobotName().equals(this.fireHitEnemyName)) {
				this.enemyName = new String();
				return;			
			}
		}
		
		if (this.enemyName.equals(new String())) {
			this.enemyName = evt.getRobotName();
		}

		if (this.enemyName.equals(evt.getRobotName())) {
			this.enemyAngle = evt.getAngle();
			this.enemyX = evt.getX(); 
			this.enemyY = evt.getY();
			this.modeAttackCliclesOutOfTrack = 0;
			this.setMode(this.modeAttacking);
		}
	}

	public void onFireDetected(FireDetectedEvent evt) {
		/*if (this.mode == this.modeAttacking && evt.getOwner().equals(this.enemyName)) {
			this.fireDetectedEnemyName = new String();
			return;
		}*/ 
		this.fireDetectedEnemyName = evt.getOwner();
		this.firePower = evt.getPower();
	}
	
	private void setMode(int newMode) {
		if (newMode == this.mode) return;
		this.mode = newMode;
	}

//	---------------------------------------------   Declarative methods
	
	private String calculateMessage() {
		switch (this.mode) {
			case modeSearchingForTarget  : return "You can run, but you can't hide"; 
			case modeAttacking  : return "Let's dance Mr. " + this.enemyName; 
			case modeSearchingForAttacker : return "Now you'll pay Mr. " + this.fireHitEnemyName; 
			case modeDefending : return "Run Forest, run !!!"; //""Do you think I'm stupid Mr. " + this.fireDetectedEnemyName + " ?"; 
		}
		return "I'm tired, I'm going to sleep.... BYE";
	}

	private int calculateFirePower() {
		switch (this.mode) {
			case modeSearchingForTarget  : return 0;
			case modeAttacking  : return this.modeAttackCalculateFirePower();
			case modeSearchingForAttacker  : return 3;
			case modeDefending  : return 0;
		}
		return 0;
	}

	private double calculateRotation() {
		switch (this.mode) {
			case modeSearchingForTarget  : return this.modeSearchingForTargetCalculateRotation();
			case modeAttacking  : return this.modeAttackCalculateRotation(); 
			case modeSearchingForAttacker  : return this.modeSearchingForAttackerCalculateRotation(); 
			case modeDefending  : return this.modeDefendingCalculateRotation(); 
		}
		return 0;
	}

	private int calculateVelocity() {
		switch (this.mode) {
			case modeSearchingForTarget : return this.modeSearchingForTargetCalculateVelocity(); 
			case modeAttacking : return this.modeAttackCalculateVelocity(); 
			case modeSearchingForAttacker : return this.modeSearchingForAttackerCalculateVelocity(); 
			case modeDefending : return this.modeDefendingCalculateVelocity(); 
		}
		return 0;
	}

	// ------------------------------------------   Defense methods

	private double modeDefendingCalculateRotation() {
		return 0;
	}
	
	private int modeDefendingCalculateFirePower() {
		return 0;
	}	
	private int modeDefendingCalculateVelocity() {
		return -5; 
	}

	// ------------------------------------------   Attack methods

	private double modeAttackCalculateRotation() {
		return this.utilGetRotation(new Point (this.enemyX, this.enemyY));
	}
	
	private int modeAttackCalculateFirePower() {
		double magicDistance = this.utilGetMagicDistance(this.utilGetMyPosition().x, this.utilGetMyPosition().y, this.enemyX, this.enemyY);
		return magicDistance < 70 ? 9 : 5;
	}
	
	private int modeAttackCalculateVelocity() {
		double magicDistance = this.utilGetMagicDistance(this.utilGetMyPosition().x, this.utilGetMyPosition().y, this.enemyX, this.enemyY);
		return magicDistance > 9 ? 5 : -5;
	}
	
	// ------------------------------------------   SearchForAttack methods
	
	private double modeSearchingForAttackerCalculateRotation() {
		return this.utilGetAngle(this.utilGetArenaCenter());
	}

	private int modeSearchingForAttackerCalculateVelocity() {
		return 3;
	}
	
	// ------------------------------------------   SearchForTarget methods
	
	private double modeSearchingForTargetCalculateRotation() {
		if (this.cliclesCounter % 350 == 0) {
			this.modeSearchingForTargetSwitchDestination();
		}
		double distance = this.utilGetMagicDistance(this.utilGetMyPosition().x, this.utilGetMyPosition().y, this.modeSearchForTargetDestination.x, this.modeSearchForTargetDestination.y); 
		return distance > 5 ? this.utilGetRotation(this.modeSearchForTargetDestination) : 180;
	}

	private int modeSearchingForTargetCalculateVelocity() {
		double distance = this.utilGetMagicDistance(this.utilGetMyPosition().x, this.utilGetMyPosition().y, this.modeSearchForTargetDestination.x, this.modeSearchForTargetDestination.y); 
		return distance > 5 ? 5 : 0;
	}
	
	private void modeSearchingForTargetSwitchDestination() {
		switch (this.utilGetQuadrant()) {
			case utilQuadrantNorthEast : this.modeSearchForTargetDestination = this.utilGetQuadrantCenter(this.utilQuadrantSouthWest); break; 
			case utilQuadrantSouthWest : this.modeSearchForTargetDestination = this.utilGetQuadrantCenter(this.utilQuadrantNorthWest); break;
			case utilQuadrantNorthWest : this.modeSearchForTargetDestination = this.utilGetQuadrantCenter(this.utilQuadrantSouthEast); break;
			case utilQuadrantSouthEast : this.modeSearchForTargetDestination = this.utilGetQuadrantCenter(this.utilQuadrantNorthEast); break;
		}
	}

//	---------------------------------------------   Utilitary methods

	private double utilGetAngle(Point target) {
		double angle;
		try {
			angle = Math.toDegrees(Math.atan2((target.y - this.utilGetMyPosition().y) * -1, (target.x - this.utilGetMyPosition().x)));
		} catch (Exception e ) {
			angle = 90;
		}
		angle = (angle < 0) ? angle + 360 : angle;
		return angle;
	}

	private Point utilGetArenaCenter() {
		if (this.utilArenaCenter != null) return this.utilArenaCenter;
		int x = (int)this.getScreenBounds().width / 2;
		int y = (int)this.getScreenBounds().height / 2;
		return this.utilArenaCenter = new Point(x,y);
	}

	private Point utilGetQuadrantCenter(int quadrant) {
		int x = 0; 
		int y = 0;
		switch (quadrant) {
			case utilQuadrantNorthEast : x = 3; y = 1; break;
			case utilQuadrantNorthWest : x = 1; y = 1; break;
			case utilQuadrantSouthEast : x = 3; y = 3; break;
			case utilQuadrantSouthWest : x = 1; y = 3; break;
		}
		return new Point(((this.getScreenBounds().width / 4) * x), ((this.getScreenBounds().height / 4) * y));
	}

	private Point utilGetRealPosition(int x, int y) {
		return new Point((int)x + (this.getBounds().width  / 2),(int)y + (this.getBounds().height / 2));
	}

	private Point utilGetMyPosition() {
		return utilGetRealPosition(this.getPosition().x, this.getPosition().y);
	}

	private double utilGetMagicDistance(int x, int y, int x2, int y2) {
		return Math.sqrt(Math.abs(x2-x) + Math.abs(y2 - y)); // pergunta pro Ramon que ele explica isso...
	}

	private double utilGetDistance(int x, int y, int x2, int y2) {
		return Math.sqrt(Math.abs(x2-x)^2 + Math.abs(y2 - y)^2);
	}

	private int utilGetQuadrant() {
		Point center = this.utilGetArenaCenter();
		Point me = this.utilGetMyPosition();
		if (me.x > center.x) {
			return me.y < center.y ? this.utilQuadrantNorthEast : this.utilQuadrantSouthEast; 
		} else  {
			return me.y < center.y ? this.utilQuadrantNorthWest : this.utilQuadrantSouthWest;
		}		
	}

	private double utilGetRotation(Point point) {
		double destinationAngle = this.utilGetAngle(point);
		double delta = Math.abs(this.getAngle() - destinationAngle);
		if (delta < 1) return 0; 
		int direction;
		if (delta < 180) {
			direction = this.getAngle() <= destinationAngle ? this.utilRight : this.utilLeft;
		} else {
			direction = this.getAngle() >= destinationAngle ? this.utilRight : this.utilLeft;
		}
		if (delta >= 5 & destinationAngle < 5) return 5 * direction;
		return (delta >= 5) ? destinationAngle * direction : delta * direction;
	}


//	---------------------------------------------   Declaration Session
	
	// ------------------  Events data
	private String fireHitEnemyName;
	private int fireHitPower;
	
	private String enemyName = new String();
	private double enemyAngle;
	private int enemyX;
	private int enemyY;
	
	private String fireDetectedEnemyName;
	private int firePower;
	 	
	// ------------------  Work properties
	
	private int cliclesCounter = 0;
	
	private final int modeSearchingForTarget = 0;
	private final int modeAttacking = 1;
	private final int modeDefending = 3;		
	private final int modeSearchingForAttacker = 2;
	private final int modeAttackMaximumCiclesOutOfTrack = 50;		
	private int mode = this.modeSearchingForTarget;
	private Point modeSearchForTargetDestination;
	private int modeAttackCliclesOutOfTrack = 0;
	private final int modeSearchingForAttackerCiclesLimit = 250;
	private int modeSearchingForAttackerCicles = 0;
	
	private int fireHitCount = 0;
	private final int fireHitCountLimitToSearch = 2;
		
	// utilities properties
	private Point utilArenaCenter; 

	private final int utilLeft = -1;
	private final int utilRight = 1;

	private final int utilQuadrantNorthEast = 0;
	private final int utilQuadrantSouthWest = 1;
	private final int utilQuadrantNorthWest = 2;
	private final int utilQuadrantSouthEast = 3;
}
