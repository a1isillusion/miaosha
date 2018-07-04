package nullguo.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nullguo.dao.OrderDao;
import nullguo.domain.MiaoshaOrder;
import nullguo.domain.MiaoshaUser;
import nullguo.domain.OrderInfo;
import nullguo.vo.GoodsVo;

@Service
public class OrderService {
@Autowired
OrderDao orderDao;

public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {

	return orderDao.getMiaoshaOrderByUserIdGoodsId(userId,goodsId);
}
@Transactional
public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
	OrderInfo orderInfo=new OrderInfo();
	orderInfo.setCreateDate(new Date());
	orderInfo.setDeliveryAddrId(0L);
	orderInfo.setGoodsCount(1);
	orderInfo.setGoodsId(goods.getId());
	orderInfo.setGoodsName(goods.getGoodsName());
	orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
	orderInfo.setOrderChannel(1);
	orderInfo.setStatus(0);
    orderInfo.setUserId(user.getId());
    orderDao.insert(orderInfo);
    MiaoshaOrder miaoshaOrder=new MiaoshaOrder();
    miaoshaOrder.setOrderId(orderInfo.getId());
    miaoshaOrder.setUserId(user.getId());
    miaoshaOrder.setGoodsId(goods.getId());
    orderDao.insertMiaoshaOrder(miaoshaOrder);
	return orderInfo;
}

}
