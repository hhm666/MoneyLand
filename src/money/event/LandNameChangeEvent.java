package money.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @see money.MoneyLand#setLandName(Integer, String)
 */
public class LandNameChangeEvent extends LandEvent implements Cancellable{
	protected int id;
	protected String name;
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}
	public LandNameChangeEvent(int id, String name){
		this.id = id;
		this.name = name;
	}

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}
}
