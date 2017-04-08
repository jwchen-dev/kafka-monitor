<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!-- Navigation -->
<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
	<!-- Brand and toggle get grouped for better mobile display -->
	<div class="navbar-header">
		<button type="button" class="navbar-toggle" data-toggle="collapse"
			data-target=".navbar-ex1-collapse">
			<span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span>
			<span class="icon-bar"></span> <span class="icon-bar"></span>
		</button>
		<a class="navbar-brand" href="/AnotherKafkaMonitor">AnotherKafkaMonitor</a>
	</div>
	<!-- Top Menu Items -->
	<ul class="nav navbar-right top-nav">
		<li class="dropdown"><a href="#" class="dropdown-toggle"
			data-toggle="dropdown"><i class="fa fa-bookmark"></i> V1.1.0 </a></li>
	</ul>
	<!-- Sidebar Menu Items - These collapse to the responsive navigation menu on small screens -->
	<div class="collapse navbar-collapse navbar-ex1-collapse">
		<ul class="nav navbar-nav side-nav">
			<li id="navbar_dash"><a href="/AnotherKafkaMonitor"><i
					class="fa fa-fw fa-dashboard"></i> Dashboard</a></li>
			<li><a href="#" data-toggle="collapse" data-target="#demo"><i
					class="fa fa-fw fa-comments-o"></i> Topic <i
					class="fa fa-fw fa-caret-down"></i></a>
				<ul id="demo" class="collapse">
					<li id="navbar_list"><a href="/AnotherKafkaMonitor/topic/list"><i
							class="fa fa-table fa-fw"></i> TopicList</a></li>
					<li id="navbar_create"><a href="/AnotherKafkaMonitor/topic/create"><i
							class="fa fa-plus-square-o fa-fw"></i> Create</a></li>
				</ul></li>
			<li id="navbar_consumers"><a href="/AnotherKafkaMonitor/consumers"><i
					class="fa fa-fw fa-users"></i> Consumers</a></li>
			
			
			<li id="navbar_cli"><a href="/AnotherKafkaMonitor/cluster/info"><i
							class="fa fa-sitemap fa-fw"></i> ClusterInfo</a></li>
					
			<!--  		
			<li><a href="#" data-toggle="collapse" data-target="#demo2"><i
					class="fa fa-fw fa-cloud"></i> Cluster Info <i
					class="fa fa-fw fa-caret-down"></i></a>
				<ul id="demo2" class="collapse">
					<li id="navbar_cli"><a href="/AnotherKafkaMonitor/cluster/info"><i
							class="fa fa-sitemap fa-fw"></i> ZK & Kafka</a></li>
					<li id="navbar_zk"><a href="/AnotherKafkaMonitor/cluster/zkcli"><i
							class="fa fa-terminal fa-fw"></i> ZkCli</a></li>
				</ul>
			</li>
			-->	
			
			<li><a href="#" data-toggle="collapse" data-target="#demo1"><i
					class="fa fa-fw fa-bell"></i> Alarm <i
					class="fa fa-fw fa-caret-down"></i></a>
				<ul id="demo1" class="collapse">
					<li id="navbar_modify"><a href="/AnotherKafkaMonitor/alarm/modify"><i
							class="fa fa-edit fa-fw"></i> Alarm List</a></li>
					<li id="navbar_add"><a href="/AnotherKafkaMonitor/alarm/add"><i
							class="fa fa-info-circle fa-fw"></i> Add</a></li>
				</ul>
			</li>
			
			<li id="navbar_zk"><a href="/AnotherKafkaMonitor/cluster/zkcli"><i
							class="fa fa-terminal fa-fw"></i> ZkShell</a></li>
		</ul>
	</div>
	<!-- /.navbar-collapse -->
</nav>