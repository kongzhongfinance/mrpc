# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.22)
# Database: mrpc_admin
# Generation Time: 2018-06-06 11:16:00 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table consumers
# ------------------------------------------------------------

DROP TABLE IF EXISTS `consumers`;

CREATE TABLE `consumers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_id` varchar(100) NOT NULL DEFAULT '' COMMENT '消费者APPID',
  `consumer_alias` varchar(100) DEFAULT NULL COMMENT '消费者别名',
  `ip` varchar(20) DEFAULT NULL COMMENT '消费者IP',
  `port` tinyint(5) DEFAULT NULL COMMENT '消费者启动端口',
  `pid` tinyint(5) DEFAULT NULL COMMENT '消费者进程ID',
  `owner` varchar(100) DEFAULT NULL COMMENT '消费者负责人',
  `owner_email` varchar(100) DEFAULT NULL COMMENT '负责人邮箱',
  `online_time` datetime DEFAULT NULL COMMENT '最后一次上线时间',
  `created_time` datetime DEFAULT NULL COMMENT '消费者创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `udx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务消费者';



# Dump of table producers
# ------------------------------------------------------------

DROP TABLE IF EXISTS `producers`;

CREATE TABLE `producers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_id` varchar(100) NOT NULL DEFAULT '' COMMENT '服务APPID',
  `service_id` varchar(200) NOT NULL DEFAULT '' COMMENT '服务全名',
  `service_alias` varchar(100) DEFAULT NULL COMMENT '服务别名',
  `ip` varchar(20) NOT NULL DEFAULT '' COMMENT '服务所在主机',
  `port` int(5) NOT NULL COMMENT '服务端口',
  `pid` int(5) DEFAULT NULL COMMENT '启动进程ID',
  `version` varchar(20) DEFAULT NULL COMMENT '服务版本号',
  `status` varchar(20) NOT NULL DEFAULT '' COMMENT '服务状态',
  `owner` varchar(50) DEFAULT NULL COMMENT '负责人',
  `owner_email` varchar(50) DEFAULT NULL COMMENT '负责人邮箱',
  `online_time` datetime NOT NULL COMMENT '最后一次上线时间',
  `offline_time` datetime NOT NULL COMMENT '最后一次下线时间',
  `updated_time` datetime NOT NULL COMMENT '最后操作时间',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务';



# Dump of table service_calls
# ------------------------------------------------------------

DROP TABLE IF EXISTS `service_calls`;

CREATE TABLE `service_calls` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `producer_app_id` varchar(100) NOT NULL DEFAULT '',
  `consumer_app_id` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `udx_producer_consumer` (`producer_app_id`,`consumer_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务调用关系';



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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志';



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




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
