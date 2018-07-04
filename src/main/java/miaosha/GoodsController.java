package miaosha;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.Spring;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.mysql.cj.protocol.a.SimplePacketSender;

import nullguo.domain.MiaoshaOrder;
import nullguo.domain.MiaoshaUser;
import nullguo.domain.OrderInfo;
import nullguo.rabbitmq.MQSender;
import nullguo.rabbitmq.MiaoshaMessage;
import nullguo.redis.RedisService;
import nullguo.result.CodeMsg;
import nullguo.result.Result;
import nullguo.service.GoodsService;
import nullguo.service.MiaoshaService;
import nullguo.service.MiaoshaUserService;
import nullguo.service.OrderService;
import nullguo.util.MD5Util;
import nullguo.util.UUIDUtil;
import nullguo.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController implements InitializingBean{
	private static Logger log=LoggerFactory.getLogger(GoodsController.class);
	@Autowired
	MiaoshaUserService miaoshaUserService;
	@Autowired
	RedisService redisservice;
	@Autowired
	GoodsService goodsService;
	@Autowired
	OrderService orderService;
	@Autowired
	MiaoshaService miaoshaService;
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	MQSender MQSender;
	
@RequestMapping(value="/to_list",produces="text/html")	
@ResponseBody
public String tolist(Model model,
		HttpServletRequest request,
		HttpServletResponse response,
		@CookieValue(value="token",required=false)String cookieToken,
		@RequestParam(value="toekn",required=false)String paramToken) {
	if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)) {
		return "login";
	}
	String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
	MiaoshaUser user=miaoshaUserService.getByToken(token);
	model.addAttribute("user", user);
	List<GoodsVo> goodsList=goodsService.listGoodsVo();
	model.addAttribute("goodsList", goodsList);
  // return "goods_list";
	String html=redisservice.get("GoodsList",String.class);
	if (!StringUtils.isEmpty(html)) {
		return html;
	}
	SpringWebContext context=new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
	html=thymeleafViewResolver.getTemplateEngine().process("goods_list",context);
	if (!StringUtils.isEmpty(html)) {
		redisservice.set("GoodsList", html);
		
	}
	return html;
}
@RequestMapping("/to_detail/{goodsId}")	
public String todetail(Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId) {
	model.addAttribute("user", user);
    GoodsVo goods=goodsService.getGoodsVoById(goodsId);
    model.addAttribute("goods",goods);
    log.info("startdate"+goods.getStartDate()); 
    log.info("enddate"+goods.getEndDate());
    long startTime=goods.getStartDate().getTime();
    long endTime=goods.getEndDate().getTime();
    long now=System.currentTimeMillis();
    int miaoshaStatus=0;
    int remainSeconds=0;
	log.info("start"+startTime);
	log.info("now"+now);
    if(startTime>now) {
    	miaoshaStatus=0;
    	remainSeconds=(int)(startTime-now)/1000;
    	log.info(""+remainSeconds);
    }
    else if (now>endTime) {
		miaoshaStatus=2;
		remainSeconds=-1;
	}
    else {
    	miaoshaStatus=1;
    	remainSeconds=0;
    }
    model.addAttribute("miaoshaStatus", miaoshaStatus);
    model.addAttribute("remainSeconds", remainSeconds);
	return "goods_detail";
}
@RequestMapping("/do_miaosha")
public String domiaosha(Model model,@RequestParam("goodsId")long goodsId	
		,@CookieValue(value="token",required=false)String cookieToken,
		@RequestParam(value="token",required=false)String paramToken) {
	if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)) {
		return "login";
	}
	log.info("chenggong");
	String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
	MiaoshaUser user=miaoshaUserService.getByToken(token);
	long stock=redisservice.decr("goods:"+goodsId);
	if(stock<=0) {
		log.info("秒杀数量没了");
		return "miaosha_fail";
	}
	MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
	if(order!=null) {
		log.info("秒杀过了");
		model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA);
		return "miaosha_fail";
	}
	//入队
	MiaoshaMessage message=new MiaoshaMessage();
	message.setGoodsId(goodsId);
	message.setUser(user);
    MQSender.sendMiaoshaMessage(message);
	return "hello";
	/*GoodsVo goods=goodsService.getGoodsVoById(goodsId);
	int stock=goods.getStockCount();
	if(stock<=0) {
		log.info("秒杀数量没了");
		model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER);
		return "miaosha_fail";
	}
	MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
	if(order!=null) {
		log.info("秒杀过了");
		model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA);
		return "miaosha_fail";
	}
	//减库存 下订单 写入秒杀订单
	OrderInfo orderInfo= miaoshaService.miaosha(user,goods);
	model.addAttribute("orderInfo",orderInfo);
	model.addAttribute("goods", goods);
	return "order_detail";
	*/
}
@RequestMapping(value="/result",method=RequestMethod.GET)
@ResponseBody
public Result<Long> miaoshaResult(Model model,@RequestParam("goodsId")long goodsId,
		@CookieValue(value="token",required=false)String cookieToken,
		@RequestParam(value="token",required=false)String paramToken) {
	if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)) {
		return Result.error(CodeMsg.SERVER_ERROR);
	}
	log.info("chenggong");
	String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
	MiaoshaUser user=miaoshaUserService.getByToken(token);
	long orederId=miaoshaService.getMiaoshaResult(user.getId(),goodsId);
	return Result.success(orederId);
	
}
@Override
public void afterPropertiesSet() throws Exception {
	List<GoodsVo> list=goodsService.listGoodsVo();
	if(list==null)return;
	for(GoodsVo goods:list) {
		redisservice.set("goods:"+goods.getId(), goods.getStockCount());
	}
}
@RequestMapping(value="/path",method=RequestMethod.GET)
@ResponseBody
public Result<String> path(Model model,@RequestParam("goodsId")long goodsId,
		@CookieValue(value="token",required=false)String cookieToken,
		@RequestParam(value="token",required=false)String paramToken) {
	if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)) {
		return Result.error(CodeMsg.SERVER_ERROR);
	}
	log.info("path success");
    String string=MD5Util.md5(UUIDUtil.uuid()+"123456");
	String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
	MiaoshaUser user=miaoshaUserService.getByToken(token);
    redisservice.set("path:"+user.getId()+"_"+goodsId,string);
	return Result.success(string);
	
}
}
