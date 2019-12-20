package com.burning8393.herostory.rank;

import com.alibaba.fastjson.JSONObject;
import com.burning8393.herostory.async.AsyncOperationProcessor;
import com.burning8393.herostory.async.IAsyncOperation;
import com.burning8393.herostory.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 排行榜服务
 */
public final class RankService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RankService.class);

    /**
     * 单例对象
     */
    private static final RankService instance = new RankService();

    /**
     * 私有化类默认构造器
     */
    private RankService() {

    }

    /**
     * 获取单例对象
     * @return 单例对象
     */
    public static RankService getInstance() {
        return instance;
    }

    /**
     * 获取排行榜
     *
     * @param callback 回调函数
     */
    public void getRank(Function<List<RankItem>, Void> callback) {
        if (null == callback) {
            return;
        }

        IAsyncOperation asyncOp = new AsyncGetRank() {
            @Override
            public void doFinish() {
                callback.apply(this.getRankItemList());
            }
        };

        AsyncOperationProcessor.getINSTANCE().process(asyncOp);
    }

    /**
     * 异步方式获取排行榜
     */
    private class AsyncGetRank implements IAsyncOperation {

        /**
         * 排行条目列表
         */
        private List<RankItem> rankItemList = null;

        /**
         * 获取排行条目列表
         *
         * @return 排行条目列表
         */
        public List<RankItem> getRankItemList() {
            return rankItemList;
        }

        @Override
        public void doAsync() {
            try (Jedis jedis = RedisUtil.getJedis()) {
                // 获取字符串集合
                Set<Tuple> valSet = jedis.zrevrangeWithScores("Rank", 0, 9);

                rankItemList = new ArrayList<>();
                int rankId = 0;

                for (Tuple tuple : valSet) {
                    // 获取用户id
                    int userId = Integer.parseInt(tuple.getElement());

                    // 获取用户基本信息
                    String jsonStr = jedis.hget("User_" + userId, "BasicInfo");
                    if (null == jsonStr) {
                        continue;
                    }

                    // 创建排名条目
                    RankItem rankItem = new RankItem();
                    rankItem.userId = userId;
                    rankItem.rankId = ++rankId;
                    rankItem.win = (int) tuple.getScore();

                    JSONObject jsonObject = JSONObject.parseObject(jsonStr);

                    rankItem.userName = jsonObject.getString("userName");
                    rankItem.heroAvatar = jsonObject.getString("heroAvatar");

                    rankItemList.add(rankItem);
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * 刷新排行榜
     *
     * @param winnerId 获胜者id
     * @param loseId 失败者id
     */
    public void refreshRank(int winnerId, int loseId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            // 增加用户的胜利和失败的次数
            jedis.hincrBy("User_" + winnerId, "Win", 1);
            jedis.hincrBy("User_" + loseId, "Lose", 1);

            // 查看玩家总共赢了多少次？
            String winStr = jedis.hget("User_" + winnerId, "Win");
            int winInt = Integer.parseInt(winStr);

            // 修改排名数据
            jedis.zadd("Rank", winInt, "User_" + winnerId);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
