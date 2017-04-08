package cc.triffic.wc.kafkamonitor.quartz;

import cc.triffic.wc.kafkamonitor.domain.TupleDomain;
import cc.triffic.wc.kafkamonitor.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kafka.monitor.common.model.AlarmDomain;
import com.kafka.monitor.common.model.OffsetZkDomain;
import com.kafka.monitor.common.model.OffsetsSQLiteDomain;
import com.kafka.monitor.common.util.CalendarUtils;
import com.kafka.monitor.common.util.DBZKDataUtils;
import com.kafka.monitor.common.util.KafkaClusterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.kafka.monitor.common.client.GetOffsetInfo;
import com.kafka.monitor.common.model.ConsumerInfoModel;


public class OffsetsQuartz {
	private static LRUCacheUtils<String, TupleDomain> lruCache = new LRUCacheUtils<String, TupleDomain>(100000);
	private static Logger LOG = LoggerFactory.getLogger(OffsetsQuartz.class);

	@Deprecated
	public void cleanHistoryData() {
		System.out.println(CalendarUtils.getStatsPerDate());
	}

	public void jobQuartz() {
		LOG.info("----OffsetsQuartz.jobQuartz----start------");
		AlarmDomain alarm;
		try {
			//old consumer api
//			List<OffsetsSQLiteDomain> list = getOldConsumerOffsets();
			List<OffsetsSQLiteDomain> list = new ArrayList<OffsetsSQLiteDomain>();

			//new consumer api
			List<ConsumerInfoModel> newOffsetInfo = GetOffsetInfo.getNewOffsetInfoVersion2();
			for (ConsumerInfoModel cifm : newOffsetInfo) {
				OffsetsSQLiteDomain model = new OffsetsSQLiteDomain(cifm.getGroup(), cifm.getTopic(), cifm.getOffsets(), cifm.getCreated());
				model.setLag(cifm.getLag());
				model.setLogSize(cifm.getLogSize());
				list.add(model);
			}

			DBZKDataUtils.insert(list);
			boolean alarmEnable = SystemConfigUtils.getBooleanProperty("anotherkafkamonitor.mail.enable");
			if (alarmEnable) {
				List<AlarmDomain> listAlarm = alarmConfigure();
				Iterator<AlarmDomain> iter = listAlarm.iterator();
				while (iter.hasNext()) {
					alarm = (AlarmDomain) iter.next();
					for (OffsetsSQLiteDomain offset : list)
						if ((offset.getGroup().equals(alarm.getGroup()))
								&& (offset.getTopic().equals(alarm.getTopics()))
								&& (offset.getLag() > alarm.getLag()))
							try {
								SendMessageUtils.send(
										alarm.getOwners(),
										"Kafka监控平台Alarm Notice",
										"Lag exceeds a specified threshold,Topic is ["
												+ alarm.getTopics()
												+ "],current lag is ["
												+ offset.getLag()
												+ "],expired lag is ["
												+ alarm.getLag() + "].");
							} catch (Exception ex) {
								LOG.error("Topic[" + alarm.getTopics()
										+ "] Send alarm mail has error,msg is "
										+ ex.getMessage());
							}
				}
			}
		} catch (Exception ex) {
			LOG.error("[Quartz.offsets] has error,msg is: ", ex);
		}
		LOG.info("----OffsetsQuartz.jobQuartz----finish------");
	}

	private List<OffsetsSQLiteDomain> getOldConsumerOffsets() {
		String group;
		String topic;
		Iterator<?> localIterator2;
		List<String> hosts = getBrokers();
		List<OffsetsSQLiteDomain> list = new ArrayList<OffsetsSQLiteDomain>();

		//old version,<group,topics>
		Map<String, List<String>> consumers = KafkaClusterUtils.getConsumers();

		String statsPerDate = CalendarUtils.getStatsPerDate();
		for (Map.Entry<String, List<String>> entry : consumers.entrySet()) {
            group = (String) entry.getKey();
            for (localIterator2 = ((List<?>) entry.getValue()).iterator(); localIterator2.hasNext();) {
                topic = (String) localIterator2.next();
                OffsetsSQLiteDomain offsetSQLite = new OffsetsSQLiteDomain();
                for (String partitionStr : KafkaClusterUtils.findTopicPartition(topic)) {
                    int partition = Integer.parseInt(partitionStr);
                    long logSize = KafkaClusterUtils.getLogSize(hosts, topic, partition);
                    OffsetZkDomain offsetZk = KafkaClusterUtils.getOffset(topic, group, partition);
                    offsetSQLite.setGroup(group);
                    offsetSQLite.setCreated(statsPerDate);
                    offsetSQLite.setTopic(topic);
                    if (logSize == 0L) {
                        offsetSQLite.setLag(0L + offsetSQLite.getLag());
                    } else {
                        long lag = offsetSQLite.getLag() + ((offsetZk.getOffset() == -1L) ? 0L : logSize - offsetZk.getOffset());
                        offsetSQLite.setLag(lag);
                    }
                    offsetSQLite.setLogSize(logSize+ offsetSQLite.getLogSize());
                    offsetSQLite.setOffsets(offsetZk.getOffset() + offsetSQLite.getOffsets());
                }
                list.add(offsetSQLite);
            }
        }
		return list;
	}

	private static List<String> getBrokers() {
		TupleDomain tuple;
		String key = "group_topic_offset_graph_consumer_brokers";
		String brokers = "";
		if (lruCache.containsKey(key)) {
			tuple = (TupleDomain) lruCache.get(key);
			brokers = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / 60000.0D > 30.0D)
				lruCache.remove(key);
		} else {
			brokers = KafkaClusterUtils.getAllBrokersInfo();
			tuple = new TupleDomain();
			tuple.setRet(brokers);
			tuple.setTimespan(System.currentTimeMillis());
			lruCache.put(key, tuple);
		}
		JSONArray arr = JSON.parseArray(brokers);
		List<String> list = new ArrayList<String>();
		for (Iterator<?> localIterator = arr.iterator(); localIterator.hasNext();) {
			Object object = localIterator.next();
			JSONObject obj = (JSONObject) object;
			String host = obj.getString("host");
			int port = obj.getInteger("port").intValue();
			list.add(host + ":" + port);
		}
		return list;
	}

	private static List<AlarmDomain> alarmConfigure() {
		String ret = DBZKDataUtils.getAlarm();
		List<AlarmDomain> list = new ArrayList<AlarmDomain>();
		JSONArray array = JSON.parseArray(ret);
		for (Iterator<?> localIterator = array.iterator(); localIterator.hasNext();) {
			Object object = localIterator.next();
			AlarmDomain alarm = new AlarmDomain();
			JSONObject obj = (JSONObject) object;
			alarm.setGroup(obj.getString("group"));
			alarm.setTopics(obj.getString("topic"));
			alarm.setLag(obj.getLong("lag").longValue());
			alarm.setOwners(obj.getString("owner"));
			list.add(alarm);
		}
		return list;
	}

	private List<OffsetsSQLiteDomain> getNewConsumerOffsets() {

		return null;
	}

	public static void main(String[] args) {
		OffsetsQuartz offsets = new OffsetsQuartz();
		offsets.jobQuartz();
	}
}