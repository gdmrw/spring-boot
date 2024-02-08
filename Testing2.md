# Finite State machine
Finite models are essential in testing for simplifying complex systems, allowing exhaustive testing, and facilitating automated test case generation. They offer a clear way to represent systems as finite state machines, making it possible to explore all possible states and transitions. This approach is critical for ensuring systems behave as expected under every scenario, especially in critical applications where failure is unacceptable.

Key benefits include:

- Simplification: Reducing complex systems to manageable models.
- Exhaustive Testing: Enabling complete coverage of all possible states and transitions.
- Automated Testing: Supporting the generation of test cases and regression testing.
- Formal Verification: Allowing mathematical proofs of system correctness through model checking and other formal methods.

Finite models are widely used in software and hardware testing to improve test coverage, find potential issues, and ensure system reliability. However, challenges such as scalability and the accuracy of the models must be managed to ensure effective testing outcomes.

## Srping boot actuator
Spring boot actuator is one of the most important component from spring boot.provides a set of ready-to-use features to help you monitor and manage your application. These features allow you to inspect the internal state of your application, such as health status, metrics, environment information, and more. This is especially useful for microservices architectures in production environments.

There are some endpoints from actuator:

- `/actuator`: The root endpoint that lists all available actuator endpoints.
- `/health`: Summarizes the health status of your application.
- `/heapdump`: Triggers a dump of the Java heap.
- `/httptrace`: Shows HTTP trace information (by default, the last 100 HTTP request-response exchanges).
- `/info`: Displays arbitrary application info.
- `/loggers`: Enables viewing and modifying the logging level of application loggers.
- `/metrics`: Shows ‘metrics’ information for the current application.
- `/shutdown`: Lets you gracefully shut down the application (disabled by default).
- `/env`: Exposes properties from Spring’s ConfigurableEnvironment.


This is the function what actuator offers; I won’t explain the detailed functions here. The spring boot basic policy is: **All the function except /health will be treated as sensitive data which will not open to the public until the auth passed.**

---

The finite state machine without verification will be look like this:

<div style="text-align: center;">
<img src="/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/not auth diagram.drawio.svg"/>
</div>

All the actuator function except health will be received a http 302 redirect response. The health will be received http ok and show the data query. 

Let's see how the finite state machine change when verification completed.

<div style="text-align: center;">
<img src="/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/Actuator diagram_authorized.drawio.svg"/>
</div>

Now, the server will make more response, not just including all the actuator services, but also the exception handling. like `/foo` will response http500. http 403 will pop up when make a GET request in prohibited port when CORS enable. 

Below shows a bunch of screenshots about how it actually reacts: 

> first the login page
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/截屏2024-02-08 上午5.34.28.png)

> try to enter actuator/env and excute, page automatically fall back to login page
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/截屏2024-02-08 上午5.34.47.png)

> try use `actuator/health` get the new return 
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/截屏2024-02-08 上午5.35.18.png)

> `/actuator/env` now functional after entering correct username and password
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/截屏2024-02-08 上午5.40.03.png)

> try exception page, get http 500
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/截屏2024-02-08 上午5.45.32.png)

>try to get some page not existed, get http 404
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/截屏2024-02-08 上午5.45.46.png)

> login fail can also be a simple state machine, but it's code implementation based on spring security, hence not within the scope of this discussion
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/截屏2024-02-08 上午5.37.40.png)

>All new test cases append to `SampleActuatorApplicationTests`

