import java.awt.Point;
import java.util.HashMap;
import java.util.Vector;

import org.javaarena.engine.AbstractRobot;
import org.javaarena.events.EnemyDetectedEvent;
import org.javaarena.events.FireDetectedEvent;
import org.javaarena.events.FireHitEvent;

public class RaptorOld2 extends AbstractRobot {

	public RaptorOld2() {
		super("Raptor");
	}


	public void onFireHit(FireHitEvent evt) {
	}

	public void onEnemyDetected(EnemyDetectedEvent evt) {
		if (this.enemyName.equals(new String())) {
			this.enemyName = evt.getRobotName();
			this.taskAttackCiclesOutOfTrack = 0;
			this.tasksEmptyQueue();
		}
		if (this.enemyName.equals(evt.getRobotName())) {
			Point p = new Point(evt.getX(), evt.getY());
			this.enemyDegree = (int)evt.getAngle();
			this.taskRotateHotAngle = this.utilGetAngle(p);
System.out.println(this.taskRotateHotAngle);			
			this.enemyPosition = p;
			this.enemyVelocity = evt.getVelocity();
			this.taskAttackCiclesOutOfTrack = 0;
			if (!this.enemyPosition.equals(p)) {
				this.tasksEmptyQueue();
			}
		}
		this.mode = this.modeAttacking;
	}

	public void onFireDetected(FireDetectedEvent evt) {
	}

	public void update() {
		this.taskCurrent = this.taskGetNext();
		if (this.taskCurrent != this.taskNone) {
			this.taskExecute();
		} else {
			if (mode == this.modeNone) {
				return;
			}
			switch (this.mode) {
				case modeBootingRobot : { this.modeBootingRobot(); break; }
				case modeSearchingForTarget : { this.modeSearchingForTarget(); break; }
				case modeAttacking : { this.modeAttacking(); break; }
			}
		}
		this.setRotation(this.updateAngle);
		this.setVelocity(this.updateVelocity);
		this.fire(this.updateFire);
	}
	
/* -----------------------------  modes methods -------------------------------- */

	private void modeNone() {
		this.updateFire = 0;
		this.mode = this.modeNone;
	}
	
	private void modeAttacking() {
		this.setMessage("Let's dance Mr. " + this.enemyName);
		this.mode = this.modeAttacking;
		this.tasksAddAttack(enemyVelocity, enemyDegree, enemyPosition, enemyName);
	}		

	private void modeSearchingForTarget() {
		this.setMessage("You can run, but you can't hide....");
		this.updateFire = 0;
		this.mode = this.modeSearchingForTarget;
		this.tasksAddGotoXY(this.utilGetArenaCenter(), 5);
		this.tasksAddSpin(1);
	}

	private void modeBootingRobot() {
		this.setMessage("Booting robot...");
		this.updateFire = 0;
		this.modeSearchingForTarget();
	}

	
/* -----------------------------  task methods -------------------------------- */

	private int taskGetNext()  {
		if (this.taskCurrent != this.taskNone) return this.taskCurrent;
		if (this.taskQueue.isEmpty()) return this.taskNone;
		this.taskProperties = (HashMap)this.taskQueue.firstElement();
		this.taskCurrent = ((Integer)this.taskProperties.get("taskType")).intValue();
		this.taskQueue.remove(0);
		return taskCurrent;
	}
	
	private void taskExecute() {
		switch (this.taskCurrent) {
			case taskRotate: { this.taskRotateExecute(); break; }
			case taskGotoXY: { this.taskGotoXYExecute(); break; }
			case taskSpin: { this.taskSpinExecute(); break; }
			case taskAttack: { this.taskAttackExecute(); break; }
		}
	}

	private void taskSetCurrent(int task) {
		if (task == this.taskNone) {
			this.taskProperties = null;
			this.updateAngle = 0;
			this.updateVelocity = 0;
		}
		this.taskCurrent = task;
	}

	private void tasksAdd(HashMap properties, int queuePosition) {
		if (queuePosition == this.taskQueueEnd) {
			this.taskQueue.add(properties);
		} else { 
			this.taskQueue.insertElementAt(properties, this.taskQueueBegin);
		}
	}

	private void tasksEmptyQueue() {
		this.taskQueue.removeAllElements();
		this.taskSetCurrent(this.taskNone);
	}

/* ----------------------- Task Attack --------------------- */

