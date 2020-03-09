package org.xy.redis.sharing.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author wangxianyu
 */
public class RedisScanExtendedUtil {
    private static final long STEP_SCAN_SIZE = 500L;

    enum Ignore {
        /**
         * 分步扫描
         * 不会造成阻塞
         * 不保证正确性
         */
        SCAN,
        /**
         * 全部扫描
         * 可能会造成阻塞
         * 保证正确性
         */
        LARGE
    }

    public static List<RankEntity> reverseRangeWithScores(final StringRedisTemplate targetTemplate, final String pKey, final long start, final long end) {
        if (end < 1) {
            return Collections.emptyList();
        }
        Assert.isTrue(start < end, String.format("[start:%s] Cannot be greater than [end:%s] !", start, end));
        return reverseRangeWithScores(targetTemplate, Ignore.SCAN, pKey, start, end);
    }

    public static List<RankEntity> reverseRangeWithScores(final StringRedisTemplate targetTemplate, final Ignore ignore, final String pKey, final long start, final long end) {
        Assert.isTrue(Integer.MAX_VALUE >= end, " end 超出上限！");
        if (end < 1) {
            return Collections.emptyList();
        }
        Assert.isTrue(start < end, String.format("[start:%s] Cannot be greater than [end:%s] !", start, end));
        List<RankEntity> rs;
        switch (ignore) {
            case SCAN:
                rs = reverseRangeWithScoresScan(targetTemplate, pKey, start, end);
                break;
            case LARGE:
                rs = reverseRangeWithScoresLarge(targetTemplate, pKey, start, end);
                break;
            default:
                throw new IllegalArgumentException(" no support! ");
        }
        return rs;
    }

    private static List<RankEntity> reverseRangeWithScoresScan(final StringRedisTemplate targetTemplate, final String pKey, final long start, final long end) {
        Assert.isTrue(Integer.MAX_VALUE >= end, " end 超出上限！");
        if (end < 1) {
            return Collections.emptyList();
        }
        Assert.isTrue(start < end, String.format("[start:%s] Cannot be greater than [end:%s] !", start, end));
        List<RankEntity> rs = new ArrayList<>();
        if (end > STEP_SCAN_SIZE) {
            long stepStart = start;
            long stepEnd = start + STEP_SCAN_SIZE;
            while (stepEnd > stepStart) {
                rs.addAll(reverseRangeWithScoresLarge(targetTemplate, pKey, stepStart, stepEnd));
                stepStart = ++stepEnd;
                stepEnd = Math.min(stepStart + STEP_SCAN_SIZE, end);
            }
        } else {
            return reverseRangeWithScoresLarge(targetTemplate, pKey, start, end);
        }
        return rs;
    }

    private static List<RankEntity> reverseRangeWithScoresLarge(final StringRedisTemplate targetTemplate, final String pKey, final long start, final long end) {
        if (end < 1) {
            return Collections.emptyList();
        }
        Assert.isTrue(start < end, String.format("[start:%s] Cannot be greater than [end:%s] !", start, end));
        Assert.isTrue(Integer.MAX_VALUE >= end, " end 超出上限！");
        Integer startRank = Integer.parseInt(String.valueOf(start));
        Set<ZSetOperations.TypedTuple<String>> tuples = targetTemplate.opsForZSet().reverseRangeWithScores(pKey, start, end);
        List<RankEntity> rs = new ArrayList<>(tuples.size());
        for (ZSetOperations.TypedTuple<String> typedTuple : tuples) {
            RankEntity rsEle = new RankEntity();
            rsEle.setKey(typedTuple.getValue());
            rsEle.setScore(typedTuple.getScore());
            rsEle.setRank(++startRank);
            rs.add(rsEle);
        }
        return rs;
    }

    public static class RankEntity implements Serializable {
        private static final long serialVersionUID = 6472737698324631974L;
        private String key;
        private Double score;
        private Integer rank;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }

        @Override
        public String toString() {
            return "RankEntity{" +
                    "key='" + key + '\'' +
                    ", score=" + score +
                    ", rank=" + rank +
                    '}';
        }
    }
}
