# Static Analyzers
Static analysis tools are essential in software development, focusing on improving code quality, identifying bugs, and ensuring adherence to coding standards without executing the code. They serve multiple purposes:

1. Code Quality: Enhance code readability, maintainability, and efficiency.
2. Bug Detection: Identify and fix errors early in the development process.
3. Security: Detect potential security vulnerabilities.
4. Performance: Highlight inefficient code patterns for optimization.
These tools are used throughout the development lifecycle, particularly in:

1. Development: Integrated into IDEs, they provide real-time feedback to developers.
2. Code Review: Automate the detection of potential issues before peer review.
3. Continuous Integration: Included in CI pipelines to enforce code quality standards.
4. Compliance: Ensure code adheres to industry-specific standards.
> Tools like Checkstyle, PMD, and SonarQube are popular choices, each offering unique features to aid developers in creating robust, efficient, and secure Java applications.

---

# Spring boot: CheckStyle and PMD

Both CheckStyle and PMD are easy to install in IDEA plugin. You can also integrate it inside the dependency through Gradle, I try to add the Spotbugs into Gradle at first, but it does not function and cause horrible issue. Then I give up and turn to CheckStyle and PMD.

Due to the limitation of the plugin, We use `SampleActuatorApplication.java` in actuator smoke as a single control group for testing. Here is the result:

![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/img/CheckStyle.png)

I don't know why it automatically generates the chinese output, it did convent for me but also make more work for me. 
You can see in the rules section on the left right corner we use `google checks`, we have two default configurations, one is call `Sun checks`, I choose google checks since sun checks generate a more horrible result than Google, but a Google result is not very good either. I'll talk about it later.

Let's take a look at how PMD performance. 
![CheckStyle](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/img/PMD Count.png)

PMD plugin supports multi file scan, and I ask it to check some test system. And only 10 files generate 67 violations. Its shocking result, but let's see what really happen. 

![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/img/CheckStyle.png)

Let's go back to the Check style: On the `highlight text` it said: 'method def modifier' indent 8 and should be 2. The above line says： there is tab in line. Both problem happen in line 36, lets see what line 36 do. 

![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/img/CheckStyle Prompt.png)

This is line 36 in 
`SampleActuatorApplication` class, we can see this is a @Bean annotation, which is a spring boot characteristic. It's clear that the default Checkstyle configuration cannot identify the annotation and treat this as an unexpected indentation.
Let's see how PMP did.

![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/img/PMP detailed.png)

Similarly, PMP doesn't perform very well in default, in line 34, it said it should declare at least one constructor, but actually, spring boot will automatically offer a default constructor when there is no explicit constructor exist.

Hence, both static analyzers may not function well without specific configuration. And nowadays, we did have a very powerful static analyzer call IDEA. 

### Check Overlap
Some functions both static analyzers provided are similar, for example, the code style. In PMD, its located in the `documentation section` and `comment size`:
![](/Users/wjx/Documents/winter quarter/SWE261P/sping-boot-presentation/img/PMD documentaion.png)

The document section is ask you to add some comments on the class and constructor, and the comment size will inform you when the comment is too large. I do not see the overlap appear. In summary, the checkStyle is focusing on the formatting only, but the PMD  is mainly used to find potential problems in the code, such as complex code that may cause errors, unused parameters or variables, overly complex expressions, etc. It focuses more on the logical and structural quality of the code. However, without targeted configuration, both plugins are far from the level that can be deployed in a production environment.




