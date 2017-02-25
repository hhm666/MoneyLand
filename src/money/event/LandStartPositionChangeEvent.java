package money.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Position;

/**
 * @see money.MoneyLand#setLandStartPosition(Integer, Position)
 */
public class LandStartPositionChangeEvent extends LandEvent implements Cancellable {
	protected int id;
	protected Position start;
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}
	public LandStartPositionChangeEvent(int id, Position start){
		this.id = id;
		this.start = start;
	}

	public int getId(){
		return id;
	}

	public Position getStart(){
		return start;
	}
}
