This project demonstrates broken bidirectional communication between services via websocket.

It is a bit verbose but the key part demonstrating the issue is small.

The setup is as following:

- The user creates a job package using API, for example `curl http://localhost:63552/schedule/10`
- The manager forwards the request to the scheduler
- The scheduler persists the request because it takes some time to allocate resources. After resources are allocated, it persist corresponding event
- The manager is subscribed to the event and it forwards it to the executor
- The executor might regroup jobs withing the batch, because of that executor and manager use bidirect async communication
- The manager defines a flow which leaps JobRequests via the executor and is supposed to get results back as they are ready
- The process works with locally defined flow
- The process does not work if the same flow used as a service - the results are never coming back

Two approaches, local and remote can be found in ManagerServiceImpl, lines 51 and 52;

https://github.com/slavaschmidt/lagom-test/blob/master/manager-impl/src/main/scala/concept/ManagerServiceImpl.scala#L51
https://github.com/slavaschmidt/lagom-test/blob/master/manager-impl/src/main/scala/concept/ManagerServiceImpl.scala#L52


Changing the behaviour can be done in line 44

https://github.com/slavaschmidt/lagom-test/blob/master/manager-impl/src/main/scala/concept/ManagerServiceImpl.scala#L44



Output with executor flow:


21:14:21.777 [info] Manager [] - Processing : 10

21:14:35.874 [info] Executor [] - Executing: Source(SourceShape(Map.out(2100995652)))

Output with local flow:


21:13:07.306 [info] Manager [] - Processing : 10

21:13:20.912 [info] Manager [] - Got batch of jobs done, now there are 10 available

