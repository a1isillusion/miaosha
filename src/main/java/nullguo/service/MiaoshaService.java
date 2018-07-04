package nullguo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nullguo.domain.MiaoshaOrder;
import nullguo.domain.MiaoshaUser;
import nullguo.domain.OrderInfo;
import nullguo.redis.RedisService;
import nullguo.vo.GoodsVo;

@Service
public class MiaoshaService {
	@Autowired
	GoodsService goodsService;
	@Autowired
	OrderService orderService;
	@Autowired
	RedisService redisService;
    @Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		boolean success=goodsService.reduceStock(goods);
		if(success)
		return orderService.createOrder(user,goods);
		else {
			setGoodsOver(goods.getId());
			return null;
		}
		
	}

	public long getMiaoshaResult(Long userId, long goodsId) {
		MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		if(order!=null)return order.getOrderId();
		else {
			boolean isOver =getGoodsOver(goodsId);
			if(isOver) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	
	private void setGoodsOver(Long id) {
		redisService.set("isover:"+id, true);
		
	}
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists("isover:"+goodsId);
	}

}
