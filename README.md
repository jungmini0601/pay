## 프로젝트 개요
- 객체 지향 사실과 오해를 읽고 객체지향 프로그래밍을 연습하기 위하여 프로젝트를 시작 했습니다.
- 동시성 이슈 해결에 대한 이해를 높이고 싶어서 간편송금 기능을 골랐습니다.

### 도메인 모델링
![image](https://user-images.githubusercontent.com/126523988/231641050-3d402f7d-f075-4a84-bb95-6126782cdc1c.png)

### [사용자 시나리오](https://github.com/jungmini0601/pay/wiki)
#### [회원, 친구 시나리오](https://github.com/jungmini0601/pay/wiki/%ED%9A%8C%EC%9B%90-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230802866-5aa2c84e-5348-48b5-ba9a-1d3f8f948381.png)

![image](https://user-images.githubusercontent.com/126523988/230802933-4977c85d-5d3f-499b-9d62-fc6757e5e762.png)

#### [계좌, 거래 시나리오](https://github.com/jungmini0601/pay/wiki/%EA%B3%84%EC%A2%8C-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230803093-47e7eb86-6c68-44d6-bdf6-ea9517b92726.png)

## 주요 이슈 사항
- [송금 및 충전 기능에서 Serializable 격리 수준을 어떻게 구현 할 것인지?](https://jungmini-laboratory.tistory.com/56)
- [비즈니스 로직은 Service에서 가져가야 하는지 domain에서 가져가야 하는지?](https://github.com/jungmini0601/pay/issues/26)

### 사용한 전략
- [브렌치 관리 전략](https://jungmini-laboratory.tistory.com/26)
- [단위 테스트 통합 테스트 블랙 박스 테스팅을 이용한 테스트 전략](https://jungmini-laboratory.tistory.com/28)

### 프로젝트 종료
- [테스트 커버리지 라인97% 브렌치 93%](https://github.com/jungmini0601/pay/pull/50)
- [이슈](https://github.com/jungmini0601/pay/issues?q=is%3Aissue+is%3Aclosed)
- [PR](https://github.com/jungmini0601/pay/pulls?q=is%3Apr+is%3Aclosed)
