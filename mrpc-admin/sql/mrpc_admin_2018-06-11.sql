# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.22)
# Database: mrpc_admin
# Generation Time: 2018-06-11 05:32:22 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table rpc_clients
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rpc_clients`;

CREATE TABLE `rpc_clients` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_id` varchar(100) NOT NULL DEFAULT '' COMMENT '消费者APPID',
  `app_alias` varchar(100) DEFAULT NULL COMMENT '消费者别名',
  `host` varchar(20) NOT NULL DEFAULT '' COMMENT '消费者IP',
  `pid` int(5) NOT NULL COMMENT '消费者进程号',
  `owner` varchar(100) DEFAULT NULL COMMENT '消费者负责人',
  `owner_email` varchar(100) DEFAULT NULL COMMENT '负责人邮箱',
  `online_time` datetime DEFAULT NULL COMMENT '最后一次上线时间',
  `created_time` datetime DEFAULT NULL COMMENT '消费者创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `udx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='RPC服务消费者';

LOCK TABLES `rpc_clients` WRITE;
/*!40000 ALTER TABLE `rpc_clients` DISABLE KEYS */;

INSERT INTO `rpc_clients` (`id`, `app_id`, `app_alias`, `host`, `pid`, `owner`, `owner_email`, `online_time`, `created_time`)
VALUES
	(1,'demo',NULL,'10.10.30.205',54582,NULL,NULL,'2018-06-11 13:27:46','2018-06-11 13:27:47');

/*!40000 ALTER TABLE `rpc_clients` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table rpc_notices
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rpc_notices`;

CREATE TABLE `rpc_notices` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `address` varchar(30) NOT NULL DEFAULT '',
  `api_type` varchar(50) NOT NULL DEFAULT '',
  `content` varchar(2000) NOT NULL DEFAULT '',
  `created_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='RPC推送通知';



# Dump of table rpc_server_calls
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rpc_server_calls`;

CREATE TABLE `rpc_server_calls` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `producer_app_id` varchar(100) NOT NULL DEFAULT '',
  `consumer_app_id` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `udx_producer_consumer` (`producer_app_id`,`consumer_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务调用关系';

LOCK TABLES `rpc_server_calls` WRITE;
/*!40000 ALTER TABLE `rpc_server_calls` DISABLE KEYS */;

INSERT INTO `rpc_server_calls` (`id`, `producer_app_id`, `consumer_app_id`)
VALUES
	(2,'demo','demo'),
	(1,'demo1','demo');

/*!40000 ALTER TABLE `rpc_server_calls` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table rpc_servers
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rpc_servers`;

CREATE TABLE `rpc_servers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_id` varchar(50) NOT NULL DEFAULT '' COMMENT 'AppId',
  `app_alias` varchar(50) DEFAULT NULL COMMENT 'App别名',
  `host` varchar(20) NOT NULL DEFAULT '',
  `port` int(5) NOT NULL,
  `pid` int(5) DEFAULT NULL COMMENT '进程id',
  `status` varchar(20) NOT NULL DEFAULT '' COMMENT '服务状态 online:在线 offline:离线',
  `owner` varchar(50) DEFAULT NULL COMMENT '负责人',
  `owner_email` varchar(50) DEFAULT NULL COMMENT '负责人邮箱',
  `online_time` datetime NOT NULL COMMENT '最后一次上线时间',
  `offline_time` datetime DEFAULT NULL COMMENT '最后一次下线时间',
  `updated_time` datetime DEFAULT NULL COMMENT '最后操作时间',
  PRIMARY KEY (`id`),
  KEY `udx_app_server_port` (`app_id`,`host`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='RPC服务节点';

LOCK TABLES `rpc_servers` WRITE;
/*!40000 ALTER TABLE `rpc_servers` DISABLE KEYS */;

INSERT INTO `rpc_servers` (`id`, `app_id`, `app_alias`, `host`, `port`, `pid`, `status`, `owner`, `owner_email`, `online_time`, `offline_time`, `updated_time`)
VALUES
	(12,'demo','测试服务2','10.10.30.160',5069,36506,'OFFLINE','biezhi','biezhi.me@gmail.com','2018-06-08 18:18:20','2018-06-08 18:32:08',NULL);

/*!40000 ALTER TABLE `rpc_servers` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table rpc_services
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rpc_services`;

CREATE TABLE `rpc_services` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `service_id` varchar(200) NOT NULL DEFAULT '' COMMENT '服务全名',
  `service_alias` varchar(100) DEFAULT NULL COMMENT '服务别名',
  `app_id` varchar(20) NOT NULL DEFAULT '' COMMENT '所属服务',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='RPC服务接口';

LOCK TABLES `rpc_services` WRITE;
/*!40000 ALTER TABLE `rpc_services` DISABLE KEYS */;

INSERT INTO `rpc_services` (`id`, `service_id`, `service_alias`, `app_id`)
VALUES
	(157,'com.kongzhong.mrpc.demo.service.PayService',NULL,'demo1'),
	(158,'com.kongzhong.mrpc.demo.service.BenchmarkService',NULL,'demo1'),
	(159,'com.kongzhong.mrpc.demo.service.UserService',NULL,'demo1'),
	(160,'com.kongzhong.mrpc.embedded.ConfigService',NULL,'demo1'),
	(273,'com.kongzhong.mrpc.demo.service.PayService',NULL,'demo'),
	(274,'com.kongzhong.mrpc.demo.service.BenchmarkService',NULL,'demo'),
	(275,'com.kongzhong.mrpc.demo.service.UserService',NULL,'demo'),
	(276,'com.kongzhong.mrpc.embedded.ConfigService',NULL,'demo');

/*!40000 ALTER TABLE `rpc_services` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_logs
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sys_logs`;

CREATE TABLE `sys_logs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `action` varchar(50) NOT NULL DEFAULT '',
  `username` varchar(50) NOT NULL DEFAULT '',
  `content` varchar(1000) DEFAULT NULL,
  `ip` varchar(20) DEFAULT NULL,
  `created_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统操作日志';



# Dump of table sys_users
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sys_users`;

CREATE TABLE `sys_users` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `username` varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(50) NOT NULL DEFAULT '' COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(100) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(255) NOT NULL COMMENT '状态 0:禁用，1:正常',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_id` bigint(255) NOT NULL COMMENT '创建用户id',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `modified_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统用户';

LOCK TABLES `sys_users` WRITE;
/*!40000 ALTER TABLE `sys_users` DISABLE KEYS */;

INSERT INTO `sys_users` (`user_id`, `username`, `password`, `email`, `mobile`, `status`, `remark`, `created_id`, `created_time`, `modified_time`)
VALUES
	(1,'admin','a66abb5684c45962d887564f08346e8d','admin@example.com','13000000001',1,NULL,1,'2018-06-05 21:40:39','2017-08-15 21:41:00');

/*!40000 ALTER TABLE `sys_users` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
