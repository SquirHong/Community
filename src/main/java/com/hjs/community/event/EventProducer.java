package com.hjs.community.event;

import com.alibaba.fastjson.JSONObject;
import com.hjs.community.entity.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author hong
 * @create 2023-02-09 14:13
 */
@Component
public class EventProducer {

    @Resource
    private KafkaTemplate kafkaTemplate;

    //处理事件
    public void fireEvent(Event event){
        //将事件发布到指定的主题
//        kafkaTemplate.send(event.getTopic(),event);
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
