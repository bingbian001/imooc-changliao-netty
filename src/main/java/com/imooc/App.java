package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import tk.mybatis.spring.annotation.MapperScan;

/**
 * Applicaiton启动类
 * 
 * @MapperScan: 扫描mybatis mapper包路径
 * @ComponentScan: 扫描所需要的包， 包含一些自用的工具类包所在路径
 */
@SpringBootApplication
@MapperScan(basePackages="com.imooc.mapper")
@ComponentScan(basePackages={"com.imooc", "org.n3r.idworker"})
public class App 
{
	@Bean
	public SpringUtil getSpringUtil(){
		return new SpringUtil();
	}
	
    public static void main( String[] args )
    {
    	SpringApplication.run(App.class, args);
    }
}
