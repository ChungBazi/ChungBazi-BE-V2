package chungbazi.chungbazi_be.global.apiPayload.code.status;

import chungbazi.chungbazi_be.global.apiPayload.code.BaseErrorCode;
import chungbazi.chungbazi_be.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    //일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // Not Found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER404", "해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_NOTIFICATION(HttpStatus.NOT_FOUND, "NOTIFICATION001", "알림이 존재하지 않습니다."),
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, "TOKEN4021", "토큰이 존재하지 않습니다."),
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "POST404", "해당 게시글을 찾을 수 없습니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND,"COMMENT404","해당 댓글을 찾을 수 없습니다."),
    NOT_FOUND_CHARACTER(HttpStatus.NOT_FOUND, "CHARACTER404", "해당 케릭터를 찾을 수 없습니다."),
    NOT_FOUND_DOCUMENT(HttpStatus.NOT_FOUND, "DOCUMENT401", "해당 문서를 찾을 수 없습니다."),
    NOT_FOUND_CART(HttpStatus.NOT_FOUND, "CART401", "해당 장바구니를 찾을 수 없습니다."),
    NOT_FOUND_POST_LIKE(HttpStatus.NOT_FOUND, "POST_LIKE404", "해당 게시글 좋아요를 찾을 수 없습니다."),
    NOT_FOUND_COMMENT_LIKE(HttpStatus.NOT_FOUND, "COMMENT_LIKE404", "해당 댓글 좋아요를 찾을 수 없습니다."),

    // User 관련 에러
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "USER4002", "닉네임은 필수입니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "USER400", "유효하지 않은 입력값입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "USER400", "이미 존재하는 닉네임입니다."),
    INVALID_CHARACTER(HttpStatus.BAD_REQUEST, "CHARACTER400", "유효하지 않은 캐릭터입니다."),
    ALREADY_EXISTS_EMAIL(HttpStatus.BAD_REQUEST,"USER4005","중복된 이메일 입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST,"USER4006","잘못된 비밀번호 입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST,"USER4007","비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST,"USER4008","기존 비밀번호와 같습니다."),
    ONBOARDING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "USER4009", "온보딩 정보가 없습니다. 온보딩부터 완료해주세요."),

    //Policy
    CATEGORY_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "POLICY5001", "존재하지 않는 정책 코드 입니다."),
    CATEGORY_NAME_NOT_FOUND(HttpStatus.BAD_REQUEST, "POLICY5002", "존재하지 않는 정책 카테고리 명입니다."),
    NO_SEARCH_NAME(HttpStatus.BAD_REQUEST, "POLICY5003", "검색어를 입력해주세요."),
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY5004", "존재하지 않는 정책입니다."),
    NO_CURSOR(HttpStatus.BAD_REQUEST, "POLICY5005", "커서가 존재하지않습니다."),
    NOT_VALID_TYPE_YEAR_MONTH(HttpStatus.BAD_REQUEST, "POLICY5006", "유효한 날짜 형식이 아닙니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "POLICY4001", "유효한 커서 형식이 아닙니다."),

    //Cart
    ALREADY_EXIST_CART(HttpStatus.BAD_REQUEST, "Cart4001", "이미 해당 정책을 장바구니에 담았습니다."),

    //Notification
    GOOGLE_REQUEST_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "FCMTOKEN001", "firebase 접근 토큰이 유효하지 않습니다."),
    FCM_SEND_FAILURE(HttpStatus.BAD_REQUEST, "FCMSEND001", "FCM 메시지 전송에 실패했습니다."),
    COMMUNITY_ALARM_POST_OR_COMMENT_NULL(HttpStatus.BAD_REQUEST, "NOTIFICATION002", "Community 알림에서 post 또는 comment가 null입니다."),
    CHAT_ALARM_CHAT_NULL(HttpStatus.BAD_REQUEST, "NOTIFICATION003", "Chat 알림에서 chat이 null입니다."),
    POLICY_ALARM_CHAT_NULL(HttpStatus.BAD_REQUEST, "NOTIFICATION004", "Chat 알림에서 chat이 null입니다."),
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "NOTIFICATION005", "알림 타입이 존재하지 않습니다."),
    NOT_FOUND_FCM_TOKEN(HttpStatus.NOT_FOUND, "FCMTOKEN404", "해당 fcm 토큰이 존재하지 않습니다."),

    //인증 관련 에러
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN4012", "잘못된 형식의 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4011", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4012", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN4013", "지원하지 않는 토큰입니다."),
    EMPTY_CLAIMS(HttpStatus.BAD_REQUEST, "TOKEN4014", "클레임이 비어있습니다."),
    EMPTY_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN4015", "비어있는 토큰입니다."),

    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "TOKEN4017", "사용자 ID의 형식이 올바르지 않습니다."),
    INVALID_ARGUMENTS(HttpStatus.BAD_REQUEST, "TOKEN4018", "잘못된 인자가 제공되었습니다."),
    BLOCKED_TOKEN(HttpStatus.FORBIDDEN, "TOKEN4019", "차단된 사용자의 토큰입니다."),
    DEACTIVATED_ACCOUNT(HttpStatus.BAD_REQUEST, "TOKEN4020", "삭제된 계정입니다."),
    UNABLE_TO_SEND_EMAIL(HttpStatus.BAD_REQUEST,"TOKEN4021","이메일을 보낼 수 없습니다."),
    NO_SUCH_ALGORITHM(HttpStatus.BAD_REQUEST,"TOKEN4022","인증 코드 생성 중 문제가 발생했습니다."),
    INVALID_AUTHCODE(HttpStatus.BAD_REQUEST,"TOKEN4023","인증코드가 불일치 합니다."),
    UNABLE_TO_READ_EMAIL_TEMPLATE(HttpStatus.BAD_REQUEST,"TOKEN4024","이메일 템플릿을 읽어올 수 없습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN,"AUTH4025" ,"계정 접근이 금지되었습니다." ),


    //s3 관련 에러
    NO_FILE_EXTENTION(HttpStatus.BAD_REQUEST, "UPLOAD400", "파일의 이름에 확장자가 존재하지 않습니다."),
    PICTURE_EXTENSION_ERROR(HttpStatus.BAD_REQUEST, "PICTURE400", "이미지의 확장자가 잘못되었습니다."),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD413", "파일 크기가 허용 범위를 초과했습니다."),

    //community 관련 에러
    FILE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "UPLOAD400", "파일은 10장을 초과할 수 없습니다."),
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "LIKE400", "이미 좋아요를 했습니다."),
    UNABLE_TO_DELETE_POST(HttpStatus.BAD_REQUEST,"POST400", "게시글은 본인만 삭제할 수 있습니다"),
    UNABLE_TO_DELETE_COMMENT(HttpStatus.BAD_REQUEST,"COMMENT400", "댓글은 본인만 삭제할 수 있습니다"),


    //채팅 관련 에러
    NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, "CHATROOM404", "존재하지 않는 채팅방입니다."),
    ACCESS_DENIED_CHATROOM(HttpStatus.FORBIDDEN, "CHATROOM400", "채팅방에 접근할 권한이 없습니다."),
    NOT_FOUND_MESSAGE(HttpStatus.NOT_FOUND,"MESSAGE400","존재하지 않는 메세지입니다."),
    BLOCKED_CHATROOM(HttpStatus.FORBIDDEN,"CHATROOM401","차단된 채팅방입니다."),
    NOT_FOUND_CHATROOM_SETTING(HttpStatus.NOT_FOUND, "CHATROOMSETTING404", "존재하지 않는 채팅방 알림 설정입니다."),
    CAN_NOT_CHATTING_MYSELF(HttpStatus.BAD_REQUEST, "CHATROOMS405", "자기 자신에게 쪽지를 보낼 수 없습니다."),
    NOT_FOUND_OTHER_USER(HttpStatus.NOT_FOUND, "CHATROOM406", "채팅방에 참여한 다른 유저를 찾을 수 없습니다."),


    //차단 관련 에러
    INVALID_BLOCK(HttpStatus.BAD_REQUEST,"BLOCK400","자기 자신을 차단할 수 없습니다."),
    ALEADY_BLOCKED(HttpStatus.BAD_REQUEST,"BLOCK401","이미 차단된 사용자입니다."),
    NOT_FOUND_USERBLOCK(HttpStatus.NOT_FOUND, "BLOCK404", "존재하지 않는 차단정보입니다."),

    //신고 관련 에러
    ALREADY_REPORT(HttpStatus.BAD_REQUEST,"REPORT6001","이미 신고한 id입니다."),
    UNABLE_REPORT_MYSELF(HttpStatus.BAD_REQUEST,"REPORT6002","자기 자신을 신고할 수 없습니다"),
    DESCRIPTION_REQUIRED(HttpStatus.BAD_REQUEST,"REPORT6003","신고 옵션이 기타인 경우, 사유를 필수로 추가해주셔야합니다."),

    //챗봇 관련 에러
    OPENAI_API_ERROR(HttpStatus.BAD_GATEWAY, "OPENAI4001", "OpenAI API 호출 중 오류가 발생했습니다."),
    OPENAI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "OPENAI4002", "OpenAI API 응답이 지연되었습니다."),
    OPENAI_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "OPENAI4003", "OpenAI API 응답 포맷이 올바르지 않습니다."),

    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
