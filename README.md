# JPQL을 이용하여 N+1 문제를 해결하고 쿼리성능 개선하기.

## 테스트 목적
- 유저와 게시물 같이 1:N 연관관계를 가지고 있는 객체들을 JPA로 조회할 때 흔히 발생하는 문제인 N + 1 문제를 해결하고 쿼리의 개수를 줄여서 성능을 최적화 하는 방법에 대해 탐구한다.

## 테스트 조건
- 1,2,3번 유저와 각각의 유저가 작성한 1,2,3번 게시물을 1:N 양방향 참조관계를 가지도록 저장한다.
- 저장된 게시물 엔티티들을 조회하여 프런트 엔드에서 원하는 데이터 형태인 ForTestResponse라는 DTO로 가공하여 리스트의 형태로 프런트 엔드로 리턴한다고 가정한다.

## JPQL 사용 시 주의사항!!
- Fetch Join에서는 Paging이 불가능하다!!
- JPA 리포지토리 클래스 메서드에서 엔티티가 아니라 DTO를 바로 리턴하게 만들어주기 위해서는 Convertor라는 인터페이스를 정의하고, JQPL의 필드 이름과 컨버터 인터페이스의 get~()메서드들의 이름을 잘 일치시켜야 한다.
- 예를 들면 아래의 이미지와 같이 작성하면 된다.

![218472E9-D8DC-4224-AB68-F7FF89C99CDC](https://user-images.githubusercontent.com/99060708/222730328-6979a6fa-7223-46c6-bbc9-47e00222c8c6.jpeg)

## 테스트 결과
### 첫 번째 상황에서는 Join 없이 엔티티를 직접 참조하여 Dto를 만드는 방법을 사용했다. 그 결과 리스트의 요소의 개수만큼의 쿼리가 추가로 발생하여 N+1 문제가 발생함을 알 수 있었다.
![09E25DF2-9F13-4B28-A7F8-F28F1C0934BD](https://user-images.githubusercontent.com/99060708/222726521-f98d2586-e59a-43e6-a4b4-dbd0fa2bf9c6.jpeg)
- 발생한 쿼리 개수 : 리스트 조회 쿼리 1회 + (유저 엔티티 조회 * 리스트 내 요소 개수 3)회
  - 내부 엔티티를 이용하여 리스트 조회
  - Hibernate:
  select
  feed0_.feed_id as feed_id1_0_,
  feed0_.created_at as created_2_0_,
  feed0_.modified_at as modified3_0_,
  feed0_.content as content4_0_,
  feed0_.image_urls as image_ur5_0_,
  feed0_.user_id as user_id6_0_
  <br>from feed feed0_
  - 테스트 피드리스펀스 빌드 시작!!
  - Hibernate:
  select
  user0_.user_id as user_id1_1_0_,
  user0_.created_at as created_2_1_0_,
  user0_.modified_at as modified3_1_0_,
  user0_.nickname as nickname4_1_0_,
  user0_.profile_image_url as profile_5_1_0_,
  user0_.signup_type as signup_t6_1_0_,
  user0_.user_status as user_sta7_1_0_,
  user0_.username as username8_1_0_
  <br>from
  user user0_
  where
  user0_.user_id=?
  - 테스트 피드리스펀스 빌드 시작!!
  - Hibernate:
  select
  user0_.user_id as user_id1_1_0_,
  user0_.created_at as created_2_1_0_,
  user0_.modified_at as modified3_1_0_,
  user0_.nickname as nickname4_1_0_,
  user0_.profile_image_url as profile_5_1_0_,
  user0_.signup_type as signup_t6_1_0_,
  user0_.user_status as user_sta7_1_0_,
  user0_.username as username8_1_0_
  <br>from user user0_
  where
  user0_.user_id=?
  - 테스트 피드리스펀스 빌드 시작!!
  - Hibernate:
  select
  user0_.user_id as user_id1_1_0_,
  user0_.created_at as created_2_1_0_,
  user0_.modified_at as modified3_1_0_,
  user0_.nickname as nickname4_1_0_,
  user0_.profile_image_url as profile_5_1_0_,
  user0_.signup_type as signup_t6_1_0_,
  user0_.user_status as user_sta7_1_0_,
  user0_.username as username8_1_0_
  from
  user user0_
  where
  user0_.user_id=?

### 두 번째 상황에서는 FeedRepository에서 Feed 엔티티를 리턴하는 것이 아니라, ForTestResponse라는 타입을 리턴하게 만들었고, 이를 위해서 아래의 이미지와 같은 JPQL을 작성하여 실행하였다.
![F0D35D01-B9E3-4BC5-9878-BD1BAAE35CE8](https://user-images.githubusercontent.com/99060708/222727645-ccbd2628-0cc7-4380-b89d-f9c08c76d7bf.jpeg)
![99FD6902-D6F3-4844-B88F-109A98A77DE3](https://user-images.githubusercontent.com/99060708/222727657-c08f4001-2261-4132-843c-e568d0c8913c.jpeg)
- 테스트 결과, 단 1 번의 쿼리로 원하는 Dto 리스트를 바로 만들어낼 수 있었고 N+1 문제가 해결된 것을 확인할 수 있었다.

### 세 번째 상황에서는 두 번째 상황과 동일하되, 생성 시점을 기준으로 필터링하고 페이지리퀘스트 객체를 이용하여 페이징 처리까지 실행하게 만들었다. 이 또한 쿼리는 단 1번만 실행되었고, N+1 문제는 발생하지 않았다.
![7EE9296A-7AF5-4D35-85C5-EA3620670EA7](https://user-images.githubusercontent.com/99060708/222728382-4f3cd097-1e9a-45b9-92d5-5a9342d02b46.jpeg)
![F171A39F-C03C-44AC-B2A8-A2472C250CA0](https://user-images.githubusercontent.com/99060708/222728388-e38a2396-fb86-469f-9624-0890e0934ecc.jpeg)

### 네 번째 상황에서는 세 번째 상황과 동일하되, JPQL 내에서 IN 쿼리도 사용하여 더욱 복잡한 쿼리에서도 JPQL을 활용한 N+1 문제의 해결이 가능함을 알 수 있었다.
![83A8F828-45EB-477F-8CD1-C78C9CDED089](https://user-images.githubusercontent.com/99060708/222728869-511ffa7c-dfc6-40aa-9d22-0a3f332fea1c.jpeg)
![62574BFB-3401-469A-BF40-7DFFEEF2D944](https://user-images.githubusercontent.com/99060708/222728875-31e78006-eef2-40ef-98fb-83f38efbf00a.jpeg)
