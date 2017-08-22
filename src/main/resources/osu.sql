# Host: localhost  (Version 5.7.18-log)
# Date: 2017-08-22 22:36:21
# Generator: MySQL-Front 6.0  (Build 2.20)


#
# Structure for table "userrole"
#

DROP TABLE IF EXISTS `userrole`;
CREATE TABLE `userrole` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `role` varchar(255) NOT NULL DEFAULT 'creep',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `唯一索引` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

#
# Structure for table "userinfo"
#

DROP TABLE IF EXISTS `userinfo`;
CREATE TABLE `userinfo` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `count300` int(11) DEFAULT NULL,
  `count100` int(11) DEFAULT NULL,
  `count50` int(11) DEFAULT NULL,
  `playcount` int(11) DEFAULT NULL,
  `accuracy` float DEFAULT NULL,
  `pp_raw` float DEFAULT NULL,
  `ranked_score` bigint(10) DEFAULT NULL,
  `total_score` bigint(10) DEFAULT NULL,
  `level` float DEFAULT NULL,
  `pp_rank` int(11) DEFAULT NULL,
  `count_rank_ss` int(11) DEFAULT NULL,
  `count_rank_s` int(11) DEFAULT NULL,
  `count_rank_a` int(11) DEFAULT NULL,
  `queryDate` date DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `1` (`user_id`),
  CONSTRAINT `1` FOREIGN KEY (`user_id`) REFERENCES `userrole` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
