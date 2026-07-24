# 🏃 런메이트 (RUNMATE)

**한강공원 러닝크루 관리 앱**

부담 없이 가볍게, 오늘 같이 뛸 사람을 찾는 한강 러닝 매칭 서비스입니다.

---

## 📱 주요 기능

- **모임 관리**: 러닝 모임 생성/참여, 공개·비공개 모임 (초대코드 지원)
- **한강공원 지도**: 서울 11개 한강공원 정보 및 레벨별 추천 코스
- **러닝 인증**: 사진 업로드 기반 러닝 기록 (거리·시간·페이스), 마이페이지에서 참여 모임 선택 후 바로 인증도 가능
- **로그인/회원가입**: 아이디·비밀번호 기반 로그인, 세션 유지
- **마이페이지**: 개인 러닝 통계, 인증 사진 모아보기, 최근 참여 이력(인증 완료한 모임만 표시), 프로필/설정
- **랭킹**: 이번 달 참여 랭킹, 명예의 전당


---

## 🛠 기술 스택

- **언어**: Kotlin
- **개발 환경**: Android Studio
- **DB**: SQLite (SQLiteOpenHelper)
- **UI**: Material Components, RecyclerView, ConstraintLayout

---

## 👥 팀 구성 (역할 분담)

| 담당 | 역할 | 주요 화면 |
|---|---|---|
| ① 홈/모임 | 홈 화면(모임 리스트), 모임 만들기, 모임 상세, 참여/신청 로직, 로그인/회원가입/스플래시 | 홈, 모임 만들기, 모임 상세, 로그인, 회원가입, 스플래시 |
| ② 지도/장소 | 장소 검색, 정적 지도 이미지 연동 | 한강공원 선택, 러닝코스 추천 |
| ③ 러닝 인증 | 러닝 인증 화면, 사진 업로드, 거리·시간 기록, 랭킹 | 러닝 인증, 랭킹 |
| ④ 마이페이지 | 마이페이지(통계), 프로필/설정 | 마이페이지, 프로필 설정, 사진 갤러리 |

---

## 🗄 데이터베이스 구조

`DBHelper.kt`에서 아래 6개 테이블로 통일 관리됩니다.

### `users` (유저)

| 컬럼 | 설명 |
|---|---|
| id | PK |
| nickname | 닉네임 |
| level | 러닝 레벨 (초보/중급/고수) |
| profile_img | 프로필 이미지 경로 |
| username | 로그인 아이디 |
| password | 비밀번호 |

### `meetings` (모임)

| 컬럼 | 설명 |
|---|---|
| id | PK |
| host_id | 호스트 (users FK) |
| title | 모임 이름 |
| date, time | 날짜/시간 |
| location_name, lat, lng | 위치 정보 |
| max_people | 모집 인원 |
| is_public | 공개(1)/비공개(0) |
| invite_code | 비공개 모임 초대코드 |
| description | 모임 설명 |
| pace | 속도 |

### `meeting_participants` (모임 참여)

| 컬럼 | 설명 |
|---|---|
| id | PK |
| meeting_id | 모임 (meetings FK) |
| user_id | 참여자 (users FK) |
| status | 참여 상태 |

### `running_records` (러닝 인증)

| 컬럼 | 설명 |
|---|---|
| id | PK |
| meeting_id, user_id | FK |
| photo_path | 인증 사진 |
| distance, time | 거리/시간 |
| date | 날짜 |

### `diet_challenges` (식단 챌린지) — 스키마만 존재, 화면 미구현

| 컬럼 | 설명 |
|---|---|
| id | PK |
| creator_id | 개설자 (users FK) |
| title |	챌린지 이름 |
| start_date, end_date | 기간 |
| description	| 설명 |

### `diet_records (식단 인증)` — 스키마만 존재, 화면 미구현

| 컬럼 | 설명 |
|---|---|
| id	| PK |
| challenge_id, user_id |	FK |
| photo_path	| 인증 사진 |
| memo	| 메모 |
| date	| 날짜 |

---

## 🌿 브랜치 전략

- `main` : 배포/통합 기준 브랜치
- `feature/home` : 홈/모임 담당
- `feature/place` : 지도/장소 담당
- `feature/running` : 러닝 인증/랭킹 담당
- `feature/diet` : 마이페이지 담당

각자 담당 브랜치에서 작업 후 Pull Request를 통해 `main`에 병합합니다.

---

## 📂 패키지 구조

```
com.android.runmate
├── data                    // DBHelper, 데이터 모델(Meeting, MeetingDetail 등), PlaceRepository
├── ui
│   ├── auth                // SplashActivity, LoginActivity, SignupActivity
│   │   └── util
│   │       └── SessionManager.kt   // 로그인 세션 유지
│   ├── home                // HomeActivity, MeetingAdapter
│   ├── meeting              // CreateMeetingActivity, MeetingDetailActivity
│   ├── place                // ParkSelectActivity, CourseListActivity 등
│   ├── proof                // RunProofActivity (러닝 인증)
│   ├── ranking               // RankingActivity
│   └── privatejoin          // PrivateJoinActivity (비공개 모임 입장)
├── MyPageActivity.kt         // ui 하위 패키지가 아닌 최상위 패키지에 위치
├── ProfileSettingsActivity.kt
├── PhotoGalleryActivity.kt
└── CustomerServiceActivity.kt
```

---

## 🚀 시작하기

### 1. 저장소 클론

```bash
git clone https://github.com/eunjinpk/Runmate.git
cd Runmate
git checkout main
git pull origin main
```

### 2. 작업 시작 전 본인 브랜치로 이동

```bash
git checkout feature/[본인담당]
git pull origin main   # 최신 상태 반영 후 작업 시작
```

### 3. 작업 후 커밋 & push

```bash
git add .
git commit -m "feat: 작업 내용"
git push origin feature/[본인담당]
```

### 4. main에 병합

GitHub에서 Pull Request를 생성하여 팀원 확인 후 병합합니다.

---

## 📋 개발 규칙

- DB 컬럼명은 임의로 변경하지 않고 통일된 스키마를 그대로 사용합니다.
- 각자 담당 브랜치에서 작업하며, `main`에 직접 push하지 않습니다.
- 작업 시작 전 `git pull origin main`으로 최신 상태를 반영합니다.
- 화면/기능 완성 시 Pull Request를 통해 팀원 확인 후 병합합니다.
