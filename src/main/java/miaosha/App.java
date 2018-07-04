package miaosha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Hello world!
 *
 */
@RestController
@SpringBootApplication
@MapperScan("nullguo.dao")
@ComponentScan(basePackages= {"nullguo.service,miaosha,nullguo.redis,nullguo.rabbitmq"})
public class App 
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }
    @RequestMapping("/aaa")
    String sayHello(){
       return "hello world";
    }
}
