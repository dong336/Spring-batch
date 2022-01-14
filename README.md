# Memo
스케일링이 없는 기본 배치임  
JobParameter 가 존재할 때, 동일한 Job에 대한 또다른 JobInstance를 생성해서 실행할 수 있음  
없을 경우 restartable 옵션이 없으면 1번만 실행됌  
(JobParameter + Job => JobInstance (실제로 동작할 인스턴스))


<br></br><br></br><br></br><br></br>
# Spring-batch
* 스프링 공식 docs : https://docs.spring.io/spring-batch/docs/current/reference/html/
* 소스 샘플 참고 : https://deeplify.dev/back-end/spring/batch-tutorial

<br></br><br></br><br></br><br></br>
# 일반적인 배치 처리 가이드라인
<pre>
* 데이터 처리와 저장을 물리적으로 가깝게 유지합니다
* 시스템 리소스 사용(I/O 수행)을 최소화하고 내부 메모리에서 가능한 한 작업을 수행합니다
* 동일한 데이터에 대한 배치 처리를 중복해서 실행하지 않습니다
* 재할당을 방지하기 위해 배치 애플리케이션 시작 시 충분한 메모리를 할당합니다
* 데이터 무결성을 유지하기 위해 적절한 검사, 유효성 검사를 포함합니다
* 실제 데이터 용량을 사용하는 production 환경과 유사한 환경에서 스트레스 테스트를 계획합니다
* 데이터베이스, 파일 백업이 필요한지 고려합니다
</pre>

<br></br>
## 애플리케이션의 I/O를 검토하여 불필요한 물리적 I/O(SQL문)를 방지합니다
-	모든 트랜잭션에 대한 데이터를 단 한번 읽고, 캐시하거나 보관하도록 합니다 
(동일한 목적의 트랜잭션을 반복하지 않습니다)
-	불필요한 테이블 또는 인덱스 스캔을 하지 않습니다
-	SQL문의 WHERE절에 반드시 키 값을 지정합니다

<br></br><br></br><br></br><br></br>
# 배치 처리 전략

<br></br>
## 배치 스테레오 타입
<p>
각 Job에 일대다 Step이 있으며 각 Step에는 정확히 하나의 ItemReader, ItemProcessor, ItemWriter가 있습니다. JobLauncher로 Job을 실행시켜야 하고, 현재 실행 중인 프로레스에 대한 메타데이터를 JobRepository에 저장해야 합니다.
</p>
  
<br></br>
## Job
Job은 전체적인 일괄 처리를 캡슐화하는 단위입니다. XML 또는 자바 베이스 설정 파일로 연결될 수 있습니다. Job은 단순히 Step 인스턴스의 컨테이너입니다. 자바 기반 설정을 사용하는 경우, 빌더 패턴으로 Job을 인스턴스화하여 실행합니다.

<br></br>
## JobInstance
각 JobInstance는 여러 번 실행할 수 있으며 특정 Job에 해당하고 JobParameter를 식별하는 JobInstancs를 주어진 시간에 실행할 수 있습니다. 비즈니스 새부로직은 ItemReader의 몫입니다. 동일한 JobInstance를 사용할 때 이전 실행의 ‘상태’(ExecutionContext)가 사용되는지 여부가 결정됩니다. 새로운 JobInstance를 사용한다는 것은 ‘처음부터 시작’을 의미하고, 기존 인스턴스를 사용한다는 것은 일반적으로 ‘중단한 곳에서 시작’을 의미합니다.

<br></br>
## JobParameters
하나의 JobInstance를 다른 것과 어떻게 구별됩니까? 정답은 JobParameters 입니다. JobParameters 객체는 배치 처리를 시작하는 데 사용되는 매개 변수 집합을 보유합니다. 따라서 다음과 같이 정의 됩니다. JobInstance = Job + JobParameters 식별. 이로써 전달되는 매개변수를 제어하여 JobInstance가 정의되는 방법을 제어합니다. 추가로 Spring은 JobInstance의 JobParameters가 없는 Job 도 허용합니다. (이 경우 Job 당 JobInstance는 하나입니다.)

<br></br>
## JobExecution
한 Job을 실행시키는 기술 컨셉입니다. Execution이 실패 또는 성공으로 끝나며 주어진 JobInstance가 성공적으로 실행되지 않으면 Execution이 완료되지 않은 것으로 간주됩니다. 

<br></br>
## 요약
Job은 작업이 무엇이고 어떻게 실행되는지 정의하고, JobInstance는 JobExecution 이 실행할 정확한 재시작에 대한 시멘틱적 설명입니다. 그러나 JobExecution은 실제로 발생할 일에 대한 기본 매커니즘입니다. 제어하고 유지해야 하는 다양한 속성이 있습니다. 이러한 속성들은 지속되고 실행 상태를 완전히 결정하는 데에 사용할 수 있습니다.

<br></br>
## Step
Step은 순차적인 단계를 캡슐화하는 도메인 단위입니다. 따라서 모든 Job은 하나 이상의 Step으로 구성됩니다. Step에는 실제 배치 과정을 정의하고 제어하는 구체적인 정보가 포함되며 개발자의 재량에 따르는 세부 비즈니스 로직을 포함합니다.

<br></br>
## StepExecution
JobExecution과 유사하게 Step이 실행될 때마다 새로운 StepExecution이 생성됩니다. 그러나 이전 Step이 실패하여 Step이 실행되지 않았으며 이후의 Step의 실행이 지속되지 않습니다. StepExecution은 실행할 Step이 실제로 시작될 때만 생성됩니다. 또한 StepExecution은 개발자가 배치 실행 중 유지해야하는 모든 데이터가 포함된 ExecutionContext가 포함되어 있습니다.

<br></br>
## ExecutionContext
StepExecution 또는 JobExecution 개체로 범위가 지정된 상태에 대한 키와 값의 쌍 집합입니다. 프레임웍은 커밋 지점에서 주기적으로 ExecutionContext를 유지합니다. 실행 중에 치명적 오류가 발생하거나 전원이 꺼지더라도 ItemReader가 상태를 저장할 수 있습니다.

<br></br>
## JobRepository
위에서 언급한 모든 스테레오 타입에 대한 지속성 메커니즘입니다. JobLauncher, Job, Step 구현을 위한 CRUD 작업을 제공합니다. Job이 처음 시작되면 JobRepository에서 JobExecution을 가져오고 실행 도중 StepExecution, JobExecution 구현이 저장소로 전달되어 유지됩니다. @EnableBatchProcessing 주석을 통해 자동으로 JobRepository를 제공합니다.

<br></br>
## JobLauncher
Job을 실행하기 위한 JobParameters 셋을 가진 간단한 인터페이스입니다.

<br></br>
## ItemReader
한 번에 한 항목씩 Step에 대한 검색을 추상화 해놓은 것 입니다. ItemReader 인터페이스에서 세부 내용을 볼 수 있습니다.

<br></br>
## ItemWriter
ItemReader와 유사하지만, 역연산을 합니다. 읽는 것이 아니라 기록합니다. 데이터베이스나 메세징큐의 삽입, 업데이트, send등의 작업을 구현합니다.

<br></br>
## ItemProcessor
비즈니스 처리의 추상화를 구현합니다. 다른 비즈니스에 대한 변환, 적용에 대한 액세스 지점입니다. 
