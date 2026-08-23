package team.mino.core.data.auth.extension

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import team.mino.core.errorhandling.MinoDomainException

/**
 * 인증 제공자 SDK 예외를 도메인 예외로 매핑한다. 열거 밖이면 수신자를 그대로 돌려준다.
 *
 * 갈래 분류는 contracts/identity-proof-attachment.md §2, 매핑의 성질(화이트리스트 열거·열거 밖 rethrow·
 * `CancellationException` 보존)은 docs/conventions/error_handling.md §3이 소유한다.
 * 던지지 않는 순수 함수이며, throw는 호출부인 [awaitDomain]이 한다.
 */
internal fun Throwable.toDomainExceptionOrSelf(): Throwable =
    when (this) {
        // 연결 실패
        is FirebaseNetworkException -> MinoDomainException.Network(this)
        // 발급 실패 — 호출 한도 초과
        is FirebaseTooManyRequestsException -> MinoDomainException.Auth(this)
        // 발급 실패 — 인증 구성 오류 등 인증 제공자가 신원을 발급하지 못한 경우
        is FirebaseAuthException -> MinoDomainException.Auth(this)
        // CancellationException과 열거 밖 예외는 여기로 와 원본 그대로 돌아가야 한다
        // — 공통 상위 타입(FirebaseException·RuntimeException 등) 분기를 추가해 위에서 가로채지 말 것
        else -> this
    }