	private void taskAttackInitialize() {
		this.updateFire = 9;
		this.tasksAddAttackForReentry(
			((Integer)this.taskProperties.get("velocity")).intValue(), 
			((Integer)this.taskProperties.get("degree")).intValue(),
			(Point)this.taskProperties.get("position"),
			(String)this.taskProperties.get("name")
		);
		Point ep = this.utilGetPointToFire((Point)this.taskProperties.get("position"), ((Integer)this.taskProperties.get("degree")).intValue(), ((Integer)this.taskProperties.get("velocity")).intValue());
		//this.tasksAddRotate(this.taskRotateHotAngle, this.taskQueueBegin, true);
		this.tasksAddGotoXY(ep, 5, false, this.taskQueueBegin);
		this.taskSetCurrent(this.taskNone);
	}

	private void taskAttackExecute() {
		if (this.taskAttackCiclesOutOfTrack > this.taskAttackMaximumCiclesOutOfTrack){
			this.taskSetCurrent(this.taskNone);
			this.utilEnemyReset();
			this.mode = this.modeSearchingForTarget;
			return;						
		}

		if (!((Boolean)this.taskProperties.get("isInitialized")).booleanValue()) {
			this.taskAttackInitialize();
		}
		this.taskAttackCiclesOutOfTrack ++;
		//int distance = utilGetDistance(this.utilGetMyPosition().x, this.utilGetMyPosition().y, this.enemyPosition.x, this.enemyPosition.x); 
		this.updateFire = 9; //distance < this.enemyMaximumDistanceForFire ? 9 : 0;
		this.updateVelocity = 0;
/*		if (distance <= 5) {
			this.updateVelocity = -5;
		} else {
			this.updateVelocity = 5; //(distance <= 100) ? 5 : -5;
		}
		
		this.updateVelocity = this.utilGetDistance(this.utilGetMyPosition().x, this.utilGetMyPosition().y, this.enemyPosition.x, this.enemyPosition.x) > 64 ? 5 : 0; //((Integer)this.taskProperties.get("velocity")).intValue();
		this.updateFire = 9; //this.utilGetDistance(this.enemyPosition.x, this.enemyPosition.y, this.utilGetMyPosition().x, this.utilGetMyPosition().y) < this.enemyMaximumDistanceForFire ? 9 : 0;
		int angle = this.utilGetAngle(this.enemyPosition);
		
		this.updateAngle = (this.utilGetAngle(this.utilGetMyPosition()) == angle) ? angle : 0;
		if (updateAngle == 0) {
			this.updateVelocity = 5; 
			this.updateFire = 9; //(distance <= 150) ? 9 : 0;
		} else {
			this.updateVelocity = 0; 
			this.updateFire = 0; //(distance <= 150) ? 9 : 0;
		}
//System.out.println(" angle: " + angle + " updateAngle " + this.updateAngle + " position " + this.enemyPosition + "  myPosition " + this.utilGetMyPosition() );
 * 
 */		  

	}
	
	private void tasksAddAttack(int enemyVelocity, int enemyDegree, Point enemyPosition, String enemyName, boolean isInitialized, int queuePosition) { 
		HashMap properties = new HashMap();
		properties.put("taskType", new Integer(this.taskAttack));
		properties.put("velocity", new Integer(enemyVelocity));
		properties.put("degree", new Integer((int)enemyDegree));
		properties.put("position", enemyPosition);
		properties.put("name", enemyName);
		properties.put("isInitialized", new Boolean(isInitialized));
		this.tasksAdd(properties, queuePosition);
	}

	private void tasksAddAttackForReentry(int enemyVelocity, int enemyDegree, Point enemyPosition, String enemyName) { 
		this.tasksAddAttack(enemyVelocity, enemyDegree, enemyPosition, enemyName, true, this.taskQueueBegin);
	}
	
	private void tasksAddAttack(int enemyVelocity, int enemyDegree, Point enemyPosition, String enemyName) { 
		this.tasksAddAttack(enemyVelocity, enemyDegree, enemyPosition, enemyName, false, this.taskQueueEnd);
	}
	
/* ----------------------- Task goto XY --------------------- */
	private void taskGotoXYInitialize() {
		Point destination = (Point)this.taskProperties.get("destinationPoint");
		this.tasksAddGotoXYForReentry(destination, ((Integer)this.taskProperties.get("velocity")).intValue(), this.taskQueueBegin);
		this.tasksAddRotate(this.utilGetAngle(destination), this.taskQueueBegin, false);
		this.taskSetCurrent(this.taskNone);
	}
	
