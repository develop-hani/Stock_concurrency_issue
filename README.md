# 동시성 이슈 해결하기

인프런에서 [재고 시스템으로 알아보는 동시성 이슈 해결 방법](https://www.inflearn.com/course/%EB%8F%99%EC%8B%9C%EC%84%B1%EC%9D%B4%EC%8A%88-%EC%9E%AC%EA%B3%A0%EC%8B%9C%EC%8A%A4%ED%85%9C/dashboard) 강의를 들으며 정리한 내용이다. <br/>

문제 발생 상황은 master 브랜치에, 각각의 해결 과정은 새로운 브랜치에 정리하였다.
- [동시성 이슈 발생](https://github.com/develop-hani/Stock_concurrency_issue/tree/master)
- [Java의 synchronized로 해결](https://github.com/develop-hani/Stock_concurrency_issue/tree/synchronized)
- [Database의 Lock으로 해결](https://github.com/develop-hani/Stock_concurrency_issue/tree/database)
  - [Pessimistic Lock 적용](https://github.com/develop-hani/Stock_concurrency_issue/tree/8da6ce7917b0d3d160c7ceb972382061a2cd87ca)
  - [Optimistic Lock 적용](https://github.com/develop-hani/Stock_concurrency_issue/tree/02032b206d009104a6646ee3332be401a82cf25a)
  - [Named Lock 적용](https://github.com/develop-hani/Stock_concurrency_issue/tree/20ddb2299a027f10b6a547aa193e8355ee62ef01)
- [Redis를 이용하여 해결](https://github.com/develop-hani/Stock_concurrency_issue/tree/redis)
  - [Lettuce 적용](https://github.com/develop-hani/Stock_concurrency_issue/tree/3777ac780c0233acbc5c5952d8a43951fe908054)

## 🤝 동시성 이슈(Concurrency Issue)란?

**하나의 데이터**를 **둘 이상의 thread나 session**이 제어할 때 발생하는 문제이다. <br/>

/)/) (\(\ <br/>
( . .) (. . ) <br/>
( づ🍫⊂ ) <br/>

### 예시 코드
Test code는 [이곳](https://github.com/develop-hani/Stock_concurrency_issue/blob/master/src/test/java/com/practice/stock/service/StockServiceTest.java)에서 확인할 수 있다. <br/>
Thread를 이용하여 동시에 100개의 요청을 보낸다.  <br/>
- **ExecutorService**는 비동기로 실행하는 작업을 단순화하여 사용할 수 있도록 도와주는 Java API이다.
- **CountDownLatch**는 다른 스레드가 수행하는 작업이 끝날 때까지 기다릴 수 있는 기능을 제공한다.

```java
@Test
public void 동시에_100개_요청() throws InterruptedException {
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; ++i) {
        executorService.submit( () -> {
            try {
                stockService.decreaseStock(1L, 1L);
            } finally {
                latch.countDown();
            }
        });
    }
    latch.await();

    Stock stock = stockRepository.findById(1L).orElseThrow();
        
    assertEquals(0L, stock.getQuantity());
}
```

이 경우  test에 실패한다. (expected: <0> but was: <94>) <br/>
**Race condition**이 발생했기 때문이다.  <br/>
Race conditiond은 둘 이상의 Thread에 공유 자원 동시에 접근할 때 발생하는 문제이다.  <br/>

**기대했던 순서**는 Thread1 재고 확인 -> Thread1 재고 감소 -> Thread2 재고 확인 -> Thread2 재고 감소 -> ... 이지만 <br/>
**실제 실행 순서**는 Thread1 재고 확인 -> Thread2 재고 확인 -> Thread1 재고 감소 -> Thread2 재고 감소 -> ... 이므로 문제가 발생한다. <br/>
이를 해결하기 위해 **하나의 스레드 작업이 완료된 이후에 다른 스레드 작업**을 하도록한다.

## ❓ 강의 중 궁금했던 내용
### save()가 아닌 saveAndFlush()를 사용하는 이유