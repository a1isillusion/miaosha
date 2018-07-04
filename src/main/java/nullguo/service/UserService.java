package nullguo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nullguo.dao.UserDao;
import nullguo.domain.User;

@Service
public class UserService {
	@Autowired
	UserDao userDao;
public User getById(int id) {
	return userDao.getById(id);
}
}
