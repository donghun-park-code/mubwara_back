package site.metacoding.finals.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.MemberSubstitution.Substitution.Chain.Step.Resolution;
import site.metacoding.finals.config.auth.PrincipalUser;
import site.metacoding.finals.config.jwt.JwtProcess;
import site.metacoding.finals.config.jwt.JwtSecret;
import site.metacoding.finals.domain.user.User;
import site.metacoding.finals.domain.user.UserRepository;
import site.metacoding.finals.dto.ResponseDto;
import site.metacoding.finals.dto.user.UserReqDto.JoinReqDto;
import site.metacoding.finals.dto.user.UserRespDto.JoinRespDto;
import site.metacoding.finals.dto.user.UserRespDto.OauthLoginRespDto;
import site.metacoding.finals.handler.OauthHandler;
import site.metacoding.finals.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final OauthHandler oauthHandler;
    private final UserService userService;

    @GetMapping("/join/{username}")
    public ResponseEntity<?> checkSameUsername(@PathVariable String username) {
        String check = userService.checkUsername(username);
        return new ResponseEntity<>(new ResponseDto<>(HttpStatus.CREATED, "아이디 중복 체크 여부", check),
                HttpStatus.OK);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinShopApi(@RequestBody JoinReqDto joinReqDto) {
        JoinRespDto respDto = userService.join(joinReqDto);
        return new ResponseEntity<>(new ResponseDto<>(HttpStatus.CREATED, "가게(유저) 회원가입 완료", respDto),
                HttpStatus.CREATED);
    }

    @GetMapping(value = "/oauth/{serviceName}", headers = "access_token")
    public ResponseEntity<?> oauthKakao(@RequestHeader("access_token") String token, @PathVariable String serviceName,
            HttpServletResponse response) {

        System.out.println("디버그 토큰 : " + token);

        OauthLoginRespDto respDto = oauthHandler.processKakaoLogin(serviceName, token);

        return new ResponseEntity<>(new ResponseDto<>(HttpStatus.OK, "카카오 로그인", respDto),
                HttpStatus.OK);

    }

    @GetMapping(value = "/refresh/token", headers = "Authorization")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token, HttpServletRequest request,
            HttpServletResponse response) {

        System.out.println("디버그 토큰 : " + token);

        String refresh = request.getHeader("Authorization").replace("Bearer ", "");
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(JwtSecret.SECRET)).build().verify(token);

        Long id = decodedJWT.getClaim("id").asLong();
        User userPS = userService.findById(id);
        PrincipalUser principalUser = new PrincipalUser(userPS);
        String access = JwtProcess.create(principalUser, (1000 * 60 * 60));

        response.setHeader("access-token", access);

        // 403 포비든으로 던져주기
        return new ResponseEntity<>(new ResponseDto<>(HttpStatus.OK, "엑세스 토큰 재발급", "유저아이디 : " + id),
                HttpStatus.OK);

    }
}
