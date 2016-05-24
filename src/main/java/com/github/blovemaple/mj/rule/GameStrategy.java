package com.github.blovemaple.mj.rule;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.ActionTypeAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 游戏策略。即一种游戏规则的定义。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface GameStrategy {

	/**
	 * 检查一个麻将桌是否符合条件开始进行一局。
	 * 
	 * @param table
	 *            麻将桌
	 * @return 如果可以开始，返回true，否则返回false。
	 */
	public boolean checkReady(MahjongTable table);

	/**
	 * 获取全部麻将牌的集合。
	 */
	public Set<Tile> getAllTiles();

	/**
	 * 在一局开始之前对上下文进行必要操作。
	 */
	public void readyContext(GameContext context);

	/**
	 * 根据当前状态获取开局的发牌动作，用于发牌。
	 */
	public Action getDealAction(GameContext context);

	/**
	 * 返回游戏进行中（发牌后，直到结束）所有动作类型列表。
	 */
	public Set<? extends ActionType> getAllActionTypesInGame();

	/**
	 * 返回听牌状态中（听牌后，直到结束，听牌的玩家）所有动作类型列表。
	 */
	public Set<? extends ActionType> getAllActionTypesInTing();

	/**
	 * 获取动作优先级比较器。优先级越高的越小。
	 */
	public Comparator<ActionTypeAndLocation> getActionPriorityComparator();

	/**
	 * 根据当前状态返回指定玩家超时默认做的动作。
	 * 
	 * @return 默认动作，null表示不做动作
	 */
	public Action getPlayerDefaultAction(GameContext context,
			PlayerLocation location, Set<ActionType> choises);

	/**
	 * 根据当前状态返回默认动作。默认动作是所有玩家都没有可选动作或均选择不做动作之后自动执行的动作。
	 * 
	 * @return 默认动作
	 */
	public ActionAndLocation getDefaultAction(GameContext context,
			Map<PlayerLocation, Set<ActionType>> choises);

	/**
	 * 获取此策略支持的所有和牌类型。
	 */
	public Set<? extends WinType> getAllWinTypes();

	/**
	 * 判断指定条件下是否可和牌。如果aliveTiles非null，则用于替换playerInfo中的信息做出判断，
	 * 否则利用playerInfo中的aliveTiles做出判断。<br>
	 * 默认实现为使用此策略支持的所有和牌类型进行判断，至少有一种和牌类型判断可以和牌则可以和牌。
	 */
	public default boolean canWin(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		return getAllWinTypes().stream()
				.anyMatch(winType -> winType.match(playerInfo, aliveTiles));
		//TODO 缓存
	}

	/**
	 * 获取此策略支持的所有番种和番数。
	 */
	public Map<? extends FanType, Integer> getAllFanTypes();

	/**
	 * 检查和牌的所有番种和番数。如果aliveTiles非null，则用于替换playerInfo中的信息做出判断，
	 * 否则利用playerInfo中的aliveTiles做出判断。<br>
	 * 默认实现为使用此策略支持的所有番种和番数进行统计。
	 */
	public default Map<FanType, Integer> getFans(PlayerInfo playerInfo,
			Set<Tile> aliveTiles) {
		// 在所有番种中过滤出所有符合的番种
		Set<FanType> fanTypes = getAllFanTypes().keySet().stream()
				.filter(fanType -> fanType.match(playerInfo, null))
				.collect(Collectors.toSet());
		// 去除被覆盖的番种
		Map<? extends FanType, Set<? extends FanType>> coveredFanTypes = getAllCoveredFanTypes();
		if (coveredFanTypes != null)
			coveredFanTypes.forEach((type, covered) -> {
				if (fanTypes.contains(type))
					fanTypes.removeAll(covered);
			});
		// 查询番数组成map
		return fanTypes.stream().collect(
				Collectors.toMap(Function.identity(), getAllFanTypes()::get));

		//TODO 缓存
	}

	/**
	 * 获取所有番种和被覆盖的番种。如果覆盖的番种和被覆盖的番种同时存在，则不计被覆盖的番种。<br>
	 * 不允许两个番种相互覆盖。
	 * 
	 * @return 覆盖的番种-所有被它覆盖的番种
	 */
	public Map<? extends FanType, Set<? extends FanType>> getAllCoveredFanTypes();

	/**
	 * 根据当前状态判断游戏是否结束。
	 */
	public boolean tryEndGame(GameContext context);
}
