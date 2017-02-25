package money.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Position;

/**
 * @see money.MoneyLand#addLand(Position, Position, String)
 */
public class LandAddEvent extends LandEvent implements Cancellable {
	protected Position start;
	protected Position end;
	protected String owner;
	protected int id;

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}
	public LandAddEvent (Position start, Position end, String owner, int id){
		this.start = start;
		this.end = end;
		this.owner = owner;
		this.id = id;
	}

	public Position getStart(){
		return start;
	}

	public Position getEnd(){
		return end;
	}

	public String getOwner(){
		return owner;
	}

	public int getId(){
		return id;
	}
}
