package cc.triffic.wc.kafkamonitor.service;

import cc.triffic.wc.kafkamonitor.domain.DashboardDomain;
import cc.triffic.wc.kafkamonitor.utils.SystemConfigUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kafka.monitor.common.util.KafkaClusterUtils;

import java.util.Iterator;

public class DashboardService {
	public static String getDashboard() {
		JSONObject obj = new JSONObject();
		obj.put("kafka", getKafka());
		obj.put("dashboard", dashboard());

		return obj.toJSONString();
	}

	private static String dashboard() {
		int zks = SystemConfigUtils.getPropertyArray("kafka.zk.list", ",").length;
		String topicObject = KafkaClusterUtils.getAllPartitions();
		int topics = JSON.parseArray(topicObject).size();
		String kafkaObject = KafkaClusterUtils.getAllBrokersInfo();
		int brokers = JSON.parseArray(kafkaObject).size();
		DashboardDomain dash = new DashboardDomain();
		dash.setBrokers(brokers);
		dash.setConsumers(0);
		dash.setTopics(topics);
		dash.setZks(zks);
		dash.setConsumers(ConsumerService.getConsumerNumbers());
		return dash.toString();
	}

	private static String getKafka() {
		String kafka = KafkaClusterUtils.getAllBrokersInfo();
		JSONObject obj = new JSONObject();
		obj.put("name", "Kafka Brokers");
		JSONArray arr = JSON.parseArray(kafka);
		JSONArray arr2 = new JSONArray();
		for (Iterator<?> localIterator = arr.iterator(); localIterator.hasNext();) {
			Object tmp = localIterator.next();
			JSONObject obj1 = (JSONObject) tmp;
			JSONObject obj2 = new JSONObject();
			obj2.put("name",
					obj1.getString("host") + ":" + obj1.getInteger("port"));
			arr2.add(obj2);
		}
		obj.put("children", arr2);
		return obj.toJSONString();
	}
}