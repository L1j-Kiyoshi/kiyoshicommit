-- ----------------------------
-- Table structure for raid_config
-- ----------------------------
DROP TABLE IF EXISTS `raid_config`;
CREATE TABLE `raid_config` (
  `raid_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `raid_name` varchar(255) DEFAULT NULL,
  `min_level` int(11) NOT NULL DEFAULT '-1',
  `max_level` int(11) NOT NULL DEFAULT '-1',
  `min_player` int(11) NOT NULL DEFAULT '-1',
  `max_player` int(11) NOT NULL DEFAULT '-1',
  `max_raid_limit` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`raid_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
