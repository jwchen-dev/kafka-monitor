package cc.triffic.wc.kafkamonitor.utils;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

public final class ZKPoolUtils {
	private static final Logger LOG = LoggerFactory
			.getLogger(ZKPoolUtils.class);
	public static String zkInfo = SystemConfigUtils.getProperty("kafka.zk.list");
	private Vector<ZkClient> pool;
	private Vector<ZkClient> poolZKSerializer;
	private static int poolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");
	private static ZKPoolUtils instance = null;

	private ZKPoolUtils() {
		initZKPoolUtils();
	}

	private void initZKPoolUtils() {
		if (this.zkInfo == null ) {
			this.zkInfo = "node1:2181";
		}
		LOG.info("Initialization ZkClient pool size [" + this.poolSize + "],zk="+this.zkInfo);
		if (this.poolSize == 0 ) {
			this.poolSize = 4;
		}
		LOG.info("Initialization ZkClient pool size [" + this.poolSize + "]");
		this.pool = new Vector<ZkClient>(this.poolSize);
		this.poolZKSerializer = new Vector<ZkClient>(this.poolSize);
		addZkClient();
		addZkSerializerClient();
	}

	private void addZkClient() {
		ZkClient zkc = null;
		for (int i = 0; i < this.poolSize; ++i)
			try {
				zkc = new ZkClient(this.zkInfo);
				this.pool.add(zkc);
			} catch (Exception ex) {
				LOG.error("addZkClient:", ex);
			}
	}

	private void addZkSerializerClient() {
		ZkClient zkClient = null;
		for (int i = 0; i < this.poolSize; ++i)
			try {
				LOG.info("zk info ="+this.zkInfo);
				this.zkInfo = "node1:2181";
				ZkSerializer zkSerializer = new ZkClientSerializer();
//				zkClient = new ZkClient(this.zkInfo, 2147483647, 100000, new SerializableSerializer());
				zkClient = new ZkClient(this.zkInfo, 2147483647, 100000, zkSerializer);
				this.poolZKSerializer.add(zkClient);
			} catch (Exception ex) {
				LOG.error("addZkSerializerClient:", ex);
			}
	}

	public synchronized void releaseZKSerializer(ZkClient zkc) {
		if (this.poolZKSerializer.size() < 25) {
			this.poolZKSerializer.add(zkc);
		}
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux"))
			LOG.debug("release poolZKSerializer,and available size ["
					+ this.poolZKSerializer.size() + "]");
		else
			LOG.debug("release poolZKSerializer,and available size ["
					+ this.poolZKSerializer.size() + "]");
	}

	public synchronized void release(ZkClient zkc) {
		if (this.pool.size() < 25) {
			this.pool.add(zkc);
		}
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux"))
			LOG.debug("release pool,and available size [" + this.pool.size()
					+ "]");
		else
			LOG.debug("release pool,and available size [" + this.pool.size()
					+ "]");
	}

	public synchronized void closePool() {
		int i;
		if ((this.pool != null) && (this.pool.size() > 0)) {
			for (i = 0; i < this.pool.size(); ++i) {
				try {
					((ZkClient) this.pool.get(i)).close();
				} catch (Exception ex) {
					LOG.error("closePool:", ex);

				} finally {
					this.pool.remove(i);
				}
			}
		}

		if ((this.poolZKSerializer != null)
				&& (this.poolZKSerializer.size() > 0)) {
			for (i = 0; i < this.poolZKSerializer.size(); ++i) {
				try {
					((ZkClient) this.poolZKSerializer.get(i)).close();
				} catch (Exception ex) {
					LOG.error("closePool2:", ex);
				} finally {
					this.poolZKSerializer.remove(i);
				}
			}
		}
		instance = null;
	}

	public synchronized ZkClient getZkClient() {
		ZkClient zkc = null;
		try {
			String osName;
			if (this.pool.size() > 0) {
				zkc = (ZkClient) this.pool.get(0);
				this.pool.remove(0);
				osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Linux")) {
					LOG.debug("get pool,and available size ["
							+ this.pool.size() + "]");
				} else
					LOG.debug("get pool,and available size [" + this.pool.size() + "]");
			} else {
				addZkClient();
				zkc = (ZkClient) this.pool.get(0);
				this.pool.remove(0);
				osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Linux")) {
					LOG.debug("get pool,and available size ["
							+ this.pool.size() + "]");
				} else
					LOG.warn("get pool,and available size [" + this.pool.size()
							+ "]");
			}
		} catch (Exception e) {
			LOG.error("ZK init has error,msg is: " ,e);
		}
		return zkc;
	}

	public synchronized ZkClient getZkClientSerializer() {
		if (this.poolZKSerializer.size() > 0) {
			ZkClient zkc = (ZkClient) this.poolZKSerializer.get(0);
			this.poolZKSerializer.remove(0);
			String osName = System.getProperties().getProperty("os.name");
			if (osName.contains("Linux"))
				LOG.debug("get poolZKSerializer,and available size ["
						+ this.poolZKSerializer.size() + "]");
			else {
				LOG.debug("get poolZKSerializer,and available size ["
						+ this.poolZKSerializer.size() + "]");
			}
			return zkc;
		}
		addZkSerializerClient();
		ZkClient zkc = (ZkClient) this.poolZKSerializer.get(0);
		this.poolZKSerializer.remove(0);
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux"))
			LOG.debug("get poolZKSerializer,and available size ["
					+ this.poolZKSerializer.size() + "]");
		else {
			LOG.warn("get poolZKSerializer,and available size ["
					+ this.poolZKSerializer.size() + "]");
		}
		return zkc;
	}

	public static synchronized ZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new ZKPoolUtils();
		}
		return instance;
	}
}