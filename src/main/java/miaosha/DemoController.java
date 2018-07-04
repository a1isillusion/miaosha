package miaosha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import nullguo.domain.User;
import nullguo.rabbitmq.MQSender;
import nullguo.redis.RedisService;
import nullguo.result.Result;
import nullguo.service.UserService;

@Controller
public class DemoController {
	@Autowired
	UserService userService;
	@Autowired
	RedisService redisservice;
	@Autowired
	MQSender sender;
@RequestMapping("/hello")
@ResponseBody
public Result<User> hello(){
return Result.success(userService.getById(1));
}
@RequestMapping("/user")
@ResponseBody
public String helloerror() {
	String string=redisservice.get("miaoshakey", String.class);
	return ""+string;
}
@RequestMapping("/mq")
@ResponseBody
public Result<String> mq() {
	sender.send("fuck you 37");
	return Result.success("fuck you 37");
}
@RequestMapping("/mqtopic")
@ResponseBody
public Result<String> mqtopic() {
	sender.sendTopic("fuck you 37");
	return Result.success("fuck you 37");
}
@RequestMapping("/mqfanout")
@ResponseBody
public Result<String> mqfanout() {
	sender.sendFanout("fuck you 37");
	return Result.success("fuck you 37");
}
@RequestMapping("/mqheaders")
@ResponseBody
public Result<String> mqheaders() {
	sender.sendHeaders("fuck you 37");
	return Result.success("fuck you 37");
}
}
