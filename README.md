# MyPay 시스템

### 프로젝트 개요
- 객체 지향 사실과 오해를 읽고 객체지향 프로그래밍을 연습하기 위하여 프로젝트를 시작 했습니다.
- 동시성 이슈 해결에 대한 이해를 높이고 싶어서 간편송금 기능을 골랐습니다.
- 실제로 충전 및 송금 기능을 구현 해 보고 싶었지만 핀테크 사업자가 없어서 데이터 흐름만 구현하기로 했습니다.
- 개인적인 호기심으로 Jenkins/sonarqube/Ansible/kubernetes를 활용 해 보고 싶어서 CI/CD 파이프라인을 구축하기로 했습니다.

### 도메인 모델링
![image](https://user-images.githubusercontent.com/126523988/231641050-3d402f7d-f075-4a84-bb95-6126782cdc1c.png)

### [사용자 시나리오](https://github.com/jungmini0601/pay/wiki)
#### [회원, 친구 시나리오](https://github.com/jungmini0601/pay/wiki/%ED%9A%8C%EC%9B%90-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230802866-5aa2c84e-5348-48b5-ba9a-1d3f8f948381.png)

![image](https://user-images.githubusercontent.com/126523988/230802933-4977c85d-5d3f-499b-9d62-fc6757e5e762.png)

#### [계좌, 거래 시나리오](https://github.com/jungmini0601/pay/wiki/%EA%B3%84%EC%A2%8C-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230803093-47e7eb86-6c68-44d6-bdf6-ea9517b92726.png)

### 프로젝트 결과 (계속 업데이트 예정)
- 객체지향적으로 잘 작성되었는지 확인하기 위하여 당근마켓 주니어 엔지니어님, 카카오페이 테크리드 엔지니어님에게 코드리뷰 요청
  - MVC 아키텍처의 한계점에 대해 전수 받았습니다 -> 현재 구조에서는 domain의 변경이 발생할 경우 변경의 여파가 너무 퍼지는 단점이 존재합니다. 
  - 클린 아키텍처라는 서적을 추천 받았습니다.
- [테스트 커버리지 라인97% 브렌치 93% 달성](https://github.com/jungmini0601/pay/pull/50)
- Jenkins, Ansible, Kubernates, SonarQube를 이용한 CI/CD 파이프라인 구축 (배포 자동화 파이프라인 및 아키텍처 그림 추가 예정)
- [동시성 이슈 해결 PR](https://github.com/jungmini0601/pay/pull/56/commits/78f0758947e5da74739a1b555bc70f7fab071d17)
- [동시성 이슈 해결을 위한 학습](https://jungmini-laboratory.tistory.com/35)

### 추후 발전시킬 만한 내용
- RSA 암호화나 SSL 적용
- 예약 송금을 위한 메세지큐 적용(kafka)
- DB 암호화
- DB 샤딩
- 요구사항을 조금 더 복잡하게 한 후 언어 kotlin으로 전환
- MSA 아키텍처 설계후 적용 해보기
- 클린 아키텍처 적용
