# MyPay 시스템

### 프로젝트 개요
- 객체 지향 사실과 오해를 읽고 객체지향 프로그래밍을 연습하기 위하여 프로젝트를 시작 했습니다.
- 동시성 이슈 해결에 대한 이해를 높이고 싶어서 간편송금 기능을 골랐습니다.
- 실제로 충전 및 송금 기능을 구현 해 보고 싶었지만 핀테크 사업자가 없어서 데이터 흐름만 구현하기로 했습니다.

### 도메인 모델링
![image](https://user-images.githubusercontent.com/126523988/231641050-3d402f7d-f075-4a84-bb95-6126782cdc1c.png)

### [사용자 시나리오](https://github.com/jungmini0601/pay/wiki)
#### [회원, 친구 시나리오](https://github.com/jungmini0601/pay/wiki/%ED%9A%8C%EC%9B%90-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230802866-5aa2c84e-5348-48b5-ba9a-1d3f8f948381.png)

![image](https://user-images.githubusercontent.com/126523988/230802933-4977c85d-5d3f-499b-9d62-fc6757e5e762.png)

#### [계좌, 거래 시나리오](https://github.com/jungmini0601/pay/wiki/%EA%B3%84%EC%A2%8C-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230803093-47e7eb86-6c68-44d6-bdf6-ea9517b92726.png)

## 주요 이슈 사항
- [송금 및 충전 기능에서 Serializable 격리 수준을 어떻게 구현 할 것인지?](https://jungmini-laboratory.tistory.com/35)
- [비즈니스 로직은 Service에서 가져가야 하는지 domain에서 가져가야 하는지?](https://github.com/jungmini0601/pay/issues/26)
- 이번 프로젝트에서 객체지향 설계가 제대로 되었는지?(코드 리뷰 내용 블로그 추가 예정)

### 사용한 전략
- [브렌치 관리 전략](https://jungmini-laboratory.tistory.com/26)
- [단위 테스트 통합 테스트 블랙 박스 테스팅을 이용한 테스트 전략](https://jungmini-laboratory.tistory.com/28)
- [부하 테스트 전략 (시간상의 이유로 진행 X)](https://jungmini-laboratory.tistory.com/32)

### 프로젝트 결과
- [테스트 커버리지 라인97% 브렌치 93% 달성](https://github.com/jungmini0601/pay/pull/50)

### 추후 발전시킬 만한 내용
- RSA 암호화나 SSL 적용
- 예약 송금을 위한 메세지큐 적용(kafka)
- DB 암호화
- DB 샤딩, 복제
- 요구사항을 조금 더 복잡하게 한 후 언어 kotlin으로 전환
- MSA 아키텍처 설계후 적용 해보기
- DDD/핵사고날 아키텍처 CQRS등 적용
- ansible/k8s/jenkins/sonarqube 인프라 적용 해보기
