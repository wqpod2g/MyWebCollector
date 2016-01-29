/*
Navicat MySQL Data Transfer

Source Server         : 114.212.82.189
Source Server Version : 50628
Source Host           : 114.212.82.189:3306
Source Database       : xianteng

Target Server Type    : MYSQL
Target Server Version : 50628
File Encoding         : 65001

Date: 2016-01-29 11:58:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `JWNEWS`
-- ----------------------------
DROP TABLE IF EXISTS `JWNEWS`;
CREATE TABLE `JWNEWS` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `source` varchar(50) NOT NULL,
  `sentiment` int(2) NOT NULL,
  `crawltime` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4982 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of JWNEWS
-- ----------------------------
