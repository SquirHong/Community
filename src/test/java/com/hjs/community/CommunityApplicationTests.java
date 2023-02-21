package com.hjs.community;

import com.alibaba.druid.pool.DruidDataSource;
import com.hjs.community.dao.DiscussPostMapper;
import com.hjs.community.dao.LoginTicketMapper;
import com.hjs.community.dao.MessageMapper;
import com.hjs.community.dao.UserMapper;
import com.hjs.community.entity.DiscussPost;
import com.hjs.community.entity.LoginTicket;
import com.hjs.community.entity.Message;
import com.hjs.community.entity.User;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.MailClient;
import com.hjs.community.util.SensitiveFilter;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@SpringBootTest
@MapperScan("com.hjs.community.dao")
class CommunityApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private kafkaProducer kafkaProducer;

    @Test
    void testSelect() {
        User user = userMapper.selectById(11);
        System.out.println(user);
        User liubei = userMapper.selectByName("liubei");
        System.out.println(liubei);
        User user1 = userMapper.selectByEmail("nowcoder117@sina.com");
        System.out.println(user1);


    }
    @Test
    public void testInsert(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("dasd");
        user.setSalt("dasd");
        user.setEmail("xtcfyvgubh@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/111.png");
        user.setCreateTime(new Date());

        int i = userMapper.insertUser(user);
        System.out.println(i);
        System.out.println(user.getId());

    }
    @Test
    public void testUpdate(){
        String password = new String("xcfgvhsfasd");
        int i = userMapper.updatePassword(150, password);
        System.out.println(i);
        int i1 = userMapper.updateStatus(150, 1);
        System.out.println(i1);
    }
    @Test
    public void testselectPosts(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost d : discussPosts){
            System.out.println(d);
        }
        int i = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(i);
    }
    @Test
    public void testMail(){
        String subject = new String();
        mailClient.sendMail("1428981743@qq.com","你好","helloworld");

    }

    @Test
    public void testloginTicketinsert(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+60*10000));
        int i = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(i);
        System.out.println("**********************************");
        System.out.println(loginTicket.getTicket());
        LoginTicket loginTicket1 = loginTicketMapper.selectByTicket("5f4c341062a04a9a9c93c3759e133db4");
        System.out.println(loginTicket1.getId());
    }
    @Test
    public void batchSave(){

        List<User> users = new ArrayList<>(1000000);
        User user;
        for (int i=0;i<500000;i++){
            user = new User();
            user.setUsername(CommunityUtil.generateUUID().substring(0,9));
            user.setSalt(CommunityUtil.generateUUID().substring(0,5));
            user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
            user.setEmail(CommunityUtil.generateUUID().substring(0,10)+"@qq.com");
            user.setType(0);
            user.setStatus(0);
            user.setActivationCode(CommunityUtil.generateUUID());
            user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
            user.setCreateTime(new Date());
            users.add(user);
        }
        List<User> list = new ArrayList<>(20000);
        //每次批量插入5k条
        int limit = 5000;
        //关闭自动提交
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        long start = System.currentTimeMillis();
        for (int i=0;i<users.size();i++){
            list.add(users.get(i));
            if (list.size() == limit || i == users.size()-1){
                mapper.insertUsers(list);
                list.clear();
            }
        }
        sqlSession.commit();
        long end = System.currentTimeMillis();
        System.out.println("50w条数据耗时为"+(end-start)+"ms");
    }


    @Test
    public void testsensitive(){
        String s = new String("这里可以☆赌☆博☆,可以☆吸☆毒☆");
        String s1 = new String("fabc");
        String filter = sensitiveFilter.filter(s1);
        System.out.println(filter);
    }
    @Test
    public void testdatasource(){
//数据源
        System.out.println("数据源： " + dataSource.getClass());
        //获取数据库连接
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("获取连接： " + connection);
    }

    @Test
    public void contextLoads() throws SQLException {
        //看一下默认数据源
        System.out.println(dataSource.getClass());
        //获得连接
        Connection connection =   dataSource.getConnection();
        System.out.println(connection);

        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        System.out.println("druidDataSource 数据源最大连接数：" + druidDataSource.getMaxActive());
        System.out.println("druidDataSource 数据源初始化连接数：" + druidDataSource.getInitialSize());

        //关闭连接
        connection.close();
    }
    @Test
    public void testGetPassword(){
        String s = CommunityUtil.md5("aaa" + "167f9");
        System.out.println(s);
        Map<Integer,Integer> map = new HashMap<>();
//        map.conta
    }
    @Test
    public void testMessageMapper(){
        for (Message message : messageMapper.selectConversations(111, 0, 20)) {
            System.out.println(message);
        }
    }
    @Test
    public void testredis(){
        String key = "test";
        redisTemplate.opsForValue().set(key,1);
        System.out.println(redisTemplate.opsForValue().get(key));
        System.out.println(redisTemplate.opsForValue().increment(key));
        System.out.println(redisTemplate.opsForValue().increment(key));
    }

    @Test
    public void testKafka(){
        kafkaProducer.sendMessage("test","你好");
        kafkaProducer.sendMessage("test","在吗");
        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
@Component
class kafkaProducer{

    @Resource
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class kafkaConsumer{

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }

}
