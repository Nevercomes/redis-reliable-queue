# Redis Reliable Queue

## Features
> Please note: In order to support generic task types, 
> JdkSerializationRedisSerializer was used 
> instead of Jackson2JsonRedisSerializer on the Redis value serializer.

- Redis Reliable Queue:
  - Utilizes two queues: the task queue and the processing queue.
  - Includes wrapped acknowledgement (ack) and non-acknowledgement (nack) methods.
  - Features processing task timeout checks (Note: your application must support multiple instances).
  - Provides fast expiration for completed tasks.
  - Supports customized task objects.
  - Offers the capability to save task metadata independently.
- Spring Background Task Using Redis:
  - Supports the expansion of functionality through custom tasks.
