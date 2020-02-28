package org.ekstep.graph.cache.util;

import static org.ekstep.graph.cache.factory.JedisFactory.getRedisConncetion;
import static org.ekstep.graph.cache.factory.JedisFactory.returnConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.cache.exception.GraphCacheErrorCodes;
import org.ekstep.graph.dac.enums.GraphDACParams;

import redis.clients.jedis.Jedis;

public class RedisStoreUtil {

	public static void saveNodeProperty(String graphId, String objectId, String nodeProperty, String propValue) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			jedis.set(redisKey, propValue);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static String getNodeProperty(String graphId, String objectId, String nodeProperty) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			String value = jedis.get(redisKey);
			return value;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void saveNodeProperties(String graphId, String objectId, Map<String, Object> metadata) {
		Jedis jedis = getRedisConncetion();
		try {
			for (Entry<String, Object> entry : metadata.entrySet()) {
				String propertyName = entry.getKey();
				String propertyValue = entry.getValue().toString();

				String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, propertyName);
				jedis.set(redisKey, propertyValue);
			}

		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteNodeProperties(String graphId, String objectId) {
		Jedis jedis = getRedisConncetion();
		try {

			String versionKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId,
					GraphDACParams.versionKey.name());
			String consumerId = CacheKeyGenerator.getNodePropertyKey(graphId, objectId,
					GraphDACParams.consumerId.name());
			jedis.del(versionKey, consumerId);

		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteAllNodeProperty(String graphId, String propertyName) {
		Jedis jedis = getRedisConncetion();
		try {

			String delKeysPattern = CacheKeyGenerator.getAllNodePropertyKeysPattern(graphId, propertyName);
			Set<String> keys = jedis.keys(delKeysPattern);
			if (keys != null && keys.size() > 0) {
				List<String> keyList = new ArrayList<>(keys);
				jedis.del(keyList.toArray(new String[keyList.size()]));
			}

		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static Double getNodePropertyIncVal(String graphId, String objectId, String nodeProperty) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			double inc = 1.0;
			double value = jedis.incrByFloat(redisKey, inc);
			return value;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	
	// TODO: always considering object as string. need to change this.
	public static void saveList(String key, List<Object> values) {
		Jedis jedis = getRedisConncetion();
		try {
			jedis.del(key);
			for (Object val : values) {
				jedis.sadd(key, (String) val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnConnection(jedis);
		}
	}
	
	public static List<Object> getList(String key) {
		Jedis jedis = getRedisConncetion();
		try {
			 Set<String> set = jedis.smembers(key);
			 List<Object> list = new ArrayList<Object>(set);
			return list;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}
}
