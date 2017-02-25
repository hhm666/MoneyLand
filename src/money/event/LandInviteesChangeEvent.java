package money.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

import java.util.List;

/**
 * @see money.MoneyLand#setLandInvitees(Integer, List)
 */
public class LandInviteesChangeEvent extends LandEvent implements Cancellable {
	protected int id;
	protected List<String> invitees;
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}
	public LandInviteesChangeEvent(int id, List<String> invitees){
		this.id = id;
		this.invitees = invitees;
	}

	public int getId(){
		return id;
	}

	public List<String> getInvitees(){
		return invitees;
	}
}
