package money;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Position;

import java.util.List;
import java.util.function.BiConsumer;


/**
 * 源码弄丢了....
 * Javadoc 都没了
 * 懒得再写了 23333
 */
public interface MoneyLandAPI {
	static MoneyLand getInstance() {
		return MoneyLand.getInstance();
	}

	/** @deprecated */
	@Deprecated
	String getDataType();

	Integer addLand(Position var1, Position var2, String var3);

	Integer addLand(Position var1, Position var2, Player var3);

	Integer getLandId(Position var1);

	Integer getLandId(String var1);

	String getLandName(int var1);

	String getLandOwner(int var1);

	List<String> getLandInvitees(int var1);

	Position getLandStartPosition(int var1);

	Position getLandEndPosition(int var1);

	boolean isInLand(Position var1);

	boolean checkPermission(Player var1, Block var2);

	void checkPermissionThen(Player var1, Block var2, BiConsumer<Boolean, Integer> var3);

	/** @deprecated */
	@Deprecated
	void checkPermissionThen(Player var1, Block var2, BiConsumer<Boolean, Integer> var3, Boolean var4);

	void setLandName(int var1, String var2);

	void setLandOwner(int var1, String var2);

	void setLandStartPosition(int var1, Position var2);

	void setLandEndPosition(int var1, Position var2);

	void setLandInvitees(int var1, List<String> var2);

	void removeLand(int var1);
}
