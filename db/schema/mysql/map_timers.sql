-- ----------------------------
-- Table structure for `map_timers`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `map_timers` (
  `account_id` int(10) unsigned NOT NULL,
  `char_id` int(10) unsigned NOT NULL,
  `area_id` int(10) unsigned NOT NULL,
  `remaining_time` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`account_id`,`char_id`,`area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
