# Apache Kafka TOPIC and Spring Integration
- Dinesh Metkari (dineshmetkari@gmail.com)

mvn clean compile package install spring-boot:run

## Installing Apache Kafka
There are many different ways to get Apache Kafka installed. If you're on OSX, and you're using Homebrew, it can be as simple as `brew install kafka`. You can also [download the latest distribution from Apache](http://kafka.apache.org/downloads.html). I downloaded `kafka_2.10-0.8.2.1.tgz`, unzipped it, and then within you'll find there's a distribution of [Apache Zookeeper](https://zookeeper.apache.org/) as well as Kafka, so nothing else is required. I installed Apache Kafka in my `$HOME` directory, under another directory, `bin`, then I created an environment variable, `KAFKA_HOME`, that points to `$HOME/bin/kafka`.

Start Apache Zookeeper first, specifying where the configuration properties file it requires is:

```
$KAFKA_HOME/bin/zookeeper-server-start.sh  $KAFKA_HOME/config/zookeeper.properties

```  

The Apache Kafka distribution comes with default configuration files for both Zookeeper and Kafka, which makes getting started easy. You will in more advanced use cases need to customize these files.

Then start Apache Kafka. It too requires a configuration file, like this:

```
$KAFKA_HOME/bin/kafka-server-start.sh  $KAFKA_HOME/config/server.properties
```

The `server.properties` file  contains, among other things, default values for where to connect to Apache Zookeeper (`zookeeper.connect`), how much data should be sent across sockets, how many partitions there are by default, and the broker ID (`broker.id` - which must be unique across a cluster).

There are other scripts in the same directory that can be used to send and receive dummy data, very handy in establishing that everything's up and running!

Now that Apache Kafka is up and running, let's look at  working with Apache Kafka from our application.

## Some High Level Concepts..

A Kafka _broker_ cluster consists of one or more servers where each may have one or more broker processes running. Apache Kafka is designed to be highly available; there are no _master_ nodes. All nodes are interchangeable. Data is replicated from one node to another to ensure that it is still available in the event of a failure.

In Kafka, a _topic_ is a category, similar to a JMS destination or both an AMQP exchange and queue. Topics are partitioned, and the choice of which of a topic's partition a message should be sent to is made by the message producer. Each message in the partition is assigned a unique sequenced ID, its  _offset_. More partitions allow greater parallelism for consumption, but this will also result in more files across the brokers.


_Producers_ send messages to Apache Kafka broker topics and specify the partition to use for every message they produce. Message production may be synchronous or asynchronous. Producers also specify what sort of replication guarantees they want.

_Consumers_ listen for messages on topics and process the feed of published messages. As you'd expect if you've used other messaging systems, this is usually (and usefully!) asynchronous.

Like [Spring XD](http://spring.io/projects/spring-xd) and numerous other distributed system, Apache Kafka uses Apache Zookeeper to coordinate cluster information. Apache Zookeeper provides a shared hierarchical namespace (called _znodes_) that nodes can share to understand cluster topology and availability (yet another reason that [Spring Cloud](https://github.com/spring-cloud/spring-cloud-zookeeper) has forthcoming support for it..).

Zookeeper is very present in your interactions with Apache Kafka. Apache Kafka has, for example, two different APIs for acting as a consumer. The higher level API is simpler to get started with and it handles all the nuances of handling partitioning and so on. It will need a reference to a Zookeeper instance to keep the coordination state.  

Let's turn now turn to using Apache Kafka with Spring.

## Using Apache Kafka with Spring Integration
The recently released [Apache Kafka 1.1 Spring Integration adapter]() is very powerful, and provides inbound adapters for working with both the lower level Apache Kafka API as well as the higher level API.

The adapter, currently, is XML-configuration first, though work is already underway on a Spring Integration Java configuration DSL for the adapter and milestones are available. We'll look at both here, now.

To make all these examples work, I added the [libs-milestone-local Maven  repository](http://repo.spring.io/simple/libs-milestone-local) and used the following dependencies:

- org.apache.kafka:kafka_2.10:0.8.1.1
- org.springframework.boot:spring-boot-starter-integration:1.2.3.RELEASE
- org.springframework.boot:spring-boot-starter:1.2.3.RELEASE
- org.springframework.integration:spring-integration-kafka:1.1.1.RELEASE
- org.springframework.integration:spring-integration-java-dsl:1.1.0.M1

### Using the Spring Integration Apache Kafka with the Spring Integration XML DSL

First, let's look at how to use the Spring Integration outbound adapter to send `Message<T>` instances from a Spring Integration flow to an external Apache Kafka instance. The example is fairly  straightforward: a Spring Integration `channel` named `inputToKafka` acts as a conduit that forwards `Message<T>` messages to the outbound adapter, `kafkaOutboundChannelAdapter`. The adapter itself can take its configuration from the defaults specified in the `kafka:producer-context` element or it from the adapter-local configuration overrides. There may be one or many configurations in a given `kafka:producer-context` element.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:int="http://www.springframework.org/schema/integration"
       xmlns:int-kafka="http://www.springframework.org/schema/integration/kafka"
       xmlns:task="http://www.springframework.org/schema/task"
       xsi:schemaLocation="http://www.springframework.org/schema/integration/kafka http://www.springframework.org/schema/integration/kafka/spring-integration-kafka.xsd
		http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task.xsd">

    <int:channel id="inputToKafka">
        <int:queue/>
    </int:channel>

    <int-kafka:outbound-channel-adapter
            id="kafkaOutboundChannelAdapter"
            kafka-producer-context-ref="kafkaProducerContext"
            channel="inputToKafka">
        <int:poller fixed-delay="1000" time-unit="MILLISECONDS" receive-timeout="0" task-executor="taskExecutor"/>
    </int-kafka:outbound-channel-adapter>

    <task:executor id="taskExecutor" pool-size="5" keep-alive="120" queue-capacity="500"/>

    <int-kafka:producer-context id="kafkaProducerContext">
        <int-kafka:producer-configurations>
            <int-kafka:producer-configuration broker-list="localhost:9092"
                                              topic="event-stream"
                                              compression-codec="default"/>
        </int-kafka:producer-configurations>
    </int-kafka:producer-context>

</beans>


