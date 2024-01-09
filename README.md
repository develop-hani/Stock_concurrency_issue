# 동시성 이슈 해결하기

인프런에서 [재고 시스템으로 알아보는 동시성 이슈 해결 방법](https://www.inflearn.com/course/%EB%8F%99%EC%8B%9C%EC%84%B1%EC%9D%B4%EC%8A%88-%EC%9E%AC%EA%B3%A0%EC%8B%9C%EC%8A%A4%ED%85%9C/dashboard) 강의를 들으며 정리한 내용이다. <br/>

문제 발생 상황은 master 브랜치에, 각각의 해결 과정은 새로운 브랜치에 정리하였다.
- [동시성 이슈 발생](https://github.com/develop-hani/Stock_concurrency_issue/tree/master)
- [Java의 synchronized로 해결](https://github.com/develop-hani/Stock_concurrency_issue/tree/synchronized)
- [Database의 Lock으로 해결](https://github.com/develop-hani/Stock_concurrency_issue/tree/database)

## ♾️ 해결 방법 2: Database

### 📋 다양한 Lock의 예시
1. **Pessimistic lock** </br>
    Exclusive lock을 걸리면 다른 트랜잭션에서는 lock이 해제되기 전까지 **데이터를 가져갈 수 없다**. </br>
    **데드락이 걸리는 상황에 주의**해서 사용해야한다. 
2. **Optimistic lock** </br>
   **버전을 통해 정합성**을 맞추는 방법이다. </br>
   데이터를 읽은 후에 update 를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하며 업데이트를 한다.</br>
   읽은 버전에서 수정사항이 생겼을 경우에는 application에서 다시 읽은 후에 작업을 수행해야 한다.
3. **Named lock** </br>
   이름을 가진 metadata locking이다. </br>
   **이름을 가진 lock 을 획득**한 후 해제할때까지 다른 세션은 이 lock 을 획득할 수 없다. </br> 
   transaction 이 종료될 때 lock 이 자동으로 해제되지 않아 별도의 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제된다.
</br>
table이나 row단위로 lock을 거는 Pessimistic lock과 달리 Named lock은 메타 데이터에 lock을 건다.


### 😒 Pesimistic lock 활용
Pessimistic lock을 적용한 코드는 [이전 커밋](https://github.com/develop-hani/Stock_concurrency_issue/tree/8da6ce7917b0d3d160c7ceb972382061a2cd87ca)에서 볼 수 있다.
1. Pessimistic lock 적용</br>
Spring data jpa에서는 **`@Lock`을 통해 손쉽게 pessimistic lock을 구현**할 수 있다.
```java
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithPessimisticLock(Long id);
}
```

2. Test code 실행 </br>
   Test를 성공적으로 실행되는 것을 볼 수 있는데, 실행 중 `for update`라는 부분이 **lock을 걸고 데이터를 가져오는 부분**이다.
   ![Pessimistic lock 적용](./image/pessimistic%20lock.jpg)

#### Pessimistic lock의 장점
- 충돌이 빈번한 경우 Optimistic lock 보다 성능이 좋을 수 있다.
- lock을 통해 업데이트를 제어하므로 데이터의 정합성이 보장된다.

#### Pessimistic lock의 단점
- 별도로 lock을 걸어야 하므로 성능 감소가 있을 수 있다.

### 🌞 Optimistic lock 활용
Optimistic lock은 실제로 lock을 이용하지 않고 **버전을 이용**하여 데이터의 정합성을 맞추는 방법이다.</br>
Optimistic lock을 적용한 커밋은 [이전 커밋](https://github.com/develop-hani/Stock_concurrency_issue/tree/010df79d6ca0b65c71d2ae5a9f3645462721b65e)에서 볼 수 있으며 적용 과정은 아래와 같다.
</br>

1. Optimistic Lock을 활용하기 위해 Stock Entity에 **version이라는 attribute을 추가**한다. </br>
    이때 jakarta.persistence package에서 제공하는 annotation을 사용한다.
    ```java
    @Version
    private Long version;
    ```
 
2. Spring Data JPA에서 제공하는 **`@lock`을 통해 Optimistic Lock을 구현**한다.
    ```java
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);
    ```

3. Optimistic lock은 **실패했을 때 재시도를 해야하므로 facade를 만들어** 그곳에서 service layer의 함수를 호출한다.
    ```java
    while (true) {
        try {
            optimisticLockStockService.decrease(id, quantity);
            break;
        } catch (Exception e) {
            Thread.sleep(50);
        }
    }
    ```
#### Optimistic Lock의 장점
- 별도의 lock을 잡지 않으므로 pessimistic lock보다 성능이 우수하다.

#### Optimistic Lock의 단점
- update에 실패했을 때의 재시도 로직을 개발자가 직접 작성해야한다.

### ⚖️ Pessimistic Lock vs. Optimistic Lock
따라서 충돌의 발생 빈도에 따라 Lock을 다르게 사용하는 것을 추천하다.
- 충돌이 빈번하게 일어날 경우 => Pessimistic Lock
- 충돌이 적을 경우 => Optimistic Lock

### 🙋🏻 Named lock 활용
