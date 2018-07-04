package nullguo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import nullguo.dao.GoodsDao;
import nullguo.domain.MiaoshaGoods;
import nullguo.vo.GoodsVo;
@Service
public class GoodsService {
@Autowired
GoodsDao goodsDao;
public List<GoodsVo> listGoodsVo(){
	return goodsDao.listGoodsVo();
}
public GoodsVo getGoodsVoById(long goodsId) {
	return goodsDao.getGoodsVoById(goodsId);
}
@Transactional
public boolean reduceStock(GoodsVo goods) {
	MiaoshaGoods g=new MiaoshaGoods();
	g.setGoodsId(goods.getId());
	return goodsDao.reduceStock(g);
	
}
}
