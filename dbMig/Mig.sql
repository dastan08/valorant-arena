-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: valorant_arena
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `brackets`
--

DROP TABLE IF EXISTS `brackets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brackets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bracket_type` enum('LOSERS','WINNERS') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `match_number` int NOT NULL,
  `round_number` int NOT NULL,
  `status` enum('COMPLETED','IN_PROGRESS','PENDING','READY') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `loser_bracket_id` bigint DEFAULT NULL,
  `match_id` bigint DEFAULT NULL,
  `next_match_bracket_id` bigint DEFAULT NULL,
  `tournament_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9skd24ye1g4n9da5sngnacid8` (`loser_bracket_id`),
  KEY `FKf9kvmmc77dqifanpebu448h71` (`next_match_bracket_id`),
  KEY `idx_brackets_tournament_id` (`tournament_id`),
  KEY `idx_brackets_match_id` (`match_id`),
  KEY `idx_brackets_round` (`tournament_id`,`round_number`),
  CONSTRAINT `FK9skd24ye1g4n9da5sngnacid8` FOREIGN KEY (`loser_bracket_id`) REFERENCES `brackets` (`id`),
  CONSTRAINT `FKbx2qbgegus33b9ag9b0hkhpqg` FOREIGN KEY (`match_id`) REFERENCES `tournament_matches` (`id`),
  CONSTRAINT `FKdgsn7dsf1ntrg89altwkr334o` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`),
  CONSTRAINT `FKf9kvmmc77dqifanpebu448h71` FOREIGN KEY (`next_match_bracket_id`) REFERENCES `brackets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brackets`
--

LOCK TABLES `brackets` WRITE;
/*!40000 ALTER TABLE `brackets` DISABLE KEYS */;
/*!40000 ALTER TABLE `brackets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `matches`
--

