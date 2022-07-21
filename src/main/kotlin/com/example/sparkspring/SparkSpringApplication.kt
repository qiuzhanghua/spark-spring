package com.example.sparkspring

import com.redislabs.provider.redis.ReadWriteConfig
import com.redislabs.provider.redis.RedisConfig
import com.redislabs.provider.redis.RedisContext
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SparkSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
class SparkSpringApplication {
    private val log: Logger = LoggerFactory.getLogger(SparkSpringApplication::class.java)

    @Value("\${spark.master}")
    private lateinit var master: String

    @Value("\${spark.appName}")
    private lateinit var appName: String

    @Value("\${spark.redis}")
    private lateinit var useSparkRedis: String

    @Value("\${spring.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.redis.database}")
    private lateinit var redisDb: String

    @Bean
    fun sparkConf(): SparkConf {
        val s = SparkConf()
            .setMaster(master)
            .setAppName(appName)
        if (useSparkRedis.toBoolean()) {
            s.set("spark.redis.host", redisHost)
            s.set("spark.redis.database", redisDb)
        }
        return s
    }

    @Bean(destroyMethod = "close")
    fun jsc(): JavaSparkContext = JavaSparkContext(sparkConf())

    // https://reflectoring.io/spring-boot-conditionals/
    @Bean
    @ConditionalOnProperty(value = ["spark.redis"], havingValue = "true")
    fun redisConfig(): RedisConfig {
        return RedisConfig.fromSparkConf(sparkConf())
    }

    @Bean
    @ConditionalOnProperty(value = ["spark.redis"], havingValue = "true")
    fun redisContext(conf: SparkConf): RedisContext {
        return RedisContext(jsc().sc())
    }

    @Bean
    @ConditionalOnProperty(value = ["spark.redis"], havingValue = "true")
    fun readWriteConfig(): ReadWriteConfig {
        return ReadWriteConfig.fromSparkConf(sparkConf())
    }

    @Bean(destroyMethod = "close")
    fun spark(): SparkSession {
        return SparkSession.builder()
            .config(sparkConf())
            .orCreate
    }

    @Bean
    fun CommandLineRunner(jsc: JavaSparkContext, spark: SparkSession): CommandLineRunner = CommandLineRunner {
        log.debug(" ======= show airports data =======")
        val data: Dataset<Row> = spark.read().option("header", "true").csv("data/airports.csv")
        data.show(5, true)
        data.write()
            .format("org.apache.spark.sql.redis")
            .option("table", "airports")
            .option("key.column", "id")
            .mode(SaveMode.Overwrite)
            .save()
        log.debug(" ====== end of show airports ======")
    }
}

fun main(args: Array<String>) {
    runApplication<SparkSpringApplication>(*args)
}

//        val lines = jsc.textFile("data/word_count.text");
//        val words = lines.flatMap { line -> listOf(line.split(" ")).iterator() }
//        val wordCounts = words.countByValue()
//        for (entry in wordCounts) {
//            println("${entry.key} : ${entry.value}")
//        }
