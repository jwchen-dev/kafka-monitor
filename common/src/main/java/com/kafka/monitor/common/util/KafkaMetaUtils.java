package com.kafka.monitor.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kafka.monitor.common.model.KafkaBrokerDomain;
import com.kafka.monitor.common.model.KafkaMetaDomain;
import kafka.cluster.BrokerEndPoint;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class KafkaMetaUtils {
	private static Logger LOG = LoggerFactory.getLogger(KafkaMetaUtils.class);

	public static List<KafkaMetaDomain> findLeader(String topic) {
		List<KafkaMetaDomain> list = new ArrayList<KafkaMetaDomain>();
		SimpleConsumer consumer = null;
		
		List<KafkaBrokerDomain> brokerList = getBrokers();
		for(KafkaBrokerDomain broker : brokerList){
			try {
				consumer = new SimpleConsumer(broker.getHost(),broker.getPort(), 100000, 65536, "leaderLookup");
				if (consumer != null)
					break;
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
		
		if (consumer == null) {
			LOG.error("Connection [SimpleConsumer] has failed,please check brokers.");
			return list;
		}
		
		List<String> topics = Collections.singletonList(topic);
		TopicMetadataRequest req = new TopicMetadataRequest(topics);
		TopicMetadataResponse resp = consumer.send(req);
		if (resp == null) {
			LOG.error("Get [TopicMetadataResponse] has null.");
			return list;
		}
		List<TopicMetadata> metaData = resp.topicsMetadata();
		for (TopicMetadata item : metaData) {
			for (PartitionMetadata part : item.partitionsMetadata()) {
				KafkaMetaDomain kMeta = new KafkaMetaDomain();
				kMeta.setIsr(KafkaClusterUtils.geyReplicasIsr(topic,
						part.partitionId()));
				kMeta.setLeader((part.leader() == null) ? -1 : part.leader()
						.id());
				kMeta.setPartitionId(part.partitionId());
				List<Integer> repliList = new ArrayList<Integer>();
				List<BrokerEndPoint> replicas = part.replicas();
				for (BrokerEndPoint bep : replicas) {
					repliList.add(bep.id());
				}
//				for (Broker repli : part.replicas()) {
//					repliList.add(Integer.valueOf(repli.id()));
//				}
				kMeta.setReplicas(repliList.toString());
				list.add(kMeta);
			}
		}
		if (consumer != null) {
			consumer.close();
		}
		return list;
	}

	private static List<KafkaBrokerDomain> getBrokers() {
		String brokersStr = KafkaClusterUtils.getAllBrokersInfo();
		List<KafkaBrokerDomain> brokers = new ArrayList<KafkaBrokerDomain>();
		JSONArray arr = JSON.parseArray(brokersStr);
		for (Iterator<?> localIterator = arr.iterator(); localIterator
				.hasNext();) {
			Object object = localIterator.next();
			JSONObject obj = (JSONObject) object;
			KafkaBrokerDomain broker = new KafkaBrokerDomain();
			broker.setHost(obj.getString("host"));
			broker.setPort(obj.getInteger("port").intValue());
			brokers.add(broker);
		}
		return brokers;
	}

	public static void main(String[] args) {
		System.out.println(findLeader("boyaa_mf_test12345"));
	}
}