	private void taskGotoXYExecute() {
		if (!((Boolean)this.taskProperties.get("isInitialized")).booleanValue()) {
			this.taskGotoXYInitialize();
			return;
		} 

//		Point destinationPoint = (Point)this.taskProperties.get("destinationPoint");

		Point destinationPoint = this.mode == this.modeAttacking ? this.enemyPosition : (Point)this.taskProperties.get("destinationPoint");

		this.updateVelocity = ((Integer)this.taskProperties.get("velocity")).intValue();

		if ((Math.abs(this.utilGetMyPosition().x - destinationPoint.x) < 64) && (Math.abs(this.utilGetMyPosition().y - destinationPoint.y) < 64)) {
			this.updateVelocity = 0;
			this.taskCurrent = taskNone;
		}
	}
	
	private void tasksAddGotoXY(Point destinationPoint, int velocity, boolean isInitialized, int queuePosition) {
		HashMap properties = new HashMap();
		properties.put("taskType", new Integer(this.taskGotoXY));
		properties.put("destinationPoint", destinationPoint);
		properties.put("isInitialized", new Boolean(isInitialized));
		properties.put("velocity", new Integer(velocity));
		this.tasksAdd(properties, queuePosition);
	}
	
	private void tasksAddGotoXYForReentry(Point destinationPoint, int velocity, int queuePosition) {
		this.tasksAddGotoXY(destinationPoint, velocity, true, queuePosition);
	}

	private void tasksAddGotoXY(Point destinationPoint, int velocity) {
		this.tasksAddGotoXY(destinationPoint, velocity, false, this.taskQueueEnd);
	}
	
/* ----------------------- Task Spin --------------------- */

	private void taskSpinExecute() {
		this.updateVelocity = ((Integer)this.taskProperties.get("velocity")).intValue();
		this.updateAngle = 15;
	}
	
	private void tasksAddSpin(int velocity) {
		HashMap properties = new HashMap();
		properties.put("taskType", new Integer(this.taskSpin));
		properties.put("velocity", new Integer(velocity));
		this.tasksAdd(properties, this.taskQueueEnd);
	}
	
/* ----------------------- Rotate task --------------------- */
	private void taskRotateInitialize()  {
		int destinationAngle;
		if (((Boolean)this.taskProperties.get("isHot")).booleanValue()) {
			destinationAngle = this.taskRotateHotAngle;
		} else {
			destinationAngle = ((Integer)this.taskProperties.get("angle")).intValue(); 
		}
		 
		int delta = Math.abs(this.utilGetMyAngle() - destinationAngle);
		int direction;
		if (delta < 180) {
			direction = this.utilGetMyAngle() <= destinationAngle ? this.utilRight : this.utilLeft;
		} else {
			direction = this.utilGetMyAngle() >= destinationAngle ? this.utilRight : this.utilLeft;
		}
		this.taskProperties.put("direction", new Integer(direction)); 
		this.taskProperties.put("isInitialized", new Boolean(true));
	}

	private void taskRotateExecute() {
/*		if (!((Boolean)this.taskProperties.get("isInitialized")).booleanValue()) {
			this.taskRotateInitialize();
		}
		int destinationAngle = ((Integer)this.taskProperties.get("angle")).intValue();
		int direction = ((Integer)this.taskProperties.get("direction")).intValue();
		int delta = Math.abs(this.utilGetMyAngle() - destinationAngle);
*/
		this.updateVelocity = 0;
		int destinationAngle = ((Integer)this.taskProperties.get("angle")).intValue(); 
		int delta = Math.abs(this.utilGetMyAngle() - destinationAngle);
		int direction;
		if (delta < 180) {
			direction = this.utilGetMyAngle() <= destinationAngle ? this.utilRight : this.utilLeft;
		} else {
			direction = this.utilGetMyAngle() >= destinationAngle ? this.utilRight : this.utilLeft;
		}
		if (delta == 0) {
			this.taskSetCurrent(this.taskNone);
			return;
		}
		if (delta >= 5 & destinationAngle < 5) {
			this.updateAngle = 5 * direction;
		} else {
			this.updateAngle = (delta >= 5) ? destinationAngle * direction : delta * direction;
		}
	}
	
	private void tasksAddRotate(int angle) {
		this.tasksAddRotate(angle, this.taskQueueEnd, false);
	}
	
	private void tasksAddHotRotate(int angle) {
		this.tasksAddRotate(angle, this.taskQueueEnd, true);
	}

	private void tasksAddRotate(int angle, int queuePosition, boolean isHot) {
		HashMap properties = new HashMap();
		properties.put("taskType", new Integer(this.taskRotate));
		properties.put("isHot", new Boolean(true));
		angle = Math.abs(angle);
		if (angle == 0) {
			properties.put("angle", new Integer(1));
		} else {
			properties.put("angle", new Integer(angle));
		}
		properties.put("isInitialized", new Boolean(false));
		this.tasksAdd(properties, queuePosition);
	}

/* --------------------------- Utilitaries methods -------------------------- */	

