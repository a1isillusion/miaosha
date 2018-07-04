package nullguo.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
@Service
public class RedisService {
@Autowired
JedisPool jedisPool;

public <T> T get(String key,Class <T> clazz) {
	Jedis jedis=null;
	try {
	jedis=jedisPool.getResource();
    String string=jedis.get(key);
    T t =stringToBean(string,clazz);
    return t;
	} finally {
		returnToPool(jedis);
	}
	}
public <T> boolean set(String key,T value) {
	Jedis jedis=null;
	try {
	jedis=jedisPool.getResource();
	String string=beanToString(value);
	if(string==null||string.length()<=0) {
		return false;
	}
    jedis.set(key, string);
    return true;
	} finally {
		returnToPool(jedis);
	}
	}
public boolean exists(String key) {
	Jedis jedis=null;
	try {
	jedis=jedisPool.getResource();
    return jedis.exists(key);
	} finally {
		returnToPool(jedis);
	}
	
}
public static <T>String beanToString(T value) {
	if(value==null) {
		return null;
	}
	Class<?> clazz=value.getClass();
	if(clazz==int.class||clazz==Integer.class) {
		return ""+value;
	}
	else if(clazz==String.class){
		return (String)value;
	}
	else if (clazz==long.class||clazz==Long.class) {
		return ""+value;
	}
	return JSON.toJSONString(value);
}
@SuppressWarnings("unchecked")
public static <T> T stringToBean(String string,Class<T> clazz) {
	if(string==null||string.length()<=0||clazz==null) {
		return null;
	}
	if(clazz==int.class||clazz==Integer.class) {
		return (T)Integer.valueOf(string);
	}
	else if(clazz==String.class){
		return (T)string;
	}
	else if (clazz==long.class||clazz==Long.class) {
		return (T)Long.valueOf(string);
	}
	else {
		return JSON.toJavaObject(JSON.parseObject(string), clazz);
	}
	
}
private void returnToPool(Jedis jedis) {
	if(jedis!=null)jedis.close();
}
public long decr(String key) {
	Jedis jedis=null;
	try {
	jedis=jedisPool.getResource();
    return jedis.decr(key);
    
	} finally {
		returnToPool(jedis);
	}
}	

}
