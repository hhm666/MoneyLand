package money.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Position;

/**
 * @see money.MoneyLand#setLandEndPosition(Integer, Position)
 */
public class LandEndPositionChangeEvent extends LandEvent implements Cancellable {
	protected int id;
	protected Position end;
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}

	public LandEndPositionChangeEvent(int id, Position end) {
		this.id = id;
		this.end = end;
	}

	public int getId() {
		return id;
	}

	public Position getEnd() {
		return end;
	}
}
