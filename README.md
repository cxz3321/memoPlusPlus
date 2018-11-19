# memoPlusPlus
프로그램의 제작에 사용한 요소들 출처

FloatingActionButton Menu Animation
https://github.com/Clans/FloatingActionButton

Google 로그인 기능 구현 : Firebase문서
https://firebase.google.com/docs/auth/android/google-signin

메인화면 폰트 : Noto Sans
https://www.google.com/get/noto/#sans-lgc

아이콘 출처 : Icooon Mono
http://icooon-mono.com/


프로그램 세팅법
1. Firebase 콘솔에 프로그램 등록
https://firebase.google.com/docs/android/setup 를 참조
2. 프로젝트 설정에서 google-service.json다운로드 후 app폴더에 google-service.json 넣기
3. 인증 - 로그인방법 google로그인 사용

database Rules
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
