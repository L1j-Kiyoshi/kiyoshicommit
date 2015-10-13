/*
MySQL Data Transfer
Source Host: localhost
Source Database: l1jdb
Target Host: localhost
Target Database: l1jdb
Date: 2014/03/17 15:49:22
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `distortion`
-- ----------------------------
DROP TABLE IF EXISTS `distortion`;
CREATE TABLE `distortion` (
  `id` int(10) NOT NULL DEFAULT '0',
  `next_time` datetime DEFAULT NULL,
  `close_time` datetime DEFAULT NULL,
  `distortionX` int(10) NOT NULL DEFAULT '0',
  `distortionY` int(10) NOT NULL DEFAULT '0',
  `distortionMapId` int(10) NOT NULL DEFAULT '0',
  `teleportMapId` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;