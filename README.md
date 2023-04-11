# MyPay 시스템

### 프로젝트 개요
- 페이 시스템은 간편 송금 기능을 어떻게 구현할지 궁금하여 시작 했습니다.
- 검색 결과 송금과 계좌를 연동하는 기능은 핀테크 사업자가 필요하여 단순 API 형태로 구현하기로 하였습니다.

### 프로젝트 달성 성과 (계속 업데이트 예정)
- [테스트 커버리지 라인96% 브렌치 86% 달성](https://github.com/jungmini0601/pay/pull/50)
- Jenkins, Ansible, Kubernates, SonarQube를 이용한 CI/CD 파이프라인 구축 (배포 자동화 파이프라인 및 아키텍처 그림 추가 예정)
- 인덱스 설계를 통한 성능향상(성능 분석표 추가 예정)
- 서버 다중화를 통한 성능향상(성능 분석표 추가 예정)
- 동시성 이슈 해결(PR 링크 추가 예정)

### [사용자 시나리오](https://github.com/jungmini0601/pay/wiki)
#### [회원, 친구 시나리오](https://github.com/jungmini0601/pay/wiki/%ED%9A%8C%EC%9B%90-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230802866-5aa2c84e-5348-48b5-ba9a-1d3f8f948381.png)

![image](https://user-images.githubusercontent.com/126523988/230802933-4977c85d-5d3f-499b-9d62-fc6757e5e762.png)

#### [계좌, 거래 시나리오](https://github.com/jungmini0601/pay/wiki/%EA%B3%84%EC%A2%8C-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4)
![image](https://user-images.githubusercontent.com/126523988/230803093-47e7eb86-6c68-44d6-bdf6-ea9517b92726.png)


### 도메인 모델링
![image](https://user-images.githubusercontent.com/126523988/230804623-d2a8acdd-fc48-4f6c-85df-d49a69b631a1.png)

### 추후 발전시킬 만한 내용
- RSA 암호화나 SSL 적용
- 예약 송금을 위한 메세지큐 적용
- DB 암호화
- DB 샤딩, 복제
- 요구사항을 조금 더 복잡하게 한 후 언어 kotlin으로 전환
- MSA 아키텍처 설계후 적용 해보기
