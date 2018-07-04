package nullguo.rabbitmq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nullguo.domain.MiaoshaOrder;
import nullguo.domain.MiaoshaUser;
import nullguo.domain.OrderInfo;
import nullguo.redis.RedisService;
import nullguo.result.CodeMsg;
import nullguo.service.GoodsService;
import nullguo.service.MiaoshaService;
import nullguo.service.OrderService;
import nullguo.vo.GoodsVo;

@Service
public class MQReceiver {
	private static Logger log=LoggerFactory.getLogger(MQReceiver.class);
	@Autowired
	RedisService redisservice;
	@Autowired
	GoodsService goodsService;
	@Autowired
	OrderService orderService;
	@Autowired
	MiaoshaService miaoshaService;
/*@RabbitListener(queues=MQConfig.QUEUE)
public void receive(String msg) {
	log.info("receive massage:"+msg);
}
@RabbitListener(queues=MQConfig.TOPIC_QUEUE_1)
public void receiveTopic1(String msg) {
	log.info("receive topic 1 massage:"+msg);
}
@RabbitListener(queues=MQConfig.TOPIC_QUEUE_2)
public void receiveTopic2(String msg) {
	log.info("receive topic 2 massage:"+msg);
}
@RabbitListener(queues=MQConfig.HEADERS_QUEUE)
public void receiveHeaders(byte[] msg) {
	log.info("receive headers massage:"+new String(msg));
}
*/
@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
public void receive(String msg) {
	log.info("receive massage:"+msg);
	MiaoshaMessage message=RedisService.stringToBean(msg, MiaoshaMessage.class);
	MiaoshaUser user=message.getUser();
	long goodsId=message.getGoodsId();
	GoodsVo goods=goodsService.getGoodsVoById(goodsId);
	int stock=goods.getStockCount();
	if(stock<=0) {
		log.info("秒杀数量没了");
		return;
		}
	MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
	if(order!=null) {
		log.info("秒杀过了");
		return;
	}
 miaoshaService.miaosha(user,goods);
}
}
