package money.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @see money.MoneyLand#removeLand(Integer)
 */
public class LandRemoveEvent extends LandEvent implements Cancellable {
	protected int id;

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}
	public LandRemoveEvent(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}
}
