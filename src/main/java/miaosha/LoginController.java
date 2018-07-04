package miaosha;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nullguo.redis.RedisService;
import nullguo.result.CodeMsg;
import nullguo.result.Result;
import nullguo.service.MiaoshaUserService;
import nullguo.service.UserService;
import nullguo.util.UUIDUtil;
import nullguo.util.ValidatorUtil;
import nullguo.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {
	private static Logger log=LoggerFactory.getLogger(LoginController.class);
@Autowired
UserService userService;
@Autowired
MiaoshaUserService miaoshaUserService;
@Autowired
RedisService redisservice;
@RequestMapping("/to_login")
public String tologin() {
	return "login";
}
@RequestMapping("/do_login")
@ResponseBody
public Result<Boolean> dologin(HttpServletResponse response,LoginVo loginVo) {
	log.info(loginVo.toString());
	if(!ValidatorUtil.isMobile(loginVo.getMobile())) {
		return Result.error(CodeMsg.MOBILE_ERROR);
	}
	CodeMsg cm=miaoshaUserService.login(loginVo);
	if(cm.getCode()==0) {
		String token=UUIDUtil.uuid();
		redisservice.set(token,miaoshaUserService.getById(Long.parseLong(loginVo.getMobile())));//待优化
		Cookie cookie=new Cookie("token", token);
		cookie.setMaxAge(5*60);
		cookie.setPath("/");
		response.addCookie(cookie);
		return Result.success(true);
	}	
	else {
		return Result.error(cm);
	}
	
}
}
