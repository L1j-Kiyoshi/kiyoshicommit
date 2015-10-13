-- ----------------------------
-- Table structure for `raid_drop`
-- ----------------------------
DROP TABLE IF EXISTS `raid_drop`;
CREATE TABLE `raid_drop` (
  `raid_id` int(10) NOT NULL,
  `pattern_id` int(10) NOT NULL,
  `set_id` int(10) NOT NULL,
  `item_id` int(10) NOT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `min_count` int(10) NOT NULL DEFAULT '1',
  `max_count` int(10) NOT NULL DEFAULT '1',
  `chance` int(10) unsigned NOT NULL DEFAULT '1000000',
  PRIMARY KEY (`raid_id`,`pattern_id`,`set_id`,`item_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