DROP TABLE IF EXISTS `matches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `matches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bestOf` int NOT NULL,
  `createdAt` datetime(6) NOT NULL,
  `endTime` datetime(6) DEFAULT NULL,
  `notes` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `round` int NOT NULL,
  `scheduledTime` datetime(6) NOT NULL,
  `serverInfo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `startTime` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','DISPUTED','IN_PROGRESS','SCHEDULED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `team1Score` int NOT NULL,
  `team2Score` int NOT NULL,
  `updatedAt` datetime(6) NOT NULL,
  `team1_id` bigint NOT NULL,
  `team2_id` bigint NOT NULL,
  `tournament_id` bigint NOT NULL,
  `winner_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3ioil1py4fu8omd77sivakcwi` (`team1_id`),
  KEY `FKdkphr8xw4l2dgywsnbdbe04d7` (`team2_id`),
  KEY `FKbeel88lh2ksupphafotnqy7ry` (`winner_id`),
  KEY `FKeeniokyjgo5k6rmhjujatn27i` (`tournament_id`),
  CONSTRAINT `FK3ioil1py4fu8omd77sivakcwi` FOREIGN KEY (`team1_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `FKbeel88lh2ksupphafotnqy7ry` FOREIGN KEY (`winner_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `FKdkphr8xw4l2dgywsnbdbe04d7` FOREIGN KEY (`team2_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `FKeeniokyjgo5k6rmhjujatn27i` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `matches`
--

LOCK TABLES `matches` WRITE;
/*!40000 ALTER TABLE `matches` DISABLE KEYS */;
/*!40000 ALTER TABLE `matches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `matchmaking_queue`
--

DROP TABLE IF EXISTS `matchmaking_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `matchmaking_queue` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elo` int NOT NULL,
  `matchFoundAt` datetime(6) DEFAULT NULL,
  `matchId` bigint DEFAULT NULL,
  `queuedAt` datetime(6) NOT NULL,
  `region` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACCEPTED','DECLINED','EXPIRED','MATCH_FOUND','SEARCHING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK399uxcghscclsayh1fgcxambr` (`user_id`),
  CONSTRAINT `FK399uxcghscclsayh1fgcxambr` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `matchmaking_queue`
--

LOCK TABLES `matchmaking_queue` WRITE;
/*!40000 ALTER TABLE `matchmaking_queue` DISABLE KEYS */;
/*!40000 ALTER TABLE `matchmaking_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `organizers`
--

DROP TABLE IF EXISTS `organizers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organizers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `discord_server` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `organization_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rejection_reason` text COLLATE utf8mb4_unicode_ci,
  `reputation_score` decimal(3,2) DEFAULT NULL,
  `total_tournaments_hosted` int DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `verification_status` enum('PENDING','REJECTED','SUSPENDED','VERIFIED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `website_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `verified_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbllnmcub455230xod6jx6akim` (`verified_by`),
  KEY `idx_organizers_user_id` (`user_id`),
  KEY `idx_organizers_verification_status` (`verification_status`),
  CONSTRAINT `FK7kpidig5uothy6p9ttywnif6l` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKbllnmcub455230xod6jx6akim` FOREIGN KEY (`verified_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organizers`
--

LOCK TABLES `organizers` WRITE;
/*!40000 ALTER TABLE `organizers` DISABLE KEYS */;
INSERT INTO `organizers` VALUES (9,'2025-10-12 19:08:53.950284',NULL,NULL,'admin\'s Organization',NULL,0.00,0,'2025-10-12 19:08:53.950284','VERIFIED',NULL,NULL,5,NULL);
/*!40000 ALTER TABLE `organizers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scrims`
--

DROP TABLE IF EXISTS `scrims`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scrims` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bestOf` int NOT NULL,
  `challengerScore` int DEFAULT NULL,
  `completedAt` datetime(6) DEFAULT NULL,
  `createdAt` datetime(6) NOT NULL,
  `map` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `opponentScore` int DEFAULT NULL,
  `respondedAt` datetime(6) DEFAULT NULL,
  `result` enum('CANCELLED','CHALLENGER_WIN','DRAW','OPPONENT_WIN') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `scheduledTime` datetime(6) NOT NULL,
  `status` enum('ACCEPTED','CANCELLED','COMPLETED','DECLINED','IN_PROGRESS','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updatedAt` datetime(6) NOT NULL,
  `challenger_team_id` bigint NOT NULL,
  `opponent_team_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcq2b7n4kuwu82g4piany5f9q4` (`challenger_team_id`),
  KEY `FKjjjxq1h23dk0u12cuqlfwm39j` (`opponent_team_id`),
  CONSTRAINT `FKcq2b7n4kuwu82g4piany5f9q4` FOREIGN KEY (`challenger_team_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `FKjjjxq1h23dk0u12cuqlfwm39j` FOREIGN KEY (`opponent_team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scrims`
--

LOCK TABLES `scrims` WRITE;
/*!40000 ALTER TABLE `scrims` DISABLE KEYS */;
/*!40000 ALTER TABLE `scrims` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_invitations`
--

DROP TABLE IF EXISTS `team_invitations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_invitations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createdAt` datetime(6) NOT NULL,
  `message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `respondedAt` datetime(6) DEFAULT NULL,
  `status` enum('ACCEPTED','CANCELLED','DECLINED','EXPIRED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `invited_by_id` bigint NOT NULL,
  `invited_user_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnqxer3j4dc7fj8dei8o35xi0` (`invited_by_id`),
  KEY `FK7l34iko2yklgfxq26dw0atkyu` (`invited_user_id`),
  KEY `FKn2spna496q3cr3fmqc8gda2my` (`team_id`),
  CONSTRAINT `FK7l34iko2yklgfxq26dw0atkyu` FOREIGN KEY (`invited_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKn2spna496q3cr3fmqc8gda2my` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `FKnqxer3j4dc7fj8dei8o35xi0` FOREIGN KEY (`invited_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_invitations`
--

LOCK TABLES `team_invitations` WRITE;
/*!40000 ALTER TABLE `team_invitations` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_invitations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teams` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createdAt` datetime(6) NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `elo` int NOT NULL,
  `isActive` bit(1) NOT NULL,
  `logo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `maxMembers` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `losses` int NOT NULL,
  `matchesPlayed` int NOT NULL,
  `tournamentsWon` int NOT NULL,
  `winRate` double NOT NULL,
  `wins` int NOT NULL,
  `tag` varchar(5) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updatedAt` datetime(6) NOT NULL,
  `captain_id` bigint NOT NULL,
  `manager_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_a510no6sjwqcx153yd5sm4jrr` (`name`),
  UNIQUE KEY `UK_7f7rcwrri4dx5dygxjup6kq98` (`tag`),
  UNIQUE KEY `UKa510no6sjwqcx153yd5sm4jrr` (`name`),
  UNIQUE KEY `UK7f7rcwrri4dx5dygxjup6kq98` (`tag`),
  KEY `FKnm5gsd12609rk1sdsmo5d9nn4` (`captain_id`),
  KEY `FK957exwbsdbxyv2kir751ejid7` (`manager_id`),
  CONSTRAINT `FK957exwbsdbxyv2kir751ejid7` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKnm5gsd12609rk1sdsmo5d9nn4` FOREIGN KEY (`captain_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teams`
--

LOCK TABLES `teams` WRITE;
/*!40000 ALTER TABLE `teams` DISABLE KEYS */;
INSERT INTO `teams` VALUES (1,'2025-10-13 11:27:27.998985','',1000,_binary '','',5,'yryty',0,0,0,0,0,'dsadd','2025-10-13 11:27:27.998985',11,11);
/*!40000 ALTER TABLE `teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tournament_disputes`
--

DROP TABLE IF EXISTS `tournament_disputes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tournament_disputes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tournament_id` bigint NOT NULL,
  `match_id` bigint DEFAULT NULL,
  `reported_by` bigint NOT NULL,
  `reported_against` bigint DEFAULT NULL,
  `dispute_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `evidence_urls` json DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPEN',
  `assigned_to` bigint DEFAULT NULL,
  `resolution` text COLLATE utf8mb4_unicode_ci,
  `resolved_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tournament_disputes_tournament_id` (`tournament_id`),
  KEY `idx_tournament_disputes_status` (`status`),
  KEY `idx_tournament_disputes_assigned_to` (`assigned_to`),
  CONSTRAINT `chk_dispute_status` CHECK ((`status` in (_utf8mb4'OPEN',_utf8mb4'UNDER_REVIEW',_utf8mb4'RESOLVED',_utf8mb4'REJECTED'))),
  CONSTRAINT `chk_dispute_type` CHECK ((`dispute_type` in (_utf8mb4'SCORE_DISPUTE',_utf8mb4'CHEATING',_utf8mb4'RULE_VIOLATION',_utf8mb4'NO_SHOW',_utf8mb4'CONNECTION_ISSUE',_utf8mb4'OTHER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tournament_disputes`
--

LOCK TABLES `tournament_disputes` WRITE;
/*!40000 ALTER TABLE `tournament_disputes` DISABLE KEYS */;
/*!40000 ALTER TABLE `tournament_disputes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tournament_entries`
--

DROP TABLE IF EXISTS `tournament_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tournament_entries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `checked_in` bit(1) DEFAULT NULL,
  `checked_in_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `eliminated_at` datetime(6) DEFAULT NULL,
  `elimination_round` int DEFAULT NULL,
  `entry_type` enum('SOLO','TEAM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `final_placement` int DEFAULT NULL,
  `payment_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_status` enum('PAID','PENDING','REFUNDED') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seed_number` int DEFAULT NULL,
  `status` enum('CHECKED_IN','DISQUALIFIED','ELIMINATED','REGISTERED','WINNER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `team_id` bigint DEFAULT NULL,
  `tournament_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tournament_entries_tournament_id` (`tournament_id`),
  KEY `idx_tournament_entries_user_id` (`user_id`),
  KEY `idx_tournament_entries_team_id` (`team_id`),
  KEY `idx_tournament_entries_status` (`status`),
  CONSTRAINT `FK9kshq3ox3hby1q1ts4a06ji22` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `FKl251cb7xrof309k8b67a6ok20` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKlpuuu0ixys07qdp6cf9pryfr3` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tournament_entries`
--

LOCK TABLES `tournament_entries` WRITE;
/*!40000 ALTER TABLE `tournament_entries` DISABLE KEYS */;
INSERT INTO `tournament_entries` VALUES (1,_binary '\0',NULL,'2025-10-13 12:11:39.796853',NULL,NULL,'SOLO',NULL,NULL,'PENDING',NULL,'REGISTERED','2025-10-13 12:11:39.796853',NULL,2,11),(2,_binary '\0',NULL,'2025-10-13 12:21:28.358057',NULL,NULL,'TEAM',NULL,NULL,'PENDING',NULL,'REGISTERED','2025-10-13 12:21:28.358057',1,2,NULL);
/*!40000 ALTER TABLE `tournament_entries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tournament_matches`
--

DROP TABLE IF EXISTS `tournament_matches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tournament_matches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tournament_id` bigint NOT NULL,
  `round_number` int NOT NULL,
  `match_number` int NOT NULL,
  `participant1_id` bigint DEFAULT NULL,
  `participant2_id` bigint DEFAULT NULL,
  `scheduled_time` timestamp NULL DEFAULT NULL,
  `actual_start_time` timestamp NULL DEFAULT NULL,
  `actual_end_time` timestamp NULL DEFAULT NULL,
  `participant1_score` int DEFAULT '0',
  `participant2_score` int DEFAULT '0',
  `winner_id` bigint DEFAULT NULL,
  `best_of` int DEFAULT '1',
  `maps_played` json DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SCHEDULED',
  `reported_by` bigint DEFAULT NULL,
  `reported_at` timestamp NULL DEFAULT NULL,
  `confirmed_by` bigint DEFAULT NULL,
  `confirmed_at` timestamp NULL DEFAULT NULL,
  `screenshot_urls` json DEFAULT NULL,
  `match_replay_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_disputed` tinyint(1) DEFAULT '0',
  `dispute_reason` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tournament_matches_tournament_id` (`tournament_id`),
  KEY `idx_tournament_matches_status` (`status`),
  KEY `idx_tournament_matches_scheduled_time` (`scheduled_time`),
  KEY `FK1qmku6na6j4qc5xqjd7jn05it` (`confirmed_by`),
  KEY `FKfi0c7b6pjhbwjyk19v1a9babc` (`participant1_id`),
  KEY `FKlsnrunsgmgxet2m78bcmqt5mp` (`participant2_id`),
  KEY `FKaxmi184qv0pdob36ie0nwdct1` (`reported_by`),
  KEY `FKik50l93aqu201gst3sax0d6pu` (`winner_id`),
  CONSTRAINT `FK1qmku6na6j4qc5xqjd7jn05it` FOREIGN KEY (`confirmed_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKaxmi184qv0pdob36ie0nwdct1` FOREIGN KEY (`reported_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfi0c7b6pjhbwjyk19v1a9babc` FOREIGN KEY (`participant1_id`) REFERENCES `tournament_entries` (`id`),
  CONSTRAINT `FKik50l93aqu201gst3sax0d6pu` FOREIGN KEY (`winner_id`) REFERENCES `tournament_entries` (`id`),
  CONSTRAINT `FKlsnrunsgmgxet2m78bcmqt5mp` FOREIGN KEY (`participant2_id`) REFERENCES `tournament_entries` (`id`),
  CONSTRAINT `FKntwq3hn28yb2475ayat61cpdt` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`),
  CONSTRAINT `chk_match_status` CHECK ((`status` in (_utf8mb4'SCHEDULED',_utf8mb4'READY',_utf8mb4'IN_PROGRESS',_utf8mb4'COMPLETED',_utf8mb4'DISPUTED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tournament_matches`
--

LOCK TABLES `tournament_matches` WRITE;
/*!40000 ALTER TABLE `tournament_matches` DISABLE KEYS */;
/*!40000 ALTER TABLE `tournament_matches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tournament_moderators`
--

DROP TABLE IF EXISTS `tournament_moderators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tournament_moderators` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignedAt` datetime(6) NOT NULL,
  `notes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `assigned_by` bigint NOT NULL,
  `tournament_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbnuvpdp7ne33g4ojsnwuebyi1` (`tournament_id`,`user_id`),
  KEY `FK12f5fjg1o3pv8l143hyberpyr` (`assigned_by`),
  KEY `FK56b8yk03wddtk46e4hnpuxkhp` (`user_id`),
  CONSTRAINT `FK12f5fjg1o3pv8l143hyberpyr` FOREIGN KEY (`assigned_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK56b8yk03wddtk46e4hnpuxkhp` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpklfte19x1bw8ubhoooqtgvef` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Stores tournament moderator assignments';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tournament_moderators`
--

LOCK TABLES `tournament_moderators` WRITE;
/*!40000 ALTER TABLE `tournament_moderators` DISABLE KEYS */;
/*!40000 ALTER TABLE `tournament_moderators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tournament_statistics`
--

DROP TABLE IF EXISTS `tournament_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tournament_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tournament_id` bigint NOT NULL,
  `entry_id` bigint NOT NULL,
  `matches_played` int DEFAULT '0',
  `matches_won` int DEFAULT '0',
  `matches_lost` int DEFAULT '0',
  `rounds_won` int DEFAULT '0',
  `rounds_lost` int DEFAULT '0',
  `final_placement` int DEFAULT NULL,
  `prize_won` decimal(10,2) DEFAULT '0.00',
  `additional_stats` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_tournament_entry_stats` (`tournament_id`,`entry_id`),
  KEY `idx_tournament_statistics_tournament_id` (`tournament_id`),
  KEY `idx_tournament_statistics_entry_id` (`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tournament_statistics`
--

LOCK TABLES `tournament_statistics` WRITE;
/*!40000 ALTER TABLE `tournament_statistics` DISABLE KEYS */;
/*!40000 ALTER TABLE `tournament_statistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tournaments`
--

DROP TABLE IF EXISTS `tournaments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tournaments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `organizer_id` bigint NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `banner_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `game_mode` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `format` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_participants` int NOT NULL,
  `min_participants` int NOT NULL DEFAULT '4',
  `team_size` int NOT NULL DEFAULT '5',
  `min_rank` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `max_rank` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entry_fee` decimal(10,2) DEFAULT '0.00',
  `prize_pool` decimal(10,2) DEFAULT '0.00',
  `prize_distribution` json DEFAULT NULL,
  `registration_start` timestamp NOT NULL,
  `registration_end` timestamp NOT NULL,
  `check_in_start` timestamp NOT NULL,
  `check_in_end` timestamp NOT NULL,
  `tournament_start` timestamp NOT NULL,
  `tournament_end` timestamp NULL DEFAULT NULL,
  `rules` text COLLATE utf8mb4_unicode_ci,
  `match_format` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'BO1',
  `map_pool` json DEFAULT NULL,
  `allow_substitutes` tinyint(1) DEFAULT '0',
  `max_substitutes` int DEFAULT '0',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `current_round` int DEFAULT '0',
  `total_rounds` int DEFAULT NULL,
  `is_featured` tinyint(1) DEFAULT '0',
  `is_verified` tinyint(1) DEFAULT '0',
  `moderation_notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tournaments_organizer_id` (`organizer_id`),
  KEY `idx_tournaments_status` (`status`),
  KEY `idx_tournaments_start_date` (`tournament_start`),
  KEY `idx_tournaments_region` (`region`),
  CONSTRAINT `fk_tournaments_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `organizers` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_format` CHECK ((`format` in (_utf8mb4'SINGLE_ELIMINATION',_utf8mb4'DOUBLE_ELIMINATION',_utf8mb4'ROUND_ROBIN',_utf8mb4'SWISS'))),
  CONSTRAINT `chk_game_mode` CHECK ((`game_mode` in (_utf8mb4'SOLO',_utf8mb4'DUO',_utf8mb4'TEAM_5V5'))),
  CONSTRAINT `chk_tournament_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'OPEN',_utf8mb4'REGISTRATION_CLOSED',_utf8mb4'CHECK_IN',_utf8mb4'ONGOING',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tournaments`
--

LOCK TABLES `tournaments` WRITE;
/*!40000 ALTER TABLE `tournaments` DISABLE KEYS */;
INSERT INTO `tournaments` VALUES (1,9,' Valorant Champions League','A Gamers\' Heaven initiative  :Radiant: \n\nA Podium to Exhibit your Skillset and be the Spotlight.  :Immortal: \n\nRegistration Date : 21-06-2021\nTournament Date : 03-07-2021\nSlots : 32\nFREE ENTRY\nPrize Pool : 4000 INR\nRegistration link : https://forms.gle/vgSfQU177EReH2ds6\nDiscord link : https://discord.gg/QGDJfvtE\n\nTo participate, Fill up the Form and Join the Discord Server Given. There You have to Tag Your team to get Confirmation. And further Notifications will be Updated on Discord.','','TEAM_5V5','SINGLE_ELIMINATION',16,4,5,'','','ASIA',0.00,3000.00,'{}','2025-10-14 07:30:00','2025-10-21 07:30:00','2025-10-22 07:30:00','2025-10-23 07:30:00','2025-10-30 22:30:00',NULL,'- All matches except semi finals and final will be Best of 1.\n- Semi Finals will be Best of 3.\n- Grand Final will be Best of 5.\n-  All Maps, All Agents are allowed(except KAY/O).\n- 5 min of technical pause and two 2 min of tactical pauses are allowed for each team in the tournament.\n- Map will be selected through VETO. Process will be exaplained by admins.\n- Team captains has to report 30 minutes prior of the match time for the veto.\n- If a team doesn\'t show up during the match time, we will give a 10 min extra time to them to join in, if they doesn\'t show up, the opponents will get walkover. (Example, if the match is at 5 PM, we will wait max till 5.10 PM, if still one team doesn\'t show up, then the opponents get walkover).\n- Coaches/substitutes are allowed in the match lobby to spectate the match.\n- The matches will only and only be played on Mumbai Servers.\n- Requesting for changing the match date is not allowed. But you can change the time after discussing it with your opposing team and report it to the admins.\n- IGN changes are not allowed once the schedule is released.','BO1','[\"Bind\", \"Haven\", \"Split\", \"Lotus\", \"Pearl\", \"Sunset\"]',1,1,'DRAFT',0,NULL,0,0,NULL,'2025-10-12 13:38:54','2025-10-12 13:38:54'),(2,9,'Valorant Champions League','A Gamers\' Heaven initiative\nA Podium to Exhibit your Skillset and be the Spotlight.\n','/uploads/tournament-banners/7ae4d5e9-bfd6-46a9-b377-c937c55c00d6.png','TEAM_5V5','SINGLE_ELIMINATION',16,4,6,'','','ASIA',0.00,3000.00,'{}','2025-10-14 07:30:00','2025-10-21 07:30:00','2025-10-22 07:30:00','2025-10-23 07:30:00','2025-10-30 22:30:00',NULL,'- All matches except semi finals and final will be Best of 1.\n- Semi Finals will be Best of 3.\n- Grand Final will be Best of 5.\n-  All Maps, All Agents are allowed(except KAY/O).\n- 5 min of technical pause and two 2 min of tactical pauses are allowed for each team in the tournament.\n- Map will be selected through VETO. Process will be exaplained by admins.\n- Team captains has to report 30 minutes prior of the match time for the veto.\n- If a team doesn\'t show up during the match time, we will give a 10 min extra time to them to join in, if they doesn\'t show up, the opponents will get walkover. (Example, if the match is at 5 PM, we will wait max till 5.10 PM, if still one team doesn\'t show up, then the opponents get walkover).\n- Coaches/substitutes are allowed in the match lobby to spectate the match.\n- The matches will only and only be played on Mumbai Servers.\n- Requesting for changing the match date is not allowed. But you can change the time after discussing it with your opposing team and report it to the admins.\n- IGN changes are not allowed once the schedule is released.','BO1','[\"Bind\", \"Haven\", \"Split\", \"Lotus\", \"Pearl\", \"Sunset\"]',1,1,'OPEN',0,NULL,0,0,NULL,'2025-10-12 14:19:13','2025-10-12 14:26:38');
/*!40000 ALTER TABLE `tournaments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `createdAt` datetime(6) NOT NULL,
  `discordAvatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `discordId` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `discordUsername` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `elo` int NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `isVerified` bit(1) NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rank` enum('BRONZE','DIAMOND','GOLD','IMMORTAL','IRON','PLATINUM','RADIANT','SILVER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `region` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `riotAccessToken` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `riotGameName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `riotPuuid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `riotRefreshToken` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `riotTagLine` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `riotTokenExpiry` datetime(6) DEFAULT NULL,
  `role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `assists` int NOT NULL,
  `averageCombatScore` int NOT NULL,
  `deaths` int NOT NULL,
  `headshotPercentage` double NOT NULL,
  `kills` int NOT NULL,
  `losses` int NOT NULL,
  `matchesPlayed` int NOT NULL,
  `wins` int NOT NULL,
  `status` enum('ACTIVE','BANNED','SUSPENDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `team_role` enum('CAPTAIN','MANAGER','PLAYER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updatedAt` datetime(6) NOT NULL,
  `username` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `valorantId` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `team_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`),
  UNIQUE KEY `UK_aa5bjcksfvffksf73ubqhdtfc` (`valorantId`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKaa5bjcksfvffksf73ubqhdtfc` (`valorantId`),
  UNIQUE KEY `UK_db9wa19340t157bymj6npavxx` (`discordId`),
  UNIQUE KEY `UK_i1ulduwcsg5l6o4vabgs6enj5` (`riotPuuid`),
  KEY `FKfjws1rdruab2bqg7qipoqf65r` (`team_id`),
  CONSTRAINT `FKfjws1rdruab2bqg7qipoqf65r` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'default-avatar.png','2025-10-12 16:39:58.526620','3999d3a35abe625a7082a49a0695cc86','475637977121423360','dastan08',1000,'toaditya1999@gmail.com',_binary '','$2a$10$ePVPwvJpZ34SV09gTUCnJeJyMouPs3DfJbECexhi1x1V41EKuM/pG','IRON',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'ADMIN',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-12 16:40:24.481083','dastan08','dastan08#0820',NULL),(2,'default-avatar.png','2025-10-12 17:31:30.686606',NULL,NULL,NULL,1000,'toaditya08@gmail.com',_binary '\0','$2a$10$ymdBEJ/hCAS1lZjaAVSYJOi0syG78q4PRrEUSD.CaSov5zYfd8lye','SILVER',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'USER',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-12 20:05:13.794884','Aditya99','Dastan#0899',NULL),(5,'default-avatar.png','2025-10-12 17:59:56.003268',NULL,NULL,NULL,1000,'admin@test.com',_binary '','$2a$10$kiR5u32tTxg9RIBDfs1YhedgSXGLeBO6rizW8C8zQLhBJzwlMmiXy','DIAMOND',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'ORGANIZER',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-12 18:49:05.122021','admin','admin#1234',NULL),(6,'default-avatar.png','2025-10-13 16:28:03.425055',NULL,'ironwarrior#1234','IronWarrior',950,'testuser1@valorantarena.com',_binary '','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','IRON',NULL,NULL,'IronWarrior',NULL,NULL,'1234',NULL,'USER',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-13 16:28:03.425055','testuser1','IronWarrior#1234',NULL),(7,'default-avatar.png','2025-10-13 16:28:06.048753',NULL,'bronzeknight#5678','BronzeKnight',1050,'testuser2@valorantarena.com',_binary '','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','BRONZE',NULL,NULL,'BronzeKnight',NULL,NULL,'5678',NULL,'USER',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-13 16:28:06.048753','testuser2','BronzeKnight#5678',NULL),(8,'default-avatar.png','2025-10-13 16:28:08.847314',NULL,'silversniper#9012','SilverSniper',1150,'testuser3@valorantarena.com',_binary '','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','SILVER',NULL,NULL,'SilverSniper',NULL,NULL,'9012',NULL,'USER',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-13 16:28:08.847314','testuser3','SilverSniper#9012',NULL),(9,'default-avatar.png','2025-10-13 16:28:11.405412',NULL,'goldorganizer#3456','GoldOrganizer',1250,'testuser4@valorantarena.com',_binary '','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','GOLD',NULL,NULL,'GoldOrganizer',NULL,NULL,'3456',NULL,'ORGANIZER',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-13 16:28:11.405412','testuser4','GoldOrganizer#3456',NULL),(10,'default-avatar.png','2025-10-13 16:28:13.790301',NULL,'platinummod#7890','PlatinumMod',1350,'testuser5@valorantarena.com',_binary '','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','PLATINUM',NULL,NULL,'PlatinumMod',NULL,NULL,'7890',NULL,'MODERATOR',0,0,0,0,0,0,0,0,'ACTIVE',NULL,'2025-10-13 16:28:13.790301','testuser5','PlatinumMod#7890',NULL),(11,'default-avatar.png','2025-10-13 11:27:14.742404',NULL,NULL,NULL,1000,'testuser6@valorantarena.com',_binary '','$2a$10$kYOLzzoZChzCN7i4GWda7.CKWN76x4vbl9YHez4fHI99Y93yJQCzS','GOLD',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'USER',0,0,0,0,0,0,0,0,'ACTIVE','CAPTAIN','2025-10-13 11:38:22.272688','testuser6','testuser6#1234',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-16  2:02:39
