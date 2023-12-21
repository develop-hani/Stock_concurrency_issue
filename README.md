# 동시성 이슈 해결하기

인프런에서 [재고 시스템으로 알아보는 동시성 이슈 해결 방법](https://www.inflearn.com/course/%EB%8F%99%EC%8B%9C%EC%84%B1%EC%9D%B4%EC%8A%88-%EC%9E%AC%EA%B3%A0%EC%8B%9C%EC%8A%A4%ED%85%9C/dashboard) 강의를 들으며 정리한 내용이다. <br/>

문제 발생 상황은 master 브랜치에, 각각의 해결 과정은 새로운 브랜치에 정리하였다.
- [동시성 이슈 발생](https://github.com/develop-hani/Stock_concurrency_issue/tree/master)
- [Java의 synchronized로 해결](https://github.com/develop-hani/Stock_concurrency_issue/tree/synchronized)

## ♾️ 해결 방법1: Synchronized
Java의 synchronized를 메서드 선언부에 붙여 **해당 메소드에 하나의 thread**만 접근할 수 있도록한다. <br/>

### 예시 코드
아래 경우에서 `@Transactional`을 함께 사용한다면 해당 annotation의 동작 방식 때문에 의도한 대로 동작하지 않는다. <br/>
<br/>
Spring에서 `@Transactional`이 붙은 메소드를 실행하면 해당 클래스를 매핑한 클래스를 생성하여 실행한다. <br/>
트랜잭션 시작 -> method 실행 -> 트랜잭션 종료 순서로 진행이 되는데, **트랜잭션이 종료될 때 db에 데이터가 업데이트**된다. <br/>
method는 실행이 완료 되었지만 db에 업데이트가 되기 전에 다른 thread가 해당 메서드에 접근할 때 오류가 발생한다.

```java
// 재고 감소
// @Transactional
public synchronized void decreaseStock(Long id, Long quantity) {
    // Stock 조회
    Stock stock = stockRepository.findById(id).orElseThrow();
    stock.decrease(quantity);
    stockRepository.saveAndFlush(stock);
}
```

### synchronized 사용 시 문제점
Java의 synchronized는 **하나의 프로세스 안에서만 보장**된다. <br/>
<br/>
즉 **여러 서버에서 동시에 데이터에 접근할 때** 동시성 문제가 다시 발생할 수 있다.
실제 서비스에서는 여러 대의 서버를 사용하므로 해당 방법은 잘 사용하지 않는다.
