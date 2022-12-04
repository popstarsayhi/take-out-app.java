package com.vz.test;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class UploadFileTest {

    @Test
    public void test1(){
        String fileName = "errrrr.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }

    @Test
    public void testRedis(){
        //1. get connections
        Jedis jedis = new Jedis("localhost",6379);
        //2. excute
        jedis.set("username","vanessa");

        String username = jedis.get("username");
        System.out.println(username);

        jedis.del("username");

        //3. close connections
        jedis.close();
    }
}
