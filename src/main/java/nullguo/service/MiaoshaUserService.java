package nullguo.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nullguo.dao.MiaoshaUserDao;
import nullguo.domain.MiaoshaUser;
import nullguo.redis.RedisService;
import nullguo.result.CodeMsg;
import nullguo.util.MD5Util;
import nullguo.vo.LoginVo;

@Service
public class MiaoshaUserService {
@Autowired
MiaoshaUserDao miaoshaUserDao;
@Autowired
RedisService redisservice;
private static Logger log=LoggerFactory.getLogger(MiaoshaUserService.class);
public MiaoshaUser getById(long id) {
	return miaoshaUserDao.getById(id);
}
public CodeMsg login(LoginVo loginVo) {
	if(loginVo==null) {
		return CodeMsg.SERVER_ERROR;
	}
	String mobile=loginVo.getMobile();
	String formPass =loginVo.getPassword();
	MiaoshaUser user=getById(Long.parseLong(mobile));
	if(user==null) {
		return CodeMsg.MOBILE_NOT_EXIST;
	}
	String dbPass=user.getPassword();
	log.info(dbPass);
	String saltDB=user.getSalt();
	String calcPass=MD5Util.formPassToDBPass(formPass,saltDB);
	log.info(calcPass);
	if(!calcPass.equals(dbPass)) {
		return CodeMsg.PASSWORD_ERROR;
	}
return CodeMsg.SUCCESS;
}
public MiaoshaUser getByToken(String token) {
	if(StringUtils.isEmpty(token)) {
		return null;
	}
	return redisservice.get(token, MiaoshaUser.class);
}
}
