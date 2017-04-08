$(document).ready(function() {
	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/AnotherKafkaMonitor/cluster/info/ajax',
		success : function(datas) {
			if (datas != null) {
				var kafka = JSON.parse(datas.kafka);
				var zk = JSON.parse(datas.zk);
				$("#kafka_tab").html("");
				$("#zk_tab").html("");
				var kafka_tab = "<thead><tr><th>ID</th><th>IP</th><th>Port</th><th>Created</th><th>Modify</th></tr></thead><tbody>";
				for (var i = 0; i < kafka.length; i++) {
					kafka_tab += " <tr><td>" + kafka[i].id + "</td><td>" + kafka[i].host + "</td><td>" + kafka[i].port + "</td><td>" + kafka[i].created + "</td><td>" + kafka[i].modify + "</td></tr>";
				}
				kafka_tab += "</tbody>"
				$("#kafka_tab").append(kafka_tab);

				var zk_tab = "<thead><tr><th>ID</th><th>IP</th><th>Port</th><th>Mode</th></tr></thead><tbody>";
				for (var i = 0; i < zk.length; i++) {
					if(zk[i].mode=="death"){
						zk_tab += " <tr><td>" + zk[i].id + "</td><td>" + zk[i].ip + "</td><td>" + zk[i].port + "</td><td><a class='btn btn-danger btn-xs'>" + zk[i].mode + "</a></td></tr>";
					}else{
						zk_tab += " <tr><td>" + zk[i].id + "</td><td>" + zk[i].ip + "</td><td>" + zk[i].port + "</td><td><a class='btn btn-success btn-xs'>" + zk[i].mode + "</a></td></tr>";
					}
				}
				zk_tab += "</tbody>"
				$("#zk_tab").append(zk_tab);
			}
		}
	});
});