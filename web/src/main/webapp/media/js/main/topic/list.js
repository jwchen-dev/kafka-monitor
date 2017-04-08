$(document).ready(function() {
	$("#result").dataTable({
		//"searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/AnotherKafkaMonitor/topic/list/table/ajax",
		"aoColumns" : [ {
			"mData" : 'id'
		}, {
			"mData" : 'topic'
		}, {
			"mData" : 'partitions'
		}, {
			"mData" : 'partitionNumbers'
		} , {
			"mData" : 'created'
		}, {
			"mData" : 'modify'
		}]
	});

	function retrieveData(sSource, aoData, fnCallback) {
		$.ajax({
			"type" : "get",
			"contentType" : "application/json",
			"url" : sSource,
			"dataType" : "json",
			"data" : {
				aoData : JSON.stringify(aoData)
			},
			"success" : function(data) {
				fnCallback(data)
			}
		});
	}
});