package money.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @see money.MoneyLand#setLandOwner(Integer, String)
 */
public class LandOwnerChangeEvent extends LandEvent implements Cancellable {
	protected int id;
	protected String owner;
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}
	public LandOwnerChangeEvent(int id, String owner){
		this.id = id;
		this.owner = owner;
	}

	public int getId(){
		return id;
	}

	public String getOwner(){
		return owner;
	}
}
