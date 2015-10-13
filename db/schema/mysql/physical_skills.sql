-- ----------------------------
-- Table structure for `physical_skills`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `physical_skills` (
  `skill_id` int(10) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL DEFAULT 'attack',
  `ranged` int(10) NOT NULL DEFAULT '1',
  `action_id` int(10) NOT NULL DEFAULT '-1',
  `damage_value` int(10) unsigned NOT NULL DEFAULT '0',
  `damage_dice` int(10) unsigned NOT NULL DEFAULT '0',
  `damage_dice_count` int(10) unsigned NOT NULL DEFAULT '0',
  `area` int(10) NOT NULL DEFAULT '0',
  `base_left_right` int(10) NOT NULL DEFAULT '0',
  `base_front_back` int(10) NOT NULL DEFAULT '0',
  `area_front` int(10) NOT NULL DEFAULT '0',
  `area_back` int(10) NOT NULL DEFAULT '0',
  `area_right` int(10) NOT NULL DEFAULT '0',
  `area_left` int(10) NOT NULL DEFAULT '0',
  `castgfx` int(10) NOT NULL DEFAULT '-1',
  `area_effect` int(10) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
