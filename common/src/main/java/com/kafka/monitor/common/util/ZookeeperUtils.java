package com.kafka.monitor.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ZookeeperUtils {
	private static final Logger LOG = LoggerFactory
			.getLogger(ZookeeperUtils.class);

	public static String serverStatus(String host, String port) {
		String line;
		String ret = "";
		Socket sock = null;
		try {
			sock = new Socket(host, Integer.parseInt(port));
		} catch (Exception e) {
			LOG.error("Socket[" + host + ":" + port + "] connect refused");
			return "death";
		}
		BufferedReader reader = null;
		try {
			OutputStream outstream = sock.getOutputStream();
			outstream.write("stat".getBytes());
			outstream.flush();
			sock.shutdownOutput();

			reader = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));

			while ((line = reader.readLine()) != null) {
				if (line.indexOf("Mode: ") != -1)
					;
				ret = line.replaceAll("Mode: ", "").trim();
			}
		} catch (Exception ex) {
			LOG.error("Read ZK buffer has error,msg is " + ex.getMessage());
			line = "death";

			return line;
		} finally {
			try {
				sock.close();
				if (reader != null)
					reader.close();
			} catch (Exception ex) {
				LOG.error("Close read has error,msg is " + ex.getMessage());
			}
		}
		return ret;
	}
}