	private int utilGetQuadrant() {
		// TODO morte declarada.....
		Point center = this.utilGetArenaCenter();
		Point me = this.utilGetMyPosition();
		if (me.x > center.x) {
			return me.y < center.y ? this.utilQuadrantNorthEast : this.utilQuadrantSouthEast; 
		} else  {
			return me.y < center.y ? this.utilQuadrantNorthWest : this.utilQuadrantSouthWest;
		}		
	}

	private int utilGetAngleToCenter() {
		return utilGetAngle(this.utilGetArenaCenter());
	}

	private int utilGetAngle(Point target) {
		double angle;
		try {
			angle = Math.toDegrees(Math.atan2((target.y - this.utilGetMyPosition().y) * -1, (target.x - this.utilGetMyPosition().x)));
		} catch (Exception e ) {
			angle = 90;
		}
		angle = (angle < 0) ? angle + 360 : angle;

		return (int)angle;
	}

	private int utilGetMyAngle() {
		return (int)Math.round(this.getAngle());
	}
	
	private Point utilGetArenaCenter() {
		int x = (int)this.getScreenBounds().width / 2;
		int y = (int)this.getScreenBounds().height / 2;
		return new Point(x,y);
	}

	private Point utilGetMyPosition() {
		int x = (int)this.getPosition().x + (this.getBounds().width  / 2);
		int y = (int)this.getPosition().y + (this.getBounds().height / 2);
		return new Point(x,y);
	}
	
	private Point utilGetPointToFire(Point target, int angle, int velocity) {

/*		int distance = velocity * 50;
		double deltaX = distance * Math.sin(angle);		
		double deltaY = distance * Math.cos(angle);		
		System.out.println("Velocity " + velocity + " Angle " + angle);
		System.out.println("Me " + this.utilGetMyPosition());
		System.out.println("Target " + target);
		System.out.println("Delta " + deltaX + " " + deltaY);
		System.out.println("Destination " + new Point((int)(deltaX + target.x), (int)(deltaY + target.y)));
		return new Point((int)(deltaX + target.x), (int)(deltaY + target.y));
*/		
		int deltaX = (int)((target.x - this.utilGetMyPosition().x) / 3) * 2; 
		int deltaY = (int)((target.y - this.utilGetMyPosition().y) / 3) * 2;
		return new Point(deltaX + this.utilGetMyPosition().x, deltaY + this.utilGetMyPosition().y);

	}

	private void utilEnemyReset() {
		this.enemyDegree = 0;
		this.enemyName = new String();
		this.enemyPosition = null;
		this.enemyVelocity = 0;
	}
	
	private int utilGetDistance(int x, int y, int x2, int y2) {
		return (int)Math.round(Math.sqrt((x - x2) + (y - y2)));  
	}

/* -----------------------------  properties -------------------------------- */


	private final int modeNone = 0;
	private final int modeBootingRobot = 1;
	private final int modeSearchingForTarget = 2;
	private final int modeAttacking = 3;
	private final int modeSearchingForAttacker = 4;
	private int mode = this.modeBootingRobot;
	
	// this block defines the Tasks queue and properties. 
	// the names are define so badly because we are missing realy OOP in our code :), but the names are sucks.... 
	private final int taskNone = 0;
	private final int taskRotate = 1;
	private final int taskGotoXY = 2;
	private final int taskSeek = 3;
	private final int taskSpin = 4;
	private final int taskAttack = 5;
	private final int taskQueueBegin = 0; 
	private final int taskQueueEnd = -1; 
	private int taskCurrent = this.taskNone;
	private HashMap taskProperties;
 	private Vector taskQueue = new Vector();
	private int taskAttackCiclesOutOfTrack = 0;
	private int taskAttackMaximumCiclesOutOfTrack = 10;
	private int taskRotateHotAngle;
 	
 	// utilities properties
	private final int utilLeft = -1;
	private final int utilRight = 1;
	private final int utilQuadrantNorthEast = 0;
	private final int utilQuadrantSouthWest = 1;
	private final int utilQuadrantNorthWest = 2;
	private final int utilQuadrantSouthEast = 3;
	
	// properties for update
	private int updateAngle;
	private int updateVelocity;
	private int updateFire;
	private int updateCiclesCounter;
	
	// properties of the Enemy
	// properties for enemy
	private int enemyVelocity;
	private int enemyDegree;
	private Point enemyPosition;
	private String enemyName = new String();
	private final int enemyMaximumDistanceForFire = 256;
}
