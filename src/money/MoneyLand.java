package money;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import money.event.*;

import java.util.*;
import java.util.function.BiConsumer;

public final class MoneyLand extends PluginBase implements MoneyLandAPI, Listener {
	private static Map<String, Map<String, String>> setLand = new HashMap<>();
	private static Map<String, Object> config = new HashMap<>();
	private static Map<Integer, Map<String, Object>> data = new HashMap<>();
	private static String dataFolder;

	private static MoneyLand land;

	public static MoneyLand getInstance() {
		return land;
	}


	@EventHandler(priority = EventPriority.HIGH)
	public void onBreak(BlockBreakEvent event) {
		if ((!checkPermission(event.getPlayer(), event.getBlock())) && (!event.getPlayer().isOp())) {
			event.getPlayer().sendMessage(TextFormat.RED + "你没有权限使用这里");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlace(BlockPlaceEvent event) {
		if ((!checkPermission(event.getPlayer(), event.getBlock())) && (!event.getPlayer().isOp())) {
			event.getPlayer().sendMessage(TextFormat.RED + "你没有权限使用这里");
			event.setCancelled(true);
		}
	}


	private void loadData() {
		try {
			Map<String, Object> dat = new Config(getDataFolder() + "/Data.dat", Config.YAML).getAll();
			for (Map.Entry<String, Object> entry : dat.entrySet()) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> info = (HashMap<String, Object>) entry.getValue();
				data.put(new Integer(entry.getKey()), info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveData() {
		LinkedHashMap<String, Object> dat = new LinkedHashMap<>();
		data.forEach((k, v) -> dat.put(k.toString(), v));
		Config con = new Config(dataFolder + "/Data.dat", Config.YAML);
		con.setAll(dat);
		con.save();
	}


	@Override
	public void onLoad() {
		land = this;
	}

	@Override
	public void onDisable() {
		saveData();
	}

	@Override
	public void onEnable() {
		getDataFolder().mkdirs();
		saveResource("Config.yml", false);

		config = new Config(getDataFolder() + "/Config.yml", Config.YAML).getAll();

		loadData();
		dataFolder = getDataFolder().getPath();

		try {
			Server.getInstance().getScheduler().scheduleDelayedTask(this, this::saveData, 20 * 60 * 2);
		} catch (Exception e) {
			//noinspection deprecation
			Server.getInstance().getScheduler().scheduleDelayedTask(this::saveData, 20 * 60 * 2);
		}

		Server.getInstance().getPluginManager().registerEvents(this, this);

		PluginCommand cmd;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String msg = callCommand(sender, command, args);
		if (!Objects.equals(msg, "")) {
			sender.sendMessage(TextFormat.AQUA + msg);
			return true;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private String callCommand(CommandSender poster, Command command, String[] args) {
		Player sender = null;
		if (poster instanceof Player) {
			sender = (Player) poster;
		}

		Position start, end;
		Map<String, String> data;
		Position pos;
		Integer id;
		String owner, name;
		switch (command.getName().toLowerCase()) {
			case "startp":
			case "起始点":
				if (sender == null) {
					return "请在游戏内使用";
				}

				if (((List<String>) config.getOrDefault("no-land-worlds", new ArrayList<>())).contains(sender.getLevel().getFolderName())) {
					return "该世界不允许购买领地";
				}

				setLand.put(sender.getName(), new HashMap<>());
				setLand.get(sender.getName()).put("start", ((int) sender.x) + "," + ((int) sender.y) + "," + ((int) sender.z) + "," + sender.getLevel().getFolderName());
				return "领地起始点保存. 请点击在结束点处使用/endp";
			case "endp":
			case "结束点":
				if (sender == null) {
					return "请在游戏内使用";
				}

				data = setLand.get(sender.getName());
				if (data == null) {
					return "请先使用 /startp";
				}

				pos = parsePosition(data.get("start"));
				if (pos == null) {
					return "请先使用 /startp";
				}

				if (pos.getLevel().getId() != sender.getLevel().getId()) {
					return "请在同一世界使用 /startp 和 /endp";
				}

				//setLand.put(sender.getName(), new HashMap<>());
				setLand.get(sender.getName()).put("end", ((int) sender.x) + "," + ((int) sender.y) + "," + ((int) sender.z) + "," + sender.getLevel().getFolderName());
				return "领地结束点保存. 价格: " + Math.round((Math.abs((pos.x - sender.getX()) * (pos.z - sender.getZ()))) * Double.parseDouble(config.getOrDefault("square-price", "100").toString()));
			case "landop":
				if (!poster.isOp()) {
					return "你没有权限使用这个指令";
				}

				if (args.length < 1) {
					return "请使用: /landop <remove | see | list>   /领地 <删除 | 查看 | 列表>";
				}

				switch (args[0]) {
					case "list":
					case "列表":
						final Integer page = (args.length >= 2) ? new Integer(args[1]) : 0;
						poster.sendMessage(TextFormat.AQUA + "显示领地列表: (格式: #ID.[主人] 名称: 面积)");
						new Thread(() -> {
							final String[] msg = {""};
							final HashMap<Integer, String> map = new HashMap<>();


							MoneyLand.data.forEach((landId, oInfo) -> {
								try {
									Position startPos = parsePosition(oInfo.get("start").toString());
									Position endPos = parsePosition(oInfo.get("end").toString());
									if (startPos != null && endPos != null && startPos.getLevel() != null && endPos.getLevel() != null) {
										map.put(map.size(), TextFormat.YELLOW + "#" + landId.toString() + ".[" + oInfo.get("owner") + "] " + oInfo.get("name") + ": " + Math.round(Math.abs((startPos.x - endPos.x) * (startPos.z - endPos.getZ()))) + " ㎡");
									}
								} catch (Exception ignore) {


								}
							});

							if (page * 6 > map.size()) {//show last page
								Integer max = map.size() / 6;
								max = max * 6;
								for (int i = max; i < map.size(); i++) {
									msg[0] += map.getOrDefault(i, "") + "\n";
								}
							} else {
								for (int i = page * 6; i < (page + 1) * 6; i++) {
									msg[0] += map.getOrDefault(i, "") + "\n";
								}
							}

							poster.sendMessage(msg[0]);
						}).start();
						return "";
					case "see":
					case "查看":
						Integer landId;
						if ((landId = getLandId(args[2])) == null) {
							landId = new Integer(args[2]);
						}

						String landOwner = getLandOwner(landId);
						start = getLandStartPosition(landId);
						end = getLandStartPosition(landId);
						if (start == null || end == null || landOwner == null) {
							return "该领地不存在";
						}

						return TextFormat.YELLOW + "#" + landId.toString() + ".[" + landOwner + "] " + getLandName(landId) + ": " + Math.round(Math.abs((start.x - end.x) * (start.z - end.getZ()))) + " ㎡";

					case "remove":
					case "删除":
					case "delete":
						if ((landId = getLandId(args[2])) == null) {
							landId = new Integer(args[2]);
						}

						removeLand(landId);
						return "删除 ID 为 " + landId + " 的领地完成";
					default:
						return "请使用: /landop <remove | see | list>   /领地 <删除 | 查看 | 列表>";
				}
			case "land":
			case "领地":
				if (sender == null) {
					return "请在游戏内使用. 领地OP命令:  /landop";
				}

				if (args.length < 1) {
					return "请使用: " +
							"/land buy 购买领地;  " +
							"/land name <名字> 修改领地名字;  " +
							"/land whose [领地编号/领地名] 查看脚下或指定编号或指定领地名的领地信息;  " +
							"/land sell 回收领地; " +
							"/land goto 传送到某领地;  " +
							"/land invite <领地编号/领地名> <对方昵称> 把领地分享给某人;  " +
							"/land list [页码] 查看领地列表;  ";
				}

				switch (args[0]) {
					default:
						return "请使用: " +
								"/land buy 购买领地;  " +
								"/land name <名字> 修改领地名字;  " +
								"/land whose [领地编号/领地名] 查看脚下或指定编号或指定领地名的领地信息;  " +
								"/land sell [领地编号/领地名] 回收领地; " +
								"/land goto <领地编号/领地名> 传送到某领地;  " +
								"/land invite <领地编号/领地名> <对方昵称> 把领地分享给某人;  " +
								"/land list [页码] 查看领地列表;  ";

					case "buy":
					case "购买":
						data = setLand.get(sender.getName());
						if (data == null) {
							return "请先使用 /startp";
						}

						String sss = data.get("start");
						if (sss == null) {
							return "请先使用 /startp";
						}
						pos = parsePosition(sss);
						if (pos == null) {
							return "未知错误";
						}

						int need = Math.abs((pos.getFloorX() - sender.getFloorX()) * (pos.getFloorZ() - sender.getFloorZ())) * Integer.parseInt(config.getOrDefault("square-price", "100").toString());
						double money = Money.getInstance().getMoney(sender);
						if (need > money) {
							return "你没有足够的钱. 需要 " + Math.round(need) + ", 你有 " + new Long(Math.round(money)).intValue();
						}

						Money.getInstance().setMoney(sender, money - need);


						addLand(sender, pos, sender);
						return "购买成功";
					case "sell":
					case "出售":
					case "回收":

						Integer landId;
						String landOwner;


						if (args.length < 2) {
							if ((landId = getLandId(args[1])) == null) {
								landId = new Integer(args[1]);
							}

							landOwner = getLandOwner(landId);
							start = getLandStartPosition(landId);
							end = getLandStartPosition(landId);
							if (start == null || end == null || landOwner == null) {
								return "该领地不存在";
							}

							Double sellMoney;

							Money.getInstance().setMoney(sender,
									Money.getInstance().getMoney(sender) +
											(sellMoney = Math.abs((start.x - end.getX()) * (start.z - end.getZ()))
													* new Long(config.getOrDefault("sell-square-price", "50").toString())));
							removeLand(landId);

							saveData();
							return "卖出成功, 获得 " + sellMoney.toString();
						}

						if ((landId = getLandId(sender)) != null) {
							landOwner = getLandOwner(landId);
							start = getLandStartPosition(landId);
							end = getLandStartPosition(landId);
							if (start == null || end == null || landOwner == null) {
								return "该领地不存在";
							}

							Double sellMoney;

							Money.getInstance().setMoney(sender,
									Money.getInstance().getMoney(sender) +
											(sellMoney = Math.abs((start.x - end.getX()) * (start.z - end.getZ()))
													* new Long(config.getOrDefault("sell-square-price", "50").toString())));
							removeLand(landId);
							sender.sendMessage("卖出成功, 获得 " + sellMoney.toString());
							saveData();
						} else {
							return "该领地不存在";
						}

						return "";
					case "name":
					case "changename":
					case "改名":
					case "改名字":
					case "修改名字":
						if (args.length < 3) {
							return "请使用 /land name <领地 ID> <领地名>   修改领地名";
						}

						id = getLandId(args[1]);
						if (id == null) {
							return "该领地不存在";
						}

						if (!Objects.equals(getLandOwner(id), sender.getName())) {
							return "你不是该领地的房主";
						}

						setLandName(id, args[2]);

						saveData();
						return "设置成功";


					case "list":
					case "列表":
						final Player finalSender = sender;

						final Integer page = (args.length >= 2) ? new Integer(args[1]) : 0;
						sender.sendMessage(TextFormat.AQUA + "显示领地列表: (格式: #ID.[主人] 名称: 面积)");
						//System.out.println(MoneyLand.data);
						new Thread(() -> {
							final String[] msg = {""};
							final HashMap<Integer, String> map = new HashMap<>();


							MoneyLand.data.forEach((landId2, oInfo) -> {
								try {

									//@SuppressWarnings("unchecked")

									Position startPos = parsePosition(oInfo.get("start").toString());
									Position endPos = parsePosition(oInfo.get("end").toString());
									if (startPos != null && endPos != null && startPos.getLevel() != null && endPos.getLevel() != null) {
										map.put(map.size(), TextFormat.YELLOW + "#" + landId2.toString() + ".[" + oInfo.get("owner") + "] " + oInfo.get("name") + ": " + Math.round(Math.abs((startPos.x - endPos.x) * (startPos.z - endPos.getZ()))) + " ㎡");
									}


								} catch (Exception ignore) {


								}
							});

							//System.out.println(map);

							if (page * 6 > map.size()) {//show last page
								Integer max = map.size() / 6;
								max = max * 6;
								for (int i = max; i < map.size(); i++) {
									msg[0] += map.getOrDefault(i, "") + "\n";
								}
							} else {
								for (int i = page * 6; i < (page + 1) * 6; i++) {
									msg[0] += map.getOrDefault(i, "") + "\n";
								}
							}

							finalSender.sendMessage(msg[0]);
						}).start();

						return "";
					case "goto":
					case "传送":
						if (args.length < 2) {
							return "请使用 /land goto <领地ID或领地名>   传送到该领地";
						}

						if ((id = getLandId(args[1])) == null) {
							id = new Integer(args[1]);
							if ((pos = getLandStartPosition(id)) == null) {
								return "该领地ID或领地名不存在";
							}

							sender.teleport(pos);
						} else {
							sender.teleport(getLandStartPosition(id));
						}

						return "传送中...";

					case "whose":
					case "谁的":
					case "主人":
					case "脚下":
					case "this":
					case "that":
					case "here":
					case "查看":

						if (args.length >= 2) {
							if ((landId = getLandId(args[1])) == null) {
								landId = new Integer(args[1]);
							}
							landOwner = getLandOwner(landId);
							start = getLandStartPosition(landId);
							end = getLandStartPosition(landId);
							if (start == null || end == null || landOwner == null) {
								return "该领地不存在";
							}

							return TextFormat.GRAY + "#" + landId + ".[" + landOwner + "] " + getLandName(landId) + ": " + Math.round(Math.abs((start.x - end.x) * (start.z - end.z))) + " ㎡";
						}

						if ((landId = getLandId(sender)) != null) {
							landOwner = getLandOwner(landId);
							start = getLandStartPosition(landId);
							end = getLandEndPosition(landId);
							if (start == null || end == null || landOwner == null) {
								return "你不处于任何人的领地中";
							}
						} else {
							return "你不处于任何人的领地中";
						}

						return TextFormat.GRAY + "#" + landId + ".[" + landOwner + "] " + getLandName(landId) + ": " + Math.round(Math.abs((start.x - end.getX()) * (start.z - end.getZ()))) + " ㎡";
					case "invite":
					case "分享":
					case "共享":
						if (args.length < 3) {
							return "/land invite <领地编号/领地名> <对方昵称> 把领地分享给某人";
						}

						try {
							if (args.length >= 3) {
								if ((landId = getLandId(args[1])) == null) {
									landId = new Integer(args[1]);
								}
								if (MoneyLand.data.get(landId) == null) {
									return "该领地不存在";
								}

								if (!Objects.equals(getLandOwner(landId), sender.getName())) {
									return "你不是该领地的房主";
								}

								List<String> invitees = getLandInvitees(landId);
								if (invitees == null) {
									invitees = new ArrayList<>();
								}

								if (invitees.contains(args[2])) {
									invitees.remove(args[2]);
									sender.sendMessage("取消分享成功");
								} else {
									invitees.add(args[2]);
									sender.sendMessage("分享成功");
								}
								setLandInvitees(landId, invitees);

								return "";
							} else {
								return "/land invite <领地编号/领地名> <对方昵称> 把领地分享给某人";
							}
						} catch (Exception ignore) {

						}

						return "";
				}
		}
		return "";
	}


	@Deprecated
	public String getDataType() {
		return "YAML";
	}

	@SuppressWarnings("unchecked")
	public boolean checkPermission(Player player, Block block) {
		try {
			if (block == null || player == null) {
				return false;
			}

			Integer id = getLandId(block);

			if (id == null) {
				return !((List<String>) config.getOrDefault("land-worlds", new ArrayList<>())).contains(block.getLevel().getFolderName());
			}

			Map<String, Object> info = data.get(id);
			if (Objects.equals(player.getName(), info.get("owner").toString())) {
				return true;
			}

			@SuppressWarnings("unchecked")
			List<String> invitees = (List<String>) info.get("invitees");
			return invitees != null && invitees.contains(player.getName().toLowerCase()) && !((List<String>) config.getOrDefault("land-worlds", new ArrayList<>())).contains(block.getLevel().getFolderName());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public void checkPermissionThen(Player player, Block block, BiConsumer<Boolean, Integer> request) {
		try {
			if (block == null || player == null) {
				return;
			}

			Integer id = getLandId(block);
			if (id == null) {
				return;
			}

			Map<String, Object> info = data.get(id);
			if (Objects.equals(player.getName(), info.get("owner").toString())) {
				request.accept(true, id);
				return;
			}

			@SuppressWarnings("unchecked")
			List<String> invitees = (List<String>) info.get("invitees");
			if (invitees.contains(player.getName())) {
				request.accept(true, id);
				return;
			}

			request.accept(false, null);
		} catch (Exception ignore) {

		}
	}

	@Deprecated
	public void checkPermissionThen(Player player, Block block, BiConsumer<Boolean, Integer> request, Boolean threaded) {
		if (threaded) {
			new Thread(() -> checkPermissionThen(player, block, request));
		} else {
			checkPermissionThen(player, block, request);
		}
	}

	public boolean isInLand(Position pos) {
		return getLandId(pos) != null;
	}

	@SuppressWarnings("unchecked")
	public Integer getLandId(Position pos) {
		try {
			for (Map.Entry<Integer, Map<String, Object>> entry : data.entrySet()) {

				Map<String, Object> info = entry.getValue();

				Position start = parsePosition(info.get("start").toString());
				Position end = parsePosition(info.get("end").toString());

				if (start == null || end == null) {
					continue;
				}

				if (pos.getLevel() == null || start.getLevel() == null || !Objects.equals(pos.getLevel().getFolderName(), start.getLevel().getFolderName())) {
					continue;
				}

				if (((pos.x >= start.x && pos.x <= end.x) || (pos.x >= end.x && pos.x <= start.x)) &&
						((pos.z >= start.z && pos.z <= end.z) || (pos.z >= end.z && pos.z <= start.z))) {
					return entry.getKey();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Integer getLandId(String name) {
		for (Map.Entry<Integer, Map<String, Object>> entry : data.entrySet()) {
			try {
				Map<String, Object> info = entry.getValue();
				if (Objects.equals(info.getOrDefault("name", null), name)) {
					return entry.getKey();
				}

			} catch (Exception ignore) {

			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public String getLandName(int id) {
		try {
			return ((String) data.get(id).get("name"));
		} catch (Exception ignore) {

		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public String getLandOwner(int id) {
		try {
			return ((String) data.get(id).get("owner"));
		} catch (Exception ignore) {

		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Position getLandStartPosition(int id) {
		try {
			return parsePosition(data.get(id).get("start").toString());
		} catch (Exception ignore) {

		}

		return null;
	}

	private static Position parsePosition(String pos) {
		try {
			String[] woc = pos.split(",");
			//System.out.println(Arrays.toString(woc));
			if (woc.length == 4) { //x y z level
				return new Position(new Double(woc[0]), new Double(woc[1]), new Double(woc[2]), Server.getInstance().getLevelByName(woc[3]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Position getLandEndPosition(int id) {
		try {
			return parsePosition(data.get(id).get("end").toString());
		} catch (Exception ignore) {

		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public List<String> getLandInvitees(int id) {
		try {
			return ((List<String>) data.get(id).get("invitees"));
		} catch (Exception ignore) {

		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public void setLandName(int id, String name) {
		try {
			LandNameChangeEvent event = new LandNameChangeEvent(id, name);
			Server.getInstance().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}
			data.getOrDefault(id, new HashMap<>()).put("name", name);
			saveData();
		} catch (Exception ignore) {

		}
	}

	@SuppressWarnings("unchecked")
	public void setLandOwner(int id, String owner) {
		try {
			LandOwnerChangeEvent event = new LandOwnerChangeEvent(id, owner);
			Server.getInstance().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}
			data.getOrDefault(id, new HashMap<>()).put("owner", owner);
			saveData();
		} catch (Exception ignore) {

		}
	}

	@SuppressWarnings("unchecked")
	public void setLandStartPosition(int id, Position start) {
		try {
			LandStartPositionChangeEvent event = new LandStartPositionChangeEvent(id, start);
			Server.getInstance().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}
			data.getOrDefault(id, new HashMap<>()).put("start", start);
			saveData();
		} catch (Exception ignore) {

		}
	}

	@SuppressWarnings("unchecked")
	public void setLandEndPosition(int id, Position end) {
		try {
			LandEndPositionChangeEvent event = new LandEndPositionChangeEvent(id, end);
			Server.getInstance().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}
			data.getOrDefault(id, new HashMap<>()).put("end", end);
			saveData();
		} catch (Exception ignore) {

		}
	}

	@SuppressWarnings("unchecked")
	public void setLandInvitees(int id, List<String> invitees) {
		try {
			LandInviteesChangeEvent event = new LandInviteesChangeEvent(id, invitees);
			Server.getInstance().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}
			data.getOrDefault(id, new HashMap<>()).put("invitees", invitees);
			saveData();
		} catch (Exception ignore) {

		}
	}

	public Integer addLand(Position start, Position end, String owner) {
		final Integer[] max = {0};
		new Thread(() -> {
			data.forEach((id, xx) -> {
				if (id >= max[0]) {
					max[0] = id + 1;
				}
			});

			LandAddEvent event = new LandAddEvent(start, end, owner, max[0]);
			Server.getInstance().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}


			HashMap<String, Object> info = new HashMap<>();
			info.put("start", ((int) start.x) + "," + ((int) start.y) + "," + ((int) start.z) + "," + start.getLevel().getFolderName());
			info.put("owner", owner);
			info.put("end", ((int) end.x) + "," + ((int) end.y) + "," + ((int) end.z) + "," + end.getLevel().getFolderName());
			info.put("invitees", new ArrayList<String>());
			info.put("name", max[0].toString());

			data.put(max[0], info);
			saveData();
		}).start();

		return max[0];
	}

	public Integer addLand(Position start, Position end, Player owner) {
		return addLand(start, end, owner.getName());
	}

	public void removeLand(int id) {
		LandRemoveEvent event = new LandRemoveEvent(id);
		Server.getInstance().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		data.remove(id);
		saveData();
	}

